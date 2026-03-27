package com.taskmanagement.domain.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Task is the core Aggregate Root in the task management bounded context.
 */
public class Task {

    private final String id;
    private String title;
    private String description;
    private TaskStatus status;
    private LocalDate dueDate;

    public Task(String id, String title, String description, TaskStatus status, LocalDate dueDate) {
        this.id          = Objects.requireNonNull(id,      "id must not be null");
        this.title       = Objects.requireNonNull(title,   "title must not be null");
        this.status      = Objects.requireNonNull(status,  "status must not be null");
        this.dueDate     = Objects.requireNonNull(dueDate, "dueDate must not be null");
        this.description = description;
    }

    // Domain behaviour

    /**
     * Updates mutable fields. Passing {@code null} for a field means "no change".
     */
    public void update(String title, String description, TaskStatus status, LocalDate dueDate) {
        if (title != null && !title.isBlank()) {
            this.title = title;
        }
        if (description != null) {
            this.description = description;
        }
        if (status != null) {
            this.status = status;
        }
        if (dueDate != null) {
            this.dueDate = dueDate;
        }
    }


    public String getId()          { return id; }
    public String getTitle()       { return title; }
    public String getDescription() { return description; }
    public TaskStatus getStatus()  { return status; }
    public LocalDate getDueDate()  { return dueDate; }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Task task)) return false;
        return id.equals(task.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Task{id='" + id + "', title='" + title + "', status=" + status + ", dueDate=" + dueDate + '}';
    }
}
