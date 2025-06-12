package com.yandex.app.model;

import com.yandex.app.service.InMemoryTaskManager;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {
    @Test
    void testEquals() {
        Epic epic1 = new Epic(1, "Epic 1", "Description 1");
        Epic epic2 = new Epic(1, "Epic 2", "Description 2");
        Epic epic3 = new Epic(2, "Epic 1", "Description 1");

        assertEquals(epic1, epic2, "Epics with the same ID should be equal");
        assertNotEquals(epic1, epic3, "Epics with different ID should not be equal");
    }

    @Test
    void testHashCode() {
        Epic epic1 = new Epic(1, "Epic 1", "Description 1");
        Epic epic2 = new Epic(1, "Epic 2", "Description 2");
        ;

        assertEquals(epic1.hashCode(), epic2.hashCode(), "Hash codes should be equal if ids are equal");
    }

    @Test
    void testSubtaskManagement() {
        Epic epic = new Epic("Epic", "Description");

        epic.addSubtaskId(1);
        epic.addSubtaskId(2);

        assertTrue(epic.getSubtaskIds().contains(1), "Epic should contain subtask 1");
        assertTrue(epic.getSubtaskIds().contains(2), "Epic should contain subtask 2");

        epic.removeSubtaskId(1);
        assertFalse(epic.getSubtaskIds().contains(1), "Epic should not contain subtask 1 after removal");

        epic.clearSubtasks();
        assertTrue(epic.getSubtaskIds().isEmpty(), "Subtask list should be empty after clear");
    }

    @Test
    void testStatusEpic() {
        Epic epic = new Epic("Test", "Description");
        assertEquals(Status.NEW, epic.getStatus(), "Initial status should be NEW");

        epic.setStatusEpic(Status.IN_PROGRESS);
        assertEquals(Status.IN_PROGRESS, epic.getStatus(), "Status should be IN_PROGRESS");

        epic.setStatusEpic(Status.DONE);
        assertEquals(Status.DONE, epic.getStatus(), "Status should be DONE");
    }

    @Test
    void testUpdateTimeFields() {
        InMemoryTaskManager manager = new InMemoryTaskManager() {
            @Override
            public Optional<Subtask> getSubtask(int id) {
                if (id == 1) {
                    return Optional.of(new Subtask(0, 1, "Sub1", "Desc", Status.NEW,
                            Duration.ofMinutes(60), LocalDateTime.of(2025, 6, 11, 10, 0)));
                } else if (id == 2) {
                    return Optional.of(new Subtask(0, 2, "Sub2", "Desc", Status.NEW,
                            Duration.ofMinutes(30), LocalDateTime.of(2025, 6, 11, 12, 0)));
                }
                return Optional.empty();
            }
        };

        Epic epic = new Epic("Epic", "Description");
        epic.addSubtaskId(1);
        epic.addSubtaskId(2);
        epic.updateTimeFields(manager);

        assertEquals(Duration.ofMinutes(90), epic.getDuration(), "Duration should be sum of subtasks");
        assertEquals(LocalDateTime.of(2025, 6, 11, 10, 0), epic.getStartTime(), "Start time should be earliest subtask start");
        assertEquals(LocalDateTime.of(2025, 6, 11, 12, 30), epic.getEndTime(), "End time should be latest subtask end");
    }

    @Test
    void testUpdateTimeFieldsToNull() {
        InMemoryTaskManager manager = new InMemoryTaskManager() {
            @Override
            public Optional<Subtask> getSubtask(int id) {
                return Optional.empty();
            }
        };

        Epic epic = new Epic("Epic", "Description");
        epic.updateTimeFields(manager);

        assertNull(epic.getDuration(), "Duration should be null with no subtasks");
        assertNull(epic.getStartTime(), "Start time should be null with no subtasks");
        assertNull(epic.getEndTime(), "End time should be null with no subtasks");
    }

}