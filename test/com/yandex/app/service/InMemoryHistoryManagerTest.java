package com.yandex.app.service;

import static org.junit.jupiter.api.Assertions.*;
import com.yandex.app.model.Task;
import com.yandex.app.model.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;

class InMemoryHistoryManagerTest {
    private HistoryManager historyManager;

    @BeforeEach
    void setUp() {
        historyManager = Managers.getDefaultHistory();
    }

    @Test
    void limitsHistoryToTenTasks() {
        for (int i = 1; i <= 12; i++) {
            Task task = new Task(i, "Task " + i, "Description " + i, Status.NEW);
            historyManager.add(task);
        }
        List<Task> history = historyManager.getHistory();
        assertEquals(10, history.size(), "History should be limited to 10 tasks");
        assertEquals(3, history.get(0).getId(), "Первый элемент должен иметь ID 1");
    }

    @Test
    void preservesTaskDataInHistory() {
        Task task = new Task(1, "Task 1", "Description 1", Status.NEW);
        historyManager.add(task);
        Task original = new Task(1, "Task 1", "Description 1", Status.NEW);
        List<Task> history = historyManager.getHistory();
        Task fromHistory = history.get(0);
        assertEquals(original.getName(), fromHistory.getName(), "Task name should be preserved");
        assertEquals(original.getDescription(), fromHistory.getDescription(), "Task description should be preserved");
        assertEquals(original.getStatus(), fromHistory.getStatus(), "Task status should be preserved");
    }
}