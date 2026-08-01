package com.example.InvestmentGoalManagementPlatform.DTO;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class InvestmentPlanResponseDTO {

    private String planType; // "SYSTEM_GENERATED", "USER_SELECTED", or "CUSTOM_ALLOCATION"

    private Double targetAmount;
    private Integer timelineMonths;

    private Double monthlyInvestmentAmount;
    private Double monthlySavingsRequired; // only meaningful for SYSTEM_GENERATED
    private Double expectedMonthlyProfit;
    private Double expectedTotalProfit;
    private Double totalProjectedValue; // only meaningful for CUSTOM_ALLOCATION
    private Boolean goalAchievable;      // only meaningful for CUSTOM_ALLOCATION
    private Boolean aiAssisted;
    private List<AssetAllocationResponseDTO> allocations;

    @Getter
    @Builder
    public static class AssetAllocationResponseDTO {
        private Integer assetId;
        private String assetName;
        private String symbol;
        private Double monthlyAmount;
        private Double expectedAnnualReturnRate;
        private Double expectedMonthlyProfit;
        private String reasoning;
    }
}