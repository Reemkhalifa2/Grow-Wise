package com.example.InvestmentGoalManagementPlatform.DTO;

import com.example.InvestmentGoalManagementPlatform.entity.Streak;
import lombok.*;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class StreakResponseDTO {

    private Integer streakId;
    private Integer userId;

    private Integer currentStreak;
    private Integer longestStreak;

    private LocalDate lastCheckIn;

    public static StreakResponseDTO fromEntity(Streak streak) {

        StreakResponseDTO streakResponseDTO = new StreakResponseDTO();
        streakResponseDTO.setStreakId(streak.getId());
        streakResponseDTO.setUserId(streak.getUser().getId());
        streakResponseDTO.setCurrentStreak(streak.getCurrentStreak());
        streakResponseDTO.setLongestStreak(streak.getLongestStreak());
        streakResponseDTO.setLongestStreak(streak.getLongestStreak());
        streakResponseDTO.setLastCheckIn(streak.getLastCheckIn());
        return streakResponseDTO;
    }
}