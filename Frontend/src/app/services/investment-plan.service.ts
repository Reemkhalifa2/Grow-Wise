import { Injectable } from '@angular/core';
import {
  HttpClient,
  HttpParams
} from '@angular/common/http';
import { Observable } from 'rxjs';

import {
  AiAllocationSuggestion,
  AvailableAsset,
  FinancialGoalView,
  InvestmentPlanRequest,
  InvestmentPlanResponse
} from '../models/investment-plan-models';

@Injectable({
  providedIn: 'root'
})
export class InvestmentPlanService {

  private readonly apiUrl =
    'http://localhost:8080/api';

  constructor(
    private readonly http: HttpClient
  ) {}

  getGoal(
    goalId: number
  ): Observable<FinancialGoalView> {
    return this.http.get<FinancialGoalView>(
      `${this.apiUrl}/financial-goals/${goalId}`
    );
  }

  getAvailableAssets(): Observable<AvailableAsset[]> {
    return this.http.get<AvailableAsset[]>(
      `${this.apiUrl}/admin/assets/available`
    );
  }

  suggestAllocation(
    goalId: number,
    monthlyInvestmentAmount: number
  ): Observable<AiAllocationSuggestion> {

    const params = new HttpParams()
      .set('goalId', goalId)
      .set(
        'monthlyInvestmentAmount',
        monthlyInvestmentAmount
      );

    return this.http.post<AiAllocationSuggestion>(
      `${this.apiUrl}/investment-plans/ai-suggestion`,
      null,
      { params }
    );
  }

  createPlan(
    request: InvestmentPlanRequest
  ): Observable<InvestmentPlanResponse> {
    return this.http.post<InvestmentPlanResponse>(
      `${this.apiUrl}/investment-plans`,
      request
    );
  }
}