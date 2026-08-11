import { CommonModule } from '@angular/common';
import {
  ChangeDetectorRef,
  Component,
  inject,
  OnInit
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import {
  finalize,
  forkJoin
} from 'rxjs';

import { AuthService } from '../../services/auth.service';
import {
  InvestmentPlanService
} from '../../services/investment-plan.service';
import {
  investmentGoalService
} from '../../services/investmentGoal-service';
import { PortfolioService } from '../../services/portfolio-service';

import {
  AssetAllocationView,
  AvailableAsset,
  InvestmentPlanRequest
} from '../../models/investment-plan-models';
import {
  InvestmentGoalResponse
} from '../../models/investmentGoal-models';
import {
  InvestmentPlanOverview,
  InvestmentResponse
} from '../../models/portfolio-models';

import { PlanProgress } from '../../shared/plan-progress/plan-progress';
import { StatusBadge, StatusVariant } from '../../shared/status-badge/status-badge';
import { EnumLabelPipe } from '../../shared/pipes/enum-label.pipe';

type PageView = 'list' | 'create';

@Component({
  selector: 'app-investment-plan',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    PlanProgress,
    StatusBadge,
    EnumLabelPipe
  ],
  templateUrl: './investment-plan.html',
  styleUrl: './investment-plan.css'
})
export class InvestmentPlan implements OnInit {

  private readonly authService =
    inject(AuthService);

  private readonly investmentPlanService =
    inject(InvestmentPlanService);

  private readonly investmentGoalService =
    inject(investmentGoalService);

  private readonly portfolioService =
    inject(PortfolioService);

  private readonly cdr =
    inject(ChangeDetectorRef);

  userId = 0;
  view: PageView = 'list';

  // -- Plans list --
  plans: InvestmentPlanOverview[] = [];
  plansLoading = false;
  expandedPlanId: number | null = null;
  private investmentsByPlan = new Map<number, InvestmentResponse[]>();

  // -- Goal selection (create form) --
  goals: InvestmentGoalResponse[] = [];
  selectedGoalId: number | null = null;
  goal: InvestmentGoalResponse | null = null;

  // -- Assets / allocation --
  private allAssets: AvailableAsset[] = [];
  allocations: AssetAllocationView[] = [];

  // -- SIP --
  monthlyInvestmentAmount = 0;

  // -- UI state --
  loading = false;
  aiLoading = false;
  saving = false;

  planType = 'MANUAL';
  aiExplanation = '';

  toastMessage = '';
  toastIsError = false;

  ngOnInit(): void {
    this.userId =
      this.authService.getUserId() ?? 0;

    this.loadPlans();
  }

  // ============ Plans list ============

  loadPlans(): void {
    if (this.userId <= 0) {
      this.showToast('User session was not found.', true);
      return;
    }

    this.plansLoading = true;

    forkJoin({
      plans: this.investmentPlanService.getPlanOverviewsByUserId(this.userId),
      investments: this.portfolioService.getInvestmentsByUserId(this.userId)
    })
      .pipe(
        finalize(() => {
          this.plansLoading = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({
        next: result => {
          this.plans = result.plans ?? [];

          this.investmentsByPlan = new Map();
          for (const investment of result.investments ?? []) {
            const existing = this.investmentsByPlan.get(investment.planId) ?? [];
            existing.push(investment);
            this.investmentsByPlan.set(investment.planId, existing);
          }

          this.cdr.detectChanges();
        },
        error: error => {
          console.error('Failed to load investment plans:', error);
          this.showToast(
            error?.error?.message ?? 'Failed to load investment plans.',
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

  toggleDetails(planId: number): void {
    this.expandedPlanId = this.expandedPlanId === planId ? null : planId;
  }

  investmentsForPlan(planId: number): InvestmentResponse[] {
    return this.investmentsByPlan.get(planId) ?? [];
  }

  deletePlan(plan: InvestmentPlanOverview): void {
    const confirmed = window.confirm(
      `Delete "${plan.goalName ?? 'Plan #' + plan.planId}"? This cannot be undone.`
    );

    if (!confirmed) {
      return;
    }

    this.investmentPlanService.deletePlan(plan.planId).subscribe({
      next: () => {
        this.plans = this.plans.filter(p => p.planId !== plan.planId);
        this.showToast('Investment plan deleted.', false);
        this.cdr.detectChanges();
      },
      error: error => {
        this.showToast(
          error?.error?.message ?? 'Failed to delete plan.',
          true
        );
        this.cdr.detectChanges();
      }
    });
  }

  // ============ Create form ============

  startCreate(): void {
    this.view = 'create';
    this.loadCreateFormData();
  }

  cancelCreate(): void {
    this.view = 'list';
  }

  loadCreateFormData(): void {
    if (this.userId <= 0) {
      this.showToast(
        'User session was not found.',
        true
      );

      return;
    }

    this.loading = true;

    forkJoin({
      goals:
        this.investmentGoalService
          .getByUserId(this.userId),

      assets:
        this.investmentPlanService
          .getAvailableAssets()
    })
      .pipe(
        finalize(() => {
          this.loading = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({
        next: response => {
          this.goals = response.goals ?? [];
          this.allAssets = response.assets ?? [];

          if (this.goals.length > 0) {
            this.selectGoal(this.goals[0].id);
          }

          this.cdr.detectChanges();
        },

        error: error => {
          console.error(
            'Failed to load investment plan data:',
            error
          );

          this.showToast(
            error?.error?.message ??
            'Failed to load investment plan data.',
            true
          );

          this.cdr.detectChanges();
        }
      });
  }

  onGoalChange(): void {
    if (this.selectedGoalId != null) {
      this.selectGoal(this.selectedGoalId);
    }
  }

  private selectGoal(goalId: number): void {
    this.selectedGoalId = goalId;

    this.goal =
      this.goals.find(g => g.id === goalId) ?? null;

    this.allocations =
      this.createDefaultAllocations(this.allAssets);

    this.planType = 'MANUAL';
    this.aiExplanation = '';

    this.cdr.detectChanges();
  }

  private createDefaultAllocations(
    assets: AvailableAsset[]
  ): AssetAllocationView[] {

    if (assets.length === 0) {
      return [];
    }

    const basePercentage =
      Math.floor(100 / assets.length);

    return assets.map(
      (asset, index) => {

        const percentage =
          index === assets.length - 1
            ? 100 -
              basePercentage *
              (assets.length - 1)
            : basePercentage;

        return {
          assetId: asset.id,
          assetName: asset.name,
          symbol: asset.symbol,
          assetType: asset.assetType,
          riskLevel: asset.riskLevel,
          currentPrice: asset.currentPrice,
          percentage
        };
      }
    );
  }

  get currentSavings(): number {
    return Number(
      this.goal?.currentAmount ?? 0
    );
  }

  get totalPercentage(): number {
    return this.allocations.reduce(
      (total, allocation) =>
        total +
        Number(allocation.percentage || 0),
      0
    );
  }

  get allocationIsValid(): boolean {
    return Math.abs(
      this.totalPercentage - 100
    ) < 0.001;
  }

  getMonthlyAmount(
    percentage: number
  ): number {
    return (
      Number(this.monthlyInvestmentAmount || 0) *
      Number(percentage || 0) / 100
    );
  }

  allocationChanged(): void {
    this.planType = 'MANUAL';
    this.aiExplanation = '';
  }

  suggestWithAi(): void {
    if (!this.goal) {
      this.showToast(
        'Select a financial goal first.',
        true
      );

      return;
    }

    if (this.monthlyInvestmentAmount <= 0) {
      this.showToast(
        'Enter a monthly SIP amount.',
        true
      );

      return;
    }

    this.aiLoading = true;
    this.aiExplanation = '';

    this.investmentPlanService
      .suggestAllocation(
        this.goal.id,
        this.monthlyInvestmentAmount
      )
      .pipe(
        finalize(() => {
          this.aiLoading = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({
        next: suggestion => {
          this.allocations.forEach(
            allocation => {
              const percentage =
                suggestion.assetAllocations[
                  allocation.assetId
                ];

              if (percentage !== undefined) {
                allocation.percentage =
                  Number(percentage);
              }
            }
          );

          this.planType = 'AI_SUGGESTED';
          this.aiExplanation = suggestion.explanation;

          this.showToast(
            'AI allocation applied.',
            false
          );

          this.cdr.detectChanges();
        },

        error: error => {
          console.error(
            'AI suggestion failed:',
            error
          );

          this.showToast(
            error?.error?.message ??
            'AI could not generate an allocation.',
            true
          );

          this.cdr.detectChanges();
        }
      });
  }

  savePlan(): void {
    if (this.userId <= 0) {
      this.showToast(
        'User session was not found.',
        true
      );

      return;
    }

    if (!this.goal) {
      this.showToast(
        'Select a financial goal first.',
        true
      );

      return;
    }

    if (this.monthlyInvestmentAmount <= 0) {
      this.showToast(
        'Enter a monthly SIP amount.',
        true
      );

      return;
    }

    if (!this.allocationIsValid) {
      this.showToast(
        'Asset allocation must total 100%.',
        true
      );

      return;
    }

    if (this.allocations.length === 0) {
      this.showToast(
        'No assets are available.',
        true
      );

      return;
    }

    const assetAllocations:
      Record<number, number> = {};

    this.allocations.forEach(
      allocation => {
        assetAllocations[allocation.assetId] =
          Number(allocation.percentage);
      }
    );

    const request: InvestmentPlanRequest = {
      userId: this.userId,
      goalId: this.goal.id,

      monthlyInvestmentAmount:
        Number(this.monthlyInvestmentAmount),

      assetAllocations
    };

    this.saving = true;

    this.investmentPlanService
      .createPlan(request)
      .pipe(
        finalize(() => {
          this.saving = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({
        next: response => {
          console.log(
            'Investment plan saved:',
            response
          );

          this.showToast(
            'Investment plan saved successfully.',
            false
          );

          this.view = 'list';
          this.loadPlans();

          this.cdr.detectChanges();
        },

        error: error => {
          console.error(
            'Failed to save investment plan:',
            error
          );

          this.showToast(
            error?.error?.message ??
            'Failed to save investment plan.',
            true
          );

          this.cdr.detectChanges();
        }
      });
  }

  private showToast(
    message: string,
    isError: boolean
  ): void {
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
