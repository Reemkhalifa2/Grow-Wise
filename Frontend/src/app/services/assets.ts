import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { Asset, AssetRequest } from '../models/assets';

@Injectable({
  providedIn: 'root',
})
export class AssetService {
  private readonly apiUrl = 'http://localhost:8080/api/admin/assets';

  constructor(private http: HttpClient) {}

  /** Matches GetMapping("/assets") on AdminController - already implemented. */
  getAll(): Observable<Asset[]> {
    return this.http.get<Asset[]>(this.apiUrl);
  }

  /** Matches PostMapping("/assets") on AdminController - already implemented. */
  create(request: AssetRequest): Observable<Asset> {
    return this.http.post<Asset>(this.apiUrl, request);
  }

  /**
   * NOT YET IMPLEMENTED ON THE BACKEND.
   * Requires adding to AdminController:
   *   @PutMapping("/assets/{id}")
   *   public ResponseEntity<AssetAdminResponseDTO> updateAsset(
   *       @PathVariable Integer id,
   *       @Valid @RequestBody AssetAdminRequestDTO request) { ... }
   */
  update(id: number, request: AssetRequest): Observable<Asset> {
    return this.http.put<Asset>(`${this.apiUrl}/${id}`, request);
  }

  /**
   * NOT YET IMPLEMENTED ON THE BACKEND.
   * Requires adding to AdminController:
   *   @DeleteMapping("/assets/{id}")
   *   public ResponseEntity<Void> deleteAsset(@PathVariable Integer id) { ... }
   */
  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}