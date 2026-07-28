package com.example.InvestmentGoalManagementPlatform.DTO;

import com.example.InvestmentGoalManagementPlatform.entity.Asset;
import com.example.InvestmentGoalManagementPlatform.utility.AssetType;
import com.example.InvestmentGoalManagementPlatform.utility.RiskLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AssetAdminResponseDTO {
    private Integer id;
    private String name;
    private String symbol;
    private AssetType assetType;
    private RiskLevel riskLevel;
    private Double currentPrice;
    private String scrapingUrl;
    private String cssSelector;
    private Boolean autoUpdate;


    public static AssetAdminResponseDTO fromEntity(Asset asset) {
        if (asset == null) {
            return null;
        }

        return new AssetAdminResponseDTO(
                asset.getId(),
                asset.getName(),
                asset.getSymbol(),
                asset.getAssetType(),
                asset.getRiskLevel(),
                asset.getCurrentPrice(),
                asset.getScrapingUrl(),
                asset.getCssSelector(),
                asset.getAutoUpdate()
        );
    }
}


