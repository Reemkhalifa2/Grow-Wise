package com.example.InvestmentGoalManagementPlatform.DTO;

import com.example.InvestmentGoalManagementPlatform.entity.Asset;
import com.example.InvestmentGoalManagementPlatform.entity.Investment;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvestmentDTO {

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

            double quantity =
                    investment.getQuantity() == null
                            ? 1.0
                            : investment.getQuantity();

            double currentPrice =
                    asset.getCurrentPrice() == null
                            ? 0.0
                            : asset.getCurrentPrice();

            double currentValue =
                    currentPrice * quantity;

            dto.setCurrentValue(currentValue);

            double investedAmount =
                    investment.getAmountInvested() == null
                            ? 0.0
                            : investment.getAmountInvested();

            dto.setProfitOrLoss(
                    currentValue - investedAmount
            );
        } else {
            dto.setCurrentValue(0.0);

            dto.setProfitOrLoss(
                    -safeDouble(
                            investment.getAmountInvested()
                    )
            );
        }

        return dto;
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