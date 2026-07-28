package com.example.InvestmentGoalManagementPlatform.DTO;

import com.example.InvestmentGoalManagementPlatform.utility.AssetType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketDiscoveryDTO {
    private String symbol;
    private String name;
    private AssetType assetType; // STOCK, GOLD, MUTUAL_FUND
    private Double currentPrice;
    private String currency;
    private String scrapingUrl;
    private String cssSelector;
    private String sourceType;   // MSX, BANK_MUSCAT, GOLD_LIVE
    private Boolean alreadyInCatalog;
}
