package com.example.InvestmentGoalManagementPlatform.DTO;

import com.example.InvestmentGoalManagementPlatform.entities.FinancialGoal;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FinancialGoalDTO {

    @NotBlank(message = "Goal name is required")
    private String goalName;

    @NotNull(message = "Target amount is required")
    @Min(value = 0, message = "Target amount cannot be negative")
    private Double targetAmount;

    @Min(value = 0, message = "Current amount cannot be negative")
    private Double currentAmount;

    @NotNull(message = "Target date is required")
    @Future(message = "Target date must be in the future")
    private LocalDate targetDate;

    private String status;

    @NotNull(message = "User ID is required")
    private Integer userId;

    public FinancialGoal toEntity() {
        FinancialGoal financialGoal = new FinancialGoal();

        financialGoal.setGoalName(goalName);
        financialGoal.setTargetAmount(targetAmount);
        financialGoal.setCurrentAmount(currentAmount);
        financialGoal.setTargetDate(targetDate);
        financialGoal.setStatus(status);

        return financialGoal;
    }

    public void applyTo(FinancialGoal financialGoal) {
        financialGoal.setGoalName(goalName);
        financialGoal.setTargetAmount(targetAmount);
        financialGoal.setCurrentAmount(currentAmount);
        financialGoal.setTargetDate(targetDate);
        financialGoal.setStatus(status);
    }

}
