package com.example.InvestmentGoalManagementPlatform.DTO;


import com.example.InvestmentGoalManagementPlatform.entities.User;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class userDTO {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotNull(message = "Monthly salary is required")
    @Min(value = 0, message = "Monthly salary cannot be negative")
    private Integer monthlySalary;

    @NotNull(message = "Monthly expenses is required")
    @Min(value = 0, message = "Monthly expenses cannot be negative")
    private Integer monthlyExpenses;

    public User toEntity() {
        User user = new User();

        user.setFullName(fullName);
        user.setEmail(email);
        user.setPassword(password);
        user.setMonthlySalary(monthlySalary);
        user.setMonthlyExpenses(monthlyExpenses);

        return user;
    }

    public void applyTo(User user) {
        user.setFullName(fullName);
        user.setEmail(email);
        user.setMonthlySalary(monthlySalary);
        user.setMonthlyExpenses(monthlyExpenses);
    }

}
