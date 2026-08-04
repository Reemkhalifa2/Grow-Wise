
import { CommonModule } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { finalize } from 'rxjs';

import { AssetService } from '../../services/assets';
import { MarketDiscovery } from '../../models/assets';

@Component({
  selector: 'app-asset-management',
  standalone: true,
  imports: [
    CommonModule
  ],
  templateUrl: './asset-management.html',
  styleUrl: './asset-management.css'
})
export class AssetManagement implements OnInit {

  private readonly assetService = inject(AssetService);

  discoveredAssets: MarketDiscovery[] = [];

  loading = false;

  toastMessage = '';
  toastIsError = false;

  ngOnInit(): void {
    this.loadDiscoveredAssets();
  }

  loadDiscoveredAssets(): void {
    this.loading = true;

    this.assetService
      .discoverAssets()
      .pipe(
        finalize(() => {
          this.loading = false;
        })
      )
      .subscribe({
        next: assets => {
          this.discoveredAssets = assets;
        },

        error: error => {
          console.error(
            'Failed to load discovered assets:',
            error
          );

          this.showToast(
            'Failed to discover assets.',
            true
          );
        }
      });
  }

  refreshDiscovery(): void {
    this.loadDiscoveredAssets();
  }

  trackBySymbol(
    index: number,
    asset: MarketDiscovery
  ): string {
    return asset.symbol;
  }

  private showToast(
    message: string,
    isError: boolean
  ): void {
    this.toastMessage = message;
    this.toastIsError = isError;

    window.setTimeout(() => {
      this.toastMessage = '';
      this.toastIsError = false;
    }, 4000);
  }
}

