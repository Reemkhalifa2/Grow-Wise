package com.example.InvestmentGoalManagementPlatform.service;

import com.example.InvestmentGoalManagementPlatform.DTO.AssetAdminRequestDTO;
import com.example.InvestmentGoalManagementPlatform.DTO.AssetAdminResponseDTO;
import com.example.InvestmentGoalManagementPlatform.entity.Asset;
import com.example.InvestmentGoalManagementPlatform.entity.StockPriceHistory;
import com.example.InvestmentGoalManagementPlatform.exception.ScrapingException;
import com.example.InvestmentGoalManagementPlatform.repository.AssetRepository;
import com.example.InvestmentGoalManagementPlatform.repository.StockPriceHistoryRepository;
import com.example.InvestmentGoalManagementPlatform.utility.HelperUtility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class AdminService {


    private final AssetRepository assetRepository;
    private final StockPriceHistoryRepository historyRepository;
    private final PriceScrapingService priceScrapingService;

    @Autowired
    public AdminService(AssetRepository assetRepository, StockPriceHistoryRepository historyRepository, PriceScrapingService priceScrapingService) {
        this.assetRepository = assetRepository;
        this.historyRepository = historyRepository;
        this.priceScrapingService = priceScrapingService;
    }

    // 1. Fetch all assets for admin catalog table
    public List<AssetAdminResponseDTO> getAllAssets() {
        return assetRepository.findAll().stream()
                .map(AssetAdminResponseDTO::fromEntity)
                .toList();
    }

    // 2. Add New Asset (Executes immediate scrape test if scrapingUrl + cssSelector are provided)
    public AssetAdminResponseDTO createAsset(AssetAdminRequestDTO request) {

        Asset asset = new Asset();
        asset.setName(request.getName().trim());
        asset.setSymbol(request.getSymbol().trim());
        asset.setAssetType(request.getAssetType());
        asset.setRiskLevel(request.getRiskLevel());
        asset.setScrapingUrl(request.getScrapingUrl());
        asset.setCssSelector(request.getCssSelector());

        asset.setAutoUpdate(Boolean.TRUE.equals(request.getAutoUpdate()));
        String symbol = request.getSymbol().trim().toUpperCase();

        if (HelperUtility.isNotNull(assetRepository.findBySymbolIgnoreCaseAndIsActiveTrue(symbol))) {
            throw new IllegalArgumentException("Asset symbol '" + symbol + "' already exists in catalog.");
        }
        Double initialPrice = HelperUtility.isNotNull(asset.getCurrentPrice()) ? asset.getCurrentPrice() : 0.0;
        if (HelperUtility.isNotNull(asset.getScrapingUrl()) &&
                HelperUtility.isNotNull(asset.getCssSelector())) {
            try {
                initialPrice = priceScrapingService.scrapePrice(asset);
                log.info("Successfully scraped initial price for {}: {} OMR", symbol, initialPrice);
            } catch (ScrapingException e) {
                log.warn("Could not fetch initial price during creation for {}: {}. Falling back to default price.",
                        symbol, e.getMessage());
            }
        }

        // Attempt immediate initial scrape if web scraping config is provided

        asset.setCurrentPrice(initialPrice);

        asset.setIsActive(true);
        asset.setCreatedDate(LocalDateTime.now());
        Asset savedAsset = assetRepository.save(asset);

        // Record initial price history entry
        if (initialPrice > 0.0) {
            StockPriceHistory history = new StockPriceHistory();
            history.setAsset(savedAsset);
            history.setPrice(initialPrice);
            history.setRecordedAt(LocalDateTime.now());
            historyRepository.save(history);
        }

        return AssetAdminResponseDTO.fromEntity(savedAsset);
    }
}