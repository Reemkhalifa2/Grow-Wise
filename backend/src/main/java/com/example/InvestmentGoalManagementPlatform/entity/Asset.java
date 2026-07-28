package com.example.InvestmentGoalManagementPlatform.entity;

import com.example.InvestmentGoalManagementPlatform.utility.AssetType;
import com.example.InvestmentGoalManagementPlatform.utility.RiskLevel;
import jakarta.persistence.*;
import jdk.jfr.Enabled;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Asset extends BaseEntity{
    private String symbol;
    private String name;
    private AssetType assetType;
    private Double currentPrice;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RiskLevel riskLevel;
    private String scrapingUrl;
    private String cssSelector;
    private Boolean autoUpdate = false;
    @OneToMany
    private List<StockPriceHistory> priceHistories = new ArrayList<>();
    @OneToMany
    private List<Investment> investments = new ArrayList<>();
}

