package com.example.InvestmentGoalManagementPlatform.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvestmentPlanSummaryDTO {
    private Integer id;
    private Double monthlyInvestmentAmount;
    private String status;
}
