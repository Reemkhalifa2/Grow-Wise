package com.example.InvestmentGoalManagementPlatform.DTO;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserFinancialSummaryDTO {

    private Integer userId;

    private Double monthlySalary;
    private Double monthlyExpenses;
    private Double netMonthlySavings;

    private Double expenseRatioPercentage;
    private Double savingsRatePercentage;

    private Boolean canInvest;
}