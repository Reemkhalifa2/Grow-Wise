package com.example.InvestmentGoalManagementPlatform.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class InvestmentPlan extends BaseEntity {

    private Double targetAmount;
    private Integer durationMonths;
    private Double monthlySavingAmount;
    private Double monthlyInvestmentAmount;
    private Double expectedProfit;
    private String status;
    private String planType;

    private Double expectedMonthlyProfit;

    private Double totalProjectedValue;

    private Boolean goalAchievable;

    private Boolean aiAssisted;

    @ManyToOne
    private User user;
    @OneToMany
    private List<Investment> investment;


}