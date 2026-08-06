package com.example.InvestmentGoalManagementPlatform.config;

import com.example.InvestmentGoalManagementPlatform.entity.User;
import com.example.InvestmentGoalManagementPlatform.repository.UserRepository;
import com.example.InvestmentGoalManagementPlatform.utility.Role;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {

    @Bean
    public CommandLineRunner seedData(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {

            System.out.println(
                    "================================="
            );

            System.out.println(
                    "DATA SEEDER STARTED"
            );

            System.out.println(
                    "Current user count: " +
                            userRepository.count()
            );

            User existingUser =
                    userRepository.findByEmail(
                            "seeduser@example.com"
                    );

            if (existingUser != null) {
                System.out.println(
                        "Seed user already exists. ID: " +
                                existingUser.getId()
                );

                System.out.println(
                        "DATA SEEDER FINISHED"
                );

                return;
            }

            User user = new User();

            user.setFullName(
                    "Seed Test User"
            );

            user.setEmail(
                    "seeduser@example.com"
            );

            user.setPassword(
                    passwordEncoder.encode(
                            "User@123"
                    )
            );

            user.setMonthlySalary(
                    2000
            );

            user.setMonthlyExpenses(
                    1000
            );

            user.setRole(
                    Role.USER
            );

            User savedUser =
                    userRepository.saveAndFlush(
                            user
                    );

            System.out.println(
                    "USER SAVED SUCCESSFULLY"
            );

            System.out.println(
                    "Saved user ID: " +
                            savedUser.getId()
            );

            System.out.println(
                    "New user count: " +
                            userRepository.count()
            );

            System.out.println(
                    "DATA SEEDER FINISHED"
            );

            System.out.println(
                    "================================="
            );
        };
    }
}