package com.example.InvestmentGoalManagementPlatform.Repository;

import com.example.InvestmentGoalManagementPlatform.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Integer> {
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.isActive = true")
    User findByEmail(@Param("email") String email);

    boolean existsByEmail(String email);
}
