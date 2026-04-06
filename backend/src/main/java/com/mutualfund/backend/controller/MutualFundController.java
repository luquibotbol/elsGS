package com.mutualfund.backend.controller;

import com.mutualfund.backend.model.InvestmentResult;
import com.mutualfund.backend.model.MutualFund;
import com.mutualfund.backend.service.MutualFundService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class MutualFundController {

    private final MutualFundService mutualFundService;

    public MutualFundController(MutualFundService mutualFundService) {
        this.mutualFundService = mutualFundService;
    }

    /**
     * GET /api/mutual-funds
     * Returns the list of available mutual funds.
     */
    @GetMapping("/mutual-funds")
    public List<MutualFund> getMutualFunds() {
        return mutualFundService.getAllFunds();
    }

    /**
     * GET /api/future-value?ticker={ticker}&principal={principal}&years={years}
     * Calculates the predicted future value of an investment using CAPM.
     *
     * @param ticker    the mutual fund ticker symbol (e.g., "VFIAX")
     * @param principal the initial investment amount in dollars
     * @param years     the investment time horizon in years
     * @return InvestmentResult with all calculation details
     */
    @GetMapping("/future-value")
    public InvestmentResult calculateFutureValue(
            @RequestParam String ticker,
            @RequestParam double principal,
            @RequestParam int years) {
        return mutualFundService.calculateFutureValue(ticker, principal, years);
    }
}
