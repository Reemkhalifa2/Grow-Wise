import { CommonModule } from '@angular/common';
import {
  Component,
  inject,
  OnInit
} from '@angular/core';
import { finalize } from 'rxjs';

import { AuthService } from '../../services/auth.service';
import {
  PortfolioService
} from '../../services/portfolio-service';

import {
  InvestmentResponse,
  PortfolioAssetSummary,
  PortfolioPlan
} from '../../models/portfolio-models';

@Component({
  selector: 'app-portfolio',
  standalone: true,
  imports: [
    CommonModule
  ],
  templateUrl: './portfolio.html',
  styleUrl: './portfolio.css'
})
export class Portfolio implements OnInit {

  private readonly authService =
    inject(AuthService);

  private readonly portfolioService =
    inject(PortfolioService);

  plans: PortfolioPlan[] = [];

  loading = false;
  completingPlanId: number | null = null;

  toastMessage = '';
  toastIsError = false;

  ngOnInit(): void {
    this.loadPortfolio();
  }

  private get userId(): number {
    return this.authService.getUserId() ?? 0;
  }

  loadPortfolio(): void {
    const userId = this.userId;

    if (userId <= 0) {
      this.showToast(
        'User session was not found.',
        true
      );

      return;
    }

    this.loading = true;

    this.portfolioService
      .getInvestmentsByUserId(userId)
      .pipe(
        finalize(() => {
          this.loading = false;
        })
      )
      .subscribe({
        next: investments => {
          console.log(
            'Loaded investments:',
            investments
          );

          this.plans =
            this.groupInvestmentsByPlan(
              investments ?? []
            );
        },

        error: error => {
          console.error(
            'Failed to load portfolio:',
            error
          );

          this.showToast(
            error?.error?.message ??
            'Failed to load portfolio.',
            true
          );
        }
      });
  }

  private groupInvestmentsByPlan(
    investments: InvestmentResponse[]
  ): PortfolioPlan[] {

    const planGroups =
      new Map<number, InvestmentResponse[]>();

    for (const investment of investments) {
      const planInvestments =
        planGroups.get(investment.planId) ?? [];

      planInvestments.push(investment);

      planGroups.set(
        investment.planId,
        planInvestments
      );
    }

    return Array.from(
      planGroups.entries()
    ).map(([planId, planInvestments]) => {

      const assets =
        this.groupInvestmentsByAsset(
          planInvestments
        );

      const totalInvested =
        assets.reduce(
          (total, asset) =>
            total + asset.totalInvested,
          0
        );

      const currentValue =
        assets.reduce(
          (total, asset) =>
            total + asset.currentValue,
          0
        );

      const profitOrLoss =
        assets.reduce(
          (total, asset) =>
            total + asset.profitOrLoss,
          0
        );

      return {
        planId,
        totalInvested,
        currentValue,
        profitOrLoss,
        assets
      };
    });
  }

  private groupInvestmentsByAsset(
    investments: InvestmentResponse[]
  ): PortfolioAssetSummary[] {

    const assetGroups =
      new Map<number, InvestmentResponse[]>();

    for (const investment of investments) {
      const assetInvestments =
        assetGroups.get(investment.assetId) ?? [];

      assetInvestments.push(investment);

      assetGroups.set(
        investment.assetId,
        assetInvestments
      );
    }

    return Array.from(
      assetGroups.entries()
    ).map(([assetId, assetInvestments]) => {

      const totalInvested =
        assetInvestments.reduce(
          (total, investment) =>
            total +
            Number(investment.amountInvested || 0),
          0
        );

      const currentValue =
        assetInvestments.reduce(
          (total, investment) =>
            total +
            Number(investment.currentValue || 0),
          0
        );

      const profitOrLoss =
        assetInvestments.reduce(
          (total, investment) =>
            total +
            Number(investment.profitOrLoss || 0),
          0
        );

      const lastInvestmentDate =
        assetInvestments.reduce(
          (latest, investment) =>
            !latest ||
            new Date(investment.purchaseDate) >
              new Date(latest)
              ? investment.purchaseDate
              : latest,
          '' as string
        );

      const reference = assetInvestments[0];

      return {
        assetId,
        assetName: reference.assetName,
        assetSymbol: reference.assetSymbol,
        assetType: reference.assetType,
        riskLevel: reference.riskLevel,
        totalInvested,
        currentValue,
        profitOrLoss,
        monthsInvested: assetInvestments.length,
        lastInvestmentDate
      };
    });
  }

  completeMonth(
    plan: PortfolioPlan
  ): void {
    const userId = this.userId;

    if (userId <= 0) {
      this.showToast(
        'User session was not found.',
        true
      );

      return;
    }

    this.completingPlanId =
      plan.planId;

    this.portfolioService
      .completeCurrentMonth(
        userId,
        plan.planId
      )
      .pipe(
        finalize(() => {
          this.completingPlanId = null;
        })
      )
      .subscribe({
        next: () => {
          this.showToast(
            'Monthly investment completed successfully.',
            false
          );

          this.loadPortfolio();
        },

        error: error => {
          console.error(
            'Failed to complete month:',
            error
          );

          this.showToast(
            error?.error?.message ??
            'Failed to complete this month.',
            true
          );
        }
      });
  }

  get totalPortfolioInvested(): number {
    return this.plans.reduce(
      (total, plan) =>
        total + plan.totalInvested,
      0
    );
  }

  get totalPortfolioValue(): number {
    return this.plans.reduce(
      (total, plan) =>
        total + plan.currentValue,
      0
    );
  }

  get totalProfitOrLoss(): number {
    return this.plans.reduce(
      (total, plan) =>
        total + plan.profitOrLoss,
      0
    );
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