package com.mutualfund.backend.controller;

import com.mutualfund.backend.model.*;
import com.mutualfund.backend.service.AiService;
import com.mutualfund.backend.service.MutualFundService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiService aiService;
    private final MutualFundService mutualFundService;

    public AiController(AiService aiService, MutualFundService mutualFundService) {
        this.aiService = aiService;
        this.mutualFundService = mutualFundService;
    }

    /**
     * POST /api/ai/portfolio
     * Generates an AI-optimized portfolio allocation.
     */
    @PostMapping("/portfolio")
    public AiResponse generatePortfolio(@RequestBody AiPortfolioRequest request) {
        String result = aiService.generatePortfolio(
                request.getAmount(),
                request.getRiskTolerance(),
                mutualFundService.getAllFunds());
        return new AiResponse(result);
    }

    /**
     * POST /api/ai/analyze
     * Generates an AI risk analysis for an investment result.
     */
    @PostMapping("/analyze")
    public AiResponse analyzeRisk(@RequestBody InvestmentResult request) {
        String result = aiService.analyzeRisk(request);
        return new AiResponse(result);
    }

    /**
     * POST /api/ai/compare
     * Generates an AI comparison between selected mutual funds.
     */
    @PostMapping("/compare")
    public AiResponse compareFunds(@RequestBody AiCompareRequest request) {
        String result = aiService.compareFunds(
                request.getTickers(),
                mutualFundService.getAllFunds());
        return new AiResponse(result);
    }

    /**
     * POST /api/ai/chat
     * AI investment advisor chat.
     */
    @PostMapping("/chat")
    public AiResponse chat(@RequestBody AiChatRequest request) {
        String result = aiService.chat(
                request.getMessage(),
                mutualFundService.getAllFunds());
        return new AiResponse(result);
    }
}
