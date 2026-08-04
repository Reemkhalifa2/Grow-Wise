package com.example.InvestmentGoalManagementPlatform.controller;

import com.example.InvestmentGoalManagementPlatform.DTO.InvestmentPlanRequestDTO;
import com.example.InvestmentGoalManagementPlatform.DTO.InvestmentPlanResponseDTO;
import com.example.InvestmentGoalManagementPlatform.service.InvestmentPlanService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/investment-plans")
@CrossOrigin(origins = "http://localhost:4200")

public class InvestmentPlanController {

    private final InvestmentPlanService investmentPlanService;

    public InvestmentPlanController(
            InvestmentPlanService investmentPlanService
    ) {
        this.investmentPlanService = investmentPlanService;
    }

    /*
     * Generate and save a new investment plan
     *
     * POST /api/investment-plans
     */
    @PostMapping
    public ResponseEntity<InvestmentPlanResponseDTO> createPlan(
            @Valid @RequestBody InvestmentPlanRequestDTO request
    ) {
        InvestmentPlanResponseDTO response =
                investmentPlanService.generateInvestmentPlan(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /*
     * Get one investment plan belonging to a user
     *
     * GET /api/investment-plans/{planId}/user/{userId}
     */
    @GetMapping("/{planId}/user/{userId}")
    public ResponseEntity<InvestmentPlanResponseDTO> getPlanById(
            @PathVariable Integer planId,
            @PathVariable Integer userId
    ) {
        InvestmentPlanResponseDTO response =
                investmentPlanService.getPlanById(
                        planId,
                        userId
                );

        return ResponseEntity.ok(response);
    }

    /*
     * Get all active investment plans for a user
     *
     * GET /api/investment-plans/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<InvestmentPlanResponseDTO>>
    getAllPlansForUser(
            @PathVariable Integer userId
    ) {
        List<InvestmentPlanResponseDTO> plans =
                investmentPlanService
                        .getAllPlansForUser(userId);

        return ResponseEntity.ok(plans);
    }

    /*
     * Recalculate and update an existing plan
     *
     * PUT /api/investment-plans/{planId}/user/{userId}
     */
    @PutMapping("/{planId}/user/{userId}")
    public ResponseEntity<InvestmentPlanResponseDTO> updatePlan(
            @PathVariable Integer planId,
            @PathVariable Integer userId,
            @Valid @RequestBody InvestmentPlanRequestDTO request
    ) {
        InvestmentPlanResponseDTO updatedPlan =
                investmentPlanService.updatePlan(
                        planId,
                        userId,
                        request
                );

        return ResponseEntity.ok(updatedPlan);
    }

    /*
     * Soft-delete an investment plan
     *
     * DELETE /api/investment-plans/{planId}/user/{userId}
     */
    @DeleteMapping("/{planId}/user/{userId}")
    public ResponseEntity<Void> deletePlan(
            @PathVariable Integer planId,
            @PathVariable Integer userId
    ) {
        investmentPlanService.deletePlan(
                planId,
                userId
        );

        return ResponseEntity.noContent().build();
    }
}