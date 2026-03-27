package com.taskmanagement.domain.service;

import com.taskmanagement.application.service.TaskService;
import com.taskmanagement.domain.model.Task;
import com.taskmanagement.domain.model.TaskStatus;
import com.taskmanagement.domain.repository.TaskRepository;
import com.taskmanagement.infrastructure.web.handler.InvalidTaskException;
import com.taskmanagement.infrastructure.web.handler.TaskNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskService (Unit)")
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    private TaskService taskService;

    private static final String VALID_FUTURE_DATE = LocalDate.now().plusDays(5).toString();
    private static final String YESTERDAY         = LocalDate.now().minusDays(1).toString();
    private static final String TODAY             = LocalDate.now().toString();

    @BeforeEach
    void setUp() {
        taskService = new TaskService(taskRepository);
    }


    @Test
    @DisplayName("createTask: should save and return a task with generated id")
    void createTask_shouldSaveAndReturnTask() {
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        Task result = taskService.createTask("Title", "Desc", TaskStatus.PENDING, VALID_FUTURE_DATE);

        assertNotNull(result.getId());
        assertEquals("Title",           result.getTitle());
        assertEquals("Desc",            result.getDescription());
        assertEquals(TaskStatus.PENDING, result.getStatus());
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    @DisplayName("createTask: should default status to PENDING when not provided")
    void createTask_shouldDefaultStatusToPending() {
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        Task result = taskService.createTask("Title", null, null, VALID_FUTURE_DATE);

        assertEquals(TaskStatus.PENDING, result.getStatus());
    }

    @Test
    @DisplayName("createTask: should throw InvalidTaskException when title is blank")
    void createTask_shouldThrowWhenTitleIsBlank() {
        assertThrows(InvalidTaskException.class, () ->
                taskService.createTask("  ", null, null, VALID_FUTURE_DATE));

        verifyNoInteractions(taskRepository);
    }

    @Test
    @DisplayName("createTask: should throw InvalidTaskException when title is null")
    void createTask_shouldThrowWhenTitleIsNull() {
        assertThrows(InvalidTaskException.class, () ->
                taskService.createTask(null, null, null, VALID_FUTURE_DATE));

        verifyNoInteractions(taskRepository);
    }

    @Test
    @DisplayName("createTask: should throw InvalidTaskException when dueDate is null")
    void createTask_shouldThrowWhenDueDateIsNull() {
        assertThrows(InvalidTaskException.class, () ->
                taskService.createTask("Title", null, null, null));

        verifyNoInteractions(taskRepository);
    }

    @Test
    @DisplayName("createTask: should throw InvalidTaskException when dueDate is today")
    void createTask_shouldThrowWhenDueDateIsToday() {
        assertThrows(InvalidTaskException.class, () ->
                taskService.createTask("Title", null, null, TODAY));
    }

    @Test
    @DisplayName("createTask: should throw InvalidTaskException when dueDate is in the past")
    void createTask_shouldThrowWhenDueDateIsInThePast() {
        assertThrows(InvalidTaskException.class, () ->
                taskService.createTask("Title", null, null, YESTERDAY));
    }

    @Test
    @DisplayName("createTask: should throw InvalidTaskException when dueDate format is invalid")
    void createTask_shouldThrowWhenDueDateFormatIsInvalid() {
        assertThrows(InvalidTaskException.class, () ->
                taskService.createTask("Title", null, null, "31-12-2099"));
    }

    @Test
    @DisplayName("getTaskById: should return task when found")
    void getTaskById_shouldReturnTask() {
        Task task = buildTask("id-1", "Title", TaskStatus.PENDING);
        when(taskRepository.findById("id-1")).thenReturn(Optional.of(task));

        Task result = taskService.getTaskById("id-1");

        assertEquals("id-1", result.getId());
        verify(taskRepository).findById("id-1");
    }

    @Test
    @DisplayName("getTaskById: should throw TaskNotFoundException when not found")
    void getTaskById_shouldThrowWhenNotFound() {
        when(taskRepository.findById("ghost")).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> taskService.getTaskById("ghost"));
    }

    @Test
    @DisplayName("updateTask: should update and return the task")
    void updateTask_shouldUpdateAndReturnTask() {
        Task task = buildTask("id-1", "Old Title", TaskStatus.PENDING);
        when(taskRepository.findById("id-1")).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        String newDate = LocalDate.now().plusDays(10).toString();
        Task result = taskService.updateTask("id-1", "New Title", "New desc", TaskStatus.IN_PROGRESS, newDate);

        assertEquals("New Title",            result.getTitle());
        assertEquals("New desc",             result.getDescription());
        assertEquals(TaskStatus.IN_PROGRESS, result.getStatus());
        verify(taskRepository).save(task);
    }

    @Test
    @DisplayName("updateTask: should throw TaskNotFoundException when task not found")
    void updateTask_shouldThrowWhenNotFound() {
        when(taskRepository.findById("ghost")).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () ->
                taskService.updateTask("ghost", "Title", null, null, null));
    }

    @Test
    @DisplayName("updateTask: should throw InvalidTaskException when new title is blank")
    void updateTask_shouldThrowWhenNewTitleIsBlank() {
        Task task = buildTask("id-1", "Title", TaskStatus.PENDING);
        when(taskRepository.findById("id-1")).thenReturn(Optional.of(task));

        assertThrows(InvalidTaskException.class, () ->
                taskService.updateTask("id-1", "   ", null, null, null));
    }

    @Test
    @DisplayName("updateTask: should throw InvalidTaskException when new dueDate is in the past")
    void updateTask_shouldThrowWhenNewDueDateIsInThePast() {
        Task task = buildTask("id-1", "Title", TaskStatus.PENDING);
        when(taskRepository.findById("id-1")).thenReturn(Optional.of(task));

        assertThrows(InvalidTaskException.class, () ->
                taskService.updateTask("id-1", null, null, null, YESTERDAY));
    }

    @Test
    @DisplayName("deleteTask: should call repository deleteById")
    void deleteTask_shouldCallRepository() {
        when(taskRepository.deleteById("id-1")).thenReturn(true);

        assertDoesNotThrow(() -> taskService.deleteTask("id-1"));
        verify(taskRepository).deleteById("id-1");
    }

    @Test
    @DisplayName("deleteTask: should throw TaskNotFoundException when task not found")
    void deleteTask_shouldThrowWhenNotFound() {
        when(taskRepository.deleteById("ghost")).thenReturn(false);

        assertThrows(TaskNotFoundException.class, () -> taskService.deleteTask("ghost"));
    }

    @Test
    @DisplayName("listTasks: should return all tasks when no filter provided")
    void listTasks_shouldReturnAll() {
        List<Task> tasks = List.of(
                buildTask("1", "A", TaskStatus.PENDING),
                buildTask("2", "B", TaskStatus.DONE)
        );
        when(taskRepository.findAllSortedByDueDate()).thenReturn(tasks);

        List<Task> result = taskService.listTasks(null, null, null);

        assertEquals(2, result.size());
        verify(taskRepository).findAllSortedByDueDate();
    }

    @Test
    @DisplayName("listTasks: should filter by status when provided")
    void listTasks_shouldFilterByStatus() {
        List<Task> pendingTasks = List.of(buildTask("1", "A", TaskStatus.PENDING));
        when(taskRepository.findByStatusSortedByDueDate(TaskStatus.PENDING)).thenReturn(pendingTasks);

        List<Task> result = taskService.listTasks(TaskStatus.PENDING, null, null);

        assertEquals(1, result.size());
        assertEquals(TaskStatus.PENDING, result.get(0).getStatus());
        verify(taskRepository).findByStatusSortedByDueDate(TaskStatus.PENDING);
    }

    @Test
    @DisplayName("listTasks: should apply pagination when page and size provided")
    void listTasks_shouldApplyPagination() {
        List<Task> tasks = List.of(
                buildTask("1", "A", TaskStatus.PENDING),
                buildTask("2", "B", TaskStatus.PENDING),
                buildTask("3", "C", TaskStatus.PENDING)
        );
        when(taskRepository.findAllSortedByDueDate()).thenReturn(tasks);

        List<Task> page0 = taskService.listTasks(null, 0, 2);
        List<Task> page1 = taskService.listTasks(null, 1, 2);

        assertEquals(2, page0.size());
        assertEquals("1", page0.get(0).getId());

        assertEquals(1, page1.size());
        assertEquals("3", page1.get(0).getId());
    }

    @Test
    @DisplayName("listTasks: should return empty list when page exceeds available tasks")
    void listTasks_shouldReturnEmptyForOutOfBoundsPage() {
        List<Task> tasks = List.of(buildTask("1", "A", TaskStatus.PENDING));
        when(taskRepository.findAllSortedByDueDate()).thenReturn(tasks);

        List<Task> result = taskService.listTasks(null, 5, 10);

        assertTrue(result.isEmpty());
    }

    private Task buildTask(String id, String title, TaskStatus status) {
        return new Task(id, title, null, status, LocalDate.now().plusDays(5));
    }
}
