package com.example.InvestmentGoalManagementPlatform.DTO;

import com.example.InvestmentGoalManagementPlatform.entity.Asset;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AvailableAssetDTO {

    private Integer id;
    private String name;
    private String symbol;
    private String assetType;
    private String riskLevel;
    private Double currentPrice;

    public static AvailableAssetDTO fromEntity(
            Asset asset
    ) {
        AvailableAssetDTO dto =
                new AvailableAssetDTO();

        dto.setId(asset.getId());
        dto.setName(asset.getName());
        dto.setSymbol(asset.getSymbol());

        dto.setAssetType(
                asset.getAssetType() == null
                        ? null
                        : asset.getAssetType().toString()
        );

        dto.setRiskLevel(
                asset.getRiskLevel() == null
                        ? null
                        : asset.getRiskLevel().toString()
        );

        dto.setCurrentPrice(
                asset.getCurrentPrice()
        );

        return dto;
    }
}