/**
 * Mirrors AdminDashboardResponseDTO returned by
 * GET /api/admin/dashboard on the backend.
 */
export interface DashboardStats {
  totalUsers: number;
  activeUsers: number;

  totalInvestments: number;
  activeInvestments: number;

  totalInvestmentAmount: number;
  totalCurrentValue: number;
  totalProfit: number;
}