package com.yandex.app.service;

import com.yandex.app.model.Task;
import com.yandex.app.model.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {
    private HistoryManager historyManager;

    @BeforeEach
    void setUp() {
        historyManager = Managers.getDefaultHistory();
    }

    @Test
    void testAddMultipleTasks() {
        LocalDateTime now = LocalDateTime.now();
        Task task1 = new Task(1, "Task 1", "Description 1", Status.NEW, Duration.ofMinutes(30), now);
        Task task2 = new Task(2, "Task 2", "Description 2", Status.IN_PROGRESS, Duration.ofMinutes(45), now.plusHours(1));
        Task task3 = new Task(3, "Task 3", "Description 3", Status.DONE, Duration.ofMinutes(60), now.plusHours(2));

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        List<Task> history = historyManager.getHistory();
        assertEquals(3, history.size(), "History should contain three tasks");
        assertEquals(task1, history.get(0), "First task should be task1");
        assertEquals(task2, history.get(1), "Second task should be task2");
        assertEquals(task3, history.get(2), "Third task should be task3");
    }

    @Test
    void testAddDuplicateTask() {
        LocalDateTime now = LocalDateTime.now();
        Task task1 = new Task(1, "Task 1", "Description 1", Status.NEW, Duration.ofMinutes(30), now);
        Task task2 = new Task(2, "Task 2", "Description 2", Status.IN_PROGRESS, Duration.ofMinutes(45), now.plusHours(1));

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task1);

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size(), "History should contain two tasks");
        assertEquals(task2, history.get(0), "First task should be task2");
        assertEquals(task1, history.get(1), "Second task should be task1 (moved to end)");
    }

    @Test
    void testRemoveTask() {
        LocalDateTime now = LocalDateTime.now();
        Task task1 = new Task(1, "Task 1", "Description 1", Status.NEW, Duration.ofMinutes(30), now);
        Task task2 = new Task(2, "Task 2", "Description 2", Status.IN_PROGRESS, Duration.ofMinutes(45), now.plusHours(1));

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.remove(1);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "History should contain one task");
        assertEquals(task2, history.get(0), "Remaining task should be task2");
    }

    @Test
    void testRemoveNonExistentTask() {
        LocalDateTime now = LocalDateTime.now();
        Task task1 = new Task(1, "Task 1", "Description 1", Status.NEW, Duration.ofMinutes(30), now);
        historyManager.add(task1);
        historyManager.remove(9);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "History should still contain one task");
        assertEquals(task1, history.get(0), "Task1 should remain in history");
    }

    @Test
    void testAddNullTask() {
        historyManager.add(null);
        List<Task> history = historyManager.getHistory();
        assertTrue(history.isEmpty(), "History should be empty when null task is added");
    }

    @Test
    void testPreservesTaskDataInHistory() {
        LocalDateTime now = LocalDateTime.now();
        Task task = new Task(1, "Task 1", "Description 1", Status.NEW, Duration.ofMinutes(30), now);
        historyManager.add(task);
        List<Task> history = historyManager.getHistory();
        Task fromHistory = history.get(0);
        assertEquals(task.getName(), fromHistory.getName(), "Task name should be preserved");
        assertEquals(task.getDescription(), fromHistory.getDescription(), "Task description should be preserved");
        assertEquals(task.getStatus(), fromHistory.getStatus(), "Task status should be preserved");
        assertEquals(task.getDuration(), fromHistory.getDuration(), "Task duration should be preserved");
        assertEquals(task.getStartTime(), fromHistory.getStartTime(), "Task startTime should be preserved");
    }

    @Test
    void testUnlimitedHistoryCapacity() {
        LocalDateTime now = LocalDateTime.now();
        for (int i = 1; i <= 100; i++) {
            Task task = new Task(i, "Task " + i, "Description " + i, Status.NEW, Duration.ofMinutes(30), now.plusHours(i));
            historyManager.add(task);
        }
        List<Task> history = historyManager.getHistory();
        assertEquals(100, history.size(), "History should support unlimited tasks");
        assertEquals(1, history.get(0).getId(), "First task should have ID 1");
        assertEquals(100, history.get(99).getId(), "Last task should have ID 100");
    }
}