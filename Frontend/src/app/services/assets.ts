import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import {
  
  MarketDiscovery,
} from '../models/assets';

@Injectable({
  providedIn: 'root',
})
export class AssetService {

  private readonly adminApiUrl =
    'http://localhost:8080/api/admin';

 

  private readonly discoverUrl =
    `${this.adminApiUrl}/discover`;

 

  constructor(private readonly http: HttpClient) {}



  /**
   * Gets assets discovered from external market sources.
   *
   * GET /api/admin/discover
   */
  discoverAssets(): Observable<MarketDiscovery[]> {
    return this.http.get<MarketDiscovery[]>(
      this.discoverUrl
    );
  }

 
 
}