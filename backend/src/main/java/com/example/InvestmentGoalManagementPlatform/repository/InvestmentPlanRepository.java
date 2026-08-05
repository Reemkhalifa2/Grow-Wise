package com.example.InvestmentGoalManagementPlatform.repository;

import com.example.InvestmentGoalManagementPlatform.entity.Investment;
import com.example.InvestmentGoalManagementPlatform.entity.InvestmentPlan;
import java.util.Optional;

import com.example.InvestmentGoalManagementPlatform.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvestmentPlanRepository extends JpaRepository<InvestmentPlan, Integer> {


    @Query("SELECT p FROM InvestmentPlan p " + "WHERE p.user.id = :userId AND p.isActive = true")
    List<InvestmentPlan> findByUserId(@Param("userId") Integer userId);

    @Query("SELECT p FROM InvestmentPlan p " + "WHERE p.user.id = :userId AND p.status = :status AND p.isActive = true")
    List<InvestmentPlan> findByUserIdAndStatus(@Param("userId") Integer userId, @Param("status") String status);

    @Query("SELECT p FROM InvestmentPlan p " + "WHERE p.status = :status AND p.isActive = true")
    List<InvestmentPlan> findByStatus(@Param("status") String status);

    List<InvestmentPlan> findByUserIdAndIsActiveTrueOrderByCreatedDateDesc(
            Integer userId
    );

    Optional<InvestmentPlan> findByIdAndIsActiveTrue(
            Integer planId
    );

    Optional<InvestmentPlan> findByIdAndUserIdAndIsActiveTrue(
            Integer planId,
            Integer userId
    );
    List<InvestmentPlan> findByUser(User user);
}



