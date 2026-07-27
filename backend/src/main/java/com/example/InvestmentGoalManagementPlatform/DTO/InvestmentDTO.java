package com.example.InvestmentGoalManagementPlatform.DTO;

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

    private Integer id; // response-only, ignored on create/update requests

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

    @NotNull(message = "Stock ID is required")
    private Integer stockId;

    private String stockTickerSymbol;
    private Double currentValue;
    private Double profitOrLoss;

    public Investment toEntity() { // For Creating
        Investment investment = new Investment();

        investment.setAmountInvested(amountInvested);
        investment.setQuantity(quantity);
        investment.setPurchasePrice(purchasePrice);
        investment.setPurchaseDate(purchaseDate);

        return investment;
    }

    public void applyTo(Investment investment) { // For Updating
        investment.setAmountInvested(amountInvested);
        investment.setQuantity(quantity);
        investment.setPurchasePrice(purchasePrice);
        investment.setPurchaseDate(purchaseDate);
    }

    public static InvestmentDTO fromEntity(Investment investment) {
        InvestmentDTO dto = new InvestmentDTO();

        dto.setId(investment.getId());
        dto.setAmountInvested(investment.getAmountInvested());
        dto.setQuantity(investment.getQuantity());
        dto.setPurchasePrice(investment.getPurchasePrice());
        dto.setPurchaseDate(investment.getPurchaseDate());
        dto.setUserId(investment.getUser().getId());
        dto.setPlanId(investment.getInvestmentPlan().getId());
        dto.setStockId(investment.getStock().getId());
        dto.setStockTickerSymbol(investment.getStock().getTickerSymbol());

        Double currentValue = investment.getStock().getCurrentPrice() * investment.getQuantity();
        dto.setCurrentValue(currentValue);
        dto.setProfitOrLoss(currentValue - investment.getAmountInvested());

        return dto;
    }

    public static List<InvestmentDTO> fromEntity(List<Investment> investments) {
        List<InvestmentDTO> investmentDTOList = new ArrayList<>();
        for (Investment investment : investments) {
            investmentDTOList.add(fromEntity(investment));
        }
        return investmentDTOList;
    }
}
