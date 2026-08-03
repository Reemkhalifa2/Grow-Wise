import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import {
  FormBuilder,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';

import { Auth } from '../../services/auth';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink
  ],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class Login {

  private readonly formBuilder = inject(FormBuilder);
  private readonly authService = inject(Auth);
  private readonly router = inject(Router);

  loading = false;
  showPassword = false;
  errorMessage = '';

  readonly loginForm = this.formBuilder.group({
    email: [
      '',
      [
        Validators.required,
        Validators.email
      ]
    ],

    password: [
      '',
      Validators.required
    ]
  });

  login(): void {
    this.errorMessage = '';

    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    const formValue = this.loginForm.getRawValue();

    const request = {
      email: formValue.email!,
      password: formValue.password!
    };

    this.loading = true;

    this.authService
      .login(request)
      .pipe(
        finalize(() => {
          this.loading = false;
        })
      )
      .subscribe({
        next: response => {
          this.authService.saveSession(response);

          if (
            response.role?.toUpperCase() === 'ADMIN'
          ) {
            this.router.navigate([
              '/admin-dashboard'
            ]);
          } else {
            this.router.navigate([
              '/dashboard'
            ]);
          }
        },

        error: error => {
          console.error(
            'Login failed:',
            error
          );

          this.errorMessage =
            error.error?.message ??
            'Incorrect email or password.';
        }
      });
  }

  togglePassword(): void {
    this.showPassword = !this.showPassword;
  }
  continueWithGoogle(): void {
  window.open(
    'http://localhost:8080/oauth2/authorization/google',
    'google-login',
    'width=500,height=650'
  );
}

  isInvalid(
    controlName: 'email' | 'password'
  ): boolean {
    const control =
      this.loginForm.controls[controlName];

    return control.invalid && control.touched;
  }
  toastMessage = '';
toastIsError = false;
  private showToast(message: string, isError: boolean): void {
  this.toastMessage = message;
  this.toastIsError = isError;

  setTimeout(() => {
    this.toastMessage = '';
    this.toastIsError = false;
  }, 3000);
}
}