package com.example.InvestmentGoalManagementPlatform.service;


import com.example.InvestmentGoalManagementPlatform.DTO.UserResponseDTO;
import com.example.InvestmentGoalManagementPlatform.DTO.UserUpdateDTO;
import com.example.InvestmentGoalManagementPlatform.entity.User;
import com.example.InvestmentGoalManagementPlatform.exception.ResourceNotFoundException;
import com.example.InvestmentGoalManagementPlatform.repository.UserRepository;
import com.example.InvestmentGoalManagementPlatform.utility.HelperUtility;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Helper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponseDTO getUserProfile(Integer userId) {
        User user = findUserById(userId);
        return UserResponseDTO.fromEntity(user);
    }

    public UserResponseDTO updateProfile(Integer userId, UserUpdateDTO dto) {
        User user = findUserById(userId);

        if (HelperUtility.isNotNull(dto.getFullName())) user.setFullName(dto.getFullName());
        if (HelperUtility.isNotNull(dto.getEmail())) user.setEmail(dto.getEmail());
        if (HelperUtility.isNotNull(dto.getMonthlySalary())) user.setMonthlySalary(dto.getMonthlySalary());
        if (HelperUtility.isNotNull(dto.getMonthlyExpenses())) user.setMonthlyExpenses(dto.getMonthlyExpenses());

        User updated = userRepository.save(user);
        return UserResponseDTO.fromEntity(updated);
    }



    public void deactivateAccount(Integer userId) {
        User user = findUserById(userId);
        user.setIsActive(false);
        userRepository.save(user);
    }

    private User findUserById(Integer userId) {
        User user = userRepository.findById(userId);
        if(HelperUtility.isNull(user)){
            throw new ResourceNotFoundException("User with this id not found");
        }
        return user;
    }
}