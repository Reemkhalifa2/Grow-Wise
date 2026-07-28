package com.example.InvestmentGoalManagementPlatform.service;

import com.example.InvestmentGoalManagementPlatform.DTO.FinancialGoalDTO;
import com.example.InvestmentGoalManagementPlatform.entity.FinancialGoal;
import com.example.InvestmentGoalManagementPlatform.entity.User;
import com.example.InvestmentGoalManagementPlatform.exception.ResourceNotFoundException;
import com.example.InvestmentGoalManagementPlatform.repository.FinancialGoalRepository;
import com.example.InvestmentGoalManagementPlatform.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FinancialGoalService {
    FinancialGoalRepository financialGoalRepository;
    UserRepository userRepository;

    @Autowired
    public FinancialGoalService(FinancialGoalRepository financialGoalRepository, UserRepository userRepository) {
        this.financialGoalRepository = financialGoalRepository;
        this.userRepository = userRepository;
    }

    public FinancialGoalDTO createFinancialGoal(FinancialGoalDTO dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        FinancialGoal financialGoal = dto.toEntity();
        financialGoal.setUser(user);
        financialGoal = financialGoalRepository.save(financialGoal);

        return FinancialGoalDTO.fromEntity(financialGoal);
    }

    public FinancialGoalDTO getFinancialGoalById(Integer goalId) {
        FinancialGoal financialGoal = financialGoalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Financial goal not found"));
        return FinancialGoalDTO.fromEntity(financialGoal);
    }

    public List<FinancialGoalDTO> getFinancialGoalsByUserId(Integer userId) {
        return FinancialGoalDTO.fromEntity(financialGoalRepository.findByUserId(userId));
    }

    public List<FinancialGoalDTO> getFinancialGoalsByUserIdAndStatus(Integer userId, String status) {
        return FinancialGoalDTO.fromEntity(financialGoalRepository.findByUserIdAndStatus(userId, status));
    }

    public List<FinancialGoalDTO> getFinancialGoalsByStatus(String status) {
        return FinancialGoalDTO.fromEntity(financialGoalRepository.findByStatus(status));
    }

    public FinancialGoalDTO updateFinancialGoal(Integer goalId, FinancialGoalDTO dto) {
        FinancialGoal financialGoal = financialGoalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Financial goal not found"));

        dto.applyTo(financialGoal);
        financialGoal = financialGoalRepository.save(financialGoal);

        return FinancialGoalDTO.fromEntity(financialGoal);
    }

    // Dedicated method for logging a contribution toward the goal without needing a full DTO
    public FinancialGoalDTO addContribution(Integer goalId, Double amount) {
        FinancialGoal financialGoal = financialGoalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Financial goal not found"));

        double updatedAmount = (financialGoal.getCurrentAmount() != null ? financialGoal.getCurrentAmount() : 0) + amount;
        financialGoal.setCurrentAmount(updatedAmount);

        if (financialGoal.getTargetAmount() != null && updatedAmount >= financialGoal.getTargetAmount()) {
            financialGoal.setStatus("ACHIEVED");
        }

        financialGoal = financialGoalRepository.save(financialGoal);
        return FinancialGoalDTO.fromEntity(financialGoal);
    }

    public void deleteFinancialGoal(Integer goalId) {
        FinancialGoal financialGoal = financialGoalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Financial goal not found"));

        financialGoal.setIsActive(false);
        financialGoalRepository.save(financialGoal);
    }

}
