import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, inject } from '@angular/core';import {
  FormBuilder,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
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
private readonly changeDetector = inject(ChangeDetectorRef);
  loading = false;
  showPassword = false;
  errorMessage = '';

  toastMessage = '';
  toastIsError = false;

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

    const request = {
      email: this.loginForm.controls.email.value!,
      password: this.loginForm.controls.password.value!
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
           const role = response.role?.toUpperCase();

          if (role === 'ADMIN') {
            this.router.navigate(['/admin-dashboard']);
          } else {
            this.router.navigate(['/dashboard']);
          }
        },
        error: (error: HttpErrorResponse) => {
          console.error('Login failed:', error);

          if (error.status === 401 || error.status === 403) {
            this.errorMessage = 'Incorrect email or password.';
          } else if (error.status === 0) {
            this.errorMessage = 'Cannot connect to the server.';
          } else {
            this.errorMessage =
              error.error?.message ?? 'Login failed. Please try again.';
          }

          this.showToast(this.errorMessage, true);
          this.changeDetector.detectChanges();
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

  isInvalid(controlName: 'email' | 'password'): boolean {
    const control = this.loginForm.controls[controlName];
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