package com.yandex.app.service;

import com.yandex.app.model.*;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {
    @Override
    protected InMemoryTaskManager createTaskManager() {
        return new InMemoryTaskManager();
    }

    @Test
    void canAddAndFindTaskById() {
        LocalDateTime now = LocalDateTime.now();
        Task task = new Task("Task 1", "Description 1", Status.NEW, Duration.ofMinutes(30), now);
        int taskId = manager.addTask(task);
        assertTrue(manager.getTask(taskId).isPresent(), "Task should be found by ID");
        assertEquals(task, manager.getTask(taskId).get(), "Received task should match added task");
    }

    @Test
    void canAddAndFindEpicById() {
        Epic epic = new Epic("Epic 1", "Description 1");
        int epicId = manager.addEpic(epic);
        assertTrue(manager.getEpic(epicId).isPresent(), "Epic should be found by ID");
        assertEquals(epic, manager.getEpic(epicId).get(), "Received epic should match added epic");
    }

    @Test
    void canAddAndFindSubtaskById() {
        Epic epic = new Epic("Epic 1", "Description 1");
        int epicId = manager.addEpic(epic);
        LocalDateTime now = LocalDateTime.now();
        Subtask subtask = new Subtask(epicId, "Subtask 1", "Description 1", Status.NEW, Duration.ofMinutes(30), now);
        int subtaskId = manager.addSubtask(subtask);
        assertTrue(manager.getSubtask(subtaskId).isPresent(), "Subtask should be found by ID");
        assertEquals(subtask, manager.getSubtask(subtaskId).get(), "Received subtask should match added subtask");
    }

    @Test
    void tasksWithGeneratedAndSetIdsDoNotConflict() {
        LocalDateTime now = LocalDateTime.now();
        Task task1 = new Task("Task 1", "Description 1", Status.NEW, Duration.ofMinutes(30), now);
        Task task2 = new Task(100, "Task 2", "Description 2", Status.NEW, Duration.ofMinutes(30), now.plusHours(1));
        int id1 = manager.addTask(task1);
        int id2 = manager.addTask(task2);
        assertNotEquals(id1, id2, "Generated and set IDs should not conflict");
        assertEquals(task2, manager.getTask(100).get(), "Task with set ID should be found and match added task");
    }

    @Test
    void taskShouldBeAddedWithoutChanging() {
        LocalDateTime now = LocalDateTime.now();
        Task original = new Task("Task 1", "Description 1", Status.NEW, Duration.ofMinutes(30), now);
        int id = manager.addTask(original);
        Task received = manager.getTask(id).get();
        assertEquals(original.getName(), received.getName(), "Name should remain unchanged");
        assertEquals(original.getDescription(), received.getDescription(), "Description should remain unchanged");
        assertEquals(original.getStatus(), received.getStatus(), "Status should remain unchanged");
        assertEquals(original.getDuration(), received.getDuration(), "Duration should remain unchanged");
        assertEquals(original.getStartTime(), received.getStartTime(), "StartTime should remain unchanged");
    }

    @Test
    void epicCannotBeAddedAsItsOwnSubtask() {
        Epic epic = new Epic("Epic 1", "Description 1");
        int epicId = manager.addEpic(epic);
        LocalDateTime now = LocalDateTime.now();
        Subtask subtask = new Subtask(epicId, epicId, "Subtask 1", "Description 1", Status.NEW, Duration.ofMinutes(30), now);
        int subtaskId = manager.addSubtask(subtask);
        assertEquals(-1, subtaskId, "Adding a subtask with the same ID as its epic should return -1");
        assertFalse(epic.getSubtaskIds().contains(epicId), "Epic should not be added as its own subtask");
    }

    @Test
    void updateTaskWithInvalidId() {
        LocalDateTime now = LocalDateTime.now();
        Task task = new Task("Task 1", "Description 1", Status.NEW, Duration.ofMinutes(30), now);
        int taskId = manager.addTask(task);
        Task invalidTask = new Task(999, "Task 2", "Description 2", Status.IN_PROGRESS, Duration.ofMinutes(30), now.plusHours(1));
        manager.updateTask(invalidTask);
        assertEquals(task, manager.getTask(taskId).get(), "Original task should remain unchanged");
        assertFalse(manager.getTask(999).isPresent(), "Invalid task ID should not be added");
    }
}