export interface UserProfile {
  id: number;
  fullName: string;
  email: string;
  monthlySalary: number;
  monthlyExpenses: number;
  role: string;
}

export interface FinancialSummary {
  userId: number;
  monthlySalary: number;
  monthlyExpenses: number;
  netMonthlySavings: number;
  expenseRatioPercentage: number;
  savingsRatePercentage: number;
  canInvest: boolean;
}

export interface FinancialGoal {
  id: number;
  goalName?: string;
  targetAmount?: number;
  currentAmount?: number;
}

export interface DashboardMetrics {
  totalInvestment: number;
  totalPlans: number;
  totalGoals: number;
}