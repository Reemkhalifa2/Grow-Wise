package com.example.InvestmentGoalManagementPlatform.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Streak extends BaseEntity {

    private Integer currentStreak;
    private Integer longestStreak;
    private LocalDate lastCheckIn;

    @ManyToOne
    private User user;

}