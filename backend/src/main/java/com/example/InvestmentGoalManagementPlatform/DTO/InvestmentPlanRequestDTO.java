package com.example.InvestmentGoalManagementPlatform.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class InvestmentPlanRequestDTO {

    private Integer userId;

    private Integer goalId;

    private Double monthlyInvestmentAmount;

    // Key = asset ID
    // Value = allocation percentage
    private Map<Integer, Double> assetAllocations;
}