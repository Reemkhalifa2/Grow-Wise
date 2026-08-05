import { CommonModule } from '@angular/common';
import {
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

import {
  AssetAllocationView,
  AvailableAsset,
  InvestmentPlanRequest
} from '../../models/investment-plan-models';
import {
  InvestmentGoalResponse
} from '../../models/investmentGoal-models';

@Component({
  selector: 'app-investment-plan',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
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

  userId = 0;

  // -- Goal selection --
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

    this.loadPageData();
  }

  loadPageData(): void {
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
        })
      )
      .subscribe({
        next: response => {
          this.goals = response.goals ?? [];
          this.allAssets = response.assets ?? [];

          if (this.goals.length > 0) {
            this.selectGoal(this.goals[0].id);
          }
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
        }
      });
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