package com.example.InvestmentGoalManagementPlatform.Repository;

import com.example.InvestmentGoalManagementPlatform.Entities.Investment;
import com.example.InvestmentGoalManagementPlatform.Entities.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockRepository extends JpaRepository<Stock, Integer> {
}
