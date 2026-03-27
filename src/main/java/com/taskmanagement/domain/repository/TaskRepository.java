package com.taskmanagement.domain.repository;

import com.taskmanagement.domain.model.Task;
import com.taskmanagement.domain.model.TaskStatus;

import java.util.List;
import java.util.Optional;

public interface TaskRepository {

    Task save(Task task);

    Optional<Task> findById(String id);

    List<Task> findAllSortedByDueDate();

    List<Task> findByStatusSortedByDueDate(TaskStatus status);

    boolean deleteById(String id);

    boolean existsById(String id);
}
