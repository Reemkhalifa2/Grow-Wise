package com.example.InvestmentGoalManagementPlatform.controller;

import com.example.InvestmentGoalManagementPlatform.DTO.InvestmentPlanRequestDTO;
import com.example.InvestmentGoalManagementPlatform.DTO.InvestmentPlanResponseDTO;
import com.example.InvestmentGoalManagementPlatform.service.InvestmentPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/investment-plans")
public class InvestmentPlanController {

    @Autowired
    public InvestmentPlanController(InvestmentPlanService investmentPlanService) {
        this.investmentPlanService = investmentPlanService;
    }

    private final InvestmentPlanService investmentPlanService;

    @PostMapping
    public ResponseEntity<InvestmentPlanResponseDTO> createInvestmentPlan(
            @RequestBody InvestmentPlanRequestDTO request) {
        InvestmentPlanResponseDTO response = investmentPlanService.createInvestmentPlan(request);
        return ResponseEntity.ok(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleBadRequest(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleConflict(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }
}