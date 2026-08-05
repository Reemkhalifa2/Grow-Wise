package com.example.InvestmentGoalManagementPlatform.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class PlanAllocation extends BaseEntity {

    private Double allocationPercentage;

    @ManyToOne
    @JoinColumn(
            name = "investment_plan_id",
            nullable = false
    )
    private InvestmentPlan investmentPlan;

    @ManyToOne
    @JoinColumn(
            name = "asset_id",
            nullable = false
    )
    private Asset asset;
}