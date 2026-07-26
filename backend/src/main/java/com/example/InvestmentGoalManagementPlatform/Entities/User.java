package com.example.InvestmentGoalManagementPlatform.Entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class User extends BaseEntity {
    private String fullName;
    @Column(unique = true)
    private String email;
    private String password;
    private Integer monthlySalary;
    private Integer monthlyExpenses;

}
