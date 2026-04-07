package com.mutualfund.backend.service;

import com.mutualfund.backend.model.InvestmentResult;
import com.mutualfund.backend.model.MutualFund;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AiService {

    private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";
    private static final String MODEL = "gpt-4o";

    private final RestTemplate restTemplate;
    private final String apiKey;

    public AiService(@Value("${openai.api.key}") String apiKey) {
        this.restTemplate = new RestTemplate();
        this.apiKey = apiKey;
    }

    /**
     * Generates an optimized portfolio allocation based on amount and risk tolerance.
     */
    public String generatePortfolio(double amount, String riskTolerance, List<MutualFund> funds) {
        String fundList = funds.stream()
                .map(f -> f.getTicker() + " (" + f.getName() + " - " + f.getCategory() + " [" + f.getType() + "])")
                .collect(Collectors.joining(", "));

        String systemPrompt = "You are a Goldman Sachs investment advisor AI. Given an investment amount, " +
                "risk tolerance, and a list of available mutual funds AND ETFs, generate an optimized portfolio allocation. " +
                "You can mix both mutual funds and ETFs in your recommendations. Indicate the type (MF or ETF) for each pick.\n\n" +
                "FORMATTING RULES (you MUST follow these exactly):\n" +
                "- Structure your response into clear sections using ## headers (e.g. ## Strategy Overview, ## Allocations, ## Reasoning)\n" +
                "- Each major topic MUST start with a ## header on its own line\n" +
                "- Use bullet points with - for lists\n" +
                "- Use **bold** for fund tickers, percentages, and dollar amounts\n" +
                "- Keep each section concise (3-5 bullet points max)\n" +
                "- Include at least these sections: ## Strategy Overview, ## Portfolio Allocation, ## Risk Considerations, ## Summary\n" +
                "- Do NOT use ### sub-headers, only ## headers\n" +
                "- Do NOT put multiple sections under one header";

        String userPrompt = String.format(
                "Investment amount: $%,.2f\nRisk tolerance: %s\n\nAvailable mutual funds:\n%s\n\n" +
                "Generate an optimized portfolio allocation across these funds.",
                amount, riskTolerance, fundList);

        return callOpenAI(systemPrompt, userPrompt);
    }

    /**
     * Generates a risk analysis for a calculated investment result.
     */
    public String analyzeRisk(InvestmentResult result) {
        String systemPrompt = "You are a Goldman Sachs risk analyst AI. Given an investment calculation " +
                "result using the CAPM model, provide a comprehensive risk analysis.\n\n" +
                "FORMATTING RULES (you MUST follow these exactly):\n" +
                "- Structure your response into clear sections using ## headers on their own line\n" +
                "- Use exactly these sections: ## Volatility Assessment, ## CAPM Rate Analysis, ## Upside Scenario, ## Downside Scenario, ## Risk Rating, ## Recommendation\n" +
                "- Use bullet points with - for key points within each section\n" +
                "- Use **bold** for important numbers, percentages, and ratings\n" +
                "- Keep each section to 2-4 bullet points\n" +
                "- Reference the actual numbers provided in the data\n" +
                "- Do NOT use ### sub-headers, only ## headers\n" +
                "- Do NOT combine multiple topics under one header";

        String userPrompt = String.format(
                "Investment Analysis:\n" +
                "- Ticker: %s\n" +
                "- Principal: $%,.2f\n" +
                "- Time Horizon: %d years\n" +
                "- Risk-Free Rate: %.2f%%\n" +
                "- Beta: %.4f\n" +
                "- Expected Market Return: %.2f%%\n" +
                "- CAPM Calculated Rate: %.2f%%\n" +
                "- Predicted Future Value: $%,.2f\n\n" +
                "Provide a detailed risk analysis of this investment.",
                result.getTicker(),
                result.getPrincipal(),
                result.getTimeYears(),
                result.getRiskFreeRate() * 100,
                result.getBeta(),
                result.getExpectedReturn() * 100,
                result.getCalculatedRate() * 100,
                result.getFutureValue());

        return callOpenAI(systemPrompt, userPrompt);
    }

    /**
     * Generates a comparison between selected mutual funds.
     */
    public String compareFunds(List<String> tickers, List<MutualFund> allFunds) {
        String selectedFunds = allFunds.stream()
                .filter(f -> tickers.contains(f.getTicker()))
                .map(f -> f.getTicker() + " (" + f.getName() + " - " + f.getCategory() + " [" + f.getType() + "])")
                .collect(Collectors.joining("\n- "));

        String systemPrompt = "You are a Goldman Sachs investment comparison analyst AI. Compare the given mutual funds and/or ETFs. " +
                "Note whether each is a Mutual Fund or ETF and explain the structural differences if both types are selected.\n\n" +
                "FORMATTING RULES (you MUST follow these exactly):\n" +
                "- Give each fund its OWN ## header section (e.g. ## VFIAX - Vanguard 500 Index)\n" +
                "- Under each fund section, use bullet points with - to describe: category, risk level, ideal investor, strengths, weaknesses\n" +
                "- After all individual fund sections, add a ## Head-to-Head Comparison section with bullet points comparing them directly\n" +
                "- End with a ## Recommendation section with bullet points for different investor types (conservative, moderate, aggressive)\n" +
                "- Use **bold** for fund tickers, key metrics, and ratings\n" +
                "- Keep each section to 3-5 bullet points\n" +
                "- Do NOT use ### sub-headers, only ## headers\n" +
                "- Do NOT put multiple funds under one header";

        String userPrompt = "Compare these mutual funds:\n- " + selectedFunds;

        return callOpenAI(systemPrompt, userPrompt);
    }

    /**
     * Generates curated news topics and educational video recommendations
     * for a specific fund or ETF.
     */
    public String getResources(String ticker, String fundName, String category, String type) {
        String systemPrompt = "You are a Goldman Sachs research analyst AI. Given a specific " + type + ", " +
                "generate curated learning resources: news topics to follow and educational videos to watch.\n\n" +
                "FORMATTING RULES (you MUST follow these exactly):\n" +
                "- Use exactly these ## sections in this order:\n" +
                "  ## Key News Topics\n" +
                "  ## Recommended Videos\n" +
                "  ## Research Terms\n" +
                "  ## What to Watch For\n" +
                "- Under ## Key News Topics: list 4-5 specific, current news topics related to this fund with a brief " +
                "description of why each matters. Use - bullet points.\n" +
                "- Under ## Recommended Videos: list 4-5 specific YouTube video topics to search for (e.g. " +
                "'\"VFIAX vs VOO comparison 2025\"', '\"S&P 500 index fund explained\"'). Put the exact search query in " +
                "double quotes and briefly explain what the viewer will learn. Use - bullet points.\n" +
                "- Under ## Research Terms: list 5-6 key financial terms the investor should understand for this " +
                "specific fund type. Use - bullet points with a one-line definition each.\n" +
                "- Under ## What to Watch For: list 3-4 market indicators or events that specifically affect this fund. " +
                "Use - bullet points.\n" +
                "- Use **bold** for fund tickers, search queries, and key terms\n" +
                "- Do NOT use ### sub-headers, only ## headers";

        String userPrompt = String.format(
                "Generate learning resources for:\n- Ticker: %s\n- Name: %s\n- Category: %s\n- Type: %s",
                ticker, fundName, category, type);

        return callOpenAI(systemPrompt, userPrompt);
    }

    /**
     * AI investment advisor chat — answers questions about mutual funds and investing.
     */
    public String chat(String userMessage, List<MutualFund> funds) {
        String fundList = funds.stream()
                .map(f -> f.getTicker() + " (" + f.getName() + " - " + f.getCategory() + " [" + f.getType() + "])")
                .collect(Collectors.joining(", "));

        String systemPrompt = "You are a Goldman Sachs AI investment advisor. You help users understand " +
                "mutual fund and ETF investing, CAPM model, risk assessment, and portfolio strategy. You have " +
                "access to these mutual funds and ETFs in the app: " + fundList + ".\n\n" +
                "FORMATTING RULES:\n" +
                "- Be professional, concise, and helpful\n" +
                "- Use **bold** for fund tickers and key terms\n" +
                "- Use bullet points with - for lists\n" +
                "- Keep responses to 3-6 sentences or a short bulleted list\n" +
                "- Do NOT use ## headers in chat responses (keep it conversational)\n" +
                "- If asked about specific funds, reference their actual category\n" +
                "- End with a one-line disclaimer: *This is for educational purposes only and not financial advice.*";

        return callOpenAI(systemPrompt, userMessage);
    }

    /**
     * Calls the OpenAI Chat Completions API.
     */
    @SuppressWarnings("unchecked")
    private String callOpenAI(String systemPrompt, String userPrompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> body = Map.of(
                "model", MODEL,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                ),
                "temperature", 0.7,
                "max_tokens", 1000
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    OPENAI_URL, HttpMethod.POST, request, Map.class);

            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
                if (!choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    return (String) message.get("content");
                }
            }
            return "Unable to generate a response. Please try again.";
        } catch (Exception e) {
            return "AI service error: " + e.getMessage();
        }
    }
}
