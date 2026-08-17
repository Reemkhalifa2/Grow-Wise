package com.example.InvestmentGoalManagementPlatform.service;

import com.example.InvestmentGoalManagementPlatform.DTO.AiAllocationSuggestionDTO;
import com.example.InvestmentGoalManagementPlatform.DTO.InvestmentDTO;
import com.example.InvestmentGoalManagementPlatform.DTO.InvestmentPlanOverviewDTO;
import com.example.InvestmentGoalManagementPlatform.DTO.InvestmentPlanRequestDTO;
import com.example.InvestmentGoalManagementPlatform.DTO.InvestmentPlanSummaryDTO;
import com.example.InvestmentGoalManagementPlatform.entity.Asset;
import com.example.InvestmentGoalManagementPlatform.entity.FinancialGoal;
import com.example.InvestmentGoalManagementPlatform.entity.InvestmentPlan;
import com.example.InvestmentGoalManagementPlatform.entity.PlanAllocation;
import com.example.InvestmentGoalManagementPlatform.entity.User;
import com.example.InvestmentGoalManagementPlatform.exception.ResourceNotFoundException;
import com.example.InvestmentGoalManagementPlatform.repository.AssetRepository;
import com.example.InvestmentGoalManagementPlatform.repository.FinancialGoalRepository;
import com.example.InvestmentGoalManagementPlatform.repository.InvestmentPlanRepository;
import com.example.InvestmentGoalManagementPlatform.repository.UserRepository;
import com.example.InvestmentGoalManagementPlatform.utility.HelperUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class InvestmentPlanService {
    private final InvestmentService investmentService;
    private final InvestmentPlanRepository investmentPlanRepository;
    private final UserRepository userRepository;
    private final FinancialGoalRepository financialGoalRepository;
    private final AssetRepository assetRepository;
    private final AiService aiService;
    @Autowired
    public InvestmentPlanService(
            InvestmentService investmentService,
            InvestmentPlanRepository investmentPlanRepository,
            UserRepository userRepository,
            FinancialGoalRepository financialGoalRepository,
            AssetRepository assetRepository,
            AiService aiService
    ) {
        this.investmentPlanRepository = investmentPlanRepository;
        this.userRepository = userRepository;
        this.financialGoalRepository = financialGoalRepository;
        this.assetRepository = assetRepository;
        this.aiService = aiService;
        this.investmentService = investmentService;
    }

    @Transactional
    public InvestmentPlanSummaryDTO createPlan(
            InvestmentPlanRequestDTO dto
    ) {

        validateRequest(dto);

        if(HelperUtility.isNull(dto.getUserId())){
            throw new ResourceNotFoundException("User Id not found.");
        }
        User user = userRepository.findByUserId(dto.getUserId());
        if(HelperUtility.isNull(user)){
            throw new ResourceNotFoundException("User not found.");
        }

        if(HelperUtility.isNull(dto.getGoalId())){
            throw new ResourceNotFoundException("Goal Id not found.");
        }
        FinancialGoal goal = financialGoalRepository
                .findById(dto.getGoalId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Financial goal not found with ID: "
                                        + dto.getGoalId()
                        )
                );

        validateGoalOwner(goal, user);

        InvestmentPlan plan = new InvestmentPlan();

        plan.setUser(user);
        plan.setFinancialGoal(goal);
        plan.setMonthlyInvestmentAmount(
                dto.getMonthlyInvestmentAmount()
        );
        plan.setStatus("ACTIVE");


        List<PlanAllocation> allocations =
                createAllocations(
                        dto.getAssetAllocations(),
                        plan
                );

        plan.setAssetAllocations(allocations);

        InvestmentPlan savedPlan =
                investmentPlanRepository.save(plan);

        for (
                PlanAllocation allocation :
                savedPlan.getAssetAllocations()
        ) {
            double monthlyAmount =
                    savedPlan.getMonthlyInvestmentAmount()
                            * allocation.getAllocationPercentage()
                            / 100.0;

            investmentService.createFromPlan(
                    user,
                    savedPlan,
                    allocation.getAsset(),
                    monthlyAmount
            );
        }

        return new InvestmentPlanSummaryDTO(
                savedPlan.getId(),
                savedPlan.getMonthlyInvestmentAmount(),
                savedPlan.getStatus()
        );
    }

    private void validateRequest(
            InvestmentPlanRequestDTO dto
    ) {
        if (dto == null) {
            throw new IllegalArgumentException(
                    "Investment plan request is required"
            );
        }

        if (dto.getUserId() == null) {
            throw new IllegalArgumentException(
                    "User ID is required"
            );
        }

        if (dto.getGoalId() == null) {
            throw new IllegalArgumentException(
                    "Goal ID is required"
            );
        }

        if (
                dto.getMonthlyInvestmentAmount() == null ||
                        dto.getMonthlyInvestmentAmount() <= 0
        ) {
            throw new IllegalArgumentException(
                    "Monthly investment amount must be greater than zero"
            );
        }

        if (
                dto.getAssetAllocations() == null ||
                        dto.getAssetAllocations().isEmpty()
        ) {
            throw new IllegalArgumentException(
                    "At least one asset allocation is required"
            );
        }
    }

    private void validateGoalOwner(
            FinancialGoal goal,
            User user
    ) {
        if (
                goal.getUser() == null ||
                        goal.getUser().getId() == null ||
                        !goal.getUser().getId().equals(user.getId())
        ) {
            throw new IllegalArgumentException(
                    "This financial goal does not belong to the selected user"
            );
        }
    }

    private List<PlanAllocation> createAllocations(
            Map<Integer, Double> requestedAllocations,
            InvestmentPlan plan
    ) {
        List<PlanAllocation> allocations =
                new ArrayList<>();

        double totalPercentage = 0.0;

        for (
                Map.Entry<Integer, Double> entry :
                requestedAllocations.entrySet()
        ) {
            Integer assetId = entry.getKey();
            Double percentage = entry.getValue();

            validateAllocation(
                    assetId,
                    percentage
            );

            Asset asset = assetRepository
                    .findById(assetId)
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Asset not found with ID: "
                                            + assetId
                            )
                    );

            PlanAllocation allocation =
                    new PlanAllocation();

            allocation.setInvestmentPlan(plan);
            allocation.setAsset(asset);
            allocation.setAllocationPercentage(
                    percentage
            );

            allocations.add(allocation);

            totalPercentage += percentage;
        }

        validateTotalPercentage(totalPercentage);

        return allocations;
    }

    private void validateAllocation(
            Integer assetId,
            Double percentage
    ) {
        if (assetId == null) {
            throw new IllegalArgumentException(
                    "Asset ID cannot be null"
            );
        }

        if (percentage == null) {
            throw new IllegalArgumentException(
                    "Allocation percentage is required for asset ID: "
                            + assetId
            );
        }

        if (
                percentage <= 0 ||
                        percentage > 100
        ) {
            throw new IllegalArgumentException(
                    "Allocation percentage for asset ID "
                            + assetId
                            + " must be greater than 0 and at most 100"
            );
        }
    }

    private void validateTotalPercentage(
            double totalPercentage
    ) {
        if (
                Math.abs(totalPercentage - 100.0)
                        > 0.001
        ) {
            throw new IllegalArgumentException(
                    "Asset allocations must total 100%. Current total: "
                            + totalPercentage
            );
        }
    }

    public InvestmentPlan getPlanById(
            Integer planId
    ) {
        return investmentPlanRepository
                .findById(planId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Investment plan not found with ID: "
                                        + planId
                        )
                );
    }

    public List<InvestmentPlan> getPlansByUserId(
            Integer userId
    ) {
        User user = userRepository
                .findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with ID: "
                                        + userId
                        )
                );

        return investmentPlanRepository
                .findByUser(user);
    }

    /**
     * Slim, goal-aware summary for list screens (dashboard, investment
     * plans page, portfolio page). The linked FinancialGoal's name is what
     * actually identifies a plan to the user, so it's surfaced directly
     * here instead of forcing the frontend to join plan + goal itself.
     */
    @Transactional(readOnly = true)
    public List<InvestmentPlanOverviewDTO> getPlanOverviewsByUserId(
            Integer userId
    ) {
        return investmentPlanRepository
                .findByUserIdAndIsActiveTrueOrderByCreatedDateDesc(userId)
                .stream()
                .map(this::mapToOverview)
                .toList();
    }

    private InvestmentPlanOverviewDTO mapToOverview(
            InvestmentPlan plan
    ) {
        List<InvestmentDTO> investments =
                investmentService.getInvestmentsByPlanId(plan.getId());

        double totalInvested = investments.stream()
                .mapToDouble(i -> i.getAmountInvested() == null ? 0.0 : i.getAmountInvested())
                .sum();

        double currentValue = investments.stream()
                .mapToDouble(i -> i.getCurrentValue() == null ? 0.0 : i.getCurrentValue())
                .sum();

        double profitLoss = currentValue - totalInvested;

        Double returnPercentage = totalInvested > 0
                ? (profitLoss / totalInvested) * 100
                : null;

        InvestmentService.MonthlyStatus monthlyStatus =
                investmentService.getMonthlyStatusForPlan(plan);

        FinancialGoal goal = plan.getFinancialGoal();

        return InvestmentPlanOverviewDTO.builder()
                .planId(plan.getId())
                .status(plan.getStatus())
                .monthlyInvestmentAmount(plan.getMonthlyInvestmentAmount())
                .totalInvested(round(totalInvested))
                .currentValue(round(currentValue))
                .profitLoss(round(profitLoss))
                .returnPercentage(returnPercentage == null ? null : round(returnPercentage))
                .monthlyInvestmentCompleted(monthlyStatus.completed())
                .nextInvestmentMonth(monthlyStatus.nextInvestmentMonthLabel())
                .goalId(goal == null ? null : goal.getId())
                .goalName(goal == null ? null : goal.getGoalName())
                .goalTargetAmount(goal == null ? null : goal.getTargetAmount())
                .goalCurrentAmount(goal == null ? null : goal.getCurrentAmount())
                .goalTargetDate(goal == null ? null : goal.getTargetDate())
                .build();
    }

    private double round(double value) {
        return Math.round(value * 1000.0) / 1000.0;
    }

    // Soft delete: deactivates the plan instead of removing the row
    public void deletePlan(
            Integer planId
    ) {
        InvestmentPlan plan = investmentPlanRepository
                .findById(planId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Investment plan not found with ID: "
                                        + planId
                        )
                );

        plan.setIsActive(false);
        investmentPlanRepository.save(plan);

        investmentService.deactivateInvestmentsByPlanId(planId);
    }

    private Map<Integer, Double> parseAiAllocations(
            String aiResponse,
            List<Asset> availableAssets
    ) {
        if (
                aiResponse == null ||
                        aiResponse.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "AI returned an empty response"
            );
        }

        Map<Integer, Double> allocations =
                new HashMap<>();

        List<Integer> validAssetIds =
                availableAssets.stream()
                        .map(Asset::getId)
                        .toList();

        String[] lines =
                aiResponse.split("\\R");

        for (String line : lines) {
            String cleanedLine =
                    line.trim();

            if (
                    cleanedLine.isBlank() ||
                            cleanedLine
                                    .toUpperCase()
                                    .startsWith("EXPLANATION")
            ) {
                continue;
            }

            if (!cleanedLine.contains("=")) {
                continue;
            }

            String[] parts =
                    cleanedLine.split("=", 2);

            if (parts.length != 2) {
                continue;
            }

            try {
                Integer assetId =
                        Integer.valueOf(
                                parts[0].trim()
                        );

                Double percentage =
                        Double.valueOf(
                                parts[1]
                                        .replace("%", "")
                                        .trim()
                        );

                if (!validAssetIds.contains(assetId)) {
                    throw new IllegalArgumentException(
                            "AI returned unknown asset ID: "
                                    + assetId
                    );
                }

                validateAllocation(
                        assetId,
                        percentage
                );

                allocations.put(
                        assetId,
                        percentage
                );

            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException(
                        "AI returned an invalid allocation line: "
                                + cleanedLine
                );
            }
        }

        if (allocations.isEmpty()) {
            throw new IllegalArgumentException(
                    "AI did not return valid asset allocations"
            );
        }

        return allocations;
    }
    public AiAllocationSuggestionDTO suggestAllocation(
            Integer goalId,
            Double monthlyInvestmentAmount
    ) {
        if (
                monthlyInvestmentAmount == null ||
                        monthlyInvestmentAmount <= 0
        ) {
            throw new IllegalArgumentException(
                    "Monthly investment amount must be greater than zero"
            );
        }

        FinancialGoal goal = financialGoalRepository
                .findById(goalId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Financial goal not found with ID: "
                                        + goalId
                        )
                );

        List<Asset> assets = assetRepository.findAll();

        if (assets.isEmpty()) {
            throw new IllegalArgumentException(
                    "No investment assets are available"
            );
        }

        StringBuilder assetDetails =
                new StringBuilder();

        for (Asset asset : assets) {
            assetDetails
                    .append("Asset ID: ")
                    .append(asset.getId())
                    .append(", Name: ")
                    .append(asset.getName())
                    .append(", Type: ")
                    .append(asset.getAssetType())
                    .append(", Risk: ")
                    .append(asset.getRiskLevel())
                    .append(", Current price: ")
                    .append(asset.getCurrentPrice())
                    .append("\n");
        }

        String prompt = """
            You are an investment allocation assistant.

            Create a monthly investment allocation for the following goal.

            Goal name: %s
            Target amount: %s OMR
            Current amount: %s OMR
            Monthly investment amount: %s OMR
            Goal risk level: %s

            Available assets:
            %s

            Rules:
            - Use only the listed asset IDs.
            - Allocation percentages must total exactly 100.
            - Each percentage must be greater than 0.
            - Return JSON only.
            - Do not include markdown.
            - Do not guarantee profit.

            Return this exact structure:
            {
              "assetAllocations": {
                "1": 30,
                "2": 40,
                "3": 30
              },
              "explanation": "Short explanation"
            }
            """.formatted(
                goal.getGoalName(),
                goal.getTargetAmount(),
                goal.getCurrentAmount(),
                monthlyInvestmentAmount,
                goal.getRiskLevel(),
                assetDetails
        );
        AiAllocationSuggestionDTO suggestion =
                aiService.generateAllocationSuggestion(
                        prompt,
                        goalId,
                        monthlyInvestmentAmount
                );

        if (
                suggestion.getAssetAllocations() == null ||
                        suggestion.getAssetAllocations().isEmpty()
        ) {
            throw new IllegalArgumentException(
                    "AI did not return any asset allocations"
            );
        }

        double total = suggestion
                .getAssetAllocations()
                .values()
                .stream()
                .mapToDouble(Double::doubleValue)
                .sum();

        if (Math.abs(total - 100.0) > 0.001) {
            throw new IllegalArgumentException(
                    "AI allocation did not total 100%"
            );
        }

        return suggestion;
    }
}