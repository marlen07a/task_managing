package com.yandex.app.model;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SubtaskTest {
    @Test
    void testEquals() {
        Subtask subtask1 = new Subtask(10, 1, "Subtask 1", "Description 1", Status.NEW, Duration.ofMinutes(60), LocalDateTime.now());
        Subtask subtask2 = new Subtask(20, 1, "Subtask 2", "Description 2", Status.DONE, Duration.ofMinutes(30), LocalDateTime.now().plusHours(1));
        Subtask subtask3 = new Subtask(10, 2, "Subtask 1", "Description 1", Status.NEW, Duration.ofMinutes(60), LocalDateTime.now());

        assertEquals(subtask1, subtask2, "Subtasks with the same id should be equal regardless of epicId");
        assertNotEquals(subtask1, subtask3, "Subtasks with different ids should not be equal");
    }

    @Test
    void testHashCode() {
        Subtask subtask1 = new Subtask(10, 1, "Subtask 1", "Description 1", Status.NEW, Duration.ofMinutes(60), LocalDateTime.now());
        Subtask subtask2 = new Subtask(20, 1, "Subtask 2", "Description 2", Status.DONE, Duration.ofMinutes(30), LocalDateTime.now().plusHours(1));

        assertEquals(subtask1.hashCode(), subtask2.hashCode(), "Hash codes should be equal if ids are equal");
    }

    @Test
    void testGetEpicId() {
        Subtask subtask = new Subtask(10, "Subtask 1", "Description 1", Status.NEW, Duration.ofMinutes(60), LocalDateTime.now());

        assertEquals(10, subtask.getEpicId(), "Subtask should return the correct epic id");
    }

    @Test
    void testConstructorsAndTimeFields() {
        LocalDateTime startTime = LocalDateTime.of(2025, 6, 11, 10, 0);
        Subtask subtask1 = new Subtask(5, "Subtask 1", "Description 1", Status.NEW, Duration.ofMinutes(60), startTime);

        assertEquals(5, subtask1.getEpicId(), "Epic ID should be set correctly");
        assertEquals("Subtask 1", subtask1.getName(), "Name should be set correctly");
        assertEquals("Description 1", subtask1.getDescription(), "Description should be set correctly");
        assertEquals(Status.NEW, subtask1.getStatus(), "Status should be set correctly");
        assertEquals(Duration.ofMinutes(60), subtask1.getDuration(), "Duration should be set correctly");
        assertEquals(startTime, subtask1.getStartTime(), "Start time should be set correctly");
        assertEquals(startTime.plusMinutes(60), subtask1.getEndTime(), "End time should be calculated correctly");

        Subtask subtask2 = new Subtask(5, 1, "Subtask 2", "Description 2", Status.DONE, null, null);
        assertNull(subtask2.getDuration(), "Duration should be null if not set");
        assertNull(subtask2.getStartTime(), "Start time should be null if not set");
        assertNull(subtask2.getEndTime(), "End time should be null if duration or start time is null");
    }
}