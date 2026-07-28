package com.example.InvestmentGoalManagementPlatform.repository;

<<<<<<< HEAD:backend/src/main/java/com/example/InvestmentGoalManagementPlatform/Repository/StockRepository.java
import com.example.InvestmentGoalManagementPlatform.entity.Investment;
=======
>>>>>>> 2521977 (create FinancialGoalService):backend/src/main/java/com/example/InvestmentGoalManagementPlatform/repository/StockRepository.java
import com.example.InvestmentGoalManagementPlatform.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockRepository extends JpaRepository<Stock, Integer> {

    @Query("SELECT s FROM Stock s " + "WHERE s.isActive = true")
    List<Stock> findAllActive();

    @Query("SELECT s FROM Stock s " + "WHERE s.tickerSymbol = :tickerSymbol AND s.isActive = true")
    Stock findByTickerSymbol(@Param("tickerSymbol") String tickerSymbol);

    @Query("SELECT s FROM Stock s " + "WHERE LOWER(s.companyName) LIKE LOWER(CONCAT('%', :name, '%')) AND s.isActive = true")
    List<Stock> searchByCompanyName(@Param("name") String name);
}

