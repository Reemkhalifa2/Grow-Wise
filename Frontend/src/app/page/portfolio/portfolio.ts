import { CommonModule } from '@angular/common';
import {
  ChangeDetectorRef,
  Component,
  inject,
  OnInit
} from '@angular/core';
import { finalize, forkJoin } from 'rxjs';

import { AuthService } from '../../services/auth.service';
import { PortfolioService } from '../../services/portfolio-service';
import { InvestmentPlanService } from '../../services/investment-plan.service';

import {
  InvestmentPlanOverview,
  InvestmentResponse,
  PortfolioAssetSummary
} from '../../models/portfolio-models';
import {
  AllocationChart,
  AllocationSlice
} from '../../shared/allocation-chart/allocation-chart';
import { PortfolioChart, ContributionPoint } from '../../shared/portfolio-chart/portfolio-chart';
import { PlanProgress } from '../../shared/plan-progress/plan-progress';
import { StatCard } from '../../shared/stat-card/stat-card';
import { StatusBadge, StatusVariant } from '../../shared/status-badge/status-badge';
import { EnumLabelPipe } from '../../shared/pipes/enum-label.pipe';

@Component({
  selector: 'app-portfolio',
  standalone: true,
  imports: [
    CommonModule,
    AllocationChart,
    PortfolioChart,
    PlanProgress,
    StatCard,
    StatusBadge,
    EnumLabelPipe
  ],
  templateUrl: './portfolio.html',
  styleUrl: './portfolio.css'
})
export class Portfolio implements OnInit {

  private readonly authService = inject(AuthService);
  private readonly portfolioService = inject(PortfolioService);
  private readonly investmentPlanService = inject(InvestmentPlanService);
  private readonly cdr = inject(ChangeDetectorRef);

  investments: InvestmentResponse[] = [];
  holdings: PortfolioAssetSummary[] = [];
  plans: InvestmentPlanOverview[] = [];

  loading = false;
  completingPlanId: number | null = null;
  deletingPlanId: number | null = null;

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
      this.showToast('User session was not found.', true);
      return;
    }

    this.loading = true;

    forkJoin({
      investments: this.portfolioService.getInvestmentsByUserId(userId),
      plans: this.investmentPlanService.getPlanOverviewsByUserId(userId)
    })
      .pipe(
        finalize(() => {
          this.loading = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({
        next: result => {
          this.investments = result.investments ?? [];
          this.holdings = this.groupInvestmentsByAsset(this.investments);
          this.plans = result.plans ?? [];

          this.cdr.detectChanges();
        },

        error: error => {
          console.error('Failed to load portfolio:', error);

          this.showToast(
            error?.error?.message ?? 'Failed to load portfolio.',
            true
          );

          this.cdr.detectChanges();
        }
      });
  }

  private groupInvestmentsByAsset(
    investments: InvestmentResponse[]
  ): PortfolioAssetSummary[] {

    const assetGroups = new Map<number, InvestmentResponse[]>();

    for (const investment of investments) {
      const assetInvestments = assetGroups.get(investment.assetId) ?? [];
      assetInvestments.push(investment);
      assetGroups.set(investment.assetId, assetInvestments);
    }

    return Array.from(assetGroups.entries()).map(([assetId, assetInvestments]) => {

      const totalInvested = assetInvestments.reduce(
        (total, investment) => total + Number(investment.amountInvested || 0),
        0
      );

      const currentValue = assetInvestments.reduce(
        (total, investment) => total + Number(investment.currentValue || 0),
        0
      );

      const profitOrLoss = assetInvestments.reduce(
        (total, investment) => total + Number(investment.profitOrLoss || 0),
        0
      );

      const lastInvestmentDate = assetInvestments.reduce(
        (latest, investment) =>
          !latest || new Date(investment.purchaseDate) > new Date(latest)
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

  completeMonth(plan: InvestmentPlanOverview): void {
    const userId = this.userId;

    if (userId <= 0) {
      this.showToast('User session was not found.', true);
      return;
    }

    this.completingPlanId = plan.planId;

    this.portfolioService
      .completeCurrentMonth(userId, plan.planId)
      .pipe(
        finalize(() => {
          this.completingPlanId = null;
          this.cdr.detectChanges();
        })
      )
      .subscribe({
        next: () => {
          this.showToast('Monthly investment completed successfully.', false);
          this.loadPortfolio();
        },

        error: error => {
          console.error('Failed to complete month:', error);

          this.showToast(
            error?.error?.message ?? 'Failed to complete this month.',
            true
          );

          this.cdr.detectChanges();
        }
      });
  }

  deletePlan(plan: InvestmentPlanOverview): void {
    const confirmed = window.confirm(
      `Delete "${plan.goalName ?? 'Plan #' + plan.planId}"? This cannot be undone.`
    );

    if (!confirmed) {
      return;
    }

    this.deletingPlanId = plan.planId;

    this.investmentPlanService
      .deletePlan(plan.planId)
      .pipe(
        finalize(() => {
          this.deletingPlanId = null;
          this.cdr.detectChanges();
        })
      )
      .subscribe({
        next: () => {
          this.plans = this.plans.filter(p => p.planId !== plan.planId);
          this.showToast('Investment plan deleted.', false);
          this.cdr.detectChanges();
        },

        error: error => {
          console.error('Failed to delete plan:', error);

          this.showToast(
            error?.error?.message ?? 'Failed to delete plan.',
            true
          );

          this.cdr.detectChanges();
        }
      });
  }

  planProgress(plan: InvestmentPlanOverview): number {
    if (!plan.goalTargetAmount || plan.goalTargetAmount <= 0) {
      return 0;
    }

    return Math.max(
      0,
      Math.min(((plan.goalCurrentAmount ?? 0) / plan.goalTargetAmount) * 100, 100)
    );
  }

  statusVariant(status: string): StatusVariant {
    return status?.toUpperCase() === 'ACTIVE' ? 'info' : 'neutral';
  }

  get allocationSlices(): AllocationSlice[] {
    return this.holdings
      .filter(asset => asset.currentValue > 0)
      .map(asset => ({
        label: asset.assetSymbol || asset.assetName,
        value: asset.currentValue
      }));
  }

  get contributionPoints(): ContributionPoint[] {
    return this.investments.map(investment => ({
      date: investment.purchaseDate,
      amountInvested: Number(investment.amountInvested || 0)
    }));
  }

  get totalPortfolioInvested(): number {
    return this.holdings.reduce((total, asset) => total + asset.totalInvested, 0);
  }

  get totalPortfolioValue(): number {
    return this.holdings.reduce((total, asset) => total + asset.currentValue, 0);
  }

  get totalProfitOrLoss(): number {
    return this.totalPortfolioValue - this.totalPortfolioInvested;
  }

  get totalReturnPercentage(): number {
    if (this.totalPortfolioInvested <= 0) {
      return 0;
    }

    return (this.totalProfitOrLoss / this.totalPortfolioInvested) * 100;
  }

  get isProfit(): boolean {
    return this.totalProfitOrLoss >= 0;
  }

  private showToast(message: string, isError: boolean): void {
    this.toastMessage = message;
    this.toastIsError = isError;
    this.cdr.detectChanges();

    window.setTimeout(() => {
      this.toastMessage = '';
      this.toastIsError = false;
      this.cdr.detectChanges();
    }, 4000);
  }
}
