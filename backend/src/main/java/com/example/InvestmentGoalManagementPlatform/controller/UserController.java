package com.example.InvestmentGoalManagementPlatform.controller;

import com.example.InvestmentGoalManagementPlatform.DTO.ChangePasswordDTO;
import com.example.InvestmentGoalManagementPlatform.DTO.UserFinancialSummaryDTO;
import com.example.InvestmentGoalManagementPlatform.DTO.UserResponseDTO;
import com.example.InvestmentGoalManagementPlatform.DTO.UserUpdateDTO;
import com.example.InvestmentGoalManagementPlatform.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class UserController {

    private final UserService userService;

    // Get user profile
    @GetMapping("/{userId}/profile")
    public ResponseEntity<UserResponseDTO> getUserProfile(
            @PathVariable Integer userId
    ) {
        return ResponseEntity.ok(
                userService.getUserProfile(userId)
        );
    }

    // Update user profile
    @PatchMapping("/{userId}/profile")
    public ResponseEntity<UserResponseDTO> updateProfile(
            @PathVariable Integer userId,
            @Valid @RequestBody UserUpdateDTO dto
    ) {
        return ResponseEntity.ok(
                userService.updateProfile(userId, dto)
        );
    }

    // Change password
    @PutMapping("/{userId}/password")
    public ResponseEntity<String> changePassword(
            @PathVariable Integer userId,
            @Valid @RequestBody ChangePasswordDTO dto
    ) {
        userService.changePassword(userId, dto);

        return ResponseEntity.ok(
                "Password changed successfully"
        );
    }

    // Deactivate account
    @PutMapping("/{userId}/deactivate")
    public ResponseEntity<String> deactivateAccount(
            @PathVariable Integer userId
    ) {
        userService.deactivateAccount(userId);

        return ResponseEntity.ok(
                "Account deactivated successfully"
        );
    }

    // Reactivate account
    @PutMapping("/{userId}/reactivate")
    public ResponseEntity<UserResponseDTO> reactivateAccount(
            @PathVariable Integer userId
    ) {
        return ResponseEntity.ok(
                userService.reactivateAccount(userId)
        );
    }

    // Get financial summary
    @GetMapping("/{userId}/financial-summary")
    public ResponseEntity<UserFinancialSummaryDTO> getFinancialSummary(
            @PathVariable Integer userId
    ) {
        return ResponseEntity.ok(
                userService.getFinancialSummary(userId)
        );
    }
}