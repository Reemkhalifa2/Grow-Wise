export interface UserProfile {
  userId: number;
  fullName: string;
  email: string;
  monthlySalary: number;
  monthlyExpenses: number;
  savingCapacity: number;
  role: string;
}

export interface FinancialSummary {
  totalInvested: number;
  portfolioValue: number;
  totalProfit: number;
  monthlyCapacity: number;
}

export interface FinancialGoal {
  goalName: string;
  currentAmount: number;
  targetAmount: number;
  progressPercentage: number;
  status: string;
  targetDate: string;
}