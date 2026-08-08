package com.example.InvestmentGoalManagementPlatform.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForgotPasswordResponse {

    private String message;

    // Dev-mode only: stands in for the emailed link since no mail sender is wired up.
    private String resetToken;
    private String resetLink;
}
