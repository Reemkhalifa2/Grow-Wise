import { CommonModule } from '@angular/common';
import {
  AfterViewInit,
  Component,
  ElementRef,
  ViewChild,
  inject
} from '@angular/core';
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
import { GoogleSdkService } from '../../services/google-sdk.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink
  ],
  templateUrl: './Register.html',
  styleUrl: './Register.css'
})
export class Register implements AfterViewInit {

  private readonly formBuilder = inject(FormBuilder);
  private readonly authService = inject(Auth);
  private readonly router = inject(Router);
  private readonly googleSdk = inject(GoogleSdkService);

  @ViewChild('googleButton')
  private readonly googleButton?: ElementRef<HTMLElement>;

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

  ngAfterViewInit(): void {
    if (!this.googleButton) {
      return;
    }

    this.googleSdk
      .renderButton(
        this.googleButton.nativeElement,
        idToken => this.signUpWithGoogle(idToken)
      )
      .catch(error => {
        console.error(
          'Could not load Google sign-in:',
          error
        );

        this.showToast(
          'Google sign-up is unavailable right now.',
          true
        );
      });
  }

  /**
   * The backend creates the account on first sign-in, so there is nothing left
   * to confirm — send them straight into the app.
   */
  private signUpWithGoogle(idToken: string): void {
    this.hideToast();
    this.loading = true;

    this.authService
      .loginWithGoogle(idToken)
      .pipe(
        finalize(() => {
          this.loading = false;
        })
      )
      .subscribe({
        next: response => {
          this.authService.saveSession(response);

          const role = response.role?.toUpperCase();

          this.router.navigate([
            role === 'ADMIN'
              ? '/admin-dashboard'
              : '/dashboard'
          ]);
        },

        error: error => {
          console.error(
            'Google sign-up failed:',
            error
          );

          const message =
            error.status === 0
              ? 'Cannot connect to the server.'
              : error.status === 403
                ? 'This account is no longer active.'
                : 'Google sign-up failed. Please try again.';

          this.showToast(message, true);
        }
      });
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