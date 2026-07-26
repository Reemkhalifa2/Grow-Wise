package com.example.InvestmentGoalManagementPlatform.DTO;

import com.example.InvestmentGoalManagementPlatform.Entities.InvestmentPlan;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvestmentPlanDTO {

    @NotNull(message = "Target amount is required")
    @Min(value = 0, message = "Target amount cannot be negative")
    private Double targetAmount;

    @NotNull(message = "Duration in months is required")
    @Min(value = 1, message = "Duration must be at least 1 month")
    private Integer durationMonths;

    @NotNull(message = "Monthly saving amount is required")
    @Min(value = 0, message = "Monthly saving amount cannot be negative")
    private Double monthlySavingAmount;

    @NotNull(message = "Monthly investment amount is required")
    @Min(value = 0, message = "Monthly investment amount cannot be negative")
    private Double monthlyInvestmentAmount;

    private Double expectedProfit;

    private String status;

    @NotNull(message = "User ID is required")
    private Integer userId;

    public InvestmentPlan toEntity() {
        InvestmentPlan investmentPlan = new InvestmentPlan();

        investmentPlan.setTargetAmount(targetAmount);
        investmentPlan.setDurationMonths(durationMonths);
        investmentPlan.setMonthlySavingAmount(monthlySavingAmount);
        investmentPlan.setMonthlyInvestmentAmount(monthlyInvestmentAmount);
        investmentPlan.setExpectedProfit(expectedProfit);
        investmentPlan.setStatus(status);

        return investmentPlan;
    }

    public void applyTo(InvestmentPlan investmentPlan) {
        investmentPlan.setTargetAmount(targetAmount);
        investmentPlan.setDurationMonths(durationMonths);
        investmentPlan.setMonthlySavingAmount(monthlySavingAmount);
        investmentPlan.setMonthlyInvestmentAmount(monthlyInvestmentAmount);
        investmentPlan.setExpectedProfit(expectedProfit);
        investmentPlan.setStatus(status);
    }
}

