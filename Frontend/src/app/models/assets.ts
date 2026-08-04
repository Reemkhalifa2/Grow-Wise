
export type AssetType = 'STOCK' | 'GOLD' | 'MUTUAL_FUND';


/**
 * Mirrors AssetAdminResponseDTO returned by GET /api/admin/assets
 */

export interface MarketDiscovery {
  name: string;
  symbol: string;
  assetType: AssetType;
  currentPrice: number;
  source: string;
}