package com.mutualfund.backend.service;

import com.mutualfund.backend.model.InvestmentResult;
import com.mutualfund.backend.model.MutualFund;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MutualFundService {

    private final RestTemplate restTemplate;
    private final List<MutualFund> mutualFunds;

    // Cache beta values so we only call Newton Analytics once per ticker
    private final ConcurrentHashMap<String, Double> betaCache = new ConcurrentHashMap<>();

    // Cache per-fund expected returns (1-year historical) from Yahoo Finance
    private final ConcurrentHashMap<String, Double> expectedReturnCache = new ConcurrentHashMap<>();

    // Hardcoded risk-free rate (approximate 10-Year US Treasury yield)
    private static final double RISK_FREE_RATE = 0.0425;

    // Fallback: S&P 500 historical average if Yahoo Finance fails for a fund
    private static final double FALLBACK_EXPECTED_RETURN = 0.10;

    public MutualFundService() {
        this.restTemplate = new RestTemplate();
        this.mutualFunds = initializeFunds();
        // Preload all betas and expected returns in background so calculations are instant
        preloadData();
    }

    /**
     * Returns the hardcoded list of available mutual funds.
     */
    public List<MutualFund> getAllFunds() {
        return mutualFunds;
    }

    /**
     * Fetches beta and expected returns for every fund in background at startup.
     * Calculations will be instant once this completes.
     */
    private void preloadData() {
        new Thread(() -> {
            System.out.println("Preloading data for " + mutualFunds.size() + " funds...");
            for (MutualFund fund : mutualFunds) {
                String ticker = fund.getTicker();
                try {
                    getBeta(ticker);
                    System.out.println("  Beta for " + ticker + ": " + betaCache.get(ticker));
                } catch (Exception e) {
                    System.err.println("  Failed beta for " + ticker + ": " + e.getMessage());
                }
                try {
                    getExpectedReturn(ticker);
                    System.out.println("  Expected return for " + ticker + ": " +
                            String.format("%.2f%%", expectedReturnCache.get(ticker) * 100));
                } catch (Exception e) {
                    System.err.println("  Failed expected return for " + ticker + ": " + e.getMessage());
                }
            }
            System.out.println("Data preloading complete!");
        }).start();
    }

    /**
     * Fetches the beta of a mutual fund from the Newton Analytics API.
     * Beta measures volatility relative to the S&P 500 (market beta = 1).
     */
    public double getBeta(String ticker) {
        Double cached = betaCache.get(ticker);
        if (cached != null) {
            return cached;
        }

        String url = "https://api.newtonanalytics.com/stock-beta/?ticker=" + ticker
                + "&index=^GSPC&interval=1mo&observations=12";
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response != null && response.containsKey("data")) {
                Object data = response.get("data");
                if (data instanceof Number) {
                    double beta = ((Number) data).doubleValue();
                    betaCache.put(ticker, beta);
                    return beta;
                }
            }
            throw new RuntimeException("Invalid response from Newton Analytics API for ticker: " + ticker);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch beta for ticker " + ticker + ": " + e.getMessage(), e);
        }
    }

    /**
     * Fetches the historical 5-year price data for a specific mutual fund or ETF
     * from Yahoo Finance and calculates the annualized average return.
     *
     * Method: Get monthly adjusted close prices over 5 years, compute the total
     * return, then annualize it: annualizedReturn = (endPrice/startPrice)^(1/years) - 1
     *
     * This gives a much more reliable expected return than a single year.
     */
    @SuppressWarnings("unchecked")
    public double getExpectedReturn(String ticker) {
        Double cached = expectedReturnCache.get(ticker);
        if (cached != null) {
            return cached;
        }

        String url = "https://query2.finance.yahoo.com/v8/finance/chart/" + ticker
                + "?range=5y&interval=1mo";
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            Map<String, Object> body = response.getBody();

            if (body != null) {
                Map<String, Object> chart = (Map<String, Object>) body.get("chart");
                List<Map<String, Object>> results = (List<Map<String, Object>>) chart.get("result");
                Map<String, Object> result = results.get(0);
                Map<String, Object> indicators = (Map<String, Object>) result.get("indicators");
                List<Map<String, Object>> adjcloseList = (List<Map<String, Object>>) indicators.get("adjclose");
                List<Number> prices = (List<Number>) adjcloseList.get(0).get("adjclose");

                // Filter out null values
                List<Double> validPrices = new ArrayList<>();
                for (Number p : prices) {
                    if (p != null) {
                        validPrices.add(p.doubleValue());
                    }
                }

                if (validPrices.size() >= 12) {
                    double firstPrice = validPrices.get(0);
                    double lastPrice = validPrices.get(validPrices.size() - 1);
                    double totalReturn = lastPrice / firstPrice;

                    // Calculate actual number of years from data points (monthly data)
                    double yearsOfData = (validPrices.size() - 1) / 12.0;

                    // Annualized return: (totalReturn)^(1/years) - 1
                    double annualizedReturn = Math.pow(totalReturn, 1.0 / yearsOfData) - 1;

                    expectedReturnCache.put(ticker, annualizedReturn);
                    return annualizedReturn;
                }
            }
            throw new RuntimeException("Could not parse Yahoo Finance response for: " + ticker);
        } catch (Exception e) {
            // Fallback to S&P 500 historical average if Yahoo Finance fails
            System.err.println("Yahoo Finance failed for " + ticker + ", using fallback: " + e.getMessage());
            expectedReturnCache.put(ticker, FALLBACK_EXPECTED_RETURN);
            return FALLBACK_EXPECTED_RETURN;
        }
    }

    /**
     * Calculates the future value of an investment using the Capital Asset Pricing Model (CAPM).
     *
     * CAPM Formula:
     *   rate = riskFreeRate + beta * (expectedReturn - riskFreeRate)
     *   where expectedReturn is the fund's actual 1-year historical return
     *
     * Future Value Formula:
     *   FV = principal * (1 + rate) ^ years
     */
    public InvestmentResult calculateFutureValue(String ticker, double principal, int years) {
        // Fetch beta from Newton Analytics
        double beta = getBeta(ticker);

        // Fetch fund-specific expected return from Yahoo Finance (1-year historical)
        double expectedReturn = getExpectedReturn(ticker);

        // CAPM: r = risk-free rate + beta * (fund expected return - risk-free rate)
        double calculatedRate = RISK_FREE_RATE + beta * (expectedReturn - RISK_FREE_RATE);

        // Future Value: FV = P * (1 + r)^t
        double futureValue = principal * Math.pow(1 + calculatedRate, years);

        // Round future value to 2 decimal places
        futureValue = Math.round(futureValue * 100.0) / 100.0;

        return new InvestmentResult(
                ticker,
                principal,
                years,
                RISK_FREE_RATE,
                beta,
                expectedReturn,
                calculatedRate,
                futureValue
        );
    }

    /**
     * Initializes the hardcoded list of mutual funds and ETFs.
     */
    private List<MutualFund> initializeFunds() {
        List<MutualFund> funds = new ArrayList<>();

        // ── Mutual Funds ──
        funds.add(new MutualFund("Vanguard 500 Index", "VFIAX", "Large Cap Blend", "Mutual Fund"));
        funds.add(new MutualFund("Fidelity 500 Index", "FXAIX", "Large Cap Blend", "Mutual Fund"));
        funds.add(new MutualFund("Vanguard Total International Stock Index", "VTIAX", "International Equity", "Mutual Fund"));
        funds.add(new MutualFund("Vanguard Total Bond Market Index", "VBTLX", "Intermediate Bond", "Mutual Fund"));
        funds.add(new MutualFund("Vanguard Growth Index", "VIGAX", "Large Cap Growth", "Mutual Fund"));
        funds.add(new MutualFund("Fidelity Contrafund", "FCNTX", "Large Cap Growth", "Mutual Fund"));
        funds.add(new MutualFund("Vanguard Wellington", "VWELX", "Balanced", "Mutual Fund"));
        funds.add(new MutualFund("Vanguard Balanced Index", "VBIAX", "Balanced", "Mutual Fund"));
        funds.add(new MutualFund("Fidelity Total Market Index", "FSKAX", "U.S. Total Market", "Mutual Fund"));
        funds.add(new MutualFund("T. Rowe Price Blue Chip Growth", "TRBCX", "Large Cap Growth", "Mutual Fund"));
        funds.add(new MutualFund("Vanguard Mid-Cap Index", "VIMAX", "Mid Cap Blend", "Mutual Fund"));
        funds.add(new MutualFund("Dodge & Cox Stock", "DODGX", "Large Cap Value", "Mutual Fund"));
        funds.add(new MutualFund("Fidelity Growth Company", "FDGRX", "Large Cap Growth", "Mutual Fund"));
        funds.add(new MutualFund("T. Rowe Price Growth Stock", "PRGFX", "Large Cap Growth", "Mutual Fund"));
        funds.add(new MutualFund("Vanguard Value Index", "VVIAX", "Large Cap Value", "Mutual Fund"));
        funds.add(new MutualFund("American Funds Growth Fund of America", "AGTHX", "Large Cap Growth", "Mutual Fund"));
        funds.add(new MutualFund("Fidelity Blue Chip Growth", "FBGRX", "Large Cap Growth", "Mutual Fund"));

        // ── ETFs ──
        funds.add(new MutualFund("SPDR S&P 500 ETF Trust", "SPY", "Large Cap Blend", "ETF"));
        funds.add(new MutualFund("Invesco QQQ Trust (Nasdaq 100)", "QQQ", "Large Cap Growth", "ETF"));
        funds.add(new MutualFund("Vanguard Total Stock Market ETF", "VTI", "U.S. Total Market", "ETF"));
        funds.add(new MutualFund("Vanguard S&P 500 ETF", "VOO", "Large Cap Blend", "ETF"));
        funds.add(new MutualFund("iShares Core S&P 500 ETF", "IVV", "Large Cap Blend", "ETF"));
        funds.add(new MutualFund("Vanguard Total Bond Market ETF", "BND", "Intermediate Bond", "ETF"));
        funds.add(new MutualFund("Vanguard FTSE Developed Markets ETF", "VEA", "International Equity", "ETF"));
        funds.add(new MutualFund("iShares MSCI Emerging Markets ETF", "EEM", "Emerging Markets", "ETF"));
        funds.add(new MutualFund("ARK Innovation ETF", "ARKK", "Thematic Growth", "ETF"));
        funds.add(new MutualFund("Vanguard Growth ETF", "VUG", "Large Cap Growth", "ETF"));
        funds.add(new MutualFund("Vanguard Value ETF", "VTV", "Large Cap Value", "ETF"));
        funds.add(new MutualFund("iShares Russell 2000 ETF", "IWM", "Small Cap Blend", "ETF"));
        funds.add(new MutualFund("SPDR Gold Shares", "GLD", "Commodities", "ETF"));

        return funds;
    }
}
