package com.example.InvestmentGoalManagementPlatform.repository;

import com.example.InvestmentGoalManagementPlatform.entity.Asset;
import com.example.InvestmentGoalManagementPlatform.utility.AssetType;
import org.hibernate.internal.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssetRepository extends JpaRepository<Asset, Long> {
    Optional<Asset> findBySymbolAndIsActiveTrue(String symbol);

    List<Asset> findByAssetTypeAndIsActiveTrue(AssetType assetType);

    Optional<Asset> findBySymbolIgnoreCaseAndIsActiveTrue(String symbol);

    boolean existsBySymbolIgnoreCaseAndIsActiveTrue(String symbol);


    List<Asset> findByAssetType(AssetType assetType);

    /**
     * Used by the scheduled scraping job to find only the assets that
     * need their price refreshed and have a valid scraping configuration.
     */
    List<Asset> findByAutoUpdateTrueAndIsActiveTrueAndScrapingUrlIsNotNull();
}