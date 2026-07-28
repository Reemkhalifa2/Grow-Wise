package com.example.InvestmentGoalManagementPlatform.service;

import com.example.InvestmentGoalManagementPlatform.DTO.StockDTO;
import com.example.InvestmentGoalManagementPlatform.entity.Stock;
import com.example.InvestmentGoalManagementPlatform.repository.StockRepository;
import com.example.InvestmentGoalManagementPlatform.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class StockService {
    StockRepository stockRepository;

    @Autowired
    public StockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    public StockDTO createStock(StockDTO dto) {
        Stock stock = dto.toEntity();
        stock = stockRepository.save(stock);

        return StockDTO.fromEntity(stock);
    }

    public StockDTO getStockById(Integer stockId) {
        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new ResourceNotFoundException("Stock not found"));
        return StockDTO.fromEntity(stock);
    }

    public StockDTO getStockByTickerSymbol(String tickerSymbol) {
        Stock stock = stockRepository.findByTickerSymbol(tickerSymbol);
        if (stock == null) {
            throw new ResourceNotFoundException("Stock not found");
        }
        return StockDTO.fromEntity(stock);
    }

    public List<StockDTO> getAllStocks() {
        return StockDTO.fromEntity(stockRepository.findAllActive());
    }

    public List<StockDTO> searchStocksByCompanyName(String name) {
        return StockDTO.fromEntity(stockRepository.searchByCompanyName(name));
    }

    public StockDTO updateStock(Integer stockId, StockDTO dto) {
        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new ResourceNotFoundException("Stock not found"));

        dto.applyTo(stock);
        stock = stockRepository.save(stock);

        return StockDTO.fromEntity(stock);
    }

    public StockDTO updatePrice(Integer stockId, Double newPrice) {
        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new ResourceNotFoundException("Stock not found"));

        Double change = newPrice - stock.getCurrentPrice();
        stock.setCurrentPrice(newPrice);
        stock.setDailyChange(change);
        stock.setLastUpdated(LocalDateTime.now());
        stock = stockRepository.save(stock);

        return StockDTO.fromEntity(stock);
    }

    public void deleteStock(Integer stockId) {
        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new ResourceNotFoundException("Stock not found"));

        stock.setIsActive(false);
        stockRepository.save(stock);
    }
}

