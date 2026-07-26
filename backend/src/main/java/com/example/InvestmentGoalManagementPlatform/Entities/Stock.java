package com.example.InvestmentGoalManagementPlatform.Entities;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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

}