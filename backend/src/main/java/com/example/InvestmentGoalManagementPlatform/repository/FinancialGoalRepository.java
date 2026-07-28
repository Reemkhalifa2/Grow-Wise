package com.example.InvestmentGoalManagementPlatform.repository;

import com.example.InvestmentGoalManagementPlatform.entity.FinancialGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FinancialGoalRepository extends JpaRepository<FinancialGoal, Integer> {
    @Query("SELECT f FROM FinancialGoal f " + "WHERE f.user.id = :userId AND f.isActive = true")
    List<FinancialGoal> findByUserId(@Param("userId") Integer userId);

    @Query("SELECT f FROM FinancialGoal f " + "WHERE f.user.id = :userId AND f.status = :status AND f.isActive = true")
    List<FinancialGoal> findByUserIdAndStatus(@Param("userId") Integer userId, @Param("status") String status);

    @Query("SELECT f FROM FinancialGoal f " + "WHERE f.status = :status AND f.isActive = true")
    List<FinancialGoal> findByStatus(@Param("status") String status);
}
