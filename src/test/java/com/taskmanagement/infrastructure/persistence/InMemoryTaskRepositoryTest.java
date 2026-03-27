package com.taskmanagement.infrastructure.persistence;

import com.taskmanagement.domain.model.Task;
import com.taskmanagement.domain.model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("InMemoryTaskRepository")
class InMemoryTaskRepositoryTest {

    private InMemoryTaskRepository repository;

    private static final LocalDate TOMORROW      = LocalDate.now().plusDays(1);
    private static final LocalDate IN_THREE_DAYS = LocalDate.now().plusDays(3);
    private static final LocalDate IN_WEEK       = LocalDate.now().plusDays(7);

    @BeforeEach
    void setUp() {
        repository = new InMemoryTaskRepository();
    }

    @Test
    @DisplayName("save should persist a task and findById should return it")
    void saveShouldPersistTask() {
        Task task = buildTask("1", "Task One", TaskStatus.PENDING, TOMORROW);
        repository.save(task);

        Optional<Task> found = repository.findById("1");
        assertTrue(found.isPresent());
        assertEquals("Task One", found.get().getTitle());
    }

    @Test
    @DisplayName("findById should return empty when task does not exist")
    void findByIdShouldReturnEmptyWhenNotFound() {
        Optional<Task> found = repository.findById("non-existent");
        assertTrue(found.isEmpty());
    }

    @Test
    @DisplayName("save should overwrite when saving a task with the same id")
    void saveShouldOverwriteExistingTask() {
        Task original = buildTask("1", "Original", TaskStatus.PENDING, TOMORROW);
        repository.save(original);

        Task updated = buildTask("1", "Updated", TaskStatus.DONE, TOMORROW);
        repository.save(updated);

        Optional<Task> found = repository.findById("1");
        assertTrue(found.isPresent());
        assertEquals("Updated", found.get().getTitle());
        assertEquals(TaskStatus.DONE, found.get().getStatus());
    }

    @Test
    @DisplayName("findAllSortedByDueDate should return tasks in ascending due date order")
    void findAllShouldReturnSortedByDueDate() {
        repository.save(buildTask("c", "C", TaskStatus.PENDING, IN_WEEK));
        repository.save(buildTask("a", "A", TaskStatus.PENDING, TOMORROW));
        repository.save(buildTask("b", "B", TaskStatus.PENDING, IN_THREE_DAYS));

        List<Task> tasks = repository.findAllSortedByDueDate();

        assertEquals(3, tasks.size());
        assertEquals("a", tasks.get(0).getId());
        assertEquals("b", tasks.get(1).getId());
        assertEquals("c", tasks.get(2).getId());
    }

    @Test
    @DisplayName("findAllSortedByDueDate should return empty list when store is empty")
    void findAllShouldReturnEmptyListWhenNoTasks() {
        List<Task> tasks = repository.findAllSortedByDueDate();
        assertTrue(tasks.isEmpty());
    }

    @Test
    @DisplayName("findByStatusSortedByDueDate should return only tasks with matching status, sorted")
    void findByStatusShouldFilterAndSort() {
        repository.save(buildTask("1", "Pending A",     TaskStatus.PENDING,     TOMORROW));
        repository.save(buildTask("2", "Done",          TaskStatus.DONE,        IN_THREE_DAYS));
        repository.save(buildTask("3", "Pending B",     TaskStatus.PENDING,     IN_WEEK));
        repository.save(buildTask("4", "In Progress",   TaskStatus.IN_PROGRESS, TOMORROW));

        List<Task> pending = repository.findByStatusSortedByDueDate(TaskStatus.PENDING);

        assertEquals(2, pending.size());
        assertEquals("1", pending.get(0).getId());
        assertEquals("3", pending.get(1).getId());
    }

    @Test
    @DisplayName("findByStatusSortedByDueDate should return empty list when no tasks match")
    void findByStatusShouldReturnEmptyWhenNoMatch() {
        repository.save(buildTask("1", "Task", TaskStatus.PENDING, TOMORROW));
        List<Task> done = repository.findByStatusSortedByDueDate(TaskStatus.DONE);
        assertTrue(done.isEmpty());
    }

    @Test
    @DisplayName("deleteById should remove task and return true")
    void deleteByIdShouldRemoveTask() {
        repository.save(buildTask("1", "Task", TaskStatus.PENDING, TOMORROW));

        boolean deleted = repository.deleteById("1");

        assertTrue(deleted);
        assertTrue(repository.findById("1").isEmpty());
    }

    @Test
    @DisplayName("deleteById should return false when task does not exist")
    void deleteByIdShouldReturnFalseWhenNotFound() {
        boolean deleted = repository.deleteById("ghost");
        assertFalse(deleted);
    }

    @Test
    @DisplayName("existsById should return true when task exists")
    void existsByIdShouldReturnTrueWhenExists() {
        repository.save(buildTask("1", "Task", TaskStatus.PENDING, TOMORROW));
        assertTrue(repository.existsById("1"));
    }

    @Test
    @DisplayName("existsById should return false when task does not exist")
    void existsByIdShouldReturnFalseWhenMissing() {
        assertFalse(repository.existsById("missing"));
    }

    @Test
    @DisplayName("clear should remove all tasks from the store")
    void clearShouldEmptyStore() {
        repository.save(buildTask("1", "Task", TaskStatus.PENDING, TOMORROW));
        repository.clear();
        assertTrue(repository.findAllSortedByDueDate().isEmpty());
    }

    private Task buildTask(String id, String title, TaskStatus status, LocalDate dueDate) {
        return new Task(id, title, null, status, dueDate);
    }
}
