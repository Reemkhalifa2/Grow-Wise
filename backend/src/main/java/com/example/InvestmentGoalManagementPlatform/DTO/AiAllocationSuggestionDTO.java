package com.example.InvestmentGoalManagementPlatform.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AiAllocationSuggestionDTO {

    private Integer goalId;

    private Double monthlyInvestmentAmount;

    // assetId -> percentage
    private Map<Integer, Double> assetAllocations;

    private String explanation;
}