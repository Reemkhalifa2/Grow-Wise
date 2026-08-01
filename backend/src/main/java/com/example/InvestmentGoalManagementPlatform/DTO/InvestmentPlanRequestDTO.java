package com.example.InvestmentGoalManagementPlatform.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class InvestmentPlanRequestDTO {

    private Integer financialGoalId;

    private List<AssetInput> assets;

    @Getter
    @Setter
    public static class AssetInput {
        private Integer assetId;
        private Double monthlyAmount; // null if the user didn't specify an amount for this asset
    }
}