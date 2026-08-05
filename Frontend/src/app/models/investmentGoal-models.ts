export type GoalStatus =
  | 'ACTIVE'
  | 'ACHIEVED'
  | 'CANCELLED';

export type GoalRiskLevel =
  | 'LOW'
  | 'MEDIUM'
  | 'HIGH';

export interface InvestmentGoalRequest {
  goalName: string;
  targetAmount: number;
  currentAmount: number;
  targetDate: string;
  status: GoalStatus;
  riskLevel: GoalRiskLevel;
  userId: number;
}

export interface InvestmentGoalResponse {
  id: number;
  goalName: string;
  targetAmount: number;
  currentAmount: number;
  targetDate: string;
  status: GoalStatus;
  riskLevel: GoalRiskLevel;
  userId: number;
  progressPercentage?: number;
}