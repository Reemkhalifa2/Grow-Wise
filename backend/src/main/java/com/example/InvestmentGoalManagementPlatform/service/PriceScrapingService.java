package com.example.InvestmentGoalManagementPlatform.service;

import com.example.InvestmentGoalManagementPlatform.entity.Asset;
import com.example.InvestmentGoalManagementPlatform.exception.ScrapingException;
import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.time.Duration;
@Service
public class PriceScrapingService {
    private static final int TIMEOUT_MS = (int) Duration.ofSeconds(10).toMillis();

    public Double scrapePrice(Asset asset) {
        if (asset.getScrapingUrl() == null || asset.getScrapingUrl().isBlank()) {
            throw new ScrapingException("Asset " + asset.getSymbol() + " has no scrapingUrl configured");
        }
        if (asset.getCssSelector() == null || asset.getCssSelector().isBlank()) {
            throw new ScrapingException("Asset " + asset.getSymbol() + " has no cssSelector configured");
        }

        try {
            Document doc = Jsoup.connect(asset.getScrapingUrl())
                    .userAgent("Mozilla/5.0 (compatible; InvestmentGoalPlatformBot/1.0)")
                    .timeout(TIMEOUT_MS)
                    .get();

            Element element = doc.selectFirst(asset.getCssSelector());
            if (element == null) {
                throw new ScrapingException(
                        "CSS selector '" + asset.getCssSelector() + "' matched nothing for " + asset.getSymbol());
            }

            String rawText = element.text();
            return parsePrice(rawText, asset.getSymbol());

        } catch (IOException e) {
            throw new ScrapingException("Failed to fetch " + asset.getScrapingUrl() + " for " + asset.getSymbol(), e);
        }
    }

    /**
     * Strips currency symbols, thousands separators, and whitespace, then parses the
     * remaining numeric text. Handles formats like "OMR 0.3041", "1,234.56", "$45.20".
     */
    private Double parsePrice(String rawText, String symbol) {
        String cleaned = rawText
                .replaceAll("[^0-9.,-]", "")
                .replace(",", "")
                .trim();
        try {
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            throw new ScrapingException(
                    "Could not parse a numeric price from '" + rawText + "' for " + symbol, e);
        }
    }
}
