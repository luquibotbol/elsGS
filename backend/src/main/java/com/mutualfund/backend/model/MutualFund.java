package com.mutualfund.backend.model;

public class MutualFund {
    private String name;
    private String ticker;
    private String category;
    private String type; // "Mutual Fund" or "ETF"

    public MutualFund(String name, String ticker, String category, String type) {
        this.name = name;
        this.ticker = ticker;
        this.category = category;
        this.type = type;
    }

    // Backward-compatible constructor (defaults to Mutual Fund)
    public MutualFund(String name, String ticker, String category) {
        this(name, ticker, category, "Mutual Fund");
    }

    public String getName() { return name; }
    public String getTicker() { return ticker; }
    public String getCategory() { return category; }
    public String getType() { return type; }
}
