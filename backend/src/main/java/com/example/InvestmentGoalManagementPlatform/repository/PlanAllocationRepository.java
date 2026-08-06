package com.example.InvestmentGoalManagementPlatform.repository;

import com.example.InvestmentGoalManagementPlatform.entity.PlanAllocation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanAllocationRepository
        extends JpaRepository<PlanAllocation, Integer> {
}