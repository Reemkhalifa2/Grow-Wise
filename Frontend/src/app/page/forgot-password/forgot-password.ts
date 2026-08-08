import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, inject } from '@angular/core';
import {
  FormBuilder,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import { Auth } from '../../services/auth';
import { ForgotPasswordResponse } from '../../models/auth.models';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink
  ],
  templateUrl: './forgot-password.html',
  styleUrl: './forgot-password.css'
})
export class ForgotPassword {
  private readonly formBuilder = inject(FormBuilder);
  private readonly authService = inject(Auth);
  private readonly router = inject(Router);
  private readonly changeDetector = inject(ChangeDetectorRef);

  loading = false;
  submitted = false;
  errorMessage = '';

  resetLink: string | null = null;
  resetToken: string | null = null;

  toastMessage = '';
  toastIsError = false;

  readonly forgotPasswordForm = this.formBuilder.group({
    email: [
      '',
      [
        Validators.required,
        Validators.email
      ]
    ]
  });

  submit(): void {
    this.errorMessage = '';

    if (this.forgotPasswordForm.invalid) {
      this.forgotPasswordForm.markAllAsTouched();
      return;
    }

    const email = this.forgotPasswordForm.controls.email.value!;

    this.loading = true;

    this.authService
      .forgotPassword({ email })
      .pipe(
        finalize(() => {
          this.loading = false;
        })
      )
      .subscribe({
        next: (response: ForgotPasswordResponse) => {
          this.resetLink = response.resetLink;
          this.resetToken = response.resetToken;
          this.submitted = true;
          this.changeDetector.detectChanges();
        },
        error: (error: HttpErrorResponse) => {
          console.error('Forgot password request failed:', error);

          if (error.status === 0) {
            this.errorMessage = 'Cannot connect to the server.';
          } else {
            this.errorMessage =
              error.error?.message ?? 'Something went wrong. Please try again.';
          }

          this.showToast(this.errorMessage, true);
          this.changeDetector.detectChanges();
        }
      });
  }

  continueToReset(): void {
    if (!this.resetToken) {
      return;
    }

    this.router.navigate(
      ['/reset-password'],
      { queryParams: { token: this.resetToken } }
    );
  }

  isInvalid(controlName: 'email'): boolean {
    const control = this.forgotPasswordForm.controls[controlName];
    return control.invalid && control.touched;
  }

  private showToast(message: string, isError: boolean): void {
    this.toastMessage = message;
    this.toastIsError = isError;

    setTimeout(() => {
      this.toastMessage = '';
      this.toastIsError = false;
    }, 3000);
  }
}
