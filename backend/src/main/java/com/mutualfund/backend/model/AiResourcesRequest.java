package com.mutualfund.backend.model;

public class AiResourcesRequest {
    private String ticker;
    private String fundName;
    private String category;
    private String type;

    public AiResourcesRequest() {}

    public String getTicker() { return ticker; }
    public void setTicker(String ticker) { this.ticker = ticker; }
    public String getFundName() { return fundName; }
    public void setFundName(String fundName) { this.fundName = fundName; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
