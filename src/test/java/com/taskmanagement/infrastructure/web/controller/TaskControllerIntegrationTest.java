package com.taskmanagement.infrastructure.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanagement.application.dto.CreateTaskRequest;
import com.taskmanagement.application.dto.UpdateTaskRequest;
import com.taskmanagement.domain.model.TaskStatus;
import com.taskmanagement.infrastructure.persistence.InMemoryTaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("TaskController (Integration)")
class TaskControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InMemoryTaskRepository repository;

    private static final String BASE_URL      = "/tasks";
    private static final String FUTURE_DATE   = LocalDate.now().plusDays(10).toString();
    private static final String FURTHER_DATE  = LocalDate.now().plusDays(20).toString();

    @BeforeEach
    void clearStore() {
        repository.clear();
    }

    @Test
    @DisplayName("POST /tasks: should create a task and return 201")
    void shouldCreateTask() throws Exception {
        CreateTaskRequest req = new CreateTaskRequest("Buy groceries", "Milk and eggs", null, FUTURE_DATE);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.title").value("Buy groceries"))
                .andExpect(jsonPath("$.description").value("Milk and eggs"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.due_date").value(FUTURE_DATE));
    }

    @Test
    @DisplayName("POST /tasks: should default status to PENDING")
    void shouldDefaultStatusToPending() throws Exception {
        CreateTaskRequest req = new CreateTaskRequest("Task", null, null, FUTURE_DATE);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("POST /tasks: should return 400 when title is missing")
    void shouldReturn400WhenTitleMissing() throws Exception {
        CreateTaskRequest req = new CreateTaskRequest(null, null, null, FUTURE_DATE);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }

    @Test
    @DisplayName("POST /tasks: should return 400 when due_date is missing")
    void shouldReturn400WhenDueDateMissing() throws Exception {
        CreateTaskRequest req = new CreateTaskRequest("Title", null, null, null);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /tasks: should return 400 when due_date is in the past")
    void shouldReturn400WhenDueDateInPast() throws Exception {
        CreateTaskRequest req = new CreateTaskRequest("Title", null, null,
                LocalDate.now().minusDays(1).toString());

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("future")));
    }

    @Test
    @DisplayName("POST /tasks: should return 400 when due_date format is invalid")
    void shouldReturn400WhenDueDateFormatInvalid() throws Exception {
        CreateTaskRequest req = new CreateTaskRequest("Title", null, null, "not-a-date");

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("GET /tasks/{id}: should return task when it exists")
    void shouldGetTaskById() throws Exception {
        String id = createTask("Get me", FUTURE_DATE);

        mockMvc.perform(get(BASE_URL + "/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.title").value("Get me"));
    }

    @Test
    @DisplayName("GET /tasks/{id}: should return 404 when task not found")
    void shouldReturn404WhenTaskNotFound() throws Exception {
        mockMvc.perform(get(BASE_URL + "/non-existent-id"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    @DisplayName("PUT /tasks/{id}: should update task and return 200")
    void shouldUpdateTask() throws Exception {
        String id = createTask("Original Title", FUTURE_DATE);
        UpdateTaskRequest req = new UpdateTaskRequest("Updated Title", "Updated desc", TaskStatus.IN_PROGRESS, FURTHER_DATE);

        mockMvc.perform(put(BASE_URL + "/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.description").value("Updated desc"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.due_date").value(FURTHER_DATE));
    }

    @Test
    @DisplayName("PUT /tasks/{id}: should return 404 when task not found")
    void shouldReturn404OnUpdateWhenNotFound() throws Exception {
        UpdateTaskRequest req = new UpdateTaskRequest("Title", null, null, null);

        mockMvc.perform(put(BASE_URL + "/ghost")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /tasks/{id}: should return 400 when updating with past due_date")
    void shouldReturn400OnUpdateWithPastDate() throws Exception {
        String id = createTask("Title", FUTURE_DATE);
        UpdateTaskRequest req = new UpdateTaskRequest(null, null, null,
                LocalDate.now().minusDays(1).toString());

        mockMvc.perform(put(BASE_URL + "/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /tasks/{id}: should apply only provided fields (partial update)")
    void shouldApplyPartialUpdate() throws Exception {
        String id = createTask("Original", FUTURE_DATE);
        UpdateTaskRequest req = new UpdateTaskRequest(null, null, TaskStatus.DONE, null);

        mockMvc.perform(put(BASE_URL + "/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Original"))
                .andExpect(jsonPath("$.status").value("DONE"));
    }

    @Test
    @DisplayName("DELETE /tasks/{id}: should return 204 on successful deletion")
    void shouldDeleteTask() throws Exception {
        String id = createTask("Delete me", FUTURE_DATE);

        mockMvc.perform(delete(BASE_URL + "/" + id))
                .andExpect(status().isNoContent());

        // Verify it's gone
        mockMvc.perform(get(BASE_URL + "/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /tasks/{id}: should return 404 when task not found")
    void shouldReturn404OnDeleteWhenNotFound() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/ghost"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /tasks: should return all tasks sorted by due_date")
    void shouldListAllTasksSortedByDueDate() throws Exception {
        createTask("Later Task", FURTHER_DATE);
        createTask("Earlier Task", FUTURE_DATE);

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title").value("Earlier Task"))
                .andExpect(jsonPath("$[1].title").value("Later Task"));
    }

    @Test
    @DisplayName("GET /tasks: should filter tasks by status")
    void shouldFilterTasksByStatus() throws Exception {
        createTaskWithStatus("Pending Task",      FUTURE_DATE,  TaskStatus.PENDING);
        createTaskWithStatus("Done Task",         FURTHER_DATE, TaskStatus.DONE);
        createTaskWithStatus("InProgress Task",   FUTURE_DATE,  TaskStatus.IN_PROGRESS);

        mockMvc.perform(get(BASE_URL).param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Pending Task"));
    }

    @Test
    @DisplayName("GET /tasks: should paginate results when page and size are provided")
    void shouldPaginateResults() throws Exception {
        createTask("Task A", FUTURE_DATE);
        createTask("Task B", FUTURE_DATE);
        createTask("Task C", FUTURE_DATE);

        mockMvc.perform(get(BASE_URL).param("page", "0").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(3));
    }

    @Test
    @DisplayName("GET /tasks: should return empty array when no tasks exist")
    void shouldReturnEmptyArrayWhenNoTasks() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    private String createTask(String title, String dueDate) throws Exception {
        return createTaskWithStatus(title, dueDate, null);
    }

    private String createTaskWithStatus(String title, String dueDate, TaskStatus status) throws Exception {
        CreateTaskRequest req = new CreateTaskRequest(title, null, status, dueDate);

        MvcResult result = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
    }
}
