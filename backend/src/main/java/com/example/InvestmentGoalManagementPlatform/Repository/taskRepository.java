package com.example.InvestmentGoalManagementPlatform.repository;


import com.example.InvestmentGoalManagementPlatform.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface taskRepository extends JpaRepository<Task, Integer> {

    @Query("SELECT t FROM Task t " + "WHERE t.user.id = :userId AND t.isActive = true")
    List<Task> findByUserId(@Param("userId") Integer userId);

    @Query("SELECT t FROM Task t " + "WHERE t.user.id = :userId AND t.completed = :completed AND t.isActive = true")
    List<Task> findByUserIdAndCompleted(@Param("userId") Integer userId, @Param("completed") Boolean completed);

    @Query("SELECT t FROM Task t " + "WHERE t.user.id = :userId AND t.dueDate < :date AND t.completed = false AND t.isActive = true")
    List<Task> findOverdueTasksForUser(@Param("userId") Integer userId, @Param("date") LocalDate date);

    @Query("SELECT t FROM Task t " + "WHERE t.dueDate < :date AND t.completed = false AND t.isActive = true")
    List<Task> findAllOverdueTasks(@Param("date") LocalDate date);
}