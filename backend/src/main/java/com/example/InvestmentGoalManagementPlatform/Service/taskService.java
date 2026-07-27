package com.example.InvestmentGoalManagementPlatform.Service;

import com.example.InvestmentGoalManagementPlatform.DTO.TaskDTO;
import com.example.InvestmentGoalManagementPlatform.Entities.Task;
import com.example.InvestmentGoalManagementPlatform.Entities.User;
import com.example.InvestmentGoalManagementPlatform.Repositories.taskRepository;
import com.example.InvestmentGoalManagementPlatform.exception.ResourceNotFoundException;
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
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

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


