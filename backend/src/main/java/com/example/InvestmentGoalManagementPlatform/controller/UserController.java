package com.example.InvestmentGoalManagementPlatform.controller;

import com.example.InvestmentGoalManagementPlatform.DTO.*;
import com.example.InvestmentGoalManagementPlatform.service.UserService;
import com.example.InvestmentGoalManagementPlatform.service.taskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class UserController {

    private final UserService userService;
    private final taskService taskService;

    // Get user profile
    @GetMapping("/{userId}/profile")
    public ResponseEntity<UserResponseDTO> getUserProfile(
            @PathVariable Integer userId
    ) {
        return ResponseEntity.ok(userService.getUserProfile(userId));
    }

    // Update user profile
    @PatchMapping("/{userId}/profile")
    public ResponseEntity<UserResponseDTO> updateProfile(
            @PathVariable Integer userId,
            @Valid @RequestBody UserUpdateDTO dto
    ) {
        return ResponseEntity.ok(userService.updateProfile(userId, dto));
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

    // Reactivate account
    @PutMapping("/{userId}/reactivate")
    public ResponseEntity<UserResponseDTO> reactivateAccount(
            @PathVariable Integer userId
    ) {
        return ResponseEntity.ok(userService.reactivateAccount(userId));
    }

    // Financial summary
    @GetMapping("/{userId}/financial-summary")
    public ResponseEntity<UserFinancialSummaryDTO> getFinancialSummary(
            @PathVariable Integer userId
    ) {
        return ResponseEntity.ok(userService.getFinancialSummary(userId));
    }

    // Create Task
    @PostMapping("/{userId}/tasks")
    public ResponseEntity<TaskDTO> createTask(
            @PathVariable Integer userId,
            @Valid @RequestBody TaskDTO dto
    ) {
        dto.setUserId(userId);
        return ResponseEntity.ok(taskService.createTask(dto));
    }

    // Get all tasks
    @GetMapping("/{userId}/tasks")
    public ResponseEntity<List<TaskDTO>> getTasksByUserId(
            @PathVariable Integer userId
    ) {
        return ResponseEntity.ok(taskService.getTasksByUserId(userId));
    }

    // Get tasks by status
    @GetMapping("/{userId}/tasks/status")
    public ResponseEntity<List<TaskDTO>> getTasksByStatus(
            @PathVariable Integer userId,
            @RequestParam Boolean completed
    ) {
        return ResponseEntity.ok(taskService.getTasksByUserIdAndCompleted(userId, completed));
    }

    // Get overdue tasks
    @GetMapping("/{userId}/tasks/overdue")
    public ResponseEntity<List<TaskDTO>> getOverdueTasks(
            @PathVariable Integer userId
    ) {
        return ResponseEntity.ok(taskService.getOverdueTasksForUser(userId));
    }

    // Get task by id
    @GetMapping("/{userId}/tasks/{taskId}")
    public ResponseEntity<TaskDTO> getTaskById(
            @PathVariable Integer taskId
    ) {
        return ResponseEntity.ok(taskService.getTaskById(taskId));
    }

    // Update task
    @PatchMapping("/{userId}/tasks/{taskId}")
    public ResponseEntity<TaskDTO> updateTask(
            @PathVariable Integer userId,
            @PathVariable Integer taskId,
            @RequestBody TaskDTO dto
    ) {
        dto.setUserId(userId);
        return ResponseEntity.ok(taskService.updateTask(taskId, dto));
    }

    // Mark complete
    @PutMapping("/{userId}/tasks/{taskId}/complete")
    public ResponseEntity<TaskDTO> markComplete(
            @PathVariable Integer taskId,
            @RequestParam Boolean completed
    ) {
        return ResponseEntity.ok(taskService.markComplete(taskId, completed));
    }

    // Delete task
    @DeleteMapping("/{userId}/tasks/{taskId}")
    public ResponseEntity<String> deleteTask(
            @PathVariable Integer taskId
    ) {
        taskService.deleteTask(taskId);
        return ResponseEntity.ok("Task deleted successfully");
    }
}