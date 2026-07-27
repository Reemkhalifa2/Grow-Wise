package com.example.InvestmentGoalManagementPlatform.Repository;

import com.example.InvestmentGoalManagementPlatform.Entities.Investment;
import com.example.InvestmentGoalManagementPlatform.Entities.InvestmentPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface InvestmentRepository extends JpaRepository<Investment, Integer> {

    @Query("SELECT i FROM Investment i " + "WHERE i.user.id = :userId AND i.isActive = true")
    List<Investment> findByUserId(@Param("userId") Integer userId);

    @Query("SELECT i FROM Investment i " + "WHERE i.investmentPlan.id = :planId AND i.isActive = true")
    List<Investment> findByPlanId(@Param("planId") Integer planId);

    @Query("SELECT i FROM Investment i " + "WHERE i.stock.id = :stockId AND i.isActive = true")
    List<Investment> findByStockId(@Param("stockId") Integer stockId);

    @Query("SELECT i FROM Investment i " + "WHERE i.user.id = :userId AND i.investmentPlan.id = :planId AND i.isActive = true")
    List<Investment> findByUserIdAndPlanId(@Param("userId") Integer userId, @Param("planId") Integer planId);
}

