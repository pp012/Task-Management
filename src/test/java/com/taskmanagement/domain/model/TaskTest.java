package com.taskmanagement.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Task Domain Entity")
class TaskTest {

    private static final String VALID_ID    = "task-001";
    private static final String VALID_TITLE = "Write unit tests";
    private static final LocalDate FUTURE_DATE = LocalDate.now().plusDays(7);

    @Test
    @DisplayName("should create a task with all required fields")
    void shouldCreateTaskWithRequiredFields() {
        Task task = new Task(VALID_ID, VALID_TITLE, "Some description", TaskStatus.PENDING, FUTURE_DATE);

        assertEquals(VALID_ID,    task.getId());
        assertEquals(VALID_TITLE, task.getTitle());
        assertEquals("Some description", task.getDescription());
        assertEquals(TaskStatus.PENDING, task.getStatus());
        assertEquals(FUTURE_DATE, task.getDueDate());
    }

    @Test
    @DisplayName("should allow null description on construction")
    void shouldAllowNullDescription() {
        Task task = new Task(VALID_ID, VALID_TITLE, null, TaskStatus.PENDING, FUTURE_DATE);
        assertNull(task.getDescription());
    }

    @Test
    @DisplayName("should throw NullPointerException when id is null")
    void shouldThrowWhenIdIsNull() {
        assertThrows(NullPointerException.class, () ->
                new Task(null, VALID_TITLE, null, TaskStatus.PENDING, FUTURE_DATE));
    }

    @Test
    @DisplayName("should throw NullPointerException when title is null")
    void shouldThrowWhenTitleIsNull() {
        assertThrows(NullPointerException.class, () ->
                new Task(VALID_ID, null, null, TaskStatus.PENDING, FUTURE_DATE));
    }

    @Test
    @DisplayName("should throw NullPointerException when status is null")
    void shouldThrowWhenStatusIsNull() {
        assertThrows(NullPointerException.class, () ->
                new Task(VALID_ID, VALID_TITLE, null, null, FUTURE_DATE));
    }

    @Test
    @DisplayName("should throw NullPointerException when dueDate is null")
    void shouldThrowWhenDueDateIsNull() {
        assertThrows(NullPointerException.class, () ->
                new Task(VALID_ID, VALID_TITLE, null, TaskStatus.PENDING, null));
    }
    
    @Test
    @DisplayName("should update all fields when non-null values are provided")
    void shouldUpdateAllFields() {
        Task task = new Task(VALID_ID, VALID_TITLE, "Old desc", TaskStatus.PENDING, FUTURE_DATE);

        LocalDate newDate = FUTURE_DATE.plusDays(3);
        task.update("New Title", "New desc", TaskStatus.IN_PROGRESS, newDate);

        assertEquals("New Title",          task.getTitle());
        assertEquals("New desc",           task.getDescription());
        assertEquals(TaskStatus.IN_PROGRESS, task.getStatus());
        assertEquals(newDate,              task.getDueDate());
    }

    @Test
    @DisplayName("should not update fields when null is passed")
    void shouldNotUpdateFieldsWhenNullPassed() {
        Task task = new Task(VALID_ID, VALID_TITLE, "Original desc", TaskStatus.PENDING, FUTURE_DATE);

        task.update(null, null, null, null);

        assertEquals(VALID_TITLE,       task.getTitle());
        assertEquals("Original desc",   task.getDescription());
        assertEquals(TaskStatus.PENDING, task.getStatus());
        assertEquals(FUTURE_DATE,       task.getDueDate());
    }

    @Test
    @DisplayName("should not update title when blank string is passed")
    void shouldNotUpdateTitleWhenBlank() {
        Task task = new Task(VALID_ID, VALID_TITLE, null, TaskStatus.PENDING, FUTURE_DATE);

        task.update("   ", null, null, null);

        assertEquals(VALID_TITLE, task.getTitle());
    }

    @Test
    @DisplayName("two tasks with the same id should be equal")
    void tasksWithSameIdShouldBeEqual() {
        Task t1 = new Task(VALID_ID, VALID_TITLE, null, TaskStatus.PENDING, FUTURE_DATE);
        Task t2 = new Task(VALID_ID, "Different Title", null, TaskStatus.DONE, FUTURE_DATE);

        assertEquals(t1, t2);
        assertEquals(t1.hashCode(), t2.hashCode());
    }

    @Test
    @DisplayName("two tasks with different ids should not be equal")
    void tasksWithDifferentIdsShouldNotBeEqual() {
        Task t1 = new Task("id-1", VALID_TITLE, null, TaskStatus.PENDING, FUTURE_DATE);
        Task t2 = new Task("id-2", VALID_TITLE, null, TaskStatus.PENDING, FUTURE_DATE);

        assertNotEquals(t1, t2);
    }

    @Test
    @DisplayName("toString should include id, title, status and dueDate")
    void toStringShouldContainKeyFields() {
        Task task = new Task(VALID_ID, VALID_TITLE, null, TaskStatus.PENDING, FUTURE_DATE);
        String str = task.toString();

        assertTrue(str.contains(VALID_ID));
        assertTrue(str.contains(VALID_TITLE));
        assertTrue(str.contains("PENDING"));
    }
}
