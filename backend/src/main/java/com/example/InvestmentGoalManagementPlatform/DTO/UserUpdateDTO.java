package com.example.InvestmentGoalManagementPlatform.DTO;

import com.example.InvestmentGoalManagementPlatform.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateDTO {
    private String fullName;
    private String email;
    private Integer monthlySalary;
    private Integer monthlyExpenses;


    public User toEntity() {

        User user = new User();

        user.setFullName(this.fullName);
        user.setEmail(this.email);
        user.setMonthlySalary(this.monthlySalary);
        user.setMonthlyExpenses(this.monthlyExpenses);

        return user;
    }
}
