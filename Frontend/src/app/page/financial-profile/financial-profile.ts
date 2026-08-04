import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';

import { AuthService } from '../../services/auth.service';
import { FinancialProfileService } from '../../services/financial-profile.service';
import {
  FinancialProfileRequest,
  
} from '../../models/financial-profile.models';

@Component({
  selector: 'app-financial-profile',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule
  ],
  templateUrl: './financial-profile.html',
  styleUrl: './financial-profile.css'
})
export class FinancialProfile implements OnInit {

  profileForm: FormGroup;

  isLoading = false;
  isSaving = false;

  successMessage = '';
  errorMessage = '';

  

  constructor(
    private readonly formBuilder: FormBuilder,
    private readonly authService: AuthService,
    private readonly profileService: FinancialProfileService
  ) {
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
      ],

      
      
    });
  }

  ngOnInit(): void {
    this.loadProfile();
  }

  loadProfile(): void {
    const userId = this.authService.getUserId();

    if (userId === null) {
      this.errorMessage = 'User session was not found.';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    this.profileService.getProfile(userId).subscribe({
      next: profile => {
        this.profileForm.patchValue({
          monthlySalary: profile.monthlySalary,
          monthlyExpenses: profile.monthlyExpenses,
   
    
        });

        this.isLoading = false;
      },

      error: error => {
        /*
         * A 404 can mean that this is the first time
         * the user creates a financial profile.
         */
        if (error.status !== 404) {
          this.errorMessage =
            'Unable to load your financial profile.';
        }

        this.isLoading = false;
      }
    });
  }

  

  saveProfile(): void {
    this.successMessage = '';
    this.errorMessage = '';

    console.log('Form value:', this.profileForm.getRawValue());
  console.log('Form valid:', this.profileForm.valid);
    if (this.profileForm.invalid) {
      this.profileForm.markAllAsTouched();
      this.errorMessage =
        'Please complete all required fields.';
      return;
    }

    const userId = this.authService.getUserId();

    if (userId === null) {
      this.errorMessage = 'User session was not found.';
      return;
    }

    const request: FinancialProfileRequest = {
      monthlySalary:
        Number(this.profileForm.value.monthlySalary),

      monthlyExpenses:
        Number(this.profileForm.value.monthlyExpenses)

      

    };

    if (request.monthlyExpenses > request.monthlySalary) {
      this.errorMessage =
        'Monthly expenses cannot be greater than monthly salary.';
      return;
    }

    this.isSaving = true;

    this.profileService
      .saveProfile(userId, request)
      .subscribe({
        next: response => {
          this.profileForm.patchValue(response);

          this.successMessage =
            'Financial profile saved successfully.';

          this.isSaving = false;
        },

        error: error => {
        console.error('Saving failed:', error);

        this.errorMessage =
          error.error?.message ??
          'Unable to save financial profile.';
      }
    });
  }
}