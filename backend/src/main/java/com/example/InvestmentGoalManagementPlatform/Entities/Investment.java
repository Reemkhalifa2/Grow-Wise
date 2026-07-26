package com.example.InvestmentGoalManagementPlatform.Entities;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Investment extends BaseEntity {

    private Double amountInvested;
    private Integer quantity;
    private Double purchasePrice;
    private LocalDate purchaseDate;

}