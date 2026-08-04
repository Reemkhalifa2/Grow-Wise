export type GoalStatus =
  | 'ACTIVE'
  | 'COMPLETED'
  | 'CANCELLED';

export type GoalRiskLevel =
  | 'LOW'
  | 'MEDIUM'
  | 'HIGH';

export interface investmentGoalRequest {
  goalName: string;
  targetAmount: number;
  currentAmount: number;
  targetDate: string;
  status: GoalStatus;
  riskLevel: GoalRiskLevel;
  userId: number;
}

export interface investmentGoalResponse {
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