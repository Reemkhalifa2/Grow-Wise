import { Component, inject, OnInit, NgZone } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { GoogleSdkService } from '../../services/google-sdk.service';

declare const google: any;

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class Login implements OnInit {
  private fb = inject(FormBuilder);
  private router = inject(Router);
  private ngZone = inject(NgZone);
  private googleSdk = inject(GoogleSdkService);

  loginForm: FormGroup = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]]
  });

  loading = false;
  showPassword = false;
  errorMessage = '';
  toastMessage = '';
  toastIsError = false;

  private clientId = 'YOUR_GOOGLE_CLIENT_ID.apps.googleusercontent.com';
  private tokenClient: any;

  ngOnInit(): void {
    // Pre-load script on init so it's ready when user clicks
    this.googleSdk.loadScript()
      .then(() => this.initGoogleClient())
      .catch(() => console.warn('Google SDK failed to pre-load.'));
  }

  private initGoogleClient(): void {
    if (this.tokenClient || typeof google === 'undefined') return;

    this.tokenClient = google.accounts.oauth2.initTokenClient({
      client_id: this.clientId,
      scope: 'email profile openid',
      callback: (response: any) => this.handleGoogleResponse(response)
    });
  }

  async continueWithGoogle(): Promise<void> {
    if (this.loading) return;

    try {
      // Ensure SDK is ready before executing trigger
      await this.googleSdk.loadScript();
      this.initGoogleClient();

      if (this.tokenClient) {
        this.tokenClient.requestAccessToken();
      } else {
        this.showToast('Unable to initialize Google Sign-In.', true);
      }
    } catch (err) {
      this.showToast('Google SDK failed to load. Check ad-blockers.', true);
    }
  }

  private handleGoogleResponse(response: any): void {
    this.ngZone.run(() => {
      if (response.error) {
        this.errorMessage = 'Google sign-in was canceled or failed.';
        return;
      }

      const accessToken = response.access_token;
      this.loading = true;
      console.log('Google Access Token:', accessToken);
    });
  }

  togglePassword(): void {
    this.showPassword = !this.showPassword;
  }

  isInvalid(controlName: string): boolean {
    const control = this.loginForm.get(controlName);
    return !!(control && control.invalid && control.touched);
  }

  login(): void {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }
    this.loading = true;
  }

  showToast(message: string, isError = false): void {
    this.toastMessage = message;
    this.toastIsError = isError;
    setTimeout(() => { this.toastMessage = ''; }, 3000);
  }
}