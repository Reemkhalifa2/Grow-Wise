
export type AssetType = 'STOCK' | 'GOLD' | 'MUTUAL_FUND';
export type RiskLevel = 'LOW' | 'MEDIUM' | 'HIGH';

export const ASSET_TYPES: AssetType[] = ['STOCK', 'GOLD', 'MUTUAL_FUND'];
export const RISK_LEVELS: RiskLevel[] = ['LOW', 'MEDIUM', 'HIGH'];

/**
 * Mirrors AssetAdminResponseDTO returned by GET /api/admin/assets
 */
export interface Asset {
  id: number;
  name: string;
  symbol: string;
  assetType: AssetType;
  riskLevel: RiskLevel;
  currentPrice: number;
  scrapingUrl: string;
  cssSelector: string;
  autoUpdate: boolean;
}

/**
 * Mirrors AssetAdminRequestDTO expected by POST /api/admin/assets
 * (and proposed PUT /api/admin/assets/{id})
 */
export interface AssetRequest {
  name: string;
  symbol: string;
  assetType: AssetType;
  riskLevel: RiskLevel;
  currentPrice: number;
  scrapingUrl: string;
  cssSelector: string;
  autoUpdate: boolean;
}