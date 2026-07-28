package com.example.InvestmentGoalManagementPlatform.scheduler;

import com.example.InvestmentGoalManagementPlatform.entity.Asset;
import com.example.InvestmentGoalManagementPlatform.entity.StockPriceHistory;
import com.example.InvestmentGoalManagementPlatform.repository.AssetRepository;
import com.example.InvestmentGoalManagementPlatform.repository.StockPriceHistoryRepository;
import com.example.InvestmentGoalManagementPlatform.service.PriceScrapingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MarketSyncScheduler {

    private final AssetRepository assetRepository;
    private final PriceScrapingService priceScrapingService;
    private final StockPriceHistoryRepository historyRepository;

    // Runs automatically every 25 minutes (1,500,000 ms)
    @Scheduled(fixedRateString = "${asset.sync.interval-ms:1500000}")
    @Transactional
    public void executeTwentyFiveMinPriceSync() {
        List<Asset> activeAssets = assetRepository.findByAutoUpdateTrueAndIsActiveTrueAndScrapingUrlIsNotNull();
        log.info("Starting 25-minute price update for {} active asset(s)...", activeAssets.size());

        for (Asset asset : activeAssets) {
            try {
                Double freshPrice = priceScrapingService.scrapePrice(asset);

                if (freshPrice != null && freshPrice > 0) {
                    asset.setCurrentPrice(freshPrice);
                    assetRepository.save(asset);

                    // Record price history
                    StockPriceHistory history = new StockPriceHistory();
                    history.setAsset(asset);
                    history.setPrice(freshPrice);
                    history.setRecordedAt(LocalDateTime.now());
                    historyRepository.save(history);

                    log.info("Successfully updated [{}] price -> OMR {}", asset.getSymbol(), freshPrice);
                }
            } catch (Exception e) {
                log.warn("Skipped updating [{}] due to error: {}", asset.getSymbol(), e.getMessage());
            }
        }
    }
}