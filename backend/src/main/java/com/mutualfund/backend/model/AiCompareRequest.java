package com.mutualfund.backend.model;

import java.util.List;

public class AiCompareRequest {
    private List<String> tickers;

    public AiCompareRequest() {}

    public List<String> getTickers() { return tickers; }
    public void setTickers(List<String> tickers) { this.tickers = tickers; }
}
