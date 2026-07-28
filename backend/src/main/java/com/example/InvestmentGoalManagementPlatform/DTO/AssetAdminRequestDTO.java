package com.example.InvestmentGoalManagementPlatform.DTO;

import com.example.InvestmentGoalManagementPlatform.utility.AssetType;
import com.example.InvestmentGoalManagementPlatform.utility.RiskLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssetAdminRequestDTO {

    @NotBlank(message = "Asset name is required")
    private String name;

    @NotBlank(message = "Symbol is required")
    private String symbol;

    @NotNull(message = "Asset type is required")
    private AssetType assetType; // STOCK, GOLD, MUTUAL_FUND

    @NotNull(message = "Risk level is required")
    private RiskLevel riskLevel; // LOW, MEDIUM, HIGH

    private Double currentPrice = 0.0;

    // Web Scraping Configuration
    @NotBlank(message = "Url is required")
    private String scrapingUrl;
    @NotBlank(message = "cssSelector is required")
    private String cssSelector;
    private Boolean autoUpdate = true;
}