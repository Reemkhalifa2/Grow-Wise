package com.example.InvestmentGoalManagementPlatform.repository;

import com.example.InvestmentGoalManagementPlatform.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Integer> {
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.isActive = true")
    User findByEmail(@Param("email") String email);
    @Query("SELECT u FROM User u WHERE u.id = :id AND u.isActive = true")
    User findByUserId(@Param("id") Integer id);

    boolean existsByEmail(String email);
    long countByIsActiveTrue();
}
