import { CommonModule } from '@angular/common';
import {
  Component,
  inject,
  OnInit,
  ChangeDetectorRef
} from '@angular/core';
import { Router } from '@angular/router';
import {
  finalize,
  forkJoin
} from 'rxjs';

import { Auth } from '../../services/auth';
import { DashboardService } from '../../services/user-dashboard';
import { investmentGoalService } from '../../services/investmentGoal-service';
import { PortfolioService } from '../../services/portfolio-service';

import {
  FinancialSummary,
  UserProfile,
  DashboardMetrics
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

  private readonly authService = inject(Auth);
  private readonly dashboardService = inject(DashboardService);
  private readonly financialGoalService = inject(investmentGoalService);
  private readonly portfolioService = inject(PortfolioService);
  private readonly router = inject(Router);
  private readonly cdr = inject(ChangeDetectorRef);

  loading = true;
  errorMessage = '';

  profile: UserProfile | null = null;

  summary: FinancialSummary = {
    userId: 0,
    monthlySalary: 0,
    monthlyExpenses: 0,
    netMonthlySavings: 0,
    expenseRatioPercentage: 0,
    savingsRatePercentage: 0,
    canInvest: false
  };

  metrics: DashboardMetrics = {
    totalInvestment: 0,
    totalPlans: 0,
    totalGoals: 0
  };

  ngOnInit(): void {
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
    this.cdr.markForCheck();

    forkJoin({
      profile: this.dashboardService.getProfile(userId),
      summary: this.dashboardService.getFinancialSummary(userId),
      goals: this.financialGoalService.getByUserId(userId),
      investments: this.portfolioService.getInvestmentsByUserId(userId)
    })
      .pipe(
        finalize(() => {
          this.loading = false;
          this.cdr.markForCheck();
        })
      )
      .subscribe({
        next: result => {
          this.profile = result.profile;
          this.summary = result.summary;

          const goals = result.goals ?? [];
          const investments = result.investments ?? [];

          // Total Investment sum across all user investments
          const totalInvestment = investments.reduce(
            (sum, item) => sum + Number(item.amountInvested || 0),
            0
          );

          // Total Plans: Count unique planIds
          const totalPlans = new Set(investments.map(inv => inv.planId)).size;

          // Total Goals count
          const totalGoals = goals.length;

          this.metrics = {
            totalInvestment,
            totalPlans,
            totalGoals
          };

          this.cdr.markForCheck();
        },

        error: error => {
          console.error('Dashboard request failed:', error);

          if (error.status === 401) {
            this.errorMessage = 'Your session has expired. Please log in again.';
            this.authService.logout();

            setTimeout(() => {
              this.router.navigate(['/login']);
            }, 1000);

            this.cdr.markForCheck();
            return;
          }

          if (error.status === 403) {
            this.errorMessage = 'You do not have permission to view this dashboard.';
            this.cdr.markForCheck();
            return;
          }

          if (error.status === 0) {
            this.errorMessage = 'Cannot connect to the backend server.';
            this.cdr.markForCheck();
            return;
          }

          this.errorMessage = error.error?.message ?? 'Failed to load dashboard.';
          this.cdr.markForCheck();
        }
      });
  }

  get firstName(): string {
    if (!this.profile?.fullName) {
      return 'User';
    }

    return this.profile.fullName.split(' ')[0] || 'User';
  }

  get greeting(): string {
    const hour = new Date().getHours();

    if (hour < 12) {
      return `Good morning, ${this.firstName}`;
    }

    if (hour < 18) {
      return `Good afternoon, ${this.firstName}`;
    }

    return `Good evening, ${this.firstName}`;
  }

  retry(): void {
    this.loadDashboard();
  }
}