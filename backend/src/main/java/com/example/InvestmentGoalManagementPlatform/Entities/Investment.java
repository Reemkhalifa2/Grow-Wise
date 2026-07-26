package com.example.InvestmentGoalManagementPlatform.Entities;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Investment extends BaseEntity {

    private Double amountInvested;
    private Integer quantity;
    private Double purchasePrice;
    private LocalDate purchaseDate;

    @ManyToOne
    private User user;
    @ManyToOne
    private InvestmentPlan investmentPlan;
    @ManyToOne
    private Stock stock;

}