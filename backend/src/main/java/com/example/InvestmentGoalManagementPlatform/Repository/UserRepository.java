package com.example.InvestmentGoalManagementPlatform.Repository;

import com.example.InvestmentGoalManagementPlatform.Entities.User;
import org.hibernate.internal.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);
}
