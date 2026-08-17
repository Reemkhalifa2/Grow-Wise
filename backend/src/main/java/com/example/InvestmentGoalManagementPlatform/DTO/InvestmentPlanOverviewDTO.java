package com.example.InvestmentGoalManagementPlatform.DTO;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

/**
 * Slim, goal-aware summary for list screens (dashboard, investment
 * plans page, portfolio page). See InvestmentPlanService.mapToOverview.
 */
@Getter
@Builder
public class InvestmentPlanOverviewDTO {

    private Integer planId;
    private String status;
    private Double monthlyInvestmentAmount;

    private Double totalInvested;
    private Double currentValue;
    private Double profitLoss;
    private Double returnPercentage;

    private Boolean monthlyInvestmentCompleted;
    private String nextInvestmentMonth;

    private Integer goalId;
    private String goalName;
    private Double goalTargetAmount;
    private Double goalCurrentAmount;
    private LocalDate goalTargetDate;
}
