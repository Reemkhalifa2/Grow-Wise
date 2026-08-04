import { CommonModule } from '@angular/common';
import {
  Component,
  inject,
  OnInit
} from '@angular/core';
import { Router } from '@angular/router';
import {
  finalize,
  forkJoin
} from 'rxjs';

import { Auth } from '../../services/auth';
import {
  DashboardService
} from '../../services/user-dashboard';

import {
  FinancialSummary,
  UserProfile
} from '../../models/userDashboard.models';

@Component({
  selector: 'app-user-dashboard',
  standalone: true,
  imports: [
    CommonModule
  ],
  templateUrl: './user-dashboard.html',
  styleUrl: './user-dashboard.css'
})
export class UserDashboard implements OnInit {

  private readonly authService =
    inject(Auth);

  private readonly dashboardService =
    inject(DashboardService);

  private readonly router =
    inject(Router);

  loading = true;
  errorMessage = '';

  profile: UserProfile | null = null;

  summary: FinancialSummary = {
    totalInvested: 0,
    portfolioValue: 0,
    totalProfit: 0,
    monthlyCapacity: 0
  };

  ngOnInit(): void {
    this.loadDashboard();
  }

  private loadDashboard(): void {
    const userId =
      this.authService.getUserId();

    if (userId === null) {
      this.authService.logout();
      this.router.navigate(['/login']);
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    forkJoin({
      profile:
        this.dashboardService
          .getProfile(userId),

      summary:
        this.dashboardService
          .getFinancialSummary(userId)
    })
      .pipe(
        finalize(() => {
          this.loading = false;
        })
      )
      .subscribe({
        next: result => {
          this.profile = result.profile;
          this.summary = result.summary;

          console.log(
            'Profile:',
            result.profile
          );

          console.log(
            'Financial summary:',
            result.summary
          );
        },

        error: error => {
          console.error(
            'Dashboard request failed:',
            error
          );

          if (error.status === 401) {
            this.errorMessage =
              'Your session has expired. Please log in again.';

            this.authService.logout();

            setTimeout(() => {
              this.router.navigate(['/login']);
            }, 1000);

            return;
          }

          if (error.status === 403) {
            this.errorMessage =
              'You do not have permission to view this dashboard.';

            return;
          }

          if (error.status === 0) {
            this.errorMessage =
              'Cannot connect to the backend server.';

            return;
          }

          this.errorMessage =
            error.error?.message ??
            'Failed to load dashboard.';
        }
      });
  }

  get firstName(): string {
    if (!this.profile?.fullName) {
      return 'User';
    }

    return (
      this.profile.fullName
        .split(' ')[0] || 'User'
    );
  }

  get greeting(): string {
    const hour =
      new Date().getHours();

    if (hour < 12) {
      return `Good morning, ${this.firstName}`;
    }

    if (hour < 18) {
      return `Good afternoon, ${this.firstName}`;
    }

    return `Good evening, ${this.firstName}`;
  }

  get profitPercentage(): number {
    if (
      this.summary.totalInvested === 0
    ) {
      return 0;
    }

    return (
      this.summary.totalProfit /
      this.summary.totalInvested
    ) * 100;
  }

  retry(): void {
    this.loadDashboard();
  }
}