package com.example.InvestmentGoalManagementPlatform.service;

import com.example.InvestmentGoalManagementPlatform.DTO.StreakDTO;
import com.example.InvestmentGoalManagementPlatform.DTO.StreakResponseDTO;
import com.example.InvestmentGoalManagementPlatform.entity.InvestmentPlan;
import com.example.InvestmentGoalManagementPlatform.entity.Streak;
import com.example.InvestmentGoalManagementPlatform.entity.User;
import com.example.InvestmentGoalManagementPlatform.exception.ResourceNotFoundException;
import com.example.InvestmentGoalManagementPlatform.repository.InvestmentRepository;
import com.example.InvestmentGoalManagementPlatform.repository.StreakRepository;
import com.example.InvestmentGoalManagementPlatform.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

@Service
public class StreakService {

    private final StreakRepository streakRepository;
    private final InvestmentRepository investmentRepository;
    private final UserRepository userRepository;

    public StreakService(
            StreakRepository streakRepository,
            InvestmentRepository investmentRepository,
            UserRepository userRepository
    ) {
        this.streakRepository = streakRepository;
        this.investmentRepository = investmentRepository;
        this.userRepository = userRepository;
    }

    /**
     * Called after creating or updating an investment.
     *
     * The streak increases only when the user's total investments
     * for the current month reach the plan's required monthly amount.
     */
    @Transactional
    public void updateInvestmentStreak(
            User user,
            InvestmentPlan investmentPlan
    ) {
        if (user == null) {
            throw new IllegalArgumentException(
                    "User is required to update streak"
            );
        }

        if (investmentPlan == null) {
            throw new IllegalArgumentException(
                    "Investment plan is required to update streak"
            );
        }

        Double requiredMonthlyAmount =
                investmentPlan.getMonthlyInvestmentAmount();

        if (requiredMonthlyAmount == null
                || requiredMonthlyAmount <= 0) {
            return;
        }

        YearMonth currentMonth = YearMonth.now();

        LocalDateTime startOfCurrentMonth =
                currentMonth
                        .atDay(1)
                        .atStartOfDay();

        LocalDateTime startOfNextMonth =
                currentMonth
                        .plusMonths(1)
                        .atDay(1)
                        .atStartOfDay();

        Double investedThisMonth =
                investmentRepository
                        .sumMonthlyInvestmentByUserAndPlan(
                                user.getId(),
                                investmentPlan.getId(),
                                startOfCurrentMonth,
                                startOfNextMonth
                        );

        double totalInvestedThisMonth =
                investedThisMonth == null
                        ? 0.0
                        : investedThisMonth;

        /*
         * Do not increase the streak until the user reaches
         * the required monthly investment amount.
         */
        if (totalInvestedThisMonth < requiredMonthlyAmount) {
            return;
        }

        Streak streak =
                streakRepository
                        .findByUserIdAndIsActiveTrue(
                                user.getId()
                        )
                        .orElseGet(() ->
                                createNewStreak(user)
                        );

        LocalDate today = LocalDate.now();
        LocalDate lastCheckIn = streak.getLastCheckIn();

        /*
         * The streak has already been counted this month.
         */
        if (lastCheckIn != null
                && YearMonth.from(lastCheckIn)
                .equals(currentMonth)) {
            return;
        }

        YearMonth previousMonth =
                currentMonth.minusMonths(1);

        /*
         * Continue streak if the previous completed month
         * was last month.
         */
        if (lastCheckIn != null
                && YearMonth.from(lastCheckIn)
                .equals(previousMonth)) {

            streak.setCurrentStreak(
                    safeInteger(
                            streak.getCurrentStreak()
                    ) + 1
            );

        } else {

            /*
             * First completed month or streak was interrupted.
             */
            streak.setCurrentStreak(1);
        }

        int currentStreak =
                safeInteger(
                        streak.getCurrentStreak()
                );

        int longestStreak =
                safeInteger(
                        streak.getLongestStreak()
                );

        if (currentStreak > longestStreak) {
            streak.setLongestStreak(
                    currentStreak
            );
        }

        streak.setLastCheckIn(today);
        streak.setIsActive(true);

        streakRepository.save(streak);
    }

    @Transactional(readOnly = true)
    public StreakResponseDTO getStreakByUserId(
            Integer userId
    ) {
        User user =
                userRepository.findByUserId(userId);

        if (user == null) {
            throw new ResourceNotFoundException(
                    "User not found with id: " + userId
            );
        }

        Streak streak =
                streakRepository
                        .findByUserIdAndIsActiveTrue(userId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Streak not found for user: "
                                                + userId
                                )
                        );

        return StreakResponseDTO.fromEntity(streak);
    }

    /**
     * Optional method for resetting a streak manually.
     */
    @Transactional
    public StreakResponseDTO resetStreak(
            Integer userId
    ) {
        Streak streak =
                streakRepository
                        .findByUserIdAndIsActiveTrue(userId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Streak not found for user: "
                                                + userId
                                )
                        );

        streak.setCurrentStreak(0);
        streak.setLastCheckIn(null);

        Streak updatedStreak =
                streakRepository.save(streak);

        return StreakResponseDTO.fromEntity(updatedStreak);
    }

    private Streak createNewStreak(
            User user
    ) {
        Streak streak = new Streak();

        streak.setUser(user);
        streak.setCurrentStreak(0);
        streak.setLongestStreak(0);
        streak.setLastCheckIn(null);
        streak.setIsActive(true);

        return streak;
    }

    private int safeInteger(
            Integer value
    ) {
        return value == null ? 0 : value;
    }
}