import { CommonModule } from '@angular/common';
import {
  ChangeDetectorRef,
  Component,
  inject,
  OnInit
} from '@angular/core';
import { finalize } from 'rxjs';

import { AssetService } from '../../services/assets';
import {
  MarketDiscovery,
  RiskLevel
} from '../../models/assets';

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
  private readonly cdr = inject(ChangeDetectorRef); // 1. Inject ChangeDetectorRef

  discoveredAssets: MarketDiscovery[] = [];

  loading = false;

  addingSymbols = new Set<string>();
  addedSymbols = new Set<string>();

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
          this.cdr.detectChanges(); // 2. Trigger change detection when loading finishes
        })
      )
      .subscribe({
        next: assets => {
          this.discoveredAssets = assets;
          this.cdr.detectChanges(); // 3. Ensure component updates UI immediately
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

          this.cdr.detectChanges();
        }
      });
  }

  addToCatalog(
    asset: MarketDiscovery,
    riskLevel: RiskLevel = 'MEDIUM'
  ): void {

    if (
      this.addingSymbols.has(asset.symbol) ||
      this.addedSymbols.has(asset.symbol)
    ) {
      return;
    }

    this.addingSymbols.add(asset.symbol);
    this.cdr.detectChanges(); // 4. Instantly reflect loading state on button click

    this.assetService
      .addToCatalog(asset, riskLevel)
      .pipe(
        finalize(() => {
          this.addingSymbols.delete(asset.symbol);
          this.cdr.detectChanges(); // 5. Clear per-item loading state
        })
      )
      .subscribe({
        next: createdAsset => {
          this.addedSymbols.add(asset.symbol);

          this.showToast(
            `${createdAsset.name} added to catalog.`,
            false
          );

          this.cdr.detectChanges(); // 6. Reflect "Added" status immediately
        },

        error: error => {
          console.error(
            'Failed to add asset to catalog:',
            error
          );

          this.showToast(
            error?.error?.message ||
            `Failed to add ${asset.name}.`,
            true
          );

          this.cdr.detectChanges();
        }
      });
  }

  isAdding(symbol: string): boolean {
    return this.addingSymbols.has(symbol);
  }

  isAdded(symbol: string): boolean {
    return this.addedSymbols.has(symbol);
  }

  refreshDiscovery(): void {
    this.addedSymbols.clear();
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
    this.cdr.detectChanges(); // 7. Guarantee toast displays instantly

    window.setTimeout(() => {
      this.toastMessage = '';
      this.toastIsError = false;
      this.cdr.detectChanges(); // 8. Update view when toast expires
    }, 4000);
  }
}