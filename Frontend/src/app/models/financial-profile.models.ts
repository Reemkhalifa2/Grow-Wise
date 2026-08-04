export interface FinancialProfileRequest {
  monthlySalary: number;
  monthlyExpenses: number;
}

export interface FinancialProfileResponse
  extends FinancialProfileRequest {
  id: number;
  userId: number;
}