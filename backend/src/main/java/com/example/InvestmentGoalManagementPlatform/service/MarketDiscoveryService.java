package com.example.InvestmentGoalManagementPlatform.service;

import com.example.InvestmentGoalManagementPlatform.DTO.MarketDiscoveryDTO;
import com.example.InvestmentGoalManagementPlatform.entity.Asset;
import com.example.InvestmentGoalManagementPlatform.entity.StockPriceHistory;
import com.example.InvestmentGoalManagementPlatform.repository.AssetRepository;
import com.example.InvestmentGoalManagementPlatform.repository.StockPriceHistoryRepository;
import com.example.InvestmentGoalManagementPlatform.utility.AssetType;
import com.example.InvestmentGoalManagementPlatform.utility.RiskLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarketDiscoveryService {

    private final AssetRepository assetRepository;
    private final StockPriceHistoryRepository historyRepository;
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";

    /**
     * Scrapes all three market sources (Bank Muscat, Live Gold, and MSX)
     * and flags items already saved in the DB catalog.
     */
    public List<MarketDiscoveryDTO> discoverMarketAssets() {
        List<MarketDiscoveryDTO> discoveredList = new ArrayList<>();

        // 1. Scrape Bank Muscat Mutual Funds
        discoveredList.addAll(scrapeBankMuscatFunds());

        // 2. Scrape Oman Gold Rates
        discoveredList.addAll(scrapeOmanGoldPrices());

        // 3. Scrape Popular MSX Stocks
        discoveredList.addAll(scrapeMSXPopularStocks());

        // Cross-reference with DB to check if already in catalog
        for (MarketDiscoveryDTO dto : discoveredList) {
            boolean exists = assetRepository.existsBySymbolIgnoreCaseAndIsActiveTrue(dto.getSymbol());
            dto.setAlreadyInCatalog(exists);
        }

        return discoveredList;
    }

    /**
     * Convert a discovered market item into a tracked Asset in the database
     */
    @Transactional
    public Asset addDiscoveredAssetToCatalog(MarketDiscoveryDTO dto, RiskLevel riskLevel) {
        String symbol = dto.getSymbol().toUpperCase().trim();

        if (assetRepository.existsBySymbolIgnoreCaseAndIsActiveTrue(symbol)) {
            throw new IllegalArgumentException("Asset symbol '" + symbol + "' is already in your catalog.");
        }

        Asset asset = new Asset();
        asset.setName(dto.getName());
        asset.setSymbol(symbol);
        asset.setAssetType(dto.getAssetType());
        asset.setRiskLevel(riskLevel != null ? riskLevel : RiskLevel.MEDIUM);
        asset.setCurrentPrice(dto.getCurrentPrice() != null ? dto.getCurrentPrice() : 0.0);
        asset.setScrapingUrl(dto.getScrapingUrl());
        asset.setCssSelector(dto.getCssSelector());
        asset.setAutoUpdate(true);
        asset.setIsActive(true);
        Asset saved = assetRepository.save(asset);

        // Record initial price history
        if (saved.getCurrentPrice() > 0.0) {
            StockPriceHistory history = new StockPriceHistory();
            history.setAsset(saved);
            history.setPrice(saved.getCurrentPrice());
            history.setRecordedAt(LocalDateTime.now());
            historyRepository.save(history);
        }

        return saved;
    }

    // --- Private Scraper Implementations ---

    private List<MarketDiscoveryDTO> scrapeBankMuscatFunds() {
        List<MarketDiscoveryDTO> list = new ArrayList<>();
        String url = "https://www.bankmuscat.om/en/Pages/mutualfunds.aspx";

        try {
            Document doc = Jsoup.connect(url).userAgent(USER_AGENT).timeout(10000).get();
            Elements rows = doc.select(".Row");

            for (Element row : rows) {
                Elements cells = row.select(".Cell");
                if (cells.size() >= 3) {
                    String fundName = cells.get(0).text().trim();
                    String currency = cells.get(1).text().trim();
                    String navStr = cells.get(2).text().replaceAll("[^0-9.]", "").trim();

                    if (!fundName.isEmpty() && !navStr.isEmpty()) {
                        Double nav = Double.parseDouble(navStr);
                        String generatedSymbol = "BM-" + fundName.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();

                        list.add(MarketDiscoveryDTO.builder()
                                .name(fundName)
                                .symbol(generatedSymbol)
                                .assetType(AssetType.MUTUAL_FUND)
                                .currentPrice(nav)
                                .currency(currency.isEmpty() ? "OMR" : currency)
                                .scrapingUrl(url)
                                .cssSelector(".Row:contains(" + fundName + ") .Cell:eq(2)")
                                .sourceType("BANK_MUSCAT")
                                .build());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to scrape Bank Muscat Mutual Funds: {}", e.getMessage());
        }
        return list;
    }

    private List<MarketDiscoveryDTO> scrapeOmanGoldPrices() {
        List<MarketDiscoveryDTO> list = new ArrayList<>();
        String url = "https://www.livepriceofgold.com/oman-gold-price.html";

        try {
            Document doc = Jsoup.connect(url).userAgent(USER_AGENT).timeout(10000).get();
            Element priceCell = doc.selectFirst("table tr td:contains(24K), table tr td:contains(Gold)");

            double goldPrice = 28.500; // Fallback estimate in OMR / gram
            if (priceCell != null) {
                Element row = priceCell.parent();
                if (row != null && row.select("td").size() >= 2) {
                    String rawPrice = row.select("td").get(1).text().replaceAll("[^0-9.]", "");
                    if (!rawPrice.isEmpty()) {
                        goldPrice = Double.parseDouble(rawPrice);
                    }
                }
            }

            list.add(MarketDiscoveryDTO.builder()
                    .name("Oman Gold 24K (Per Gram)")
                    .symbol("GOLD-24K-OMR")
                    .assetType(AssetType.GOLD)
                    .currentPrice(goldPrice)
                    .currency("OMR")
                    .scrapingUrl(url)
                    .cssSelector("table tr:has(td:contains(24K)) td:eq(1)")
                    .sourceType("GOLD_LIVE")
                    .build());

        } catch (Exception e) {
            log.error("Failed to scrape Gold Price: {}", e.getMessage());
        }
        return list;
    }

    private List<MarketDiscoveryDTO> scrapeMSXPopularStocks() {
        List<MarketDiscoveryDTO> list = new ArrayList<>();
        String[] symbols = {"BKMB", "OQGN", "BHKF", "AMAT"}; // Top MSX tickers

        for (String sym : symbols) {
            String url = "https://www.msx.om/snapshot.aspx?s=" + sym;
            try {
                Document doc = Jsoup.connect(url).userAgent(USER_AGENT).timeout(8000).get();
                Elements divs = doc.select("body div");
                Double lastPrice = null;

                for (Element el : divs) {
                    String text = el.text().trim();
                    if (text.contains("Last Trade Price")) {
                        String priceStr = text.replace("Last Trade Price", "").replaceAll("[^0-9.]", "").trim();
                        if (!priceStr.isEmpty()) {
                            lastPrice = Double.parseDouble(priceStr);
                            break;
                        }
                    }
                }

                if (lastPrice != null) {
                    list.add(MarketDiscoveryDTO.builder()
                            .name("MSX Listed: " + sym)
                            .symbol(sym)
                            .assetType(AssetType.STOCK)
                            .currentPrice(lastPrice)
                            .currency("OMR")
                            .scrapingUrl(url)
                            .cssSelector("div:contains(Last Trade Price)")
                            .sourceType("MSX")
                            .build());
                }
            } catch (Exception e) {
                log.warn("Could not discover MSX ticker {}: {}", sym, e.getMessage());
            }
        }
        return list;
    }
}