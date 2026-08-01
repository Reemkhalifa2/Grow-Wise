package com.example.InvestmentGoalManagementPlatform.service;

import com.example.InvestmentGoalManagementPlatform.DTO.ChangePasswordDTO;
import com.example.InvestmentGoalManagementPlatform.DTO.UserFinancialSummaryDTO;
import com.example.InvestmentGoalManagementPlatform.DTO.UserResponseDTO;
import com.example.InvestmentGoalManagementPlatform.DTO.UserUpdateDTO;
import com.example.InvestmentGoalManagementPlatform.entity.User;
import com.example.InvestmentGoalManagementPlatform.exception.InvalidCredentialsException;
import com.example.InvestmentGoalManagementPlatform.exception.ResourceNotFoundException;
import com.example.InvestmentGoalManagementPlatform.repository.UserRepository;
import com.example.InvestmentGoalManagementPlatform.utility.HelperUtility;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public UserResponseDTO getUserProfile(Integer userId) {
        User user = findUserById(userId);

        return UserResponseDTO.fromEntity(user);
    }

    @Transactional
    public UserResponseDTO updateProfile(
            Integer userId,
            UserUpdateDTO dto
    ) {
        if (dto == null) {
            throw new IllegalArgumentException(
                    "User update data is required"
            );
        }

        User user = findUserById(userId);

        if (HelperUtility.isNotNull(dto.getFullName())) {
            String fullName = dto.getFullName().trim();

            if (fullName.isBlank()) {
                throw new IllegalArgumentException(
                        "Full name cannot be empty"
                );
            }

            user.setFullName(fullName);
        }

        if (HelperUtility.isNotNull(dto.getEmail())) {
            String email = dto.getEmail()
                    .trim()
                    .toLowerCase();

            if (email.isBlank()) {
                throw new IllegalArgumentException(
                        "Email cannot be empty"
                );
            }

            /*
             * Only check for duplicate email when the email
             * is actually different from the current email.
             */
            if (!email.equalsIgnoreCase(user.getEmail())) {
                validateEmailIsAvailable(email, userId);
                user.setEmail(email);
            }
        }

        if (HelperUtility.isNotNull(dto.getMonthlySalary())) {
            validateNonNegativeAmount(
                    dto.getMonthlySalary(),
                    "Monthly salary"
            );

            user.setMonthlySalary(
                    dto.getMonthlySalary()
            );
        }

        if (HelperUtility.isNotNull(dto.getMonthlyExpenses())) {
            validateNonNegativeAmount(
                    dto.getMonthlyExpenses(),
                    "Monthly expenses"
            );

            user.setMonthlyExpenses(
                    dto.getMonthlyExpenses()
            );
        }

        User updatedUser =
                userRepository.save(user);

        return UserResponseDTO.fromEntity(
                updatedUser
        );
    }

    @Transactional
    public void changePassword(
            Integer userId,
            ChangePasswordDTO dto
    ) {
        if (dto == null) {
            throw new IllegalArgumentException(
                    "Password change data is required"
            );
        }

        validatePasswordFields(dto);

        User user = findUserById(userId);

        /*
         * First verify that the current password entered
         * by the user is correct.
         */
        boolean currentPasswordMatches =
                passwordEncoder.matches(
                        dto.getCurrentPassword(),
                        user.getPassword()
                );

        if (!currentPasswordMatches) {
            throw new InvalidCredentialsException(
                    "Current password is incorrect"
            );
        }

        /*
         * Prevent the user from using the same password again.
         */
        boolean newPasswordMatchesCurrent =
                passwordEncoder.matches(
                        dto.getNewPassword(),
                        user.getPassword()
                );

        if (newPasswordMatchesCurrent) {
            throw new InvalidCredentialsException(
                    "New password must be different from current password"
            );
        }

        if (!dto.getNewPassword()
                .equals(dto.getConfirmPassword())) {

            throw new InvalidCredentialsException(
                    "New password and confirmation do not match"
            );
        }

        validatePasswordStrength(
                dto.getNewPassword()
        );

        user.setPassword(
                passwordEncoder.encode(
                        dto.getNewPassword()
                )
        );

        userRepository.save(user);
    }

    @Transactional
    public void deactivateAccount(Integer userId) {
        User user = findUserById(userId);

        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new IllegalStateException(
                    "User account is already inactive"
            );
        }

        user.setIsActive(false);

        userRepository.save(user);
    }

    @Transactional
    public UserResponseDTO reactivateAccount(
            Integer userId
    ) {
        /*
         * This method should preferably be called by an ADMIN.
         * We use findById directly because findUserById may later
         * be changed to return only active users.
         */
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User with id "
                                        + userId
                                        + " not found"
                        )
                );

        if (Boolean.TRUE.equals(user.getIsActive())) {
            throw new IllegalStateException(
                    "User account is already active"
            );
        }

        user.setIsActive(true);

        User updatedUser =
                userRepository.save(user);

        return UserResponseDTO.fromEntity(
                updatedUser
        );
    }

    @Transactional(readOnly = true)
    public UserFinancialSummaryDTO getFinancialSummary(
            Integer userId
    ) {
        User user = findUserById(userId);

        double monthlySalary =
                safeNumber(user.getMonthlySalary());

        double monthlyExpenses =
                safeNumber(user.getMonthlyExpenses());

        double netMonthlySavings =
                Math.max(
                        0,
                        monthlySalary - monthlyExpenses
                );

        double expenseRatio =
                monthlySalary > 0
                        ? monthlyExpenses
                        / monthlySalary
                        * 100
                        : 0;

        double savingsRate =
                monthlySalary > 0
                        ? netMonthlySavings
                        / monthlySalary
                        * 100
                        : 0;

        return UserFinancialSummaryDTO.builder()
                .userId(user.getId())
                .monthlySalary(
                        round(monthlySalary)
                )
                .monthlyExpenses(
                        round(monthlyExpenses)
                )
                .netMonthlySavings(
                        round(netMonthlySavings)
                )
                .expenseRatioPercentage(
                        round(expenseRatio)
                )
                .savingsRatePercentage(
                        round(savingsRate)
                )
                .canInvest(netMonthlySavings > 0)
                .build();
    }

    private User findUserById(Integer userId) {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "User id is required"
            );
        }

        User user =
                userRepository.findByUserId(userId);

        if (HelperUtility.isNull(user)) {
            throw new ResourceNotFoundException(
                    "User with id "
                            + userId
                            + " not found"
            );
        }

        return user;
    }

    private void validateEmailIsAvailable(
            String email,
            Integer currentUserId
    ) {
        User existingUser =
                userRepository.findByEmail(email);

        if (existingUser != null
                && !existingUser.getId()
                .equals(currentUserId)) {

            throw new IllegalStateException(
                    "Email is already used by another account"
            );
        }
    }

    private void validateNonNegativeAmount(
            Integer amount,
            String fieldName
    ) {
        if (amount < 0) {
            throw new IllegalArgumentException(
                    fieldName
                            + " cannot be negative"
            );
        }
    }

    private void validatePasswordFields(
            ChangePasswordDTO dto
    ) {
        if (dto.getCurrentPassword() == null
                || dto.getCurrentPassword().isBlank()) {

            throw new InvalidCredentialsException(
                    "Current password is required"
            );
        }

        if (dto.getNewPassword() == null
                || dto.getNewPassword().isBlank()) {

            throw new InvalidCredentialsException(
                    "New password is required"
            );
        }

        if (dto.getConfirmPassword() == null
                || dto.getConfirmPassword().isBlank()) {

            throw new InvalidCredentialsException(
                    "Password confirmation is required"
            );
        }
    }

    private void validatePasswordStrength(
            String password
    ) {
        if (password.length() < 8) {
            throw new InvalidCredentialsException(
                    "Password must contain at least 8 characters"
            );
        }

        boolean containsUppercase =
                password.chars()
                        .anyMatch(Character::isUpperCase);

        boolean containsLowercase =
                password.chars()
                        .anyMatch(Character::isLowerCase);

        boolean containsNumber =
                password.chars()
                        .anyMatch(Character::isDigit);

        if (!containsUppercase
                || !containsLowercase
                || !containsNumber) {

            throw new InvalidCredentialsException(
                    "Password must contain uppercase, lowercase and number"
            );
        }
    }

    private double safeNumber(Number value) {
        return value == null
                ? 0
                : value.doubleValue();
    }

    private double round(double value) {
        return Math.round(value * 100.0)
                / 100.0;
    }
}