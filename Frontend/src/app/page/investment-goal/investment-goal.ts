import { CommonModule } from '@angular/common';
import {
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
import { finalize } from 'rxjs';

import { AuthService } from '../../services/auth.service';
import {
  investmentGoalService
} from '../../services/investmentGoal-service';
import {
  InvestmentGoalRequest,
  InvestmentGoalResponse
} from '../../models/investmentGoal-models';

@Component({
  selector: 'app-investment-goal',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule
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

  private readonly router =
    inject(Router);

  saving = false;
  loadingGoals = false;

  savedGoals: InvestmentGoalResponse[] = [];

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

    this.financialGoalService
      .getByUserId(userId)
      .pipe(
        finalize(() => {
          this.loadingGoals = false;
        })
      )
      .subscribe({
        next: goals => {
          console.log(
            'Loaded goals:',
            goals
          );

          this.savedGoals = goals;
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
        }
      });
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
        'MEDIUM',
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
            monthlyContribution: ''
          });

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

    window.setTimeout(() => {
      this.toastMessage = '';
      this.toastIsError = false;
    }, 4000);
  }
}