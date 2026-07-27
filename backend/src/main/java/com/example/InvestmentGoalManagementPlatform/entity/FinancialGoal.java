package com.example.InvestmentGoalManagementPlatform.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FinancialGoal extends BaseEntity {

    private String goalName;
    private Double targetAmount;
    private Double currentAmount;
    private LocalDate targetDate;
    private String status;
    private Double progressPercentage;

    @ManyToOne
    private User user;

}