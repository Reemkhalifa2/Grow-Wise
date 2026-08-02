package com.example.InvestmentGoalManagementPlatform.repository;

import com.example.InvestmentGoalManagementPlatform.entity.Asset;
import com.example.InvestmentGoalManagementPlatform.entity.StockPriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockPriceHistoryRepository extends JpaRepository<StockPriceHistory , Integer> {
    List<StockPriceHistory> findByAssetAndIsActiveTrueOrderByRecordedAtAsc(Asset asset);
}
