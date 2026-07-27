package com.example.InvestmentGoalManagementPlatform.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Stock extends BaseEntity {

    private String companyName;
    private String tickerSymbol;
    private Double currentPrice;
    private Double dailyChange;
    private LocalDateTime lastUpdated;

    @OneToMany
    private List<Investment> investments;
    @OneToMany
    private List<StockPriceHistory> stockPriceHistoryList;
}