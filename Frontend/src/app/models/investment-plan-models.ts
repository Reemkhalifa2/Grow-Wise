export interface AvailableAsset {
  id: number;
  name: string;
  symbol: string;
  assetType: string;
  riskLevel: string;
  currentPrice: number;
}

export interface FinancialGoalView {
  id: number;
  goalName: string;
  targetAmount: number;
  currentAmount: number | null;
  targetDate: string;
  riskLevel: string;
  status: string;
}

export interface AssetAllocationView {
  assetId: number;
  assetName: string;
  symbol: string;
  assetType: string;
  riskLevel: string;
  currentPrice: number;
  percentage: number;
}

export interface AiAllocationSuggestion {
  goalId: number;
  monthlyInvestmentAmount: number;
  assetAllocations: Record<number, number>;
  explanation: string;
}

export interface InvestmentPlanRequest {
  userId: number;
  goalId: number;
  monthlyInvestmentAmount: number;
  assetAllocations: Record<number, number>;
}

export interface InvestmentPlanResponse {
  id: number;
  monthlyInvestmentAmount: number;
  status: string;
}