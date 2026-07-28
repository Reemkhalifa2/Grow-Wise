package com.example.InvestmentGoalManagementPlatform.scheduler;

import com.example.InvestmentGoalManagementPlatform.entity.Asset;
import com.example.InvestmentGoalManagementPlatform.entity.StockPriceHistory;
import com.example.InvestmentGoalManagementPlatform.exception.ScrapingException;
import com.example.InvestmentGoalManagementPlatform.repository.AssetRepository;
import com.example.InvestmentGoalManagementPlatform.service.PriceScrapingService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AssetPriceUpdateScheduler {

    private final AssetRepository assetRepository;
    private final PriceScrapingService priceScrapingService;

    // Every 15 minutes by default - tune via application.yml as needed.
    @Scheduled(fixedRateString = "${asset.price-update.interval-ms:900000}")
    @Transactional
    public void refreshAutoUpdateAssets() {
        List<Asset> assets = assetRepository.findByAutoUpdateTrueAndScrapingUrlIsNotNull();
        log.info("Starting scheduled price refresh for {} asset(s)", assets.size());

        for (Asset asset : assets) {
            try {
                Double newPrice = priceScrapingService.scrapePrice(asset);
                asset.setCurrentPrice(newPrice);

                StockPriceHistory history = new StockPriceHistory();
                history.setAsset(asset);
                history.setPrice(newPrice);
                history.setRecordedAt(LocalDateTime.now());
                asset.getPriceHistories().add(history);

                assetRepository.save(asset);
                log.info("Updated {} ({}) -> {}", asset.getSymbol(), asset.getAssetType(), newPrice);
            } catch (ScrapingException e) {
                // One failing asset should never abort the whole batch.
                log.warn("Skipped price update for {}: {}", asset.getSymbol(), e.getMessage());
            }
        }
    }
}
