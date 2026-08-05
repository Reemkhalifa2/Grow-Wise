package com.example.InvestmentGoalManagementPlatform.DTO;

import com.example.InvestmentGoalManagementPlatform.entity.Task;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskDTO {

    private Integer id;
    @NotBlank(message = "Task title is required")
    private String title;

    private String description;

    @FutureOrPresent(message = "Due date cannot be in the past")
    private LocalDate dueDate;

    private Boolean completed;

    private Integer userId;

    // Convert DTO to Entity
    public Task toEntity() {
        Task task = new Task();

        task.setTitle(title);
        task.setDescription(description);
        task.setDueDate(dueDate);
        task.setCompleted(completed);

        return task;
    }

    // Update existing Entity
    public void applyTo(Task task) {
        task.setTitle(title);
        task.setDescription(description);
        task.setDueDate(dueDate);
        task.setCompleted(completed);
    }

    public static TaskDTO fromEntity(Task task) {
        TaskDTO dto = new TaskDTO();

        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setDueDate(task.getDueDate());
        dto.setCompleted(task.getCompleted());

        if (task.getUser() != null) {
            dto.setUserId(task.getUser().getId());
        }

        return dto;
    }

    public static List<TaskDTO> fromEntity(List<Task> tasks) {
        return tasks.stream()
                .map(TaskDTO::fromEntity)
                .toList();
    }
}