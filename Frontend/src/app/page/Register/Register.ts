import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  ValidatorFn,
  Validators
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';

import { Auth } from '../../services/auth';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink
  ],
  templateUrl: './register.html',
  styleUrl: './register.css'
})
export class Register {

  private readonly formBuilder = inject(FormBuilder);
  private readonly authService = inject(Auth);
  private readonly router = inject(Router);

  loading = false;
  toastMessage = '';
  toastIsError = false;

  readonly registerForm = this.formBuilder.group(
    {
      fullName: [
        '',
        [
          Validators.required,
          Validators.minLength(2)
        ]
      ],

      email: [
        '',
        [
          Validators.required,
          Validators.email
        ]
      ],

      password: [
        '',
        [
          Validators.required,
          Validators.minLength(6)
        ]
      ],

      confirmPassword: [
        '',
        Validators.required
      ]
    },
    {
      validators: this.passwordMatchValidator()
    }
  );

  register(): void {
    this.hideToast();

    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();

      this.showToast(
        'Please correct the form errors.',
        true
      );

      return;
    }

    const formValue = this.registerForm.getRawValue();

    const request = {
      fullName: formValue.fullName!,
      email: formValue.email!,
      password: formValue.password!
    };

    this.loading = true;

    this.authService
      .register(request)
      .pipe(
        finalize(() => {
          this.loading = false;
        })
      )
      .subscribe({
        next: () => {
          this.showToast(
            'Account created successfully.',
            false
          );

          setTimeout(() => {
            this.router.navigate(['/login']);
          }, 1000);
        },

        error: error => {
          console.error(
            'Registration failed:',
            error
          );

          const message =
            error.error?.message ??
            error.error?.error ??
            'Registration failed. Please try again.';

          this.showToast(message, true);
        }
      });
  }

  continueWithGoogle(): void {
    const width = 500;
    const height = 650;

    const left =
      window.screenX +
      (window.outerWidth - width) / 2;

    const top =
      window.screenY +
      (window.outerHeight - height) / 2;

    window.open(
      'http://localhost:8080/oauth2/authorization/google',
      'google-register',
      `width=${width},height=${height},left=${left},top=${top}`
    );
  }

  isInvalid(
    controlName:
      | 'fullName'
      | 'email'
      | 'password'
      | 'confirmPassword'
  ): boolean {
    const control =
      this.registerForm.controls[controlName];

    return control.invalid && control.touched;
  }

  get confirmPasswordInvalid(): boolean {
    const confirmPassword =
      this.registerForm.controls.confirmPassword;

    return (
      confirmPassword.touched &&
      (
        confirmPassword.invalid ||
        this.registerForm.hasError('passwordMismatch')
      )
    );
  }

  private passwordMatchValidator(): ValidatorFn {
    return (
      control: AbstractControl
    ): ValidationErrors | null => {
      const password =
        control.get('password')?.value;

      const confirmPassword =
        control.get('confirmPassword')?.value;

      if (!password || !confirmPassword) {
        return null;
      }

      return password === confirmPassword
        ? null
        : { passwordMismatch: true };
    };
  }

  private showToast(
    message: string,
    isError: boolean
  ): void {
    this.toastMessage = message;
    this.toastIsError = isError;

    setTimeout(() => {
      this.hideToast();
    }, 4000);
  }

  private hideToast(): void {
    this.toastMessage = '';
    this.toastIsError = false;
  }
}