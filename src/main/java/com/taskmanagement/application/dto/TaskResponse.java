package com.taskmanagement.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.taskmanagement.domain.model.Task;
import com.taskmanagement.domain.model.TaskStatus;

import java.time.LocalDate;

/**
 * Outbound DTO representing a {@link Task} in HTTP responses.
 *
 * <p>Keeps the HTTP contract decoupled from the domain model.
 */
public class TaskResponse {

    private String id;
    private String title;
    private String description;
    private TaskStatus status;

    @JsonProperty("due_date")
    private LocalDate dueDate;

    // Constructors
    public TaskResponse() {}

    private TaskResponse(String id, String title, String description, TaskStatus status, LocalDate dueDate) {
        this.id          = id;
        this.title       = title;
        this.description = description;
        this.status      = status;
        this.dueDate     = dueDate;
    }

    /**
     * Factory method to build a {@link TaskResponse} from a domain {@link Task}.
     *
     * @param task the domain entity
     * @return the response DTO
     */
    public static TaskResponse from(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getDueDate()
        );
    }

    // Getters & Setters
    public String getId()             { return id; }
    public void setId(String id)      { this.id = id; }

    public String getTitle()            { return title; }
    public void setTitle(String title)  { this.title = title; }

    public String getDescription()                   { return description; }
    public void setDescription(String description)   { this.description = description; }

    public TaskStatus getStatus()                { return status; }
    public void setStatus(TaskStatus status)     { this.status = status; }

    public LocalDate getDueDate()                { return dueDate; }
    public void setDueDate(LocalDate dueDate)    { this.dueDate = dueDate; }
}
