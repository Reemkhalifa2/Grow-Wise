package com.example.InvestmentGoalManagementPlatform.repository;

import com.example.InvestmentGoalManagementPlatform.entity.StockPriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockPriceHistoryRepository extends JpaRepository<StockPriceHistory , Integer> {
}
