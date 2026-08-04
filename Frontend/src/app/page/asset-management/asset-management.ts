import { CommonModule } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs';

import { AssetService } from '../../services/assets';
import {
  Asset,
  AssetRequest,
  AssetType,
  RiskLevel,
  ASSET_TYPES,
  RISK_LEVELS
} from '../../models/assets';

@Component({
  selector: 'app-asset-management',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule
  ],
  templateUrl: './asset-management.html',
  styleUrl: './asset-management.css'
})
export class AssetManagement implements OnInit {

  private readonly formBuilder = inject(FormBuilder);
  private readonly assetService = inject(AssetService);

  readonly assetTypes: AssetType[] = ASSET_TYPES;
  readonly riskLevels: RiskLevel[] = RISK_LEVELS;

  assets: Asset[] = [];
  loading = true;
  saving = false;

  showMoreDetails = false;
  editingId: number | null = null;

  toastMessage = '';
  toastIsError = false;

  readonly assetForm = this.formBuilder.group({
    name: ['', Validators.required],
    symbol: ['', Validators.required],
    assetType: ['STOCK' as AssetType, Validators.required],
    riskLevel: ['MEDIUM' as RiskLevel, Validators.required],
    currentPrice: [0, [Validators.required, Validators.min(0)]],
    scrapingUrl: ['', Validators.required],
    cssSelector: ['', Validators.required],
    autoUpdate: [true]
  });

  ngOnInit(): void {
    this.loadAssets();
  }

  loadAssets(): void {
    this.loading = true;

    this.assetService
      .getAll()
      .pipe(
        finalize(() => {
          this.loading = false;
        })
      )
      .subscribe({
        next: assets => {
          this.assets = assets;
        },

        error: error => {
          console.error('Failed to load assets:', error);
          this.showToast('Failed to load assets. Please try again.', true);
        }
      });
  }

  toggleMoreDetails(): void {
    this.showMoreDetails = !this.showMoreDetails;
  }

  submit(): void {
    if (this.assetForm.invalid) {
      this.assetForm.markAllAsTouched();

      // Reveal the collapsed fields if any of them are the ones failing.
      const hiddenFieldsInvalid =
        this.assetForm.controls.scrapingUrl.invalid ||
        this.assetForm.controls.cssSelector.invalid;

      if (hiddenFieldsInvalid) {
        this.showMoreDetails = true;
      }

      this.showToast('Please fill in all required fields.', true);
      return;
    }

    const request = this.assetForm.getRawValue() as AssetRequest;

    this.saving = true;

    const request$ = this.editingId
      ? this.assetService.update(this.editingId, request)
      : this.assetService.create(request);

    request$
      .pipe(
        finalize(() => {
          this.saving = false;
        })
      )
      .subscribe({
        next: () => {
          this.showToast(
            this.editingId ? 'Asset updated successfully.' : 'Asset added successfully.',
            false
          );

          this.resetForm();
          this.loadAssets();
        },

        error: error => {
          console.error('Failed to save asset:', error);

          const message =
            error.error?.message ??
            error.error?.error ??
            'Failed to save asset. Please try again.';

          this.showToast(message, true);
        }
      });
  }

  startEdit(asset: Asset): void {
    this.editingId = asset.id;
    this.showMoreDetails = true;

    this.assetForm.setValue({
      name: asset.name,
      symbol: asset.symbol,
      assetType: asset.assetType,
      riskLevel: asset.riskLevel,
      currentPrice: asset.currentPrice,
      scrapingUrl: asset.scrapingUrl,
      cssSelector: asset.cssSelector,
      autoUpdate: asset.autoUpdate
    });
  }

  cancelEdit(): void {
    this.resetForm();
  }

  confirmDelete(asset: Asset): void {
    const confirmed = window.confirm(
      `Delete "${asset.name}" (${asset.symbol})? This cannot be undone.`
    );

    if (!confirmed) {
      return;
    }

    this.assetService.delete(asset.id).subscribe({
      next: () => {
        this.showToast('Asset deleted.', false);
        this.loadAssets();
      },

      error: error => {
        console.error('Failed to delete asset:', error);
        this.showToast('Failed to delete asset. Please try again.', true);
      }
    });
  }

  private resetForm(): void {
    this.editingId = null;
    this.showMoreDetails = false;

    this.assetForm.reset({
      name: '',
      symbol: '',
      assetType: 'STOCK',
      riskLevel: 'MEDIUM',
      currentPrice: 0,
      scrapingUrl: '',
      cssSelector: '',
      autoUpdate: true
    });
  }

  private showToast(message: string, isError: boolean): void {
    this.toastMessage = message;
    this.toastIsError = isError;

    setTimeout(() => {
      this.toastMessage = '';
      this.toastIsError = false;
    }, 4000);
  }
}