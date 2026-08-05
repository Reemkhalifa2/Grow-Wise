export type RiskLevel = 'LOW' | 'MEDIUM' | 'HIGH';

export interface MarketDiscovery {
  name: string;
  symbol: string;
  assetType: string;
  currentPrice: number;

  oneYearReturn?: number;
  threeYearReturn?: number;
  fiveYearReturn?: number;

  source?: string;
  asOfDate?: string;
}

export interface Asset {
  id: number;
  name: string;
  symbol: string;
  assetType: string;
  currentPrice: number;
  riskLevel: RiskLevel;
  active?: boolean;
}