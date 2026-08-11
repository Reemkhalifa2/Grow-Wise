import { CommonModule } from '@angular/common';
import {
  Component,
  inject,
  OnInit,
  ChangeDetectorRef
} from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import {
  finalize,
  forkJoin
} from 'rxjs';

import { Auth } from '../../services/auth';
import { DashboardService } from '../../services/user-dashboard';
import { investmentGoalService } from '../../services/investmentGoal-service';
import { PortfolioService } from '../../services/portfolio-service';
import { InvestmentPlanService } from '../../services/investment-plan.service';
import { AssetService } from '../../services/assets';

import {
  FinancialSummary,
  UserProfile
} from '../../models/userDashboard.models';
import {
  InvestmentPlanOverview,
  InvestmentResponse
} from '../../models/portfolio-models';
import { InvestmentGoalResponse } from '../../models/investmentGoal-models';
import { MarketDiscovery } from '../../models/assets';

import { AllocationChart, AllocationSlice } from '../../shared/allocation-chart/allocation-chart';
import { PortfolioChart, ContributionPoint } from '../../shared/portfolio-chart/portfolio-chart';
import { PlanProgress } from '../../shared/plan-progress/plan-progress';
import { GoalProgress } from '../../shared/goal-progress/goal-progress';
import { StatCard } from '../../shared/stat-card/stat-card';
import { StatusBadge, StatusVariant } from '../../shared/status-badge/status-badge';
import { GoalIcon } from '../../shared/goal-icon/goal-icon';
import { computeGoalStatus, goalIconKey } from '../../shared/goal-status';

interface GoalCardView {
  goal: InvestmentGoalResponse;
  progress: number;
  status: { status: string; label: string } | null;
}

@Component({
  selector: 'app-user-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    AllocationChart,
    PortfolioChart,
    PlanProgress,
    GoalProgress,
    StatCard,
    StatusBadge,
    GoalIcon
  ],
  templateUrl: './user-dashboard.html',
  styleUrl: './user-dashboard.css'
})
export class UserDashboard implements OnInit {

  private readonly authService = inject(Auth);
  private readonly dashboardService = inject(DashboardService);
  private readonly financialGoalService = inject(investmentGoalService);
  private readonly portfolioService = inject(PortfolioService);
  private readonly investmentPlanService = inject(InvestmentPlanService);
  private readonly assetService = inject(AssetService);
  private readonly router = inject(Router);
  private readonly cdr = inject(ChangeDetectorRef);

  loading = true;
  errorMessage = '';

  profile: UserProfile | null = null;

  summary: FinancialSummary = {
    userId: 0,
    monthlySalary: 0,
    monthlyExpenses: 0,
    netMonthlySavings: 0,
    expenseRatioPercentage: 0,
    savingsRatePercentage: 0,
    canInvest: false
  };

  investments: InvestmentResponse[] = [];
  goals: InvestmentGoalResponse[] = [];
  plans: InvestmentPlanOverview[] = [];

  marketAssets: MarketDiscovery[] = [];
  marketLoading = false;

  ngOnInit(): void {
    this.loadDashboard();
  }

  private loadDashboard(): void {
    const userId = this.authService.getUserId();

    if (userId === null) {
      this.authService.logout();
      this.router.navigate(['/login']);
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.cdr.markForCheck();

    forkJoin({
      profile: this.dashboardService.getProfile(userId),
      summary: this.dashboardService.getFinancialSummary(userId),
      goals: this.financialGoalService.getByUserId(userId),
      investments: this.portfolioService.getInvestmentsByUserId(userId),
      plans: this.investmentPlanService.getPlanOverviewsByUserId(userId)
    })
      .pipe(
        finalize(() => {
          this.loading = false;
          this.cdr.markForCheck();
        })
      )
      .subscribe({
        next: result => {
          this.profile = result.profile;
          this.summary = result.summary;
          this.goals = result.goals ?? [];
          this.investments = result.investments ?? [];
          this.plans = result.plans ?? [];

          this.cdr.markForCheck();
          this.loadMarketPreview();
        },

        error: error => {
          console.error('Dashboard request failed:', error);

          if (error.status === 401) {
            this.errorMessage = 'Your session has expired. Please log in again.';
            this.authService.logout();

            setTimeout(() => {
              this.router.navigate(['/login']);
            }, 1000);

            this.cdr.markForCheck();
            return;
          }

          if (error.status === 403) {
            this.errorMessage = 'You do not have permission to view this dashboard.';
            this.cdr.markForCheck();
            return;
          }

          if (error.status === 0) {
            this.errorMessage = 'Cannot connect to the backend server.';
            this.cdr.markForCheck();
            return;
          }

          this.errorMessage = error.error?.message ?? 'Failed to load dashboard.';
          this.cdr.markForCheck();
        }
      });
  }

  private loadMarketPreview(): void {
    this.marketLoading = true;

    this.assetService
      .discoverAssets()
      .pipe(finalize(() => {
        this.marketLoading = false;
        this.cdr.markForCheck();
      }))
      .subscribe({
        next: assets => {
          this.marketAssets = (assets ?? []).slice(0, 3);
          this.cdr.markForCheck();
        },
        error: () => {
          this.marketAssets = [];
          this.cdr.markForCheck();
        }
      });
  }

  retry(): void {
    this.loadDashboard();
  }

  // ---- Portfolio hero ----

  get totalInvested(): number {
    return this.investments.reduce(
      (sum, item) => sum + Number(item.amountInvested || 0),
      0
    );
  }

  get totalCurrentValue(): number {
    return this.investments.reduce(
      (sum, item) => sum + Number(item.currentValue || 0),
      0
    );
  }

  get totalProfitLoss(): number {
    return this.totalCurrentValue - this.totalInvested;
  }

  get totalReturnPercentage(): number {
    if (this.totalInvested <= 0) {
      return 0;
    }

    return (this.totalProfitLoss / this.totalInvested) * 100;
  }

  get isProfit(): boolean {
    return this.totalProfitLoss >= 0;
  }

  get contributionPoints(): ContributionPoint[] {
    return this.investments.map(investment => ({
      date: investment.purchaseDate,
      amountInvested: Number(investment.amountInvested || 0)
    }));
  }

  // ---- Allocation ----

  get allocationSlices(): AllocationSlice[] {
    const totals = new Map<string, number>();

    for (const investment of this.investments) {
      const label = investment.assetSymbol || investment.assetName;
      const current = totals.get(label) ?? 0;
      totals.set(label, current + Number(investment.currentValue || 0));
    }

    return Array.from(totals.entries())
      .filter(([, value]) => value > 0)
      .map(([label, value]) => ({ label, value }));
  }

  // ---- KPI cards ----

  get monthlyCapacityLabel(): string {
    return `OMR ${(this.summary.netMonthlySavings ?? 0).toFixed(3)}`;
  }

  get savingsRateLabel(): string {
    return `${(this.summary.savingsRatePercentage ?? 0).toFixed(0)}%`;
  }

  // ---- Goals ----

  get topGoals(): GoalCardView[] {
    return this.goals.slice(0, 3).map(goal => {
      const progress = goal.targetAmount > 0
        ? Math.max(0, Math.min((goal.currentAmount / goal.targetAmount) * 100, 100))
        : 0;

      const linkedPlan = this.plans.find(plan => plan.goalId === goal.id);

      const status = computeGoalStatus(
        goal.targetAmount,
        goal.currentAmount,
        goal.targetDate,
        linkedPlan?.monthlyInvestmentAmount ?? null
      );

      return { goal, progress, status };
    });
  }

  goalIcon(goal: InvestmentGoalResponse) {
    return goalIconKey(goal.goalName);
  }

  statusVariant(status: string): StatusVariant {
    if (status === 'ON_TRACK') return 'success';
    if (status === 'NEEDS_ATTENTION') return 'warning';
    if (status === 'BEHIND') return 'danger';
    return 'neutral';
  }

  // ---- Plans ----

  get topPlans(): InvestmentPlanOverview[] {
    return this.plans.slice(0, 3);
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
}
