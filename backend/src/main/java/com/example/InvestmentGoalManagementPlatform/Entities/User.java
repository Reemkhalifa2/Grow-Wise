package com.example.InvestmentGoalManagementPlatform.Entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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
    @OneToMany
    private List<Task> tasks;
    @OneToMany
    private List<InvestmentPlan> investmentPlans;
    @OneToMany
    private List<FinancialGoal> financialGoals;
    @OneToMany
    private List<Streak> streaks;




}
