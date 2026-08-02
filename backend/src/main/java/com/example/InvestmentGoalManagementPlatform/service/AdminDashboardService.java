package com.example.InvestmentGoalManagementPlatform.service;

import com.example.InvestmentGoalManagementPlatform.DTO.AdminDashboardResponseDTO;
import com.example.InvestmentGoalManagementPlatform.repository.InvestmentRepository;
import com.example.InvestmentGoalManagementPlatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final InvestmentRepository investmentRepository;

    @Transactional(readOnly = true)
    public AdminDashboardResponseDTO getDashboardStatistics() {

        long totalUsers =
                userRepository.count();

        long activeUsers =
                userRepository.countByIsActiveTrue();

        long totalInvestments =
                investmentRepository.count();

        long activeInvestments =
                investmentRepository.countByIsActiveTrue();

        double totalInvestmentAmount =
                safeNumber(
                        investmentRepository
                                .calculateTotalInvestmentAmount()
                );

        double totalCurrentValue =
                safeNumber(
                        investmentRepository
                                .calculateTotalCurrentValue()
                );

        double totalProfit =
                safeNumber(
                        investmentRepository
                                .calculateTotalProfit()
                );

        return AdminDashboardResponseDTO.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .totalInvestments(totalInvestments)
                .activeInvestments(activeInvestments)
                .totalInvestmentAmount(
                        round(totalInvestmentAmount)
                )
                .totalCurrentValue(
                        round(totalCurrentValue)
                )
                .totalProfit(
                        round(totalProfit)
                )
                .build();
    }

    private double safeNumber(Double value) {
        return value == null ? 0.0 : value;
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}