package com.example.InvestmentGoalManagementPlatform.entities;

import com.example.InvestmentGoalManagementPlatform.utility.Role;
import jakarta.persistence.*;
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
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
    @OneToMany
    private List<Task> tasks;
    @OneToMany
    private List<InvestmentPlan> investmentPlans;
    @OneToMany
    private List<FinancialGoal> financialGoals;
    @OneToMany
    private List<Streak> streaks;





}
