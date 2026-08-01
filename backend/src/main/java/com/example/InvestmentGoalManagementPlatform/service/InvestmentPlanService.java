package com.example.InvestmentGoalManagementPlatform.service;

import com.example.InvestmentGoalManagementPlatform.DTO.InvestmentPlanRequestDTO;
import com.example.InvestmentGoalManagementPlatform.DTO.InvestmentPlanResponseDTO;
import com.example.InvestmentGoalManagementPlatform.entity.Asset;
import com.example.InvestmentGoalManagementPlatform.entity.FinancialGoal;
import com.example.InvestmentGoalManagementPlatform.entity.StockPriceHistory;
import com.example.InvestmentGoalManagementPlatform.repository.StockPriceHistoryRepository;
import com.example.InvestmentGoalManagementPlatform.utility.HelperUtility;
import com.example.InvestmentGoalManagementPlatform.utility.RiskLevel;
import com.example.InvestmentGoalManagementPlatform.repository.AssetRepository;
import com.example.InvestmentGoalManagementPlatform.repository.FinancialGoalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class InvestmentPlanService {

    private final FinancialGoalRepository financialGoalRepository;
    private final AssetRepository assetRepository;
    private final StockPriceHistoryRepository historyRepository;

    private final ChatModel chatModel;

    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static final ObjectMapper JSON = new ObjectMapper();

    // Fallback annual return per risk level, used only when an asset doesn't have enough
    // price history yet for the regression estimate below.
    private static final Map<RiskLevel, Double> ANNUAL_RETURN_BY_RISK = Map.of(
            RiskLevel.LOW, 0.04,
            RiskLevel.MEDIUM, 0.08,
            RiskLevel.HIGH, 0.12
    );

    public InvestmentPlanResponseDTO createInvestmentPlan(InvestmentPlanRequestDTO request) {

        FinancialGoal goal = financialGoalRepository.findById(request.getFinancialGoalId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Financial goal not found: " + request.getFinancialGoalId()));

        double targetAmount = goal.getTargetAmount();
        int months =HelperUtility.calculateRemainingMonths(goal.getTargetDate()) ;

        List<InvestmentPlanRequestDTO.AssetInput> assetInputs = request.getAssets();

        // ---------------------------------------------------------------------------------
        // CASE 1: No assets selected -> auto-pick from active assets at the goal's risk level
        // ---------------------------------------------------------------------------------
        if (assetInputs == null || assetInputs.isEmpty()) {

            List<Asset> candidateAssets = new ArrayList<>();
            for (Asset asset : assetRepository.findAllByIsActiveTrue()) {
                if (asset.getRiskLevel() == goal.getRiskLevel()) {
                    candidateAssets.add(asset);
                }
            }
            if (candidateAssets.isEmpty()) {
                throw new IllegalStateException(
                        "No active assets available for risk level " + goal.getRiskLevel());
            }

            Map<Integer, Double> estimatedReturns = new LinkedHashMap<>();
            for (Asset asset : candidateAssets) {
                estimatedReturns.put(asset.getId(), estimateAnnualReturnRate(asset));
            }

            // The LLM may choose a subset of candidateAssets here (allowExclusion = true).
            Map<Integer, LlmAllocation> llmResult = callLlmForAssetWeights(goal, candidateAssets, estimatedReturns, true);
            boolean aiAssisted = llmResult != null;

            List<Asset> chosenAssets = new ArrayList<>();
            Map<Integer, Double> weights = new LinkedHashMap<>();
            if (aiAssisted) {
                for (Asset asset : candidateAssets) {
                    if (llmResult.containsKey(asset.getId())) {
                        chosenAssets.add(asset);
                        weights.put(asset.getId(), llmResult.get(asset.getId()).weight());
                    }
                }
            } else {
                chosenAssets.addAll(candidateAssets);
                double equalWeight = 1.0 / candidateAssets.size();
                for (Asset asset : candidateAssets) {
                    weights.put(asset.getId(), equalWeight);
                }
            }

            double blendedAnnualReturnRate = 0.0;
            for (Asset asset : chosenAssets) {
                blendedAnnualReturnRate += weights.get(asset.getId()) * estimatedReturns.get(asset.getId());
            }
            double blendedMonthlyRate = blendedAnnualReturnRate / 12.0;

            double monthlyInvestmentAmount = calculateRequiredMonthlyPayment(targetAmount, blendedMonthlyRate, months);
            double totalProjectedValue = calculateFutureValue(monthlyInvestmentAmount, blendedMonthlyRate, months);
            double totalContributions = monthlyInvestmentAmount * months;
            double expectedTotalProfit = totalProjectedValue - totalContributions;
            double expectedMonthlyProfit = expectedTotalProfit / months;

            List<InvestmentPlanResponseDTO.AssetAllocationResponseDTO> allocations = new ArrayList<>();
            for (Asset asset : chosenAssets) {
                double weight = weights.get(asset.getId());
                double assetMonthlyAmount = monthlyInvestmentAmount * weight;
                double assetRate = estimatedReturns.get(asset.getId());
                String reasoning = aiAssisted ? llmResult.get(asset.getId()).reasoning() : null;

                allocations.add(InvestmentPlanResponseDTO.AssetAllocationResponseDTO.builder()
                        .assetId(asset.getId())
                        .assetName(asset.getName())
                        .symbol(asset.getSymbol())
                        .monthlyAmount(assetMonthlyAmount)
                        .expectedAnnualReturnRate(assetRate)
                        .expectedMonthlyProfit(expectedTotalProfit * weight / months)
                        .reasoning(reasoning)
                        .build());
            }

            return InvestmentPlanResponseDTO.builder()
                    .planType("SYSTEM_GENERATED")
                    .targetAmount(targetAmount)
                    .timelineMonths(months)
                    .monthlyInvestmentAmount(monthlyInvestmentAmount)
                    .monthlySavingsRequired(monthlyInvestmentAmount)
                    .expectedMonthlyProfit(expectedMonthlyProfit)
                    .expectedTotalProfit(expectedTotalProfit)
                    .aiAssisted(aiAssisted)
                    .allocations(allocations)
                    .build();
        }

        // Load and validate the assets the user selected (used by both case 2 and case 3 below)
        List<Asset> selectedAssets = new ArrayList<>();
        for (InvestmentPlanRequestDTO.AssetInput input : assetInputs) {
            Asset asset = assetRepository.findById(input.getAssetId())
                    .orElseThrow(() -> new IllegalArgumentException("Asset not found: " + input.getAssetId()));
            if (!Boolean.TRUE.equals(asset.getIsActive())) {
                throw new IllegalArgumentException("Asset is not active: " + asset.getSymbol());
            }
            selectedAssets.add(asset);
        }

        boolean anyAmountProvided = false;
        boolean allAmountsProvided = true;
        for (InvestmentPlanRequestDTO.AssetInput input : assetInputs) {
            if (input.getMonthlyAmount() != null) {
                anyAmountProvided = true;
            } else {
                allAmountsProvided = false;
            }
        }
        if (anyAmountProvided && !allAmountsProvided) {
            throw new IllegalArgumentException(
                    "Please provide a monthly amount for every selected asset, or leave all of them blank.");
        }

        // ---------------------------------------------------------------------------------
        // CASE 2: Assets selected, no amounts -> size the investment ourselves
        // ---------------------------------------------------------------------------------
        if (!allAmountsProvided) {

            Map<Integer, Double> estimatedReturns = new LinkedHashMap<>();
            for (Asset asset : selectedAssets) {
                estimatedReturns.put(asset.getId(), estimateAnnualReturnRate(asset));
            }

            // No exclusion allowed here - every selected asset must appear in the plan.
            Map<Integer, LlmAllocation> llmResult = callLlmForAssetWeights(goal, selectedAssets, estimatedReturns, false);
            boolean aiAssisted = llmResult != null && llmResult.keySet().containsAll(idsOf(selectedAssets));

            Map<Integer, Double> weights = new LinkedHashMap<>();
            if (aiAssisted) {
                for (Asset asset : selectedAssets) {
                    weights.put(asset.getId(), llmResult.get(asset.getId()).weight());
                }
            } else {
                double equalWeight = 1.0 / selectedAssets.size();
                for (Asset asset : selectedAssets) {
                    weights.put(asset.getId(), equalWeight);
                }
            }

            double blendedAnnualReturnRate = 0.0;
            for (Asset asset : selectedAssets) {
                blendedAnnualReturnRate += weights.get(asset.getId()) * estimatedReturns.get(asset.getId());
            }
            double blendedMonthlyRate = blendedAnnualReturnRate / 12.0;

            double monthlyInvestmentAmount = calculateRequiredMonthlyPayment(targetAmount, blendedMonthlyRate, months);
            double totalProjectedValue = calculateFutureValue(monthlyInvestmentAmount, blendedMonthlyRate, months);
            double totalContributions = monthlyInvestmentAmount * months;
            double expectedTotalProfit = totalProjectedValue - totalContributions;
            double expectedMonthlyProfit = expectedTotalProfit / months;

            List<InvestmentPlanResponseDTO.AssetAllocationResponseDTO> allocations = new ArrayList<>();
            for (Asset asset : selectedAssets) {
                double weight = weights.get(asset.getId());
                double assetMonthlyAmount = monthlyInvestmentAmount * weight;
                double assetRate = estimatedReturns.get(asset.getId());
                String reasoning = aiAssisted ? llmResult.get(asset.getId()).reasoning() : null;

                allocations.add(InvestmentPlanResponseDTO.AssetAllocationResponseDTO.builder()
                        .assetId(asset.getId())
                        .assetName(asset.getName())
                        .symbol(asset.getSymbol())
                        .monthlyAmount(assetMonthlyAmount)
                        .expectedAnnualReturnRate(assetRate)
                        .expectedMonthlyProfit(expectedTotalProfit * weight / months)
                        .reasoning(reasoning)
                        .build());
            }

            return InvestmentPlanResponseDTO.builder()
                    .planType("USER_SELECTED")
                    .targetAmount(targetAmount)
                    .timelineMonths(months)
                    .monthlyInvestmentAmount(monthlyInvestmentAmount)
                    .expectedMonthlyProfit(expectedMonthlyProfit)
                    .expectedTotalProfit(expectedTotalProfit)
                    .aiAssisted(aiAssisted)
                    .allocations(allocations)
                    .build();
        }

        // ---------------------------------------------------------------------------------
        // CASE 3: Assets selected with exact monthly amounts -> use them as-is (no LLM call;
        // there's nothing left to choose or weight, the user already decided the amounts)
        // ---------------------------------------------------------------------------------
        Map<Integer, Double> amountByAssetId = new HashMap<>();
        for (InvestmentPlanRequestDTO.AssetInput input : assetInputs) {
            if (input.getMonthlyAmount() <= 0) {
                throw new IllegalArgumentException("Monthly amount for asset " + input.getAssetId()
                        + " must be greater than 0.");
            }
            amountByAssetId.put(input.getAssetId(), input.getMonthlyAmount());
        }

        double monthlyInvestmentAmount = 0.0;
        double totalProjectedValue = 0.0;
        List<InvestmentPlanResponseDTO.AssetAllocationResponseDTO> allocations = new ArrayList<>();

        for (Asset asset : selectedAssets) {
            double assetMonthlyAmount = amountByAssetId.get(asset.getId());
            double assetAnnualReturnRate = estimateAnnualReturnRate(asset);
            double assetMonthlyRate = assetAnnualReturnRate / 12.0;

            double assetProjectedValue = calculateFutureValue(assetMonthlyAmount, assetMonthlyRate, months);
            double assetContributions = assetMonthlyAmount * months;
            double assetMonthlyProfit = (assetProjectedValue - assetContributions) / months;

            monthlyInvestmentAmount += assetMonthlyAmount;
            totalProjectedValue += assetProjectedValue;

            allocations.add(InvestmentPlanResponseDTO.AssetAllocationResponseDTO.builder()
                    .assetId(asset.getId())
                    .assetName(asset.getName())
                    .symbol(asset.getSymbol())
                    .monthlyAmount(assetMonthlyAmount)
                    .expectedAnnualReturnRate(assetAnnualReturnRate)
                    .expectedMonthlyProfit(assetMonthlyProfit)
                    .build());
        }

        double totalContributions = monthlyInvestmentAmount * months;
        double expectedTotalProfit = totalProjectedValue - totalContributions;
        double expectedMonthlyProfit = expectedTotalProfit / months;
        boolean goalAchievable = totalProjectedValue >= targetAmount;

        return InvestmentPlanResponseDTO.builder()
                .planType("CUSTOM_ALLOCATION")
                .targetAmount(targetAmount)
                .timelineMonths(months)
                .monthlyInvestmentAmount(monthlyInvestmentAmount)
                .expectedMonthlyProfit(expectedMonthlyProfit)
                .expectedTotalProfit(expectedTotalProfit)
                .totalProjectedValue(totalProjectedValue)
                .goalAchievable(goalAchievable)
                .aiAssisted(false)
                .allocations(allocations)
                .build();
    }

    // =====================================================================================
    // RETURN ESTIMATION - simple linear regression over price history, annualized
    // =====================================================================================

    /**
     * Fits a least-squares line through (daysSinceFirstRecord, price) and converts the slope
     * into an annualized growth rate. Needs at least 3 history points to bother regressing;
     * otherwise falls back to the flat risk-level assumption.
     */
    private double estimateAnnualReturnRate(Asset asset) {
        List<StockPriceHistory> history = historyRepository.findByAssetAndIsActiveTrueOrderByRecordedAtAsc(asset);
        if (history.size() < 3) {
            return ANNUAL_RETURN_BY_RISK.get(asset.getRiskLevel());
        }

        var start = history.get(0).getRecordedAt();
        int n = history.size();
        double sumX = 0, sumY = 0, sumXY = 0, sumXX = 0;

        for (StockPriceHistory point : history) {
            double x = Duration.between(start, point.getRecordedAt()).toDays();
            double y = point.getPrice();
            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumXX += x * x;
        }

        double meanX = sumX / n;
        double meanY = sumY / n;
        double denominator = sumXX - n * meanX * meanX;

        if (denominator == 0 || meanY <= 0) {
            return ANNUAL_RETURN_BY_RISK.get(asset.getRiskLevel());
        }

        double slopePerDay = (sumXY - n * meanX * meanY) / denominator;
        double dailyGrowthRate = slopePerDay / meanY;
        double annualReturnRate = dailyGrowthRate * 365;

        // Clamp so a short or noisy history can't produce an absurd projection
        return Math.max(-0.5, Math.min(annualReturnRate, 0.40));
    }

    // =====================================================================================
    // LLM-ASSISTED ASSET WEIGHTING
    // =====================================================================================

    private record LlmAllocation(double weight, String reasoning) {
    }

    /**
     * Asks Claude to weight (and, when allowExclusion is true, choose among) the candidate
     * assets for this goal. Returns null on any failure - callers fall back to equal-weight.
     */
    private Map<Integer, LlmAllocation> callLlmForAssetWeights(FinancialGoal goal, List<Asset> candidateAssets,
                                                            Map<Integer, Double> estimatedReturns,
                                                            boolean allowExclusion) {
        try {
            List<Map<String, Object>> assetSummaries = new ArrayList<>();
            for (Asset asset : candidateAssets) {
                Map<String, Object> summary = new LinkedHashMap<>();
                summary.put("assetId", asset.getId());
                summary.put("name", asset.getName());
                summary.put("symbol", asset.getSymbol());
                summary.put("assetType", asset.getAssetType());
                summary.put("riskLevel", asset.getRiskLevel());
                summary.put("currentPrice", asset.getCurrentPrice());
                summary.put("estimatedAnnualReturnRate", estimatedReturns.get(asset.getId()));
                assetSummaries.add(summary);
            }

            String inclusionInstruction = allowExclusion
                    ? "Choose a diversified subset of these assets that fits the goal's risk level and timeline - you don't have to include every asset."
                    : "Every asset listed below was already chosen by the user - include ALL of them, you're only deciding how much weight each one gets, not which ones to include.";

            String promptText = "You are an investment allocation assistant for a financial goal planning app.\n"
                    + "Financial goal: targetAmount=" + goal.getTargetAmount() + " OMR, timelineMonths="
                    + HelperUtility.calculateRemainingMonths(goal.getTargetDate()) + ", riskLevel=" + goal.getRiskLevel() + ".\n"
                    + "Candidate assets (JSON): " + JSON.writeValueAsString(assetSummaries) + "\n"
                    + inclusionInstruction + " "
                    + "Assign each included asset a weight between 0 and 1 so the weights sum to exactly 1.0, favoring diversification across asset types where reasonable.\n"
                    + "Respond with ONLY a JSON array, no prose, no markdown fences, in this exact shape: "
                    + "[{\"assetId\": <number>, \"weight\": <number>, \"reasoning\": \"<one short sentence>\"}]";

            String responseText = chatModel.call(promptText);

            return parseLlmWeights(responseText, candidateAssets);

        } catch (Exception e) {
            log.warn("Ollama LLM asset weighting failed, falling back to equal-weight allocation: {}", e.getMessage());
            return null; // Fallback للاستجابة العادية عند فشل الاتصال بـ Ollama
        }
    }

    private Map<Integer, LlmAllocation> parseLlmWeights(String llmText, List<Asset> candidateAssets) {
        try {
            int start = llmText.indexOf('[');
            int end = llmText.lastIndexOf(']');
            if (start == -1 || end == -1 || end < start) {
                return null;
            }
            String jsonArray = llmText.substring(start, end + 1);

            List<Map<String, Object>> items = JSON.readValue(jsonArray, List.class);

            Set<Integer> validIds = new HashSet<>(idsOf(candidateAssets));
            Map<Integer, LlmAllocation> raw = new LinkedHashMap<>();
            double weightSum = 0;

            for (Map<String, Object> item : items) {
                Integer assetId = Integer.valueOf(String.valueOf(item.get("assetId")));
                double weight = Double.parseDouble(String.valueOf(item.get("weight")));
                String reasoning = String.valueOf(item.getOrDefault("reasoning", ""));

                if (validIds.contains(assetId) && weight > 0) {
                    raw.put(assetId, new LlmAllocation(weight, reasoning));
                    weightSum += weight;
                }
            }

            if (raw.isEmpty() || weightSum <= 0) {
                return null;
            }

            // Normalize so the weights sum to exactly 1.0, in case the model's numbers were off
            Map<Integer, LlmAllocation> normalized = new LinkedHashMap<>();
            for (Map.Entry<Integer, LlmAllocation> entry : raw.entrySet()) {
                LlmAllocation allocation = entry.getValue();
                normalized.put(entry.getKey(), new LlmAllocation(allocation.weight() / weightSum, allocation.reasoning()));
            }
            return normalized;

        } catch (Exception e) {
            log.warn("Could not parse LLM asset weights: {}", e.getMessage());
            return null;
        }
    }

    private List<Integer> idsOf(List<Asset> assets) {
        List<Integer> ids = new ArrayList<>();
        for (Asset asset : assets) {
            ids.add(asset.getId());
        }
        return ids;
    }

    // =====================================================================================
    // FINANCIAL MATH - plain arithmetic, unaffected by the AI/regression estimates above
    // =====================================================================================

    private double calculateFutureValue(double monthlyPayment, double monthlyRate, int months) {
        if (monthlyRate == 0.0) {
            return monthlyPayment * months;
        }
        return monthlyPayment * ((Math.pow(1 + monthlyRate, months) - 1) / monthlyRate);
    }

    private double calculateRequiredMonthlyPayment(double targetAmount, double monthlyRate, int months) {
        if (monthlyRate == 0.0) {
            return targetAmount / months;
        }
        double factor = (Math.pow(1 + monthlyRate, months) - 1) / monthlyRate;
        return targetAmount / factor;
    }
}