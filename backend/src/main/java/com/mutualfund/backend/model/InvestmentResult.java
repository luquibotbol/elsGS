package com.mutualfund.backend.model;

public class InvestmentResult {
    private String ticker;
    private double principal;
    private int timeYears;
    private double riskFreeRate;
    private double beta;
    private double expectedReturn;
    private double calculatedRate;
    private double futureValue;

    public InvestmentResult() {}

    public InvestmentResult(String ticker, double principal, int timeYears,
                            double riskFreeRate, double beta, double expectedReturn,
                            double calculatedRate, double futureValue) {
        this.ticker = ticker;
        this.principal = principal;
        this.timeYears = timeYears;
        this.riskFreeRate = riskFreeRate;
        this.beta = beta;
        this.expectedReturn = expectedReturn;
        this.calculatedRate = calculatedRate;
        this.futureValue = futureValue;
    }

    public String getTicker() { return ticker; }
    public void setTicker(String ticker) { this.ticker = ticker; }
    public double getPrincipal() { return principal; }
    public void setPrincipal(double principal) { this.principal = principal; }
    public int getTimeYears() { return timeYears; }
    public void setTimeYears(int timeYears) { this.timeYears = timeYears; }
    public double getRiskFreeRate() { return riskFreeRate; }
    public void setRiskFreeRate(double riskFreeRate) { this.riskFreeRate = riskFreeRate; }
    public double getBeta() { return beta; }
    public void setBeta(double beta) { this.beta = beta; }
    public double getExpectedReturn() { return expectedReturn; }
    public void setExpectedReturn(double expectedReturn) { this.expectedReturn = expectedReturn; }
    public double getCalculatedRate() { return calculatedRate; }
    public void setCalculatedRate(double calculatedRate) { this.calculatedRate = calculatedRate; }
    public double getFutureValue() { return futureValue; }
    public void setFutureValue(double futureValue) { this.futureValue = futureValue; }
}
