package com.example.InvestmentGoalManagementPlatform.entity;

import com.example.InvestmentGoalManagementPlatform.utility.AssetType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
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
    @OneToMany
    private List<StockPriceHistory> priceHistories = new ArrayList<>();
    @OneToMany
    private List<Investment> investments = new ArrayList<>();
}

