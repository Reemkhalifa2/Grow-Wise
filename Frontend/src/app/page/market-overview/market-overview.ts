import { CommonModule } from '@angular/common';
import {
  ChangeDetectorRef,
  Component,
  inject,
  OnInit
} from '@angular/core';
import { finalize } from 'rxjs';

import { AssetService } from '../../services/assets';
import { MarketDiscovery } from '../../models/assets';
import { PageHeader } from '../../shared/page-header/page-header';

@Component({
  selector: 'app-market-overview',
  standalone: true,
  imports: [CommonModule, PageHeader],
  templateUrl: './market-overview.html',
  styleUrl: './market-overview.css'
})
export class MarketOverview implements OnInit {

  private readonly assetService = inject(AssetService);
  private readonly cdr = inject(ChangeDetectorRef);

  assets: MarketDiscovery[] = [];
  loading = false;
  errorMessage = '';

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.errorMessage = '';

    this.assetService
      .discoverAssets()
      .pipe(
        finalize(() => {
          this.loading = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({
        next: assets => {
          this.assets = assets ?? [];
          this.cdr.detectChanges();
        },
        error: () => {
          this.errorMessage = 'Unable to load market data right now.';
          this.cdr.detectChanges();
        }
      });
  }

  trackBySymbol(index: number, asset: MarketDiscovery): string {
    return asset.symbol;
  }
}
