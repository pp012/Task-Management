package com.taskmanagement.application.service;

import com.taskmanagement.domain.model.Task;
import com.taskmanagement.domain.model.TaskStatus;
import com.taskmanagement.domain.repository.TaskRepository;
import com.taskmanagement.domain.service.TaskDomainService;
import com.taskmanagement.infrastructure.web.handler.InvalidTaskException;
import com.taskmanagement.infrastructure.web.handler.TaskNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;


@Service
public class TaskService implements TaskDomainService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Override
    public Task createTask(String title, String description, TaskStatus status, String dueDateStr) {
        validateTitle(title);
        LocalDate dueDate = parseFutureDate(dueDateStr);

        TaskStatus resolvedStatus = (status != null) ? status : TaskStatus.PENDING;
        String id = UUID.randomUUID().toString();

        Task task = new Task(id, title, description, resolvedStatus, dueDate);
        return taskRepository.save(task);
    }


    @Override
    public Task getTaskById(String id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }

  
    @Override
    public Task updateTask(String id, String title, String description, TaskStatus status, String dueDateStr) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        LocalDate dueDate = (dueDateStr != null) ? parseFutureDate(dueDateStr) : null;

        if (title != null && title.isBlank()) {
            throw new InvalidTaskException("title must not be blank");
        }

        task.update(title, description, status, dueDate);
        return taskRepository.save(task);
    }


    @Override
    public void deleteTask(String id) {
        boolean deleted = taskRepository.deleteById(id);
        if (!deleted) {
            throw new TaskNotFoundException(id);
        }
    }

    // List with optional filter and pagination

    @Override
    public List<Task> listTasks(TaskStatus status, Integer page, Integer size) {
        List<Task> tasks = (status != null)
                ? taskRepository.findByStatusSortedByDueDate(status)
                : taskRepository.findAllSortedByDueDate();

        return applyPagination(tasks, page, size);
    }

    // Private helpers

    private void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new InvalidTaskException("title is mandatory and must not be blank");
        }
    }

    /**
     * Parses an ISO-8601 date string and enforces that the date is in the future.
     */
    private LocalDate parseFutureDate(String dueDateStr) {
        if (dueDateStr == null || dueDateStr.isBlank()) {
            throw new InvalidTaskException("due_date is mandatory and must not be blank");
        }
        LocalDate dueDate;
        try {
            dueDate = LocalDate.parse(dueDateStr);
        } catch (DateTimeParseException e) {
            throw new InvalidTaskException("due_date must be a valid date in ISO format (yyyy-MM-dd)");
        }
        if (!dueDate.isAfter(LocalDate.now())) {
            throw new InvalidTaskException("due_date must be a future date");
        }
        return dueDate;
    }

    /**
     * Applies zero-based pagination to a list.
     * Returns the full list when pagination params are absent or invalid.
     */
    private List<Task> applyPagination(List<Task> tasks, Integer page, Integer size) {
        if (page == null || size == null || page < 0 || size <= 0) {
            return tasks;
        }
        int fromIndex = page * size;
        if (fromIndex >= tasks.size()) {
            return List.of();
        }
        int toIndex = Math.min(fromIndex + size, tasks.size());
        return tasks.subList(fromIndex, toIndex);
    }
}
