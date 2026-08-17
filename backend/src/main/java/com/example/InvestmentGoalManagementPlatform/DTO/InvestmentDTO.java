package com.example.InvestmentGoalManagementPlatform.DTO;

import com.example.InvestmentGoalManagementPlatform.entity.Asset;
import com.example.InvestmentGoalManagementPlatform.entity.Investment;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvestmentDTO {

    private static final int UNITS_SCALE = 6;
    private static final int MONEY_SCALE = 3;
    private static final int PERCENT_SCALE = 2;

    private Integer id;

    @NotNull(message = "Amount invested is required")
    @Min(value = 0, message = "Amount invested cannot be negative")
    private Double amountInvested;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @NotNull(message = "Purchase price is required")
    @Min(value = 0, message = "Purchase price cannot be negative")
    private Double purchasePrice;

    @NotNull(message = "Purchase date is required")
    @PastOrPresent(message = "Purchase date cannot be in the future")
    private LocalDate purchaseDate;

    @NotNull(message = "User ID is required")
    private Integer userId;

    @NotNull(message = "Investment plan ID is required")
    private Integer planId;

    @NotNull(message = "Asset ID is required")
    private Integer assetId;

    private String assetName;
    private String assetSymbol;
    private String assetType;
    private String riskLevel;

    private Double currentValue;
    private Double profitOrLoss;
    private Double returnPercentage;

    /** amountInvested / purchasePrice at the time this investment was made. */
    private Double unitsPurchased;

    /** The asset's latest price, exposed alongside currentValue for context. */
    private Double currentPrice;

    /** Whether investmentPlan has already met its required amount this month. */
    private Boolean monthlyInvestmentCompleted;

    /** e.g. "September 2026" — the month a new contribution would count toward. */
    private String nextInvestmentMonth;

    public Investment toEntity() {
        Investment investment = new Investment();

        investment.setAmountInvested(amountInvested);
        investment.setQuantity(quantity);
        investment.setPurchasePrice(purchasePrice);
        investment.setPurchaseDate(purchaseDate);

        return investment;
    }

    public void applyTo(
            Investment investment
    ) {
        investment.setAmountInvested(amountInvested);
        investment.setQuantity(quantity);
        investment.setPurchasePrice(purchasePrice);
        investment.setPurchaseDate(purchaseDate);
    }

    public static InvestmentDTO fromEntity(
            Investment investment
    ) {
        InvestmentDTO dto =
                new InvestmentDTO();

        dto.setId(investment.getId());

        dto.setAmountInvested(
                investment.getAmountInvested()
        );

        dto.setQuantity(
                investment.getQuantity()
        );

        dto.setPurchasePrice(
                investment.getPurchasePrice()
        );

        dto.setPurchaseDate(
                investment.getPurchaseDate()
        );

        if (investment.getUser() != null) {
            dto.setUserId(
                    investment.getUser().getId()
            );
        }

        if (
                investment.getInvestmentPlan() != null
        ) {
            dto.setPlanId(
                    investment
                            .getInvestmentPlan()
                            .getId()
            );
        }

        Asset asset =
                investment.getAsset();

        BigDecimal investedAmount =
                BigDecimal.valueOf(
                        safeDouble(investment.getAmountInvested())
                );

        if (asset != null) {
            dto.setAssetId(asset.getId());
            dto.setAssetName(asset.getName());
            dto.setAssetSymbol(asset.getSymbol());

            dto.setAssetType(
                    asset.getAssetType() == null
                            ? null
                            : asset.getAssetType().name()
            );

            dto.setRiskLevel(
                    asset.getRiskLevel() == null
                            ? null
                            : asset.getRiskLevel().name()
            );

            dto.setCurrentPrice(asset.getCurrentPrice());

            // Units are the source of truth for value, never the asset's
            // raw price — that's what let a 7300-point MSM30 index level
            // get displayed as if it were a 7300 OMR portfolio balance.
            BigDecimal units = resolveUnits(investment);

            dto.setUnitsPurchased(
                    units == null ? null : units.doubleValue()
            );

            BigDecimal currentValue =
                    calculateCurrentValue(
                            units,
                            asset.getCurrentPrice()
                    );

            if (currentValue != null) {
                BigDecimal profitLoss =
                        currentValue
                                .subtract(investedAmount)
                                .setScale(
                                        MONEY_SCALE,
                                        RoundingMode.HALF_UP
                                );

                dto.setCurrentValue(currentValue.doubleValue());
                dto.setProfitOrLoss(profitLoss.doubleValue());

                dto.setReturnPercentage(
                        calculateReturnPercentage(
                                profitLoss,
                                investedAmount
                        )
                );
            } else {
                // No purchase price is on record at all. Every creation
                // path now sets one, so this should only happen for rows
                // predating this fix — report the amount invested rather
                // than fabricate a value from an unrelated price.
                dto.setCurrentValue(investedAmount.doubleValue());
                dto.setProfitOrLoss(0.0);
                dto.setReturnPercentage(0.0);
            }
        } else {
            dto.setCurrentValue(0.0);
            dto.setProfitOrLoss(-investedAmount.doubleValue());
        }

        return dto;
    }

    /**
     * Prefers the stored unitsPurchased. Falls back to deriving it from
     * amountInvested/purchasePrice for investments created before that
     * column existed, so old records don't silently show as unpriced.
     */
    private static BigDecimal resolveUnits(
            Investment investment
    ) {
        if (investment.getUnitsPurchased() != null) {
            return investment.getUnitsPurchased();
        }

        Double amountInvested = investment.getAmountInvested();
        Double purchasePrice = investment.getPurchasePrice();

        if (
                amountInvested == null ||
                        purchasePrice == null ||
                        purchasePrice <= 0
        ) {
            return null;
        }

        return BigDecimal.valueOf(amountInvested)
                .divide(
                        BigDecimal.valueOf(purchasePrice),
                        UNITS_SCALE,
                        RoundingMode.HALF_UP
                );
    }

    private static BigDecimal calculateCurrentValue(
            BigDecimal units,
            Double currentPrice
    ) {
        if (units == null || currentPrice == null) {
            return null;
        }

        return units
                .multiply(BigDecimal.valueOf(currentPrice))
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    private static Double calculateReturnPercentage(
            BigDecimal profitLoss,
            BigDecimal investedAmount
    ) {
        if (investedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        return profitLoss
                .divide(investedAmount, 6, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(PERCENT_SCALE, RoundingMode.HALF_UP)
                .doubleValue();
    }

    public static List<InvestmentDTO> fromEntity(
            List<Investment> investments
    ) {
        List<InvestmentDTO> dtoList =
                new ArrayList<>();

        if (investments == null) {
            return dtoList;
        }

        for (Investment investment : investments) {
            dtoList.add(
                    fromEntity(investment)
            );
        }

        return dtoList;
    }

    private static double safeDouble(
            Double value
    ) {
        return value == null
                ? 0.0
                : value;
    }
}