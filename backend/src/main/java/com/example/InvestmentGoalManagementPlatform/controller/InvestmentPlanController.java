package com.example.InvestmentGoalManagementPlatform.controller;

import com.example.InvestmentGoalManagementPlatform.DTO.AiAllocationSuggestionDTO;
import com.example.InvestmentGoalManagementPlatform.DTO.InvestmentPlanRequestDTO;
import com.example.InvestmentGoalManagementPlatform.entity.InvestmentPlan;
import com.example.InvestmentGoalManagementPlatform.service.InvestmentPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/investment-plans")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class InvestmentPlanController {

    private final InvestmentPlanService investmentPlanService;

    @PostMapping
    public ResponseEntity<InvestmentPlan> createPlan(
            @RequestBody InvestmentPlanRequestDTO request
    ) {
        InvestmentPlan createdPlan =
                investmentPlanService.createPlan(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdPlan);
    }

    @PostMapping("/ai-suggestion")
    public ResponseEntity<AiAllocationSuggestionDTO> suggestWithAi(
            @RequestParam Integer goalId,
            @RequestParam Double monthlyInvestmentAmount
    ) {
        return ResponseEntity.ok(
                investmentPlanService.suggestAllocation(
                        goalId,
                        monthlyInvestmentAmount
                )
        );
    } 
}