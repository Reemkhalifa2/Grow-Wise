import { CommonModule } from '@angular/common';
import {
  AfterViewInit,
  ChangeDetectorRef,
  Component,
  ElementRef,
  ViewChild,
  inject
} from '@angular/core';import {
  FormBuilder,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import { Auth } from '../../services/auth';
import { GoogleSdkService } from '../../services/google-sdk.service';
import { AuthResponse } from '../../models/auth.models';

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
export class Login implements AfterViewInit {
  private readonly formBuilder = inject(FormBuilder);
  private readonly authService = inject(Auth);
  private readonly router = inject(Router);
private readonly changeDetector = inject(ChangeDetectorRef);
  private readonly googleSdk = inject(GoogleSdkService);

  @ViewChild('googleButton')
  private readonly googleButton?: ElementRef<HTMLElement>;

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

  ngAfterViewInit(): void {
    if (!this.googleButton) {
      return;
    }

    this.googleSdk
      .renderButton(
        this.googleButton.nativeElement,
        idToken => this.signInWithGoogle(idToken)
      )
      .catch(error => {
        console.error('Could not load Google sign-in:', error);

        this.errorMessage =
          'Google sign-in is unavailable right now.';

        this.changeDetector.detectChanges();
      });
  }

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
          this.completeLogin(response);
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

  private signInWithGoogle(idToken: string): void {
    this.errorMessage = '';
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
          this.completeLogin(response);
        },
        error: (error: HttpErrorResponse) => {
          console.error('Google login failed:', error);

          if (error.status === 0) {
            this.errorMessage = 'Cannot connect to the server.';
          } else if (error.status === 403) {
            this.errorMessage =
              'This account is no longer active.';
          } else {
            this.errorMessage =
              'Google sign-in failed. Please try again.';
          }

          this.showToast(this.errorMessage, true);
          this.changeDetector.detectChanges();
        }
      });
  }

  private completeLogin(response: AuthResponse): void {
    this.authService.saveSession(response);

    const role = response.role?.toUpperCase();

    if (role === 'ADMIN') {
      this.router.navigate(['/admin-dashboard']);
    } else {
      this.router.navigate(['/dashboard']);
    }
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