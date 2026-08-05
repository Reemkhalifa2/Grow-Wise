package com.example.InvestmentGoalManagementPlatform.controller;

import com.example.InvestmentGoalManagementPlatform.DTO.AvailableAssetDTO;
import com.example.InvestmentGoalManagementPlatform.DTO.MarketDiscoveryDTO;
import com.example.InvestmentGoalManagementPlatform.entity.Asset;
import com.example.InvestmentGoalManagementPlatform.utility.RiskLevel;
import com.example.InvestmentGoalManagementPlatform.service.MarketDiscoveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class MarketDiscoveryController {

    private final MarketDiscoveryService discoveryService;

    // 1. Scrape live feeds and show available market items
    @GetMapping("/discover")
    public ResponseEntity<List<MarketDiscoveryDTO>> discoverMarket() {
        return ResponseEntity.ok(discoveryService.discoverMarketAssets());
    }

    // 2. Add selected asset from discovery feed into DB catalog
    @PostMapping("/add-to-catalog")
    public ResponseEntity<Asset> addDiscoveredToCatalog(
            @RequestBody MarketDiscoveryDTO dto,
            @RequestParam(required = false, defaultValue = "MEDIUM") RiskLevel riskLevel) {

        Asset created = discoveryService.addDiscoveredAssetToCatalog(dto, riskLevel);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/assets/available")
    public ResponseEntity<List<AvailableAssetDTO>>
    getAvailableAssets() {

        return ResponseEntity.ok(
                discoveryService.getAvailableAssets()
        );
    }
}