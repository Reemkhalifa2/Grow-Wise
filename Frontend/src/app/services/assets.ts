import { Injectable } from '@angular/core';
import {
  HttpClient,
  HttpParams
} from '@angular/common/http';
import { Observable } from 'rxjs';

import {
  Asset,
  MarketDiscovery,
  RiskLevel
} from '../models/assets';

@Injectable({
  providedIn: 'root',
})
export class AssetService {

  private readonly adminApiUrl =
    'http://localhost:8080/api/admin';

  private readonly discoverUrl =
    `${this.adminApiUrl}/discover`;

  private readonly addToCatalogUrl =
    `${this.adminApiUrl}/add-to-catalog`;

  constructor(
    private readonly http: HttpClient
  ) {}

  /**
   * Load assets scraped from external sources.
   *
   * GET /api/admin/discover
   */
  discoverAssets(): Observable<MarketDiscovery[]> {
    return this.http.get<MarketDiscovery[]>(
      this.discoverUrl
    );
  }

  /**
   * Save one discovered asset into the database.
   *
   * POST /api/admin/add-to-catalog?riskLevel=MEDIUM
   */
  addToCatalog(
    asset: MarketDiscovery,
    riskLevel: RiskLevel
  ): Observable<Asset> {

    const params = new HttpParams()
      .set('riskLevel', riskLevel);

    return this.http.post<Asset>(
      this.addToCatalogUrl,
      asset,
      { params }
    );
  }
}