package com.example.InvestmentGoalManagementPlatform.controller;

import com.example.InvestmentGoalManagementPlatform.DTO.ChangePasswordDTO;
import com.example.InvestmentGoalManagementPlatform.DTO.UserResponseDTO;
import com.example.InvestmentGoalManagementPlatform.DTO.UserUpdateDTO;
import com.example.InvestmentGoalManagementPlatform.service.UserService;
import com.example.InvestmentGoalManagementPlatform.DTO.ChangePasswordDTO;
import com.example.InvestmentGoalManagementPlatform.DTO.UserResponseDTO;
import com.example.InvestmentGoalManagementPlatform.DTO.UserUpdateDTO;
import com.example.InvestmentGoalManagementPlatform.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;


    // Get user profile
    @GetMapping("/{userId}/profile")
    public ResponseEntity<UserResponseDTO> getUserProfile(
            @PathVariable Integer userId
    ) {
        UserResponseDTO response = userService.getUserProfile(userId);
        return ResponseEntity.ok(response);
    }


    // Update user profile
    @PatchMapping("/{userId}/profile")
    public ResponseEntity<UserResponseDTO> updateProfile(
            @PathVariable Integer userId,
            @Valid @RequestBody UserUpdateDTO dto
    ) {
        UserResponseDTO response = userService.updateProfile(userId, dto);
        return ResponseEntity.ok(response);
    }


    // Change password
    @PutMapping("/{userId}/password")
    public ResponseEntity<String> changePassword(
            @PathVariable Integer userId,
            @Valid @RequestBody ChangePasswordDTO dto
    ) {
        userService.changePassword(userId, dto);
        return ResponseEntity.ok("Password changed successfully");
    }


    // Deactivate account
    @PutMapping("/{userId}/deactivate")
    public ResponseEntity<String> deactivateAccount(
            @PathVariable Integer userId
    ) {
        userService.deactivateAccount(userId);
        return ResponseEntity.ok("Account deactivated successfully");
    }
}