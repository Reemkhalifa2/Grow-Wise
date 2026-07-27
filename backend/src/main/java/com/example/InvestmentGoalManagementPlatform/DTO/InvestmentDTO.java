package com.example.InvestmentGoalManagementPlatform.DTO;

import com.example.InvestmentGoalManagementPlatform.entities.Investment;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvestmentDTO {

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

    public Investment toEntity() {
        Investment investment = new Investment();

        investment.setAmountInvested(amountInvested);
        investment.setQuantity(quantity);
        investment.setPurchasePrice(purchasePrice);
        investment.setPurchaseDate(purchaseDate);

        return investment;
    }

    public void applyTo(Investment investment) {
        investment.setAmountInvested(amountInvested);
        investment.setQuantity(quantity);
        investment.setPurchasePrice(purchasePrice);
        investment.setPurchaseDate(purchaseDate);
    }
}

