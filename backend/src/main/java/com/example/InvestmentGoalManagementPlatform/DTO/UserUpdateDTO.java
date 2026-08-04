package com.example.InvestmentGoalManagementPlatform.DTO;

import com.example.InvestmentGoalManagementPlatform.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateDTO {

    @Size(
            min = 2,
            max = 100,
            message = "Full name must be between 2 and 100 characters"
    )
    private String fullName;

    @Email(message = "Email format is invalid")
    private String email;

    @PositiveOrZero(
            message = "Monthly salary cannot be negative"
    )
    private Integer monthlySalary;

    @PositiveOrZero(
            message = "Monthly expenses cannot be negative"
    )
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
