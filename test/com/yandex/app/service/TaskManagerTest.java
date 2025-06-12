package com.yandex.app.service;

import com.yandex.app.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest<T extends TaskManager> {
    protected T manager;

    protected abstract T createTaskManager();

    @BeforeEach
    void setUp() {
        manager = createTaskManager();
    }

    @Test
    void testEpicStatusAllNew() {
        Epic epic = new Epic("Epic", "Desc");
        int epicId = manager.addEpic(epic);
        LocalDateTime now = LocalDateTime.now();
        Subtask subtask1 = new Subtask(epicId, "Sub1", "Desc", Status.NEW, Duration.ofMinutes(60), now);
        Subtask subtask2 = new Subtask(epicId, "Sub2", "Desc", Status.NEW, Duration.ofMinutes(60), now.plusHours(2));
        manager.addSubtask(subtask1);
        manager.addSubtask(subtask2);
        assertTrue(manager.getEpic(epicId).isPresent());
        assertEquals(Status.NEW, manager.getEpic(epicId).get().getStatus(), "Epic status should be NEW");
    }

    @Test
    void testEpicStatusAllDone() {
        Epic epic = new Epic("Epic", "Desc");
        int epicId = manager.addEpic(epic);
        LocalDateTime now = LocalDateTime.now();
        Subtask subtask1 = new Subtask(epicId, "Sub1", "Desc", Status.DONE, Duration.ofMinutes(60), now);
        Subtask subtask2 = new Subtask(epicId, "Sub2", "Desc", Status.DONE, Duration.ofMinutes(60), now.plusHours(2));
        manager.addSubtask(subtask1);
        manager.addSubtask(subtask2);
        assertTrue(manager.getEpic(epicId).isPresent());
        assertEquals(Status.DONE, manager.getEpic(epicId).get().getStatus(), "Epic status should be DONE");
    }

    @Test
    void testEpicStatusMixed() {
        Epic epic = new Epic("Epic", "Desc");
        int epicId = manager.addEpic(epic);
        LocalDateTime now = LocalDateTime.now();
        Subtask subtask1 = new Subtask(epicId, "Sub1", "Desc", Status.NEW, Duration.ofMinutes(60), now);
        Subtask subtask2 = new Subtask(epicId, "Sub2", "Desc", Status.DONE, Duration.ofMinutes(60), now.plusHours(2));
        manager.addSubtask(subtask1);
        manager.addSubtask(subtask2);
        assertTrue(manager.getEpic(epicId).isPresent());
        assertEquals(Status.IN_PROGRESS, manager.getEpic(epicId).get().getStatus(), "Epic status should be IN_PROGRESS");
    }

    @Test
    void testEpicStatusInProgress() {
        Epic epic = new Epic("Epic", "Desc");
        int epicId = manager.addEpic(epic);
        LocalDateTime now = LocalDateTime.now();
        Subtask subtask1 = new Subtask(epicId, "Sub1", "Desc", Status.IN_PROGRESS, Duration.ofMinutes(60), now);
        manager.addSubtask(subtask1);
        assertTrue(manager.getEpic(epicId).isPresent());
        assertEquals(Status.IN_PROGRESS, manager.getEpic(epicId).get().getStatus(), "Epic status should be IN_PROGRESS");
    }

    @Test
    void testTaskOverlap() {
        LocalDateTime now = LocalDateTime.now();
        Task task1 = new Task("Task1", "Desc", Status.NEW, Duration.ofMinutes(60), now);
        Task task2 = new Task("Task2", "Desc", Status.NEW, Duration.ofMinutes(60), now.plusMinutes(30));
        manager.addTask(task1);
        assertThrows(IllegalStateException.class, () -> manager.addTask(task2), "Should detect overlap");
    }

    @Test
    void testNonOverlappingTasks() {
        LocalDateTime now = LocalDateTime.now();
        Task task1 = new Task("Task1", "Desc", Status.NEW, Duration.ofMinutes(60), now);
        Task task2 = new Task("Task2", "Desc", Status.NEW, Duration.ofMinutes(60), now.plusHours(2));
        assertDoesNotThrow(() -> {
            manager.addTask(task1);
            manager.addTask(task2);
        });
    }

    @Test
    void testGetTask() {
        LocalDateTime now = LocalDateTime.now();
        Task task = new Task("Task", "Desc", Status.NEW, Duration.ofMinutes(60), now);
        int id = manager.addTask(task);
        assertTrue(manager.getTask(id).isPresent(), "Task should be present");
        assertEquals(task, manager.getTask(id).get(), "Retrieved task should match");
        assertFalse(manager.getTask(999).isPresent(), "Non-existent task should return empty Optional");
    }

    @Test
    void testPrioritizedTasksOrder() {
        LocalDateTime now = LocalDateTime.now();
        Task task1 = new Task("Task1", "Desc", Status.NEW, Duration.ofMinutes(60), now.plusHours(2));
        Task task2 = new Task("Task2", "Desc", Status.NEW, Duration.ofMinutes(60), now);
        manager.addTask(task1);
        manager.addTask(task2);
        assertEquals(task2, manager.getPrioritizedTasks().get(0), "Task with earlier startTime should be first");
        assertEquals(task1, manager.getPrioritizedTasks().get(1), "Task with later startTime should be second");
    }

    @Test
    void testTimeSlotReservation() {
        LocalDateTime now = LocalDateTime.now();
        Task task1 = new Task("Task1", "Desc", Status.NEW, Duration.ofMinutes(30), now);
        manager.addTask(task1);
        Task task2 = new Task("Task2", "Desc", Status.NEW, Duration.ofMinutes(15), now.plusMinutes(15));
        assertThrows(IllegalStateException.class, () -> manager.addTask(task2), "Should detect time slot overlap");
    }
}