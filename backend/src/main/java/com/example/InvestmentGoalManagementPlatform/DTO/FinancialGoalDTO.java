package com.example.InvestmentGoalManagementPlatform.DTO;

import com.example.InvestmentGoalManagementPlatform.entity.FinancialGoal;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FinancialGoalDTO {

    private Integer id; // response-only, ignored on create/update requests

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

    private Double progressPercentage; // response-only

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

    public static FinancialGoalDTO fromEntity(FinancialGoal financialGoal) {
        FinancialGoalDTO dto = new FinancialGoalDTO();

        dto.setId(financialGoal.getId());
        dto.setGoalName(financialGoal.getGoalName());
        dto.setTargetAmount(financialGoal.getTargetAmount());
        dto.setCurrentAmount(financialGoal.getCurrentAmount());
        dto.setTargetDate(financialGoal.getTargetDate());
        dto.setStatus(financialGoal.getStatus());
        dto.setUserId(financialGoal.getUser().getId());

        if (financialGoal.getTargetAmount() != null && financialGoal.getTargetAmount() > 0
                && financialGoal.getCurrentAmount() != null) {
            double progress = (financialGoal.getCurrentAmount() / financialGoal.getTargetAmount()) * 100;
            dto.setProgressPercentage(Math.min(progress, 100.0));
        } else {
            dto.setProgressPercentage(0.0);
        }

        return dto;
    }

    public static List<FinancialGoalDTO> fromEntity(List<FinancialGoal> financialGoals) {
        List<FinancialGoalDTO> financialGoalDTOList = new ArrayList<>();
        for (FinancialGoal financialGoal : financialGoals) {
            financialGoalDTOList.add(fromEntity(financialGoal));
        }
        return financialGoalDTOList;
    }
}