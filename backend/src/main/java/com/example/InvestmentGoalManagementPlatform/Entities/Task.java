package com.example.InvestmentGoalManagementPlatform.Entities;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.webmvc.autoconfigure.WebMvcProperties;

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