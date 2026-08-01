package com.example.InvestmentGoalManagementPlatform.DTO;

import com.example.InvestmentGoalManagementPlatform.entity.Investment;
import com.example.InvestmentGoalManagementPlatform.entity.InvestmentPlan;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class InvestmentPlanResponseDTO {

    private String planType;

    private Double targetAmount;
    private Integer timelineMonths;

    private Double monthlyInvestmentAmount;
    private Double monthlySavingsRequired;

    private Double expectedMonthlyProfit;
    private Double expectedTotalProfit;

    private Double totalProjectedValue;

    private Boolean goalAchievable;
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

    public static InvestmentPlanResponseDTO mapToResponseDTO(
            InvestmentPlan plan
    ) {

        List<AssetAllocationResponseDTO> allocations =
                plan.getInvestment()
                        .stream()
                        .map(InvestmentPlanResponseDTO::mapAllocation)
                        .toList();

        return InvestmentPlanResponseDTO.builder()
                .planType("SAVED_PLAN")
                .targetAmount(plan.getTargetAmount())
                .timelineMonths(plan.getDurationMonths())
                .monthlyInvestmentAmount(plan.getMonthlyInvestmentAmount())
                .monthlySavingsRequired(plan.getMonthlySavingAmount())
                .expectedMonthlyProfit(
                        plan.getDurationMonths() == null ||
                                plan.getDurationMonths() == 0
                                ? 0.0
                                : plan.getExpectedProfit() / plan.getDurationMonths()
                )
                .expectedTotalProfit(plan.getExpectedProfit())
                .totalProjectedValue(
                        (plan.getMonthlyInvestmentAmount() * plan.getDurationMonths())
                                + plan.getExpectedProfit()
                )
                .goalAchievable(
                        "ACTIVE".equalsIgnoreCase(plan.getStatus())
                )
                .aiAssisted(true)
                .allocations(allocations)
                .build();
    }

    private static AssetAllocationResponseDTO mapAllocation(
            Investment investment
    ) {

        return AssetAllocationResponseDTO.builder()
                .assetId(investment.getAsset().getId())
                .assetName(investment.getAsset().getName())
                .symbol(investment.getAsset().getSymbol())
                .monthlyAmount(investment.getAmountInvested())
                .expectedAnnualReturnRate(0.0)
                .expectedMonthlyProfit(0.0)
                .reasoning("Saved allocation")
                .build();
    }
}