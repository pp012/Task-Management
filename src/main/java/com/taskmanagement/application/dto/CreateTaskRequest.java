package com.taskmanagement.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.taskmanagement.domain.model.TaskStatus;
import jakarta.validation.constraints.NotBlank;

/*
  DTO for POST /tasks.
 */
public class CreateTaskRequest {

	@NotBlank(message = "title is required")
	private String title;

	private String description;

	private TaskStatus status;

	@NotBlank(message = "due_date is required")
	@JsonProperty("due_date")
	private String dueDate;

	public CreateTaskRequest() {
	}

	public CreateTaskRequest(String title, String description, TaskStatus status, String dueDate) {
		this.title = title;
		this.description = description;
		this.status = status;
		this.dueDate = dueDate;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public TaskStatus getStatus() {
		return status;
	}

	public void setStatus(TaskStatus status) {
		this.status = status;
	}

	public String getDueDate() {
		return dueDate;
	}

	public void setDueDate(String dueDate) {
		this.dueDate = dueDate;
	}
}
