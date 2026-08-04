import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import {
  FinancialProfileRequest,
  FinancialProfileResponse
} from '../models/financial-profile.models';

@Injectable({
  providedIn: 'root'
})
export class FinancialProfileService {

  private readonly apiUrl =
    'http://localhost:8080/api/users';

  constructor(
    private readonly http: HttpClient
  ) {}

  getProfile(
    userId: number
  ): Observable<FinancialProfileResponse> {
    return this.http.get<FinancialProfileResponse>(
      `${this.apiUrl}/${userId}/profile`
    );
  }

  saveProfile(
    userId: number,
    request: FinancialProfileRequest
    
  ): Observable<FinancialProfileResponse> {
    console.log('User ID:', userId);
console.log('Sending request...');
    return this.http.patch<FinancialProfileResponse>(
      `${this.apiUrl}/${userId}/profile`,
      request
    );
  }
}