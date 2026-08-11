import { CommonModule } from '@angular/common';
import {
  ChangeDetectorRef,
  Component,
  inject,
  OnInit
} from '@angular/core';
import { finalize } from 'rxjs';

import { DashboardStats } from '../../models/dashboard-stats';
import { Dashboard } from '../../services/dashboard';
import {
  AllocationChart,
  AllocationSlice
} from '../../shared/allocation-chart/allocation-chart';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    AllocationChart
  ],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css'
})
export class AdminDashboard implements OnInit {

  private readonly dashboardService = inject(Dashboard);
  private readonly cdr = inject(ChangeDetectorRef); // 1. Inject ChangeDetectorRef

  loading = true;
  stats: DashboardStats | null = null;

  toastMessage = '';
  toastIsError = false;

  ngOnInit(): void {
    this.loadStats();
  }

  loadStats(): void {
    this.loading = true;
    this.hideToast();

    this.dashboardService
      .getStats()
      .pipe(
        finalize(() => {
          this.loading = false;
          this.cdr.detectChanges(); // 2. Ensure loading state clears in UI
        })
      )
      .subscribe({
        next: stats => {
          this.stats = stats;
          this.cdr.detectChanges(); // 3. Render dashboard stats immediately
        },

        error: error => {
          console.error(
            'Failed to load dashboard stats:',
            error
          );

          const message =
            error.error?.message ??
            error.error?.error ??
            'Failed to load dashboard data. Please try again.';

          this.showToast(message, true);
          this.cdr.detectChanges();
        }
      });
  }

  get activeUsersLabel(): string {
    if (!this.stats) {
      return '';
    }

    return `${this.stats.activeUsers} of ${this.stats.totalUsers} active`;
  }

  get activeInvestmentsLabel(): string {
    if (!this.stats) {
      return '';
    }

    return `${this.stats.activeInvestments} of ${this.stats.totalInvestments} active`;
  }

  get isProfit(): boolean {
    return (this.stats?.totalProfit ?? 0) >= 0;
  }

  get returnPercentage(): string {
    if (
      !this.stats ||
      this.stats.totalInvestmentAmount === 0
    ) {
      return '0.0';
    }

    const percentage =
      (
        this.stats.totalProfit /
        this.stats.totalInvestmentAmount
      ) * 100;

    return percentage.toFixed(1);
  }

  get userActivitySlices(): AllocationSlice[] {
    if (!this.stats) {
      return [];
    }

    const inactive = Math.max(
      this.stats.totalUsers - this.stats.activeUsers,
      0
    );

    return [
      { label: 'Active users', value: this.stats.activeUsers },
      { label: 'Inactive users', value: inactive }
    ];
  }

  get investmentActivitySlices(): AllocationSlice[] {
    if (!this.stats) {
      return [];
    }

    const inactive = Math.max(
      this.stats.totalInvestments - this.stats.activeInvestments,
      0
    );

    return [
      { label: 'Active investments', value: this.stats.activeInvestments },
      { label: 'Inactive investments', value: inactive }
    ];
  }

  get currentValueBarWidth(): number {
    if (
      !this.stats ||
      this.stats.totalInvestmentAmount === 0
    ) {
      return 0;
    }

    const ratio =
      (
        this.stats.totalCurrentValue /
        this.stats.totalInvestmentAmount
      ) * 100;

    return Math.max(0, Math.min(ratio, 100));
  }

  private showToast(
    message: string,
    isError: boolean
  ): void {
    this.toastMessage = message;
    this.toastIsError = isError;
    this.cdr.detectChanges(); // 4. Guarantee toast appears instantly

    setTimeout(() => {
      this.hideToast();
    }, 4000);
  }

  private hideToast(): void {
    this.toastMessage = '';
    this.toastIsError = false;
    this.cdr.detectChanges(); // 5. Clear toast UI when hidden
  }
}