export interface InvestmentResponse {
  id: number;
  amountInvested: number;
  quantity: number;
  purchasePrice: number;
  purchaseDate: string;

  userId: number;
  planId: number;

  assetId: number;
  assetName: string;
  assetSymbol: string;
  assetType: string;
  riskLevel: string;

  currentValue: number;
  profitOrLoss: number;
  returnPercentage: number;
  unitsPurchased: number;
  currentPrice: number;

  monthlyInvestmentCompleted: boolean;
  nextInvestmentMonth: string;
}

export interface PortfolioAssetSummary {
  assetId: number;
  assetName: string;
  assetSymbol: string;
  assetType: string;
  riskLevel: string;

  totalInvested: number;
  currentValue: number;
  profitOrLoss: number;

  monthsInvested: number;
  lastInvestmentDate: string;
}

export interface PortfolioPlan {
  planId: number;
  totalInvested: number;
  currentValue: number;
  profitOrLoss: number;
  assets: PortfolioAssetSummary[];

  monthlyInvestmentCompleted: boolean;
  nextInvestmentMonth: string;
}

/**
 * Mirrors InvestmentPlanOverviewDTO — a goal-aware plan summary. The
 * linked goal's name is what identifies the plan to the user; planId is
 * secondary.
 */
export interface InvestmentPlanOverview {
  planId: number;
  status: string;
  monthlyInvestmentAmount: number;

  totalInvested: number;
  currentValue: number;
  profitLoss: number;
  returnPercentage: number | null;

  monthlyInvestmentCompleted: boolean;
  nextInvestmentMonth: string;

  goalId: number | null;
  goalName: string | null;
  goalTargetAmount: number | null;
  goalCurrentAmount: number | null;
  goalTargetDate: string | null;
}

export type GoalStatus = 'ON_TRACK' | 'NEEDS_ATTENTION' | 'BEHIND';