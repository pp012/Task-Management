package com.taskmanagement.infrastructure.persistence;

import com.taskmanagement.domain.model.Task;
import com.taskmanagement.domain.model.TaskStatus;
import com.taskmanagement.domain.repository.TaskRepository;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class InMemoryTaskRepository implements TaskRepository {

    private final Map<String, Task> store = new ConcurrentHashMap<>();

    @Override
    public Task save(Task task) {
        store.put(task.getId(), task);
        return task;
    }

    @Override
    public Optional<Task> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Task> findAllSortedByDueDate() {
        return store.values().stream()
                .sorted(Comparator.comparing(Task::getDueDate))
                .collect(Collectors.toList());
    }

    @Override
    public List<Task> findByStatusSortedByDueDate(TaskStatus status) {
        return store.values().stream()
                .filter(t -> t.getStatus() == status)
                .sorted(Comparator.comparing(Task::getDueDate))
                .collect(Collectors.toList());
    }

    @Override
    public boolean deleteById(String id) {
        return store.remove(id) != null;
    }

    @Override
    public boolean existsById(String id) {
        return store.containsKey(id);
    }

    public void clear() {
        store.clear();
    }
}
