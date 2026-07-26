package com.example.InvestmentGoalManagementPlatform.DTO;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDTO {
    private Integer id;
    private String fullName;
    private String email;
    private Integer monthlySalary;
    private Integer monthlyExpenses;
}
