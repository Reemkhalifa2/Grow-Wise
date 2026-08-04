package com.example.InvestmentGoalManagementPlatform.controller;

import com.example.InvestmentGoalManagementPlatform.DTO.StreakResponseDTO;
import com.example.InvestmentGoalManagementPlatform.service.StreakService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/streaks")
@CrossOrigin(origins = "http://localhost:4200")

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