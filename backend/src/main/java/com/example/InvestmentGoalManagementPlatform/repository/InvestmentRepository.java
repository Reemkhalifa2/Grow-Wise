package com.example.InvestmentGoalManagementPlatform.repository;

import com.example.InvestmentGoalManagementPlatform.entity.Investment;
import com.example.InvestmentGoalManagementPlatform.entity.InvestmentPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
public interface InvestmentRepository extends JpaRepository<Investment, Integer> {
    Optional<Investment> findByIdAndIsActiveTrue(
            Integer investmentId
    );

    List<Investment> findByUserIdAndIsActiveTrue(
            Integer userId
    );

    List<Investment> findByInvestmentPlanIdAndIsActiveTrue(
            Integer planId
    );

    List<Investment> findByAssetIdAndIsActiveTrue(
            Integer assetId
    );

    List<Investment>
    findByUserIdAndInvestmentPlanIdAndIsActiveTrue(
            Integer userId,
            Integer planId
    );
    @Query("""
       SELECT COALESCE(SUM(i.amountInvested), 0)
       FROM Investment i
       WHERE i.user.id = :userId
       AND i.investmentPlan.id = :planId
       AND i.isActive = true
       AND i.createdDate >= :startDate
       AND i.createdDate < :endDate
       """)
    Double sumMonthlyInvestmentByUserAndPlan(
            @Param("userId") Integer userId,
            @Param("planId") Integer planId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT i FROM Investment i " + "WHERE i.user.id = :userId AND i.isActive = true")
    List<Investment> findByUserId(@Param("userId") Integer userId);

    @Query("SELECT i FROM Investment i " + "WHERE i.investmentPlan.id = :planId AND i.isActive = true")
    List<Investment> findByPlanId(@Param("planId") Integer planId);

    @Query("SELECT i FROM Investment i " + "WHERE i.stock.id = :stockId AND i.isActive = true")
    List<Investment> findByStockId(@Param("stockId") Integer stockId);

    @Query("SELECT i FROM Investment i " + "WHERE i.user.id = :userId AND i.investmentPlan.id = :planId AND i.isActive = true")
    List<Investment> findByUserIdAndPlanId(@Param("userId") Integer userId, @Param("planId") Integer planId);
    long countByIsActiveTrue();

    @Query("""
           SELECT COALESCE(SUM(i.amountInvested), 0)
           FROM Investment i
           WHERE i.isActive = true
           """)
    Double calculateTotalInvestmentAmount();

    @Query("""
           SELECT COALESCE(SUM(i.currentValue), 0)
           FROM Investment i
           WHERE i.isActive = true
           """)
    Double calculateTotalCurrentValue();

    @Query("""
           SELECT COALESCE(
               SUM(
                   COALESCE(i.currentValue, 0)
                   - COALESCE(i.amountInvested, 0)
               ),
               0
           )
           FROM Investment i
           WHERE i.isActive = true
           """)
    Double calculateTotalProfit();
}

