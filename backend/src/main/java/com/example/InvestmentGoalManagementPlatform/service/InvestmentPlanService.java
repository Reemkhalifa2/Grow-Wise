package com.example.InvestmentGoalManagementPlatform.service;

import com.example.InvestmentGoalManagementPlatform.DTO.InvestmentPlanRequestDTO;
import com.example.InvestmentGoalManagementPlatform.DTO.InvestmentPlanResponseDTO;
import com.example.InvestmentGoalManagementPlatform.entity.*;
import com.example.InvestmentGoalManagementPlatform.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class InvestmentPlanService {

    private static final Logger log =
            LoggerFactory.getLogger(InvestmentPlanService.class);

    private static final double DEFAULT_ANNUAL_RETURN = 0.05;
    private static final double MAX_REASONABLE_ANNUAL_RETURN = 0.50;
    private static final double MIN_REASONABLE_ANNUAL_RETURN = -0.50;
    private static final int MINIMUM_DIVERSIFIED_ASSETS = 2;

    private final FinancialGoalRepository financialGoalRepository;
    private final AssetRepository assetRepository;
    private final StockPriceHistoryRepository stockPriceHistoryRepository;
    private final ChatModel chatModel;
    private final ObjectMapper objectMapper;
    private final InvestmentPlanRepository investmentPlanRepository;
    private final InvestmentRepository investmentRepository;
    @Autowired
    public InvestmentPlanService(
            FinancialGoalRepository financialGoalRepository,
            AssetRepository assetRepository,
            StockPriceHistoryRepository stockPriceHistoryRepository,
            InvestmentPlanRepository investmentPlanRepository,
            InvestmentRepository investmentRepository,
            ChatModel chatModel,
            ObjectMapper objectMapper
    ) {
        this.financialGoalRepository = financialGoalRepository;
        this.assetRepository = assetRepository;
        this.stockPriceHistoryRepository = stockPriceHistoryRepository;
        this.investmentPlanRepository = investmentPlanRepository;
        this.investmentRepository = investmentRepository;
        this.chatModel = chatModel;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void deletePlan(
            Integer planId,
            Integer userId
    ) {

        InvestmentPlan plan =
                investmentPlanRepository
                        .findByIdAndUserIdAndIsActiveTrue(
                                planId,
                                userId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Investment plan not found: " + planId
                                )
                        );

        plan.setIsActive(false);

        if (plan.getInvestment() != null) {
            plan.getInvestment()
                    .forEach(investment ->
                            investment.setIsActive(false)
                    );
        }

        investmentPlanRepository.save(plan);
    }

    @Transactional
    public InvestmentPlanResponseDTO updatePlan(
            Integer planId,
            Integer userId,
            InvestmentPlanRequestDTO request
    ) {

        InvestmentPlan existingPlan =
                investmentPlanRepository
                        .findByIdAndUserIdAndIsActiveTrue(
                                planId,
                                userId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Investment plan not found: " + planId
                                )
                        );

        InvestmentPlanResponseDTO recalculatedPlan =
                generateInvestmentPlan(request);

        existingPlan.setTargetAmount(
                recalculatedPlan.getTargetAmount()
        );

        existingPlan.setDurationMonths(
                recalculatedPlan.getTimelineMonths()
        );

        existingPlan.setMonthlySavingAmount(
                recalculatedPlan.getMonthlySavingsRequired()
        );

        existingPlan.setMonthlyInvestmentAmount(
                recalculatedPlan.getMonthlyInvestmentAmount()
        );

        existingPlan.setExpectedProfit(
                recalculatedPlan.getExpectedTotalProfit()
        );

        existingPlan.setStatus(
                Boolean.TRUE.equals(
                        recalculatedPlan.getGoalAchievable()
                )
                        ? "ACHIEVABLE"
                        : "NOT_ACHIEVABLE"
        );

        InvestmentPlan updatedPlan =
                investmentPlanRepository.save(existingPlan);

        return InvestmentPlanResponseDTO
                .mapToResponseDTO(updatedPlan);
    }
    @Transactional(readOnly = true)
    public List<InvestmentPlanResponseDTO> getAllPlansForUser(
            Integer userId
    ) {
        return investmentPlanRepository
                .findByUserIdAndIsActiveTrueOrderByCreatedDateDesc(userId)
                .stream()
                .map(InvestmentPlanResponseDTO::mapToResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public InvestmentPlanResponseDTO getPlanById(
            Integer planId,
            Integer userId
    ) {
        InvestmentPlan plan =
                investmentPlanRepository
                        .findByIdAndUserIdAndIsActiveTrue(
                                planId,
                                userId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Investment plan not found: " + planId
                                )
                        );

        return InvestmentPlanResponseDTO.mapToResponseDTO(plan);
    }
    @Transactional(readOnly = true)
    public InvestmentPlanResponseDTO generateInvestmentPlan(
            InvestmentPlanRequestDTO request
    ) {
        validateRequest(request);

        FinancialGoal goal = getFinancialGoal(
                request.getFinancialGoalId()
        );

        User user = getGoalUser(goal);

        double salary = safeNumber(user.getMonthlySalary());
        double expenses = safeNumber(user.getMonthlyExpenses());

        double netMonthlySavingsCapacity =
                Math.max(0, salary - expenses);

        int timelineMonths = calculateTimelineMonths(
                goal.getTargetDate()
        );

        double remainingTargetAmount =
                calculateRemainingTargetAmount(goal);

        List<Asset> candidateAssets =
                getCandidateAssets(request);

        Map<Integer, Double> annualReturnByAsset =
                calculateAnnualReturns(candidateAssets);

        List<AIAssetRecommendation> recommendations =
                askOllamaForAllocation(
                        goal,
                        salary,
                        expenses,
                        netMonthlySavingsCapacity,
                        timelineMonths,
                        remainingTargetAmount,
                        candidateAssets,
                        annualReturnByAsset
                );

        List<AIAssetRecommendation> validRecommendations =
                validateAndNormalizeRecommendations(
                        recommendations,
                        candidateAssets
                );

        double blendedAnnualReturn =
                calculateBlendedAnnualReturn(
                        validRecommendations,
                        annualReturnByAsset
                );

        double requiredMonthlyInvestment =
                calculateRequiredMonthlyInvestment(
                        remainingTargetAmount,
                        blendedAnnualReturn,
                        timelineMonths
                );

        boolean goalAchievable =
                netMonthlySavingsCapacity >= requiredMonthlyInvestment;

        AllocationCalculation allocationCalculation =
                buildAllocations(
                        validRecommendations,
                        candidateAssets,
                        annualReturnByAsset,
                        requiredMonthlyInvestment,
                        timelineMonths
                );

        double totalProjectedValue =
                allocationCalculation.totalProjectedValue();

        double expectedTotalProfit =
                allocationCalculation.expectedTotalProfit();

        double averageMonthlyProfit =
                timelineMonths > 0
                        ? expectedTotalProfit / timelineMonths
                        : 0;

        return InvestmentPlanResponseDTO.builder()
                .planType("AI_GENERATED")
                .targetAmount(goal.getTargetAmount())
                .timelineMonths(timelineMonths)
                .monthlyInvestmentAmount(
                        round(requiredMonthlyInvestment)
                )
                .monthlySavingsRequired(
                        round(netMonthlySavingsCapacity)
                )
                .expectedMonthlyProfit(
                        round(averageMonthlyProfit)
                )
                .expectedTotalProfit(
                        round(expectedTotalProfit)
                )
                .totalProjectedValue(
                        round(totalProjectedValue)
                )
                .goalAchievable(goalAchievable)
                .aiAssisted(true)
                .allocations(
                        allocationCalculation.allocations()
                )
                .build();
    }

    private void validateRequest(
            InvestmentPlanRequestDTO request
    ) {
        if (request == null) {
            throw new IllegalArgumentException(
                    "Investment plan request is required"
            );
        }

        if (request.getFinancialGoalId() == null) {
            throw new IllegalArgumentException(
                    "Financial goal id is required"
            );
        }
    }

    private FinancialGoal getFinancialGoal(
            Integer financialGoalId
    ) {
        return financialGoalRepository
                .findById(financialGoalId)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Financial goal not found: "
                                        + financialGoalId
                        )
                );
    }

    private User getGoalUser(
            FinancialGoal goal
    ) {
        User user = goal.getUser();

        if (user == null) {
            throw new IllegalStateException(
                    "Financial goal is not associated with a user"
            );
        }

        return user;
    }

    private int calculateTimelineMonths(
            LocalDate targetDate
    ) {
        if (targetDate == null) {
            throw new IllegalArgumentException(
                    "Financial goal target date is required"
            );
        }

        long months = ChronoUnit.MONTHS.between(
                LocalDate.now(),
                targetDate
        );

        return (int) Math.max(1, months);
    }

    private double calculateRemainingTargetAmount(
            FinancialGoal goal
    ) {
        double targetAmount =
                safeNumber(goal.getTargetAmount());

        double currentAmount =
                safeNumber(goal.getCurrentAmount());

        if (targetAmount <= 0) {
            throw new IllegalArgumentException(
                    "Goal target amount must be greater than zero"
            );
        }

        return Math.max(
                0,
                targetAmount - currentAmount
        );
    }

    private List<Asset> getCandidateAssets(
            InvestmentPlanRequestDTO request
    ) {
        List<Asset> activeAssets =
                assetRepository.findAllByIsActiveTrue();

        if (activeAssets == null || activeAssets.isEmpty()) {
            throw new IllegalStateException(
                    "No active assets are available"
            );
        }

        /*
         * If the user sends asset IDs, use only those assets.
         * The monthlyAmount from the request is ignored because
         * the system calculates the required monthly investment.
         */
        if (request.getAssets() != null
                && !request.getAssets().isEmpty()) {

            List<Integer> requestedAssetIds =
                    request.getAssets()
                            .stream()
                            .filter(input ->
                                    input.getAssetId() != null
                            )
                            .map(
                                    InvestmentPlanRequestDTO
                                            .AssetInput::getAssetId
                            )
                            .distinct()
                            .toList();

            if (!requestedAssetIds.isEmpty()) {
                List<Asset> selectedAssets =
                        activeAssets.stream()
                                .filter(asset ->
                                        requestedAssetIds.contains(
                                                asset.getId()
                                        )
                                )
                                .toList();

                if (selectedAssets.isEmpty()) {
                    throw new IllegalArgumentException(
                            "None of the selected assets are active or available"
                    );
                }

                return selectedAssets;
            }
        }

        return activeAssets;
    }

    private Map<Integer, Double> calculateAnnualReturns(
            List<Asset> assets
    ) {
        Map<Integer, Double> returns = new HashMap<>();

        for (Asset asset : assets) {
            returns.put(
                    asset.getId(),
                    calculateAnnualReturn(asset)
            );
        }

        return returns;
    }

    private double calculateAnnualReturn(
            Asset asset
    ) {
        List<StockPriceHistory> history =
                stockPriceHistoryRepository
                        .findByAssetAndIsActiveTrueOrderByRecordedAtAsc(
                                asset
                        );

        if (history == null || history.size() < 2) {
            return DEFAULT_ANNUAL_RETURN;
        }

        StockPriceHistory firstRecord =
                history.get(0);

        StockPriceHistory lastRecord =
                history.get(history.size() - 1);

        if (firstRecord.getPrice() == null
                || lastRecord.getPrice() == null
                || firstRecord.getPrice() <= 0
                || lastRecord.getPrice() <= 0) {
            return DEFAULT_ANNUAL_RETURN;
        }

        LocalDateTime firstDate =
                firstRecord.getRecordedAt();

        LocalDateTime lastDate =
                lastRecord.getRecordedAt();

        if (firstDate == null
                || lastDate == null
                || !lastDate.isAfter(firstDate)) {
            return calculateSimpleReturn(
                    firstRecord.getPrice(),
                    lastRecord.getPrice()
            );
        }

        long days =
                ChronoUnit.DAYS.between(
                        firstDate,
                        lastDate
                );

        if (days <= 0) {
            return calculateSimpleReturn(
                    firstRecord.getPrice(),
                    lastRecord.getPrice()
            );
        }

        double years = days / 365.25;

        if (years < 0.08) {
            return calculateSimpleReturn(
                    firstRecord.getPrice(),
                    lastRecord.getPrice()
            );
        }

        double annualizedReturn =
                Math.pow(
                        lastRecord.getPrice()
                                / firstRecord.getPrice(),
                        1.0 / years
                ) - 1.0;

        return limitReturn(annualizedReturn);
    }

    private double calculateSimpleReturn(
            double firstPrice,
            double lastPrice
    ) {
        if (firstPrice <= 0) {
            return DEFAULT_ANNUAL_RETURN;
        }

        double simpleReturn =
                (lastPrice - firstPrice)
                        / firstPrice;

        return limitReturn(simpleReturn);
    }

    private double limitReturn(
            double annualReturn
    ) {
        if (!Double.isFinite(annualReturn)) {
            return DEFAULT_ANNUAL_RETURN;
        }

        return Math.max(
                MIN_REASONABLE_ANNUAL_RETURN,
                Math.min(
                        MAX_REASONABLE_ANNUAL_RETURN,
                        annualReturn
                )
        );
    }

    private List<AIAssetRecommendation>
    askOllamaForAllocation(
            FinancialGoal goal,
            double salary,
            double expenses,
            double savingsCapacity,
            int timelineMonths,
            double remainingTargetAmount,
            List<Asset> assets,
            Map<Integer, Double> annualReturnByAsset
    ) {
        try {
            List<Map<String, Object>> assetData =
                    new ArrayList<>();

            for (Asset asset : assets) {
                Map<String, Object> data =
                        new HashMap<>();

                data.put(
                        "assetId",
                        asset.getId()
                );

                data.put(
                        "name",
                        asset.getName()
                );

                data.put(
                        "symbol",
                        asset.getSymbol()
                );

                data.put(
                        "assetType",
                        asset.getAssetType() == null
                                ? null
                                : asset.getAssetType().toString()
                );

                data.put(
                        "riskLevel",
                        asset.getRiskLevel() == null
                                ? null
                                : asset.getRiskLevel().toString()
                );

                data.put(
                        "currentPrice",
                        asset.getCurrentPrice()
                );

                data.put(
                        "estimatedAnnualReturn",
                        annualReturnByAsset.getOrDefault(
                                asset.getId(),
                                DEFAULT_ANNUAL_RETURN
                        )
                );

                assetData.add(data);
            }

            String prompt = """
                    You are an AI assistant for an investment goal management platform.

                    Your responsibility is only to recommend a diversified allocation
                    among the provided assets. Java code will perform all financial calculations.

                    USER FINANCIAL PROFILE:
                    - Monthly salary: %.2f OMR
                    - Monthly expenses: %.2f OMR
                    - Net monthly savings capacity: %.2f OMR

                    FINANCIAL GOAL:
                    - Goal name: %s
                    - Original target amount: %.2f OMR
                    - Remaining target amount: %.2f OMR
                    - Timeline: %d months
                    - Preferred goal risk level: %s

                    AVAILABLE ASSETS:
                    %s

                    STRICT RULES:
                    1. Use only assetId values from AVAILABLE ASSETS.
                    2. Select at least 2 distinct assets when at least 2 assets are available.
                    3. Do not repeat an assetId.
                    4. Every weight must be greater than 0.
                    5. All weights must sum exactly to 1.0.
                    6. Consider diversification, asset type, risk level, and estimated return.
                    7. Give one short reasoning sentence for every selected asset.
                    8. Return only a raw JSON array.
                    9. Do not include markdown, code fences, headings, or explanations outside JSON.

                    REQUIRED JSON FORMAT:
                    [
                      {
                        "assetId": 1,
                        "weight": 0.60,
                        "reasoning": "Provides growth exposure while fitting the goal timeline."
                      },
                      {
                        "assetId": 2,
                        "weight": 0.40,
                        "reasoning": "Adds diversification and reduces portfolio concentration."
                      }
                    ]
                    """.formatted(
                    salary,
                    expenses,
                    savingsCapacity,
                    goal.getGoalName(),
                    safeNumber(goal.getTargetAmount()),
                    remainingTargetAmount,
                    timelineMonths,
                    goal.getRiskLevel() == null
                            ? "NOT_SPECIFIED"
                            : goal.getRiskLevel().toString(),
                    objectMapper.writeValueAsString(assetData)
            );

            String response =
                    chatModel.call(prompt);

            String cleanJson =
                    extractJsonArray(response);

            List<AIAssetRecommendation> parsed =
                    objectMapper.readValue(
                            cleanJson,
                            new TypeReference<
                                    List<AIAssetRecommendation>
                                    >() {
                            }
                    );

            if (parsed == null || parsed.isEmpty()) {
                throw new IllegalStateException(
                        "Ollama returned an empty allocation"
                );
            }

            return parsed;

        } catch (Exception exception) {
            log.warn(
                    "Ollama allocation failed. Using fallback allocation. Reason: {}",
                    exception.getMessage()
            );

            return createFallbackAllocation(assets);
        }
    }

    private String extractJsonArray(
            String response
    ) {
        if (response == null || response.isBlank()) {
            throw new IllegalStateException(
                    "Ollama returned an empty response"
            );
        }

        String cleaned =
                response
                        .replace("```json", "")
                        .replace("```JSON", "")
                        .replace("```", "")
                        .trim();

        int start = cleaned.indexOf('[');
        int end = cleaned.lastIndexOf(']');

        if (start < 0 || end < start) {
            throw new IllegalStateException(
                    "Ollama response does not contain a JSON array"
            );
        }

        return cleaned.substring(
                start,
                end + 1
        );
    }

    private List<AIAssetRecommendation>
    createFallbackAllocation(
            List<Asset> assets
    ) {
        int numberOfAssets =
                Math.min(
                        MINIMUM_DIVERSIFIED_ASSETS,
                        assets.size()
                );

        double equalWeight =
                1.0 / numberOfAssets;

        return assets.stream()
                .limit(numberOfAssets)
                .map(asset ->
                        new AIAssetRecommendation(
                                asset.getId(),
                                equalWeight,
                                "Balanced fallback allocation because the AI response was unavailable."
                        )
                )
                .toList();
    }

    private List<AIAssetRecommendation>
    validateAndNormalizeRecommendations(
            List<AIAssetRecommendation> recommendations,
            List<Asset> candidateAssets
    ) {
        Map<Integer, Asset> candidateById =
                new HashMap<>();

        for (Asset asset : candidateAssets) {
            candidateById.put(
                    asset.getId(),
                    asset
            );
        }

        Map<Integer, AIAssetRecommendation> unique =
                new HashMap<>();

        if (recommendations != null) {
            for (AIAssetRecommendation recommendation
                    : recommendations) {

                if (recommendation == null
                        || recommendation.assetId() == null
                        || recommendation.weight() == null
                        || recommendation.weight() <= 0
                        || !candidateById.containsKey(
                        recommendation.assetId()
                )) {
                    continue;
                }

                unique.put(
                        recommendation.assetId(),
                        recommendation
                );
            }
        }

        List<AIAssetRecommendation> valid =
                new ArrayList<>(unique.values());

        int requiredMinimum =
                Math.min(
                        MINIMUM_DIVERSIFIED_ASSETS,
                        candidateAssets.size()
                );

        if (valid.size() < requiredMinimum) {
            return createFallbackAllocation(
                    candidateAssets
            );
        }

        double totalWeight =
                valid.stream()
                        .mapToDouble(
                                AIAssetRecommendation::weight
                        )
                        .sum();

        if (totalWeight <= 0
                || !Double.isFinite(totalWeight)) {
            return createFallbackAllocation(
                    candidateAssets
            );
        }

        List<AIAssetRecommendation> normalized =
                new ArrayList<>();

        for (AIAssetRecommendation recommendation
                : valid) {

            double normalizedWeight =
                    recommendation.weight()
                            / totalWeight;

            normalized.add(
                    new AIAssetRecommendation(
                            recommendation.assetId(),
                            normalizedWeight,
                            hasText(
                                    recommendation.reasoning()
                            )
                                    ? recommendation.reasoning()
                                    : "Selected for portfolio diversification."
                    )
            );
        }

        return normalized;
    }

    private double calculateBlendedAnnualReturn(
            List<AIAssetRecommendation> recommendations,
            Map<Integer, Double> annualReturnByAsset
    ) {
        double blendedReturn = 0;

        for (AIAssetRecommendation recommendation
                : recommendations) {

            double annualReturn =
                    annualReturnByAsset.getOrDefault(
                            recommendation.assetId(),
                            DEFAULT_ANNUAL_RETURN
                    );

            blendedReturn +=
                    recommendation.weight()
                            * annualReturn;
        }

        return limitReturn(blendedReturn);
    }

    private double calculateRequiredMonthlyInvestment(
            double remainingTargetAmount,
            double annualReturn,
            int months
    ) {
        if (remainingTargetAmount <= 0) {
            return 0;
        }

        if (months <= 0) {
            throw new IllegalArgumentException(
                    "Timeline must be at least one month"
            );
        }

        double monthlyRate =
                annualReturn / 12.0;

        /*
         * When the expected return is zero or negative,
         * use simple monthly saving calculation.
         */
        if (monthlyRate <= 0) {
            return remainingTargetAmount / months;
        }

        double growthFactor =
                Math.pow(
                        1 + monthlyRate,
                        months
                ) - 1;

        if (growthFactor <= 0
                || !Double.isFinite(growthFactor)) {
            return remainingTargetAmount / months;
        }

        return remainingTargetAmount
                * monthlyRate
                / growthFactor;
    }

    private AllocationCalculation buildAllocations(
            List<AIAssetRecommendation> recommendations,
            List<Asset> candidateAssets,
            Map<Integer, Double> annualReturnByAsset,
            double requiredMonthlyInvestment,
            int timelineMonths
    ) {
        Map<Integer, Asset> assetById =
                new HashMap<>();

        for (Asset asset : candidateAssets) {
            assetById.put(
                    asset.getId(),
                    asset
            );
        }

        List<InvestmentPlanResponseDTO
                .AssetAllocationResponseDTO> allocations =
                new ArrayList<>();

        double totalProjectedValue = 0;
        double expectedTotalProfit = 0;

        for (AIAssetRecommendation recommendation
                : recommendations) {

            Asset asset =
                    assetById.get(
                            recommendation.assetId()
                    );

            if (asset == null) {
                continue;
            }

            double monthlyAmount =
                    requiredMonthlyInvestment
                            * recommendation.weight();

            double annualReturn =
                    annualReturnByAsset.getOrDefault(
                            asset.getId(),
                            DEFAULT_ANNUAL_RETURN
                    );

            double monthlyRate =
                    annualReturn / 12.0;

            double futureValue =
                    calculateFutureValueOfMonthlyInvestments(
                            monthlyAmount,
                            monthlyRate,
                            timelineMonths
                    );

            double totalInvested =
                    monthlyAmount
                            * timelineMonths;

            double assetTotalProfit =
                    futureValue - totalInvested;

            double assetAverageMonthlyProfit =
                    timelineMonths > 0
                            ? assetTotalProfit
                            / timelineMonths
                            : 0;

            totalProjectedValue += futureValue;
            expectedTotalProfit += assetTotalProfit;

            allocations.add(
                    InvestmentPlanResponseDTO
                            .AssetAllocationResponseDTO
                            .builder()
                            .assetId(asset.getId())
                            .assetName(asset.getName())
                            .symbol(asset.getSymbol())
                            .monthlyAmount(
                                    round(monthlyAmount)
                            )
                            .expectedAnnualReturnRate(
                                    round(annualReturn)
                            )
                            .expectedMonthlyProfit(
                                    round(
                                            assetAverageMonthlyProfit
                                    )
                            )
                            .reasoning(
                                    recommendation.reasoning()
                            )
                            .build()
            );
        }

        return new AllocationCalculation(
                allocations,
                totalProjectedValue,
                expectedTotalProfit
        );
    }

    private double calculateFutureValueOfMonthlyInvestments(
            double monthlyInvestment,
            double monthlyRate,
            int months
    ) {
        if (monthlyInvestment <= 0
                || months <= 0) {
            return 0;
        }

        if (monthlyRate <= 0) {
            return monthlyInvestment
                    * months;
        }

        double futureValue =
                monthlyInvestment
                        * (
                        (
                                Math.pow(
                                        1 + monthlyRate,
                                        months
                                ) - 1
                        )
                                / monthlyRate
                );

        if (!Double.isFinite(futureValue)) {
            return monthlyInvestment
                    * months;
        }

        return futureValue;
    }

    private double safeNumber(
            Number value
    ) {
        return value == null
                ? 0
                : value.doubleValue();
    }

    private boolean hasText(
            String value
    ) {
        return value != null
                && !value.isBlank();
    }

    private double round(
            double value
    ) {
        return Math.round(value * 100.0)
                / 100.0;
    }

    public record AIAssetRecommendation(
            Integer assetId,
            Double weight,
            String reasoning
    ) {
    }

    private record AllocationCalculation(
            List<InvestmentPlanResponseDTO
                    .AssetAllocationResponseDTO> allocations,
            double totalProjectedValue,
            double expectedTotalProfit
    ) {
    }
}