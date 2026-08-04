package com.example.InvestmentGoalManagementPlatform.controller;

import com.example.InvestmentGoalManagementPlatform.DTO.InvestmentDTO;
import com.example.InvestmentGoalManagementPlatform.service.InvestmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/investments")
@CrossOrigin(origins = "http://localhost:4200")

public class InvestmentController {

    private final InvestmentService investmentService;

    public InvestmentController(
            InvestmentService investmentService
    ) {
        this.investmentService = investmentService;
    }

    /*
     * Create a new investment
     *
     * POST /api/investments
     */
    @PostMapping
    public ResponseEntity<InvestmentDTO> createInvestment(
            @Valid @RequestBody InvestmentDTO dto
    ) {
        InvestmentDTO createdInvestment =
                investmentService.createInvestment(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdInvestment);
    }

    /*
     * Get one active investment by ID
     *
     * GET /api/investments/{investmentId}
     */
    @GetMapping("/{investmentId}")
    public ResponseEntity<InvestmentDTO> getInvestmentById(
            @PathVariable Integer investmentId
    ) {
        return ResponseEntity.ok(
                investmentService.getInvestmentById(
                        investmentId
                )
        );
    }

    /*
     * Get all active investments for a user
     *
     * GET /api/investments/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<InvestmentDTO>>
    getInvestmentsByUserId(
            @PathVariable Integer userId
    ) {
        return ResponseEntity.ok(
                investmentService.getInvestmentsByUserId(
                        userId
                )
        );
    }

    /*
     * Get all active investments for a plan
     *
     * GET /api/investments/plan/{planId}
     */
    @GetMapping("/plan/{planId}")
    public ResponseEntity<List<InvestmentDTO>>
    getInvestmentsByPlanId(
            @PathVariable Integer planId
    ) {
        return ResponseEntity.ok(
                investmentService.getInvestmentsByPlanId(
                        planId
                )
        );
    }

    /*
     * Get all active investments for an asset
     *
     * GET /api/investments/asset/{assetId}
     */
    @GetMapping("/asset/{assetId}")
    public ResponseEntity<List<InvestmentDTO>>
    getInvestmentsByAssetId(
            @PathVariable Integer assetId
    ) {
        return ResponseEntity.ok(
                investmentService.getInvestmentsByAssetId(
                        assetId
                )
        );
    }

    /*
     * Get investments for a specific user and plan
     *
     * GET /api/investments/user/{userId}/plan/{planId}
     */
    @GetMapping("/user/{userId}/plan/{planId}")
    public ResponseEntity<List<InvestmentDTO>>
    getInvestmentsByUserIdAndPlanId(
            @PathVariable Integer userId,
            @PathVariable Integer planId
    ) {
        return ResponseEntity.ok(
                investmentService
                        .getInvestmentsByUserIdAndPlanId(
                                userId,
                                planId
                        )
        );
    }

    /*
     * Update an investment
     *
     * PUT /api/investments/{investmentId}
     */
    @PutMapping("/{investmentId}")
    public ResponseEntity<InvestmentDTO> updateInvestment(
            @PathVariable Integer investmentId,
            @Valid @RequestBody InvestmentDTO dto
    ) {
        return ResponseEntity.ok(
                investmentService.updateInvestment(
                        investmentId,
                        dto
                )
        );
    }

    /*
     * Soft-delete an investment
     *
     * DELETE /api/investments/{investmentId}
     */
    @DeleteMapping("/{investmentId}")
    public ResponseEntity<Void> deleteInvestment(
            @PathVariable Integer investmentId
    ) {
        investmentService.deleteInvestment(
                investmentId
        );

        return ResponseEntity.noContent().build();
    }
}