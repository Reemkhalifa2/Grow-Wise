package com.example.InvestmentGoalManagementPlatform.entity;

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
public class Task extends BaseEntity {

    private String title;
    private String description;
    private LocalDate dueDate;
    private Boolean completed;

    @ManyToOne
    private User user;



}