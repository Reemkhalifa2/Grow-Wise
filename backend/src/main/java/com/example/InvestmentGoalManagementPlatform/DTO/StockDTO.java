package com.example.InvestmentGoalManagementPlatform.DTO;

import com.example.InvestmentGoalManagementPlatform.Entities.Stock;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockDTO {

    @NotBlank(message = "Company name is required")
    private String companyName;

    @NotBlank(message = "Ticker symbol is required")
    private String tickerSymbol;

    @NotNull(message = "Current price is required")
    @Min(value = 0, message = "Price cannot be negative")
    private Double currentPrice;

    private Double dailyChange;

    private LocalDateTime lastUpdated;


    public Stock toEntity() {
        Stock stock = new Stock();

        stock.setCompanyName(companyName);
        stock.setTickerSymbol(tickerSymbol);
        stock.setCurrentPrice(currentPrice);
        stock.setDailyChange(dailyChange);
        stock.setLastUpdated(lastUpdated);

        return stock;
    }


    public void applyTo(Stock stock) {
        stock.setCompanyName(companyName);
        stock.setTickerSymbol(tickerSymbol);
        stock.setCurrentPrice(currentPrice);
        stock.setDailyChange(dailyChange);
        stock.setLastUpdated(lastUpdated);
    }
}