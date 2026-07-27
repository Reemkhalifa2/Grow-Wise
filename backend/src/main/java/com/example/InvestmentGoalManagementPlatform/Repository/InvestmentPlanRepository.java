package com.example.InvestmentGoalManagementPlatform.Repository;

import com.example.InvestmentGoalManagementPlatform.Entities.Investment;
import com.example.InvestmentGoalManagementPlatform.Entities.InvestmentPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvestmentPlanRepository extends JpaRepository<InvestmentPlan, Integer> {
}
