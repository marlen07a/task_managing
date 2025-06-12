package com.yandex.app.service;

import com.yandex.app.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {
    @TempDir
    File tempDir;
    private File file;

    @Override
    protected FileBackedTaskManager createTaskManager() {
        file = new File(tempDir, "tasks.csv");
        return new FileBackedTaskManager(file);
    }

    @BeforeEach
    void setUp() {
        super.setUp();
    }

    @Test
    void testSaveAndLoad() {
        LocalDateTime now = LocalDateTime.now();
        Task task = new Task("Task", "Task Desc", Status.NEW, Duration.ofMinutes(60), now);
        Epic epic = new Epic("Epic", "Epic Desc");
        int epicId = manager.addEpic(epic);
        Subtask subtask = new Subtask(epicId, "Subtask", "Subtask Desc", Status.DONE, Duration.ofMinutes(30), now.plusHours(2));
        manager.addTask(task);
        manager.addSubtask(subtask);

        FileBackedTaskManager restored = FileBackedTaskManager.loadFromFile(file);
        assertEquals(1, restored.getAllTasks().size(), "Task should be restored");
        assertEquals(1, restored.getAllEpics().size(), "Epic should be restored");
        assertEquals(1, restored.getAllSubtasks().size(), "Subtask should be restored");
        assertEquals(Duration.ofMinutes(60), restored.getAllTasks().get(0).getDuration(), "Task duration should be restored");
        assertEquals(now, restored.getAllTasks().get(0).getStartTime(), "Task startTime should be restored");
        assertEquals(Duration.ofMinutes(30), restored.getAllEpics().get(0).getDuration(), "Epic duration should be restored");
        assertEquals(now.plusHours(2), restored.getAllSubtasks().get(0).getStartTime(), "Subtask startTime should be restored");
    }

    @Test
    void testFileSaveException() {
        File invalidFile = new File(tempDir, "invalid.csv");
        try {
            invalidFile.createNewFile();
            invalidFile.setReadOnly();
            FileBackedTaskManager invalidManager = new FileBackedTaskManager(invalidFile);
            LocalDateTime now = LocalDateTime.now();
            Task task = new Task("Task", "Desc", Status.NEW, Duration.ofMinutes(60), now);
            assertThrows(FileBackedTaskManager.ManagerSaveException.class, () -> invalidManager.addTask(task), "Should throw ManagerSaveException");
        } catch (java.io.IOException e) {
            fail("IOException during test setup");
        }
    }

    @Test
    void testLoadEmptyFile() {
        FileBackedTaskManager restored = FileBackedTaskManager.loadFromFile(file);
        assertTrue(restored.getAllTasks().isEmpty(), "Tasks should be empty for new file");
        assertTrue(restored.getAllEpics().isEmpty(), "Epics should be empty for new file");
        assertTrue(restored.getAllSubtasks().isEmpty(), "Subtasks should be empty for new file");
        assertTrue(restored.getHistory().isEmpty(), "History should be empty for new file");
    }
}