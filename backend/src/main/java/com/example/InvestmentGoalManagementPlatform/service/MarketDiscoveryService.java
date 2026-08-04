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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        discoveredList.addAll(scrapeMeethaqEquityFund());

        // 2. Scrape Oman Gold Rates
        discoveredList.addAll(scrapeOmanGoldPrices());

        // 3. Scrape Popular MSX Stocks
        discoveredList.addAll(scrapeOmanStockMarketIndex());

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


    private List<MarketDiscoveryDTO> scrapeMeethaqEquityFund() {
        List<MarketDiscoveryDTO> list = new ArrayList<>();

        String url =
                "https://www.meethaq.om/en/Pages/equityfund.aspx";

        try {
            Document document = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .referrer("https://www.google.com/")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .timeout(15_000)
                    .followRedirects(true)
                    .get();

            String pageText = document.text()
                    .replace('\u00A0', ' ')
                    .replaceAll("\\s+", " ")
                    .trim();

            Double unitPrice = extractMeethaqUnitPrice(pageText);

            if (unitPrice == null) {
                log.warn(
                        "Meethaq Equity Fund unit price was not found at {}",
                        url
                );

                return list;
            }

            list.add(
                    MarketDiscoveryDTO.builder()
                            .name("Meethaq Equity Fund")
                            .symbol("MEEF")
                            .assetType(AssetType.MUTUAL_FUND)
                            .currentPrice(unitPrice)
                            .currency("OMR")
                            .scrapingUrl(url)
                            .cssSelector("body")
                            .sourceType("MEETHAQ_EQUITY_FUND")
                            .build()
            );

            log.info(
                    "Discovered Meethaq Equity Fund price: {} OMR",
                    unitPrice
            );

        } catch (Exception exception) {
            log.error(
                    "Failed to scrape Meethaq Equity Fund",
                    exception
            );
        }

        return list;
    }

    private Double extractMeethaqUnitPrice(String pageText) {
        if (pageText == null || pageText.isBlank()) {
            return null;
        }

        Pattern pattern = Pattern.compile(
                "Unit Price.*?(?:RO|OMR)\\s*([0-9]+(?:\\.[0-9]+)?)",
                Pattern.CASE_INSENSITIVE
        );

        Matcher matcher = pattern.matcher(pageText);

        if (!matcher.find()) {
            return null;
        }

        try {
            return Double.parseDouble(matcher.group(1));
        } catch (NumberFormatException exception) {
            log.warn(
                    "Could not parse Meethaq unit price from '{}'",
                    matcher.group(1)
            );

            return null;
        }
    }


    private List<MarketDiscoveryDTO> scrapeOmanGoldPrices() {
        List<MarketDiscoveryDTO> list = new ArrayList<>();

        String url =
                "https://www.livepriceofgold.com/oman-gold-price.html";

        try {
            Document document = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .referrer("https://www.google.com/")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .timeout(15_000)
                    .followRedirects(true)
                    .get();

            Element goldRow = document.select("table tr")
                    .stream()
                    .filter(row -> {
                        String text = row.text()
                                .replace('\u00A0', ' ')
                                .toUpperCase();

                        return text.contains("1 GRAM GOLD 24K");
                    })
                    .findFirst()
                    .orElse(null);

            if (goldRow == null) {
                log.warn("24K gold row was not found at {}", url);
                return list;
            }

            Elements cells = goldRow.select("td");

            log.info("Gold row found: {}", goldRow.text());
            log.info("Gold row cell count: {}", cells.size());

            if (cells.size() < 2) {
                log.warn(
                        "24K gold row has insufficient cells: {}",
                        goldRow.text()
                );
                return list;
            }

            String rawPrice = cells.get(1).text();

            double goldPrice = parsePrice(rawPrice);

            list.add(
                    MarketDiscoveryDTO.builder()
                            .name("Oman Gold 24K (Per Gram)")
                            .symbol("GOLD-24K-OMR")
                            .assetType(AssetType.GOLD)
                            .currentPrice(goldPrice)
                            .currency("OMR")
                            .scrapingUrl(url)
                            .cssSelector(
                                    "table tr:contains(1 GRAM GOLD 24K) td:nth-child(2)"
                            )
                            .sourceType("GOLD_LIVE")
                            .build()
            );

            log.info(
                    "Successfully scraped Oman 24K gold price: {} OMR",
                    goldPrice
            );

        } catch (Exception exception) {
            log.error(
                    "Failed to scrape Oman gold price",
                    exception
            );
        }

        return list;
    }

    private double parsePrice(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            throw new IllegalArgumentException(
                    "Gold price value is empty"
            );
        }

        String cleanedValue = rawValue
                .replace(",", "")
                .replaceAll("[^0-9.]", "")
                .trim();

        if (cleanedValue.isBlank()) {
            throw new IllegalArgumentException(
                    "Could not extract a number from: " + rawValue
            );
        }

        return Double.parseDouble(cleanedValue);
    }

    private List<MarketDiscoveryDTO> scrapeOmanStockMarketIndex() {
        List<MarketDiscoveryDTO> list = new ArrayList<>();

        String url =
                "https://tradingeconomics.com/oman/stock-market";

        try {
            Document document = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .referrer("https://www.google.com/")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .timeout(15_000)
                    .followRedirects(true)
                    .get();

            Double indexValue = null;

            for (Element row : document.select("table tr")) {
                String rowText = row.text()
                        .replace('\u00A0', ' ')
                        .trim();

                if (!rowText.toUpperCase().contains("MSM 30")) {
                    continue;
                }

                Elements cells = row.select("td");

                if (cells.size() < 2) {
                    continue;
                }

                String rawValue = cells.get(1).text();
                indexValue = parsePrice(rawValue);
                break;
            }

            if (indexValue == null) {
                log.warn("MSM 30 value was not found at {}", url);
                return list;
            }

            list.add(
                    MarketDiscoveryDTO.builder()
                            .name("Oman Stock Market Index")
                            .symbol("MSM30")
                            .assetType(AssetType.STOCK)
                            .currentPrice(indexValue)
                            .currency("POINTS")
                            .scrapingUrl(url)
                            .cssSelector("table tr:contains(MSM 30) td:nth-child(2)")
                            .sourceType("TRADING_ECONOMICS")
                            .build()
            );

            log.info(
                    "Successfully scraped MSM 30 index: {} points",
                    indexValue
            );

        } catch (Exception exception) {
            log.error(
                    "Failed to scrape Oman stock market index",
                    exception
            );
        }

        return list;
    }
}