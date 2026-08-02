package com.example.InvestmentGoalManagementPlatform.DTO;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminDashboardResponseDTO {

    private Long totalUsers;
    private Long activeUsers;

    private Long totalInvestments;
    private Long activeInvestments;

    private Double totalInvestmentAmount;
    private Double totalCurrentValue;
    private Double totalProfit;
}