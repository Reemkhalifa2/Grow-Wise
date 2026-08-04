import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import {
  investmentGoalRequest,
  investmentGoalResponse
} from '../models/investmentGoal-models';


@Injectable({
  providedIn: 'root',
})
export class investmentGoalService {
  private readonly apiUrl = 'http://localhost:8080/api/goals';
goalName: any;
id: any;
targetAmount: string | number | undefined;
currentAmount: string | number | undefined;
progressPercentage: string | number | undefined;
status: any;

  constructor(private http: HttpClient) {}

  list(): Observable<investmentGoalResponse[]> {
    return this.http.get<investmentGoalResponse[]>(this.apiUrl);
  }

  save(goal: investmentGoalRequest): Observable<investmentGoalResponse> {
    return this.http.post<investmentGoalResponse>(this.apiUrl, goal);
  }
}