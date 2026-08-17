package com.example.InvestmentGoalManagementPlatform.controller;

import com.example.InvestmentGoalManagementPlatform.DTO.AiAllocationSuggestionDTO;
import com.example.InvestmentGoalManagementPlatform.DTO.InvestmentPlanOverviewDTO;
import com.example.InvestmentGoalManagementPlatform.DTO.InvestmentPlanRequestDTO;
import com.example.InvestmentGoalManagementPlatform.DTO.InvestmentPlanSummaryDTO;
import com.example.InvestmentGoalManagementPlatform.service.InvestmentPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/investment-plans")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class InvestmentPlanController {

    private final InvestmentPlanService investmentPlanService;

    @PostMapping
    public ResponseEntity<InvestmentPlanSummaryDTO> createPlan(
            @RequestBody InvestmentPlanRequestDTO request
    ) {
        InvestmentPlanSummaryDTO createdPlan =
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

    @DeleteMapping("/{planId}")
    public ResponseEntity<Void> deletePlan(
            @PathVariable Integer planId
    ) {
        investmentPlanService.deletePlan(planId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<InvestmentPlanOverviewDTO>> getPlanOverviewsByUserId(
            @PathVariable Integer userId
    ) {
        return ResponseEntity.ok(
                investmentPlanService.getPlanOverviewsByUserId(userId)
        );
    }
}