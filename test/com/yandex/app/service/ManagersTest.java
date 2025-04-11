package com.yandex.app.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ManagersTest {
    @Test
    void getDefault() {
        TaskManager taskManager = Managers.getDefault();
        assertNotNull(taskManager, "TaskManager не должен быть null");
        assertTrue(taskManager instanceof InMemoryTaskManager, "TaskManager is instance of InMemoryTaskManager");

        assertNotNull(taskManager.getAllTasks(), "Tasks list should be initialized");
        assertNotNull(taskManager.getAllEpics(), "Epics list should be initialized");
        assertNotNull(taskManager.getAllSubtasks(), "Subtasks list should be initialized");
        assertNotNull(taskManager.getHistory(), "History should be initialized");
    }

    @Test
    void getDefaultHistory() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        assertNotNull(historyManager, "HistoryManager не должен быть null");
        assertTrue(historyManager instanceof InMemoryHistoryManager, "HistoryManager is instance of InMemoryHistoryManager");

        assertNotNull(historyManager.getHistory(), "History list should be initialized");
        assertTrue(historyManager.getHistory().isEmpty(), "Initial history should be empty");
    }
}