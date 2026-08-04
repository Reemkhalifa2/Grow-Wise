
import { CommonModule } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import {
  FormBuilder,
  ReactiveFormsModule
} from '@angular/forms';
import { finalize } from 'rxjs';

import { investmentGoalService } from '../../services/investmentGoal-service';
import {
  investmentGoalRequest,
  investmentGoalResponse
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

  private readonly formBuilder = inject(FormBuilder);

  private readonly financialGoalService =
    inject(investmentGoalService);

  saving = false;
  loadingGoals = true;

  savedGoals: investmentGoalResponse[] = [];

  toastMessage = '';
  toastIsError = false;

  readonly goalForm = this.formBuilder.group({
    monthlySalary: [4200],
    monthlyExpenses: [2650],
    currentSavings: [18500],
    targetAmount: [40000],
    monthlyContribution: [300]
  });

  ngOnInit(): void {
    this.loadGoals();
  }

  private get userId(): number {
    return Number(localStorage.getItem('userId')) || 0;
  }

  loadGoals(): void {
    this.loadingGoals = true;

    this.financialGoalService
      .list()
      .pipe(
        finalize(() => {
          this.loadingGoals = false;
        })
      )
      .subscribe({
        next: goals => {
          this.savedGoals = goals;
        },

        error: error => {
          console.error(
            'Failed to load saved goals:',
            error
          );

          this.showToast(
            'Failed to load saved goals.',
            true
          );
        }
      });
  }

  private get values() {
    const raw = this.goalForm.getRawValue();

    return {
      salary: Number(raw.monthlySalary) || 0,
      expenses: Number(raw.monthlyExpenses) || 0,
      currentSavings: Number(raw.currentSavings) || 0,
      targetAmount: Number(raw.targetAmount) || 0,
      contribution: Number(raw.monthlyContribution) || 0
    };
  }

  get availableInvestmentAmount(): number {
    const { salary, expenses } = this.values;

    return salary - expenses;
  }

  get isAvailableAmountNegative(): boolean {
    return this.availableInvestmentAmount < 0;
  }

  get monthlyInvestmentCapacity(): number {
    return this.availableInvestmentAmount;
  }

  get capacityPercentOfIncome(): string {
    const { salary } = this.values;

    if (salary <= 0) {
      return '0';
    }

    const percentage =
      (this.availableInvestmentAmount / salary) * 100;

    return percentage.toFixed(0);
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
      (currentSavings / targetAmount) * 100;

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
      (currentSavings / targetAmount) * 100;

    return Math.max(
      0,
      Math.min(percentage, 100)
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
    return this.remainingAmount <= 0;
  }

  get monthsToGoal(): number | null {
    const { contribution } = this.values;

    if (this.isGoalReached) {
      return 0;
    }

    if (contribution <= 0) {
      return null;
    }

    return Math.ceil(
      this.remainingAmount / contribution
    );
  }

  get timelineLabel(): string {
    const months = this.monthsToGoal;

    if (months === null) {
      return '—';
    }

    return `${months} month${months === 1 ? '' : 's'}`;
  }

  get timelineYearsMonthsLabel(): string {
    const months = this.monthsToGoal;

    if (months === null) {
      return 'Add a monthly contribution';
    }

    const years = Math.floor(months / 12);
    const remainingMonths = months % 12;

    return `${years} year${years === 1 ? '' : 's'}, ` +
      `${remainingMonths} month${remainingMonths === 1 ? '' : 's'}`;
  }

  private get projectedDate(): Date {
    const months = this.monthsToGoal ?? 1;

    const projected = new Date();

    projected.setMonth(
      projected.getMonth() + Math.max(months, 1)
    );

    return projected;
  }

  get projectedDateLabel(): string {
    if (this.monthsToGoal === null) {
      return 'Not available';
    }

    return this.projectedDate.toLocaleDateString(
      'en-US',
      {
        month: 'long',
        year: 'numeric'
      }
    );
  }

  get achievementCaption(): string {
    const { contribution } = this.values;

    if (this.isGoalReached) {
      return 'Goal already reached. Nice work.';
    }

    if (contribution <= 0) {
      return 'Add a monthly contribution to see your projected timeline.';
    }

    return `At OMR ${contribution}/month, you could reach this goal around ${this.projectedDateLabel}. Returns are not included.`;
  }

  saveGoal(): void {
    const {
      currentSavings,
      targetAmount
    } = this.values;

    if (this.userId <= 0) {
      this.showToast(
        'User session was not found.',
        true
      );

      return;
    }

    const request: investmentGoalRequest = {
      goalName: 'Investment goal',
      targetAmount,
      currentAmount: currentSavings,
      targetDate:
        this.projectedDate
          .toISOString()
          .split('T')[0],
      status: 'ACTIVE',
      riskLevel: 'MEDIUM',
      userId: this.userId
    };

    this.saving = true;

    this.financialGoalService
      .save(request)
      .pipe(
        finalize(() => {
          this.saving = false;
        })
      )
      .subscribe({
        next: savedGoal => {
          this.savedGoals = [
            savedGoal,
            ...this.savedGoals
          ];

          this.showToast(
            'Goal saved successfully.',
            false
          );
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

          this.showToast(message, true);
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
