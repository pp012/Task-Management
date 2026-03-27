package com.taskmanagement.domain.service;

import com.taskmanagement.domain.model.Task;
import com.taskmanagement.domain.model.TaskStatus;

import java.util.List;

public interface TaskDomainService {

    Task createTask(String title, String description, TaskStatus status, String dueDate);

    Task getTaskById(String id);

    Task updateTask(String id, String title, String description, TaskStatus status, String dueDate);

  
    void deleteTask(String id);

    List<Task> listTasks(TaskStatus status, Integer page, Integer size);
}
