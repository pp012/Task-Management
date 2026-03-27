package com.taskmanagement.infrastructure.web.controller;

import com.taskmanagement.application.dto.*;
import com.taskmanagement.domain.model.Task;
import com.taskmanagement.domain.model.TaskStatus;
import com.taskmanagement.domain.service.TaskDomainService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskDomainService taskDomainService;

    public TaskController(TaskDomainService taskDomainService) {
        this.taskDomainService = taskDomainService;
    }

   
    /**
     * Creates a new task.
     */
    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody CreateTaskRequest request) {
        Task task = taskDomainService.createTask(
                request.getTitle(),
                request.getDescription(),
                request.getStatus(),
                request.getDueDate()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(TaskResponse.from(task));
    }

    // GET /tasks/{id} — Retrieve a task

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTask(@PathVariable String id) {
        Task task = taskDomainService.getTaskById(id);
        return ResponseEntity.ok(TaskResponse.from(task));
    }

    // PUT /tasks/{id} — Update a task

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable String id,
            @RequestBody UpdateTaskRequest request) {

        Task task = taskDomainService.updateTask(
                id,
                request.getTitle(),
                request.getDescription(),
                request.getStatus(),
                request.getDueDate()
        );
        return ResponseEntity.ok(TaskResponse.from(task));
    }

    // DELETE /tasks/{id} — Delete a task

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable String id) {
        taskDomainService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    // GET /tasks — List all tasks (with optional filter and pagination)

    @GetMapping
    public ResponseEntity<?> listTasks(
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {

        List<Task> tasks = taskDomainService.listTasks(status, page, size);
        List<TaskResponse> responses = tasks.stream().map(TaskResponse::from).toList();

        // Return paginated wrapper when pagination params are provided
        if (page != null && size != null && page >= 0 && size > 0) {
            List<Task> allTasks = taskDomainService.listTasks(status, null, null);
            PagedResponse<TaskResponse> paged = new PagedResponse<>(
                    responses, page, size, allTasks.size()
            );
            return ResponseEntity.ok(paged);
        }

        return ResponseEntity.ok(responses);
    }
}
