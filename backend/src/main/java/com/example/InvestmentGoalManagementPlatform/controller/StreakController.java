package com.example.InvestmentGoalManagementPlatform.controller;

import com.example.InvestmentGoalManagementPlatform.DTO.StreakResponseDTO;
import com.example.InvestmentGoalManagementPlatform.service.StreakService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/streaks")
public class StreakController {

    private final StreakService streakService;

    public StreakController(StreakService streakService) {
        this.streakService = streakService;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<StreakResponseDTO> getUserStreak(
            @PathVariable Integer userId) {

        return ResponseEntity.ok(
                streakService.getStreakByUserId(userId)
        );
    }
}