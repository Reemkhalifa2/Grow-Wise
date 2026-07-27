package com.example.InvestmentGoalManagementPlatform.DTO;

import com.example.InvestmentGoalManagementPlatform.Entities.Stock;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockDTO {

    private Integer id; // response-only, ignored on create/update requests

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

    public static StockDTO fromEntity(Stock stock) {
        StockDTO dto = new StockDTO();

        dto.setId(stock.getId());
        dto.setCompanyName(stock.getCompanyName());
        dto.setTickerSymbol(stock.getTickerSymbol());
        dto.setCurrentPrice(stock.getCurrentPrice());
        dto.setDailyChange(stock.getDailyChange());
        dto.setLastUpdated(stock.getLastUpdated());

        return dto;
    }

    public static List<StockDTO> fromEntity(List<Stock> stocks) {
        List<StockDTO> stockDTOList = new ArrayList<>();
        for (Stock stock : stocks) {
            stockDTOList.add(fromEntity(stock));
        }
        return stockDTOList;
    }
}