import { CommonModule } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import {
  Router,
  RouterLink,
  RouterLinkActive
} from '@angular/router';
import { forkJoin, finalize } from 'rxjs';

import { Auth } from '../../services/auth';
import { DashboardService } from '../../services/user-dashboard';

import {
  FinancialSummary,
  UserProfile,
  FinancialGoal
} from '../../models/userDashboard.models';

@Component({
  selector: 'app-user-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    RouterLinkActive
  ],
  templateUrl: './user-dashboard.html',
  styleUrl: './user-dashboard.css'
})
export class Dashboard implements OnInit {

  private readonly authService = inject(Auth);
  private readonly dashboardService =
    inject(DashboardService);
  private readonly router = inject(Router);

  loading = true;
  errorMessage = '';

  fullName = '';
  role = '';
  initials = '';
  currentDate = '';

  profile: UserProfile | null = null;

  stats: FinancialSummary = {
    totalInvested: 0,
    portfolioValue: 0,
    totalProfit: 0,
    monthlyCapacity: 0
  };

  goal: FinancialGoal = {
  goalName: 'No active goal',
  currentAmount: 0,
  targetAmount: 0,
  progressPercentage: 0,
  status: 'NOT_STARTED',
  targetDate: ''
};

  ngOnInit(): void {
    this.currentDate =
      new Intl.DateTimeFormat('en-GB', {
        weekday: 'long',
        day: 'numeric',
        month: 'long',
        year: 'numeric'
      }).format(new Date());

    this.loadDashboard();
  }

  private loadDashboard(): void {
    const userId = this.authService.getUserId();

    if (userId === null) {
      this.authService.logout();
      this.router.navigate(['/login']);
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    forkJoin({
      profile:
        this.dashboardService.getProfile(userId),

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
          this.stats = result.summary;

          this.fullName =
            result.profile.fullName;

          this.role =
            result.profile.role;

          this.initials =
            this.getInitials(
              result.profile.fullName
            );
        },

        error: error => {
          console.error(
            'Dashboard loading failed:',
            error
          );

          if (error.status === 401 ||
              error.status === 403) {
            this.authService.logout();
            this.router.navigate(['/login']);
            return;
          }

          this.errorMessage =
            error.error?.message ??
            'Failed to load dashboard information.';
        }
      });
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  get greeting(): string {
    const firstName =
      this.fullName.split(' ')[0] || 'User';

    const hour = new Date().getHours();

    if (hour < 12) {
      return `Good morning, ${firstName}`;
    }

    if (hour < 18) {
      return `Good afternoon, ${firstName}`;
    }

    return `Good evening, ${firstName}`;
  }

  get isAdmin(): boolean {
    return this.role.toUpperCase() === 'ADMIN';
  }

  get profitPercentage(): number {
    if (this.stats.totalInvested === 0) {
      return 0;
    }

    return (
      this.stats.totalProfit /
      this.stats.totalInvested
    ) * 100;
  }

  private getInitials(
    fullName: string
  ): string {
    return fullName
      .trim()
      .split(/\s+/)
      .slice(0, 2)
      .map(name =>
        name.charAt(0).toUpperCase()
      )
      .join('');
  }
}