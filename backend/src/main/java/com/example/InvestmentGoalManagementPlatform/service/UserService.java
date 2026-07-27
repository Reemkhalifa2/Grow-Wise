package com.example.InvestmentGoalManagementPlatform.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
/*
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserResponseDTO getUserProfile(Long userId) {
        User user = findUserById(userId);
        return user(user);
    }

    public UserResponseDTO updateProfile(Long userId, UserUpdateDTO dto) {
        User user = findUserById(userId);

        if (dto.getFullName() != null) user.setFullName(dto.getFullName());
        if (dto.getEmail() != null) user.setEmail(dto.getEmail());
        if (dto.getPhoneNumber() != null) user.setPhoneNumber(dto.getPhoneNumber());
        if (dto.getMonthlyIncome() != null) user.setMonthlyIncome(dto.getMonthlyIncome());
        if (dto.getMonthlyExpenses() != null) user.setMonthlyExpenses(dto.getMonthlyExpenses());
        if (dto.getInvestmentPreference() != null) user.setInvestmentPreference(dto.getInvestmentPreference());

        // Recalculate saving capacity if income/expenses changed
        user.setSavingCapacity(user.getMonthlyIncome() - user.getMonthlyExpenses());

        User updated = userRepository.save(user);
        return userMapper.toDto(updated);
    }

    public void changePassword(Long userId, ChangePasswordDTO dto) {
        User user = findUserById(userId);

        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Current password is incorrect");
        }

        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new PasswordMismatchException("New password and confirmation do not match");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
    }

    public void deactivateAccount(Long userId) {
        User user = findUserById(userId);
        user.setIsActive(false);
        userRepository.save(user);
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
    }*/
}