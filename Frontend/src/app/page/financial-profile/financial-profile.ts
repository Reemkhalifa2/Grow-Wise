import { CommonModule } from '@angular/common';
import {
  Component,
  OnInit,
  ChangeDetectorRef,
  inject
} from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';

import { AuthService } from '../../services/auth.service';
import { FinancialProfileService } from '../../services/financial-profile.service';
import { FinancialProfileRequest } from '../../models/financial-profile.models';
import { StatCard } from '../../shared/stat-card/stat-card';

@Component({
  selector: 'app-financial-profile',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    StatCard
  ],
  templateUrl: './financial-profile.html',
  styleUrl: './financial-profile.css'
})
export class FinancialProfile implements OnInit {

  private readonly formBuilder = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly profileService = inject(FinancialProfileService);
  private readonly cdr = inject(ChangeDetectorRef);

  profileForm: FormGroup;

  isLoading = false;
  isSaving = false;

  successMessage = '';
  errorMessage = '';

  constructor() {
    this.profileForm = this.formBuilder.group({
      monthlySalary: [
        null,
        [
          Validators.required,
          Validators.min(0)
        ]
      ],
      monthlyExpenses: [
        null,
        [
          Validators.required,
          Validators.min(0)
        ]
      ]
    });
  }

  ngOnInit(): void {
    this.loadProfile();
  }

  loadProfile(): void {
    const userId = this.authService.getUserId();

    if (userId === null) {
      this.errorMessage = 'User session was not found.';
      this.cdr.markForCheck();
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';
    this.cdr.markForCheck();

    this.profileService.getProfile(userId).subscribe({
      next: profile => {
        if (profile) {
          this.profileForm.patchValue({
            monthlySalary: profile.monthlySalary,
            monthlyExpenses: profile.monthlyExpenses
          });
        }
        this.isLoading = false;
        this.cdr.markForCheck();
      },

      error: error => {
        if (error.status !== 404) {
          this.errorMessage = 'Unable to load your financial profile.';
        }
        this.isLoading = false;
        this.cdr.markForCheck();
      }
    });
  }

  saveProfile(): void {
    this.successMessage = '';
    this.errorMessage = '';

    if (this.profileForm.invalid) {
      this.profileForm.markAllAsTouched();
      this.errorMessage = 'Please complete all required fields.';
      this.cdr.markForCheck();
      return;
    }

    const userId = this.authService.getUserId();

    if (userId === null) {
      this.errorMessage = 'User session was not found.';
      this.cdr.markForCheck();
      return;
    }

    const request: FinancialProfileRequest = {
      monthlySalary: Number(this.profileForm.value.monthlySalary),
      monthlyExpenses: Number(this.profileForm.value.monthlyExpenses)
    };

    if (request.monthlyExpenses > request.monthlySalary) {
      this.errorMessage = 'Monthly expenses cannot be greater than monthly salary.';
      this.cdr.markForCheck();
      return;
    }

    this.isSaving = true;
    this.cdr.markForCheck();

    this.profileService
      .saveProfile(userId, request)
      .subscribe({
        next: response => {
          if (response) {
            this.profileForm.patchValue(response);
          }

          this.successMessage = 'Financial profile saved successfully.';
          this.isSaving = false;
          this.cdr.markForCheck(); // Explicitly trigger re-render on save
        },

        error: error => {
          console.error('Saving failed:', error);
          this.errorMessage = error.error?.message ?? 'Unable to save financial profile.';
          this.isSaving = false;
          this.cdr.markForCheck();
        }
      });
  }

  get monthlySalary(): number {
    return Number(this.profileForm.value.monthlySalary) || 0;
  }

  get monthlyExpenses(): number {
    return Number(this.profileForm.value.monthlyExpenses) || 0;
  }

  get availableCapacity(): number {
    return Math.max(this.monthlySalary - this.monthlyExpenses, 0);
  }

  get savingsRatePercent(): number {
    if (this.monthlySalary <= 0) {
      return 0;
    }

    return Math.max(
      0,
      Math.min((this.availableCapacity / this.monthlySalary) * 100, 100)
    );
  }

  get expenseRatioPercent(): number {
    if (this.monthlySalary <= 0) {
      return 0;
    }

    return Math.max(
      0,
      Math.min((this.monthlyExpenses / this.monthlySalary) * 100, 100)
    );
  }
}