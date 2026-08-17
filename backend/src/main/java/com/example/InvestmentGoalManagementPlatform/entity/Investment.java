package com.example.InvestmentGoalManagementPlatform.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Investment extends BaseEntity {

    private Double amountInvested;
    private Integer quantity;
    private Double purchasePrice;

    /**
     * amountInvested / purchasePrice at the moment this investment was made.
     * Kept separate from {@link #quantity} (unused/legacy) so fractional
     * units are preserved instead of being rounded to a whole number.
     *
     * scale=6: an index-style asset like MSM30 prices in the thousands, so
     * an OMR-sized investment buys a unit count as small as 0.002 — the
     * column's original scale of 2 (left over from an earlier attempt at
     * this same field) silently truncated that to 0.00.
     */
    @Column(precision = 20, scale = 6)
    private BigDecimal unitsPurchased;

    private LocalDate purchaseDate;

    @ManyToOne
    private User user;
    @ManyToOne
    private InvestmentPlan investmentPlan;
    @ManyToOne
    private Stock stock;
    @ManyToOne
    private Asset asset;


}