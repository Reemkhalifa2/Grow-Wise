import { CommonModule } from '@angular/common';
import {
  ChangeDetectorRef,
  Component,
  inject,
  OnInit
} from '@angular/core';
import {
  FormBuilder,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';
import { Router } from '@angular/router';
import { finalize, forkJoin } from 'rxjs';

import { AuthService } from '../../services/auth.service';
import {
  investmentGoalService
} from '../../services/investmentGoal-service';
import { InvestmentPlanService } from '../../services/investment-plan.service';
import {
  InvestmentGoalRequest,
  InvestmentGoalResponse,
  GoalRiskLevel
} from '../../models/investmentGoal-models';
import { InvestmentPlanOverview } from '../../models/portfolio-models';

import { GoalIcon } from '../../shared/goal-icon/goal-icon';
import { StatusBadge, StatusVariant } from '../../shared/status-badge/status-badge';
import { computeGoalStatus, goalIconKey } from '../../shared/goal-status';

@Component({
  selector: 'app-investment-goal',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    GoalIcon,
    StatusBadge
  ],
  templateUrl: './investment-goal.html',
  styleUrl: './investment-goal.css'
})
export class FinancialGoal implements OnInit {

  private readonly formBuilder =
    inject(FormBuilder);

  private readonly authService =
    inject(AuthService);

  private readonly financialGoalService =
    inject(investmentGoalService);

  private readonly investmentPlanService =
    inject(InvestmentPlanService);

  private readonly router =
    inject(Router);

  private readonly cdr =
    inject(ChangeDetectorRef); // 1. Inject ChangeDetectorRef

  saving = false;
  loadingGoals = false;
  deletingGoalId: number | null = null;

  savedGoals: InvestmentGoalResponse[] = [];
  private plans: InvestmentPlanOverview[] = [];

  readonly riskLevels: GoalRiskLevel[] = ['LOW', 'MEDIUM', 'HIGH'];

  toastMessage = '';
  toastIsError = false;

  readonly goalForm = this.formBuilder.group({
    goalName: [
      '',
      [
        Validators.required
      ]
    ],

    monthlySalary: [
      '',
      [
        Validators.required,
        Validators.min(0)
      ]
    ],

    monthlyExpenses: [
      '',
      [
        Validators.required,
        Validators.min(0)
      ]
    ],

    currentSavings: [
      '',
      [
        Validators.required,
        Validators.min(0)
      ]
    ],

    targetAmount: [
      '',
      [
        Validators.required,
        Validators.min(1)
      ]
    ],

    monthlyContribution: [
      '',
      [
        Validators.required,
        Validators.min(0)
      ]
    ],

    riskLevel: [
      'MEDIUM' as GoalRiskLevel,
      [
        Validators.required
      ]
    ]
  });

  ngOnInit(): void {
    this.loadGoals();
  }

  private get userId(): number {
    return this.authService.getUserId() ?? 0;
  }

  loadGoals(): void {
    const userId = this.userId;

    if (userId <= 0) {
      this.loadingGoals = false;

      this.showToast(
        'User session was not found.',
        true
      );

      return;
    }

    this.loadingGoals = true;

    forkJoin({
      goals: this.financialGoalService.getByUserId(userId),
      plans: this.investmentPlanService.getPlanOverviewsByUserId(userId)
    })
      .pipe(
        finalize(() => {
          this.loadingGoals = false;
          this.cdr.detectChanges(); // 2. Clear loading state in view
        })
      )
      .subscribe({
        next: result => {
          this.savedGoals = result.goals ?? [];
          this.plans = result.plans ?? [];
          this.cdr.detectChanges(); // 3. Render loaded goals array immediately
        },

        error: error => {
          console.error(
            'Failed to load saved goals:',
            error
          );

          const message =
            error.error?.message ??
            error.error?.error ??
            'Failed to load saved goals.';

          this.showToast(
            message,
            true
          );

          this.cdr.detectChanges();
        }
      });
  }

  goalIcon(goal: InvestmentGoalResponse) {
    return goalIconKey(goal.goalName);
  }

  goalStatus(goal: InvestmentGoalResponse) {
    const linkedPlan = this.plans.find(plan => plan.goalId === goal.id);

    return computeGoalStatus(
      goal.targetAmount,
      goal.currentAmount,
      goal.targetDate,
      linkedPlan?.monthlyInvestmentAmount ?? null
    );
  }

  statusVariant(status: string): StatusVariant {
    if (status === 'ON_TRACK') return 'success';
    if (status === 'NEEDS_ATTENTION') return 'warning';
    if (status === 'BEHIND') return 'danger';
    return 'neutral';
  }

  setRiskLevel(riskLevel: GoalRiskLevel): void {
    this.goalForm.patchValue({ riskLevel });
  }

  createInvestmentPlan(
    goalId: number
  ): void {
    this.router.navigate([
      '/investment-plan',
      goalId
    ]);
  }

  private get values() {
    const raw =
      this.goalForm.getRawValue();

    return {
      goalName:
        raw.goalName?.trim() ?? '',

      salary:
        Number(raw.monthlySalary) || 0,

      expenses:
        Number(raw.monthlyExpenses) || 0,

      currentSavings:
        Number(raw.currentSavings) || 0,

      targetAmount:
        Number(raw.targetAmount) || 0,

      contribution:
        Number(raw.monthlyContribution) || 0
    };
  }

  get availableInvestmentAmount(): number {
    const {
      salary,
      expenses
    } = this.values;

    return salary - expenses;
  }

  get isAvailableAmountNegative(): boolean {
    return this.availableInvestmentAmount < 0;
  }

  get monthlyInvestmentCapacity(): number {
    return Math.max(
      this.availableInvestmentAmount,
      0
    );
  }

  get capacityPercentOfIncome(): string {
    const {
      salary
    } = this.values;

    if (salary <= 0) {
      return '0';
    }

    const percentage =
      (
        this.availableInvestmentAmount /
        salary
      ) * 100;

    return Math.max(
      percentage,
      0
    ).toFixed(0);
  }

  get savingProgressPercent(): string {
    const {
      currentSavings,
      targetAmount
    } = this.values;

    if (targetAmount <= 0) {
      return '0.0';
    }

    const percentage =
      (
        currentSavings /
        targetAmount
      ) * 100;

    return percentage.toFixed(1);
  }

  get achievementBarWidth(): number {
    const {
      currentSavings,
      targetAmount
    } = this.values;

    if (targetAmount <= 0) {
      return 0;
    }

    const percentage =
      (
        currentSavings /
        targetAmount
      ) * 100;

    return Math.max(
      0,
      Math.min(
        percentage,
        100
      )
    );
  }

  get remainingAmount(): number {
    const {
      currentSavings,
      targetAmount
    } = this.values;

    return Math.max(
      targetAmount - currentSavings,
      0
    );
  }

  get isGoalReached(): boolean {
    return (
      this.values.targetAmount > 0 &&
      this.remainingAmount <= 0
    );
  }

  get monthsToGoal(): number | null {
    const {
      contribution,
      targetAmount
    } = this.values;

    if (targetAmount <= 0) {
      return null;
    }

    if (this.isGoalReached) {
      return 0;
    }

    if (contribution <= 0) {
      return null;
    }

    return Math.ceil(
      this.remainingAmount /
      contribution
    );
  }

  get timelineLabel(): string {
    const months =
      this.monthsToGoal;

    if (months === null) {
      return '—';
    }

    return `${months} month${months === 1 ? '' : 's'}`;
  }

  get timelineYearsMonthsLabel(): string {
    const months =
      this.monthsToGoal;

    if (months === null) {
      return 'Add a monthly contribution';
    }

    if (months === 0) {
      return 'Goal reached';
    }

    const years =
      Math.floor(months / 12);

    const remainingMonths =
      months % 12;

    if (years === 0) {
      return (
        `${remainingMonths} month` +
        `${remainingMonths === 1 ? '' : 's'}`
      );
    }

    if (remainingMonths === 0) {
      return (
        `${years} year` +
        `${years === 1 ? '' : 's'}`
      );
    }

    return (
      `${years} year${years === 1 ? '' : 's'}, ` +
      `${remainingMonths} month` +
      `${remainingMonths === 1 ? '' : 's'}`
    );
  }

  private get projectedDate(): Date {
    const projected =
      new Date();

    const months =
      this.monthsToGoal ?? 1;

    projected.setMonth(
      projected.getMonth() +
      Math.max(
        months,
        1
      )
    );

    return projected;
  }

  private get projectedDateValue(): string {
    const projected =
      this.projectedDate;

    const year =
      projected.getFullYear();

    const month =
      String(
        projected.getMonth() + 1
      ).padStart(
        2,
        '0'
      );

    const day =
      String(
        projected.getDate()
      ).padStart(
        2,
        '0'
      );

    return `${year}-${month}-${day}`;
  }

  get projectedDateLabel(): string {
    if (this.monthsToGoal === null) {
      return 'Not available';
    }

    if (this.isGoalReached) {
      return 'Goal reached';
    }

    return this.projectedDate
      .toLocaleDateString(
        'en-US',
        {
          month: 'long',
          year: 'numeric'
        }
      );
  }

  get achievementCaption(): string {
    const {
      contribution,
      targetAmount
    } = this.values;

    if (targetAmount <= 0) {
      return 'Enter your target amount to see the projection.';
    }

    if (this.isGoalReached) {
      return 'Goal already reached. Nice work.';
    }

    if (contribution <= 0) {
      return 'Add a monthly contribution to see your projected timeline.';
    }

    return (
      `At OMR ${contribution.toFixed(3)} per month, ` +
      `you could reach this goal around ` +
      `${this.projectedDateLabel}. ` +
      `Investment returns are not included.`
    );
  }

  saveGoal(): void {
    if (this.goalForm.invalid) {
      this.goalForm.markAllAsTouched();

      this.showToast(
        'Please complete all required fields.',
        true
      );

      return;
    }

    const userId =
      this.userId;

    if (userId <= 0) {
      this.showToast(
        'User session was not found.',
        true
      );

      return;
    }

    const {
      goalName,
      currentSavings,
      targetAmount
    } = this.values;

    const request: InvestmentGoalRequest = {
      goalName,
      targetAmount,
      currentAmount:
        currentSavings,
      targetDate:
        this.projectedDateValue,
      status:
        this.isGoalReached
          ? 'ACHIEVED'
          : 'ACTIVE',
      riskLevel:
        (this.goalForm.value.riskLevel as GoalRiskLevel) ?? 'MEDIUM',
      userId
    };

    console.log(
      'Saving goal:',
      request
    );

    this.saving = true;

    this.financialGoalService
      .create(request)
      .pipe(
        finalize(() => {
          this.saving = false;
          this.cdr.detectChanges(); // 4. Clear saving spinner in UI
        })
      )
      .subscribe({
        next: savedGoal => {
          console.log(
            'Goal saved successfully:',
            savedGoal
          );

          this.showToast(
            'Goal saved successfully.',
            false
          );

          this.goalForm.reset({
            goalName: '',
            monthlySalary: '',
            monthlyExpenses: '',
            currentSavings: '',
            targetAmount: '',
            monthlyContribution: '',
            riskLevel: 'MEDIUM'
          });

          this.cdr.detectChanges(); // 5. Refresh form fields visually
          this.loadGoals();
        },

        error: error => {
          console.error(
            'Failed to save goal:',
            error
          );

          const message =
            error.error?.message ??
            error.error?.error ??
            'Failed to save goal. Please try again.';

          this.showToast(
            message,
            true
          );

          this.cdr.detectChanges();
        }
      });
  }

  deleteGoal(
    goal: InvestmentGoalResponse
  ): void {
    const confirmed = window.confirm(
      `Delete goal "${goal.goalName}"? This cannot be undone.`
    );

    if (!confirmed) {
      return;
    }

    this.deletingGoalId = goal.id;

    this.financialGoalService
      .delete(goal.id)
      .pipe(
        finalize(() => {
          this.deletingGoalId = null;
          this.cdr.detectChanges();
        })
      )
      .subscribe({
        next: () => {
          this.savedGoals = this.savedGoals.filter(
            existingGoal =>
              existingGoal.id !== goal.id
          );

          this.showToast(
            'Goal deleted.',
            false
          );

          this.cdr.detectChanges();
        },

        error: error => {
          console.error(
            'Failed to delete goal:',
            error
          );

          const message =
            error.error?.message ??
            error.error?.error ??
            'Failed to delete goal.';

          this.showToast(
            message,
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
    this.toastMessage =
      message;

    this.toastIsError =
      isError;

    this.cdr.detectChanges(); // 6. Force toast to appear instantly

    window.setTimeout(() => {
      this.toastMessage = '';
      this.toastIsError = false;
      this.cdr.detectChanges(); // 7. Clear toast when timer expires
    }, 4000);
  }
}