package com.example.InvestmentGoalManagementPlatform.Entities;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockPriceHistory extends BaseEntity {
    private Integer price;
    private LocalDateTime recordedDate;

}
