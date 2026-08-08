import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, inject } from '@angular/core';
import {
  FormBuilder,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import { Auth } from '../../services/auth';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink
  ],
  templateUrl: './reset-password.html',
  styleUrl: './reset-password.css'
})
export class ResetPassword {
  private readonly formBuilder = inject(FormBuilder);
  private readonly authService = inject(Auth);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly changeDetector = inject(ChangeDetectorRef);

  readonly token: string | null =
    this.route.snapshot.queryParamMap.get('token');

  loading = false;
  showPassword = false;
  submitted = false;
  errorMessage = '';

  toastMessage = '';
  toastIsError = false;

  readonly resetPasswordForm = this.formBuilder.group({
    newPassword: [
      '',
      [
        Validators.required,
        Validators.minLength(8)
      ]
    ],
    confirmPassword: [
      '',
      Validators.required
    ]
  });

  submit(): void {
    this.errorMessage = '';

    if (this.resetPasswordForm.invalid) {
      this.resetPasswordForm.markAllAsTouched();
      return;
    }

    const newPassword = this.resetPasswordForm.controls.newPassword.value!;
    const confirmPassword = this.resetPasswordForm.controls.confirmPassword.value!;

    if (newPassword !== confirmPassword) {
      this.errorMessage = 'Passwords do not match.';
      return;
    }

    if (!this.token) {
      this.errorMessage = 'This reset link is invalid or missing a token.';
      return;
    }

    this.loading = true;

    this.authService
      .resetPassword({
        token: this.token,
        newPassword,
        confirmPassword
      })
      .pipe(
        finalize(() => {
          this.loading = false;
        })
      )
      .subscribe({
        next: () => {
          this.submitted = true;
          this.showToast('Password reset. You can now log in.', false);
          this.changeDetector.detectChanges();

          setTimeout(() => {
            this.router.navigate(['/']);
          }, 2000);
        },
        error: (error: HttpErrorResponse) => {
          console.error('Reset password failed:', error);

          if (error.status === 0) {
            this.errorMessage = 'Cannot connect to the server.';
          } else if (error.status === 400) {
            this.errorMessage = error.error ?? 'Invalid or expired reset link.';
          } else {
            this.errorMessage = 'Something went wrong. Please try again.';
          }

          this.showToast(this.errorMessage, true);
          this.changeDetector.detectChanges();
        }
      });
  }

  togglePassword(): void {
    this.showPassword = !this.showPassword;
  }

  isInvalid(controlName: 'newPassword' | 'confirmPassword'): boolean {
    const control = this.resetPasswordForm.controls[controlName];
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
