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
}