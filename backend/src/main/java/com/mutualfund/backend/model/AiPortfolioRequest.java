package com.mutualfund.backend.model;

public class AiPortfolioRequest {
    private double amount;
    private String riskTolerance; // "conservative", "moderate", "aggressive"

    public AiPortfolioRequest() {}

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getRiskTolerance() { return riskTolerance; }
    public void setRiskTolerance(String riskTolerance) { this.riskTolerance = riskTolerance; }
}
