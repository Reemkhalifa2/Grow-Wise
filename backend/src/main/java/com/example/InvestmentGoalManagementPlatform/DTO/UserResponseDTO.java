package com.example.InvestmentGoalManagementPlatform.DTO;


import com.example.InvestmentGoalManagementPlatform.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDTO {
    private Integer id;
    private String fullName;
    private String email;
    private Integer monthlySalary;
    private Integer monthlyExpenses;

    public static UserResponseDTO fromEntity(User user) {

        UserResponseDTO dto = new UserResponseDTO();

        dto.setId(user.getId());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setMonthlySalary(user.getMonthlySalary());
        dto.setMonthlyExpenses(user.getMonthlyExpenses());

        return dto;
    }
    public static List<UserResponseDTO> fromEntity(List<User> users) {

        List<UserResponseDTO> userDTOList = new ArrayList<>();

        for (User user : users) {
            userDTOList.add(fromEntity(user));
        }

        return userDTOList;
    }
}
