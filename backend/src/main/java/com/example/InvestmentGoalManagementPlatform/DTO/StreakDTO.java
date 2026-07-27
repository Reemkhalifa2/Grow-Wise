package com.example.InvestmentGoalManagementPlatform.DTO;

import com.example.InvestmentGoalManagementPlatform.entities.Streak;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StreakDTO {

    private Integer currentStreak;
    private Integer longestStreak;

    private LocalDate lastCheckIn;

    @NotNull(message = "User ID is required")
    private Integer userId;


    public Streak toEntity() {
        Streak streak = new Streak();

        streak.setCurrentStreak(currentStreak);
        streak.setLongestStreak(longestStreak);
        streak.setLastCheckIn(lastCheckIn);

        return streak;
    }


    public void applyTo(Streak streak) {
        streak.setCurrentStreak(currentStreak);
        streak.setLongestStreak(longestStreak);
        streak.setLastCheckIn(lastCheckIn);
    }
}