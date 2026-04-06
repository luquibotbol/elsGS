import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { MutualFund } from '../models/mutual-fund.model';
import { InvestmentResult } from '../models/investment-result.model';

export interface AiResponse {
  content: string;
}

@Injectable({
  providedIn: 'root',
})
export class ApiService {
  private readonly apiUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  // ── Calculator APIs ──

  getMutualFunds() {
    return this.http.get<MutualFund[]>(`${this.apiUrl}/mutual-funds`);
  }

  calculateFutureValue(ticker: string, principal: number, years: number) {
    return this.http.get<InvestmentResult>(`${this.apiUrl}/future-value`, {
      params: {
        ticker: ticker,
        principal: principal.toString(),
        years: years.toString(),
      },
    });
  }

  // ── AI APIs ──

  generatePortfolio(amount: number, riskTolerance: string) {
    return this.http.post<AiResponse>(`${this.apiUrl}/ai/portfolio`, {
      amount,
      riskTolerance,
    });
  }

  analyzeRisk(investmentResult: InvestmentResult) {
    return this.http.post<AiResponse>(`${this.apiUrl}/ai/analyze`, investmentResult);
  }

  compareFunds(tickers: string[]) {
    return this.http.post<AiResponse>(`${this.apiUrl}/ai/compare`, { tickers });
  }

  chatWithAdvisor(message: string) {
    return this.http.post<AiResponse>(`${this.apiUrl}/ai/chat`, { message });
  }
}
