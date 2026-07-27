package com.example.InvestmentGoalManagementPlatform.repository;

import com.example.InvestmentGoalManagementPlatform.entity.Asset;
import com.example.InvestmentGoalManagementPlatform.utility.AssetType;
import org.hibernate.internal.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssetRepository extends JpaRepository<Asset, Long> {
    Optional<Asset> findBySymbol(String symbol);
    List<Asset> findByAssetTypeAndActiveTrue(AssetType assetType);
    List<Asset> findByActiveTrue();
}