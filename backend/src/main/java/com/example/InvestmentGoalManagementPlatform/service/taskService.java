package com.example.InvestmentGoalManagementPlatform.service;

import com.example.InvestmentGoalManagementPlatform.DTO.TaskDTO;
import com.example.InvestmentGoalManagementPlatform.entity.Task;
import com.example.InvestmentGoalManagementPlatform.entity.User;
import com.example.InvestmentGoalManagementPlatform.repository.UserRepository;
import com.example.InvestmentGoalManagementPlatform.repository.taskRepository;
import com.example.InvestmentGoalManagementPlatform.exception.ResourceNotFoundException;
import com.example.InvestmentGoalManagementPlatform.utility.HelperUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class taskService {
    taskRepository taskRepository;
    UserRepository userRepository;

    @Autowired
    public taskService(taskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    public TaskDTO createTask(TaskDTO dto) {
        User user = userRepository.findByUserId(dto.getUserId());
        if (HelperUtility.isNull(user)) {
            throw new ResourceNotFoundException(
                    "User not found"
            );
        }

        Task task = dto.toEntity();
        task.setUser(user);
        task = taskRepository.save(task);

        return TaskDTO.fromEntity(task);
    }

    public TaskDTO getTaskById(Integer taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        return TaskDTO.fromEntity(task);
    }

    public List<TaskDTO> getTasksByUserId(Integer userId) {
        return TaskDTO.fromEntity(taskRepository.findByUserId(userId));
    }

    public List<TaskDTO> getTasksByUserIdAndCompleted(Integer userId, Boolean completed) {
        return TaskDTO.fromEntity(taskRepository.findByUserIdAndCompleted(userId, completed));
    }

    public List<TaskDTO> getOverdueTasksForUser(Integer userId) {
        return TaskDTO.fromEntity(taskRepository.findOverdueTasksForUser(userId, LocalDate.now()));
    }

    public TaskDTO updateTask(Integer taskId, TaskDTO dto) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        dto.applyTo(task);
        task = taskRepository.save(task);

        return TaskDTO.fromEntity(task);
    }

    public TaskDTO markComplete(Integer taskId, Boolean completed) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        task.setCompleted(completed);
        task = taskRepository.save(task);

        return TaskDTO.fromEntity(task);
    }

    public void deleteTask(Integer taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        task.setIsActive(false);
        taskRepository.save(task);
    }
}


