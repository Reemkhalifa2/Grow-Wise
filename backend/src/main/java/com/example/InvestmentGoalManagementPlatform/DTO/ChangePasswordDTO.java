package com.example.InvestmentGoalManagementPlatform.DTO;

import lombok.Data;

@Data
public class ChangePasswordDTO {
    private String newPassword;
    private String confirmPassword;
}