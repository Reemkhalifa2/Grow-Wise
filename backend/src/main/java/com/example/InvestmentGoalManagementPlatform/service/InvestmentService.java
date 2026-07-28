package com.example.InvestmentGoalManagementPlatform.service;

import com.example.InvestmentGoalManagementPlatform.DTO.InvestmentDTO;
import com.example.InvestmentGoalManagementPlatform.entity.Investment;
import com.example.InvestmentGoalManagementPlatform.entity.InvestmentPlan;
import com.example.InvestmentGoalManagementPlatform.entity.Stock;
import com.example.InvestmentGoalManagementPlatform.entity.User;
import com.example.InvestmentGoalManagementPlatform.repository.InvestmentPlanRepository;
import com.example.InvestmentGoalManagementPlatform.repository.InvestmentRepository;
import com.example.InvestmentGoalManagementPlatform.repository.StockRepository;
import com.example.InvestmentGoalManagementPlatform.repository.UserRepository;
import com.example.InvestmentGoalManagementPlatform.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InvestmentService {
    InvestmentRepository investmentRepository;
    UserRepository userRepository;
    InvestmentPlanRepository investmentPlanRepository;
    StockRepository stockRepository;

    @Autowired
    public InvestmentService(InvestmentRepository investmentRepository,
                             UserRepository userRepository,
                             InvestmentPlanRepository investmentPlanRepository,
                            StockRepository stockRepository) {
        this.investmentRepository = investmentRepository;
        this.userRepository = userRepository;
        this.investmentPlanRepository = investmentPlanRepository;
        this.stockRepository = stockRepository;
    }

    public InvestmentDTO createInvestment(InvestmentDTO dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        InvestmentPlan investmentPlan = investmentPlanRepository.findById(dto.getPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("Investment plan not found"));

        Stock stock = stockRepository.findById(dto.getStockId())
                .orElseThrow(() -> new ResourceNotFoundException("Stock not found"));

        Investment investment = dto.toEntity();
        investment.setUser(user);
        investment.setInvestmentPlan(investmentPlan);
        investment.setStock(stock);
        investment = investmentRepository.save(investment);

        return InvestmentDTO.fromEntity(investment);
    }

    public InvestmentDTO getInvestmentById(Integer investmentId) {
        Investment investment = investmentRepository.findById(investmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Investment not found"));
        return InvestmentDTO.fromEntity(investment);
    }

    public List<InvestmentDTO> getInvestmentsByUserId(Integer userId) {
        return InvestmentDTO.fromEntity(investmentRepository.findByUserId(userId));
    }

    public List<InvestmentDTO> getInvestmentsByPlanId(Integer planId) {
        return InvestmentDTO.fromEntity(investmentRepository.findByPlanId(planId));
    }

    public List<InvestmentDTO> getInvestmentsByStockId(Integer stockId) {
        return InvestmentDTO.fromEntity(investmentRepository.findByStockId(stockId));
    }

    public List<InvestmentDTO> getInvestmentsByUserIdAndPlanId(Integer userId, Integer planId) {
        return InvestmentDTO.fromEntity(investmentRepository.findByUserIdAndPlanId(userId, planId));
    }

    public InvestmentDTO updateInvestment(Integer investmentId, InvestmentDTO dto) {
        Investment investment = investmentRepository.findById(investmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Investment not found"));

        dto.applyTo(investment);
        investment = investmentRepository.save(investment);

        return InvestmentDTO.fromEntity(investment);
    }

    public void deleteInvestment(Integer investmentId) {
        Investment investment = investmentRepository.findById(investmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Investment not found"));

        investment.setIsActive(false);
        investmentRepository.save(investment);
    }
}