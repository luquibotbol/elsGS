package com.mutualfund.backend.service;

import com.mutualfund.backend.model.InvestmentResult;
import com.mutualfund.backend.model.MutualFund;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for MutualFundService — validates CAPM calculations,
 * fund list, and formula correctness.
 */
class MutualFundServiceTest {

    /**
     * Test: getAllFunds returns the expected number of hardcoded mutual funds.
     */
    @Test
    void getAllFunds_returns17Funds() {
        MutualFundService service = new MutualFundService();
        List<MutualFund> funds = service.getAllFunds();
        assertEquals(17, funds.size(), "Should have 17 hardcoded mutual funds");
    }

    /**
     * Test: Each fund has non-null ticker, name, and category.
     */
    @Test
    void getAllFunds_allFieldsPopulated() {
        MutualFundService service = new MutualFundService();
        for (MutualFund fund : service.getAllFunds()) {
            assertNotNull(fund.getTicker(), "Ticker should not be null");
            assertNotNull(fund.getName(), "Name should not be null");
            assertNotNull(fund.getCategory(), "Category should not be null");
            assertFalse(fund.getTicker().isEmpty(), "Ticker should not be empty");
        }
    }

    /**
     * Test: CAPM formula produces correct result with known inputs.
     * CAPM: rate = riskFreeRate + beta * (expectedReturn - riskFreeRate)
     * FV = principal * (1 + rate)^years
     *
     * With riskFreeRate=4.25%, beta=1.0, expectedReturn=10%:
     * rate = 0.0425 + 1.0 * (0.10 - 0.0425) = 0.10 (10%)
     * FV = 1000 * (1.10)^10 = 2593.74
     */
    @Test
    void capmFormula_correctCalculation() {
        double riskFreeRate = 0.0425;
        double beta = 1.0;
        double expectedReturn = 0.10;
        double principal = 1000.0;
        int years = 10;

        double calculatedRate = riskFreeRate + beta * (expectedReturn - riskFreeRate);
        double futureValue = principal * Math.pow(1 + calculatedRate, years);
        futureValue = Math.round(futureValue * 100.0) / 100.0;

        assertEquals(0.10, calculatedRate, 0.0001, "CAPM rate should be 10% when beta=1");
        assertEquals(2593.74, futureValue, 0.01, "FV should match compound interest formula");
    }

    /**
     * Test: Low beta fund should produce lower CAPM rate than high beta.
     */
    @Test
    void capmFormula_lowBetaLowerRate() {
        double riskFreeRate = 0.0425;
        double expectedReturn = 0.10;

        double lowBeta = 0.5;
        double highBeta = 1.5;

        double lowRate = riskFreeRate + lowBeta * (expectedReturn - riskFreeRate);
        double highRate = riskFreeRate + highBeta * (expectedReturn - riskFreeRate);

        assertTrue(lowRate < highRate, "Lower beta should produce lower CAPM rate");
        assertTrue(lowRate > riskFreeRate, "CAPM rate should exceed risk-free rate for positive beta");
    }

    /**
     * Test: Zero beta should equal the risk-free rate.
     */
    @Test
    void capmFormula_zeroBetaEqualsRiskFree() {
        double riskFreeRate = 0.0425;
        double expectedReturn = 0.10;
        double beta = 0.0;

        double rate = riskFreeRate + beta * (expectedReturn - riskFreeRate);
        assertEquals(riskFreeRate, rate, 0.0001, "Zero beta should equal risk-free rate");
    }

    /**
     * Test: Future value with 0 years should equal principal.
     */
    @Test
    void futureValue_zeroYearsEqualsPrincipal() {
        double principal = 5000.0;
        double rate = 0.08;
        int years = 0;

        double fv = principal * Math.pow(1 + rate, years);
        assertEquals(principal, fv, 0.01, "FV with 0 years should equal principal");
    }

    /**
     * Test: Future value increases with more years.
     */
    @Test
    void futureValue_increasesWithTime() {
        double principal = 10000.0;
        double rate = 0.07;

        double fv5 = principal * Math.pow(1 + rate, 5);
        double fv10 = principal * Math.pow(1 + rate, 10);
        double fv20 = principal * Math.pow(1 + rate, 20);

        assertTrue(fv5 < fv10, "10 years should be greater than 5 years");
        assertTrue(fv10 < fv20, "20 years should be greater than 10 years");
    }

    /**
     * Test: InvestmentResult model has all fields populated correctly.
     */
    @Test
    void investmentResult_fieldsCorrect() {
        InvestmentResult result = new InvestmentResult(
                "VFIAX", 10000.0, 10, 0.0425, 0.41, 0.12, 0.074, 20000.0);

        assertEquals("VFIAX", result.getTicker());
        assertEquals(10000.0, result.getPrincipal());
        assertEquals(10, result.getTimeYears());
        assertEquals(0.0425, result.getRiskFreeRate());
        assertEquals(0.41, result.getBeta());
        assertEquals(0.12, result.getExpectedReturn());
        assertEquals(0.074, result.getCalculatedRate());
        assertEquals(20000.0, result.getFutureValue());
    }

    /**
     * Test: MutualFund model has correct fields.
     */
    @Test
    void mutualFund_fieldsCorrect() {
        MutualFund fund = new MutualFund("Vanguard 500 Index", "VFIAX", "Large Cap Blend");
        assertEquals("VFIAX", fund.getTicker());
        assertEquals("Vanguard 500 Index", fund.getName());
        assertEquals("Large Cap Blend", fund.getCategory());
    }

    /**
     * Test: VFIAX is in the fund list (a known good ticker for Newton Analytics).
     */
    @Test
    void fundList_containsVFIAX() {
        MutualFundService service = new MutualFundService();
        boolean found = service.getAllFunds().stream()
                .anyMatch(f -> "VFIAX".equals(f.getTicker()));
        assertTrue(found, "Fund list should contain VFIAX");
    }
}
