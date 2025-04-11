package com.yandex.app.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SubtaskTest {
    @Test
    void testEquals() {
        Subtask subtask1 = new Subtask(10, 1, "Subtask 1", "Description 1", Status.NEW);
        Subtask subtask2 = new Subtask(20, 1, "Subtask 2", "Description 2", Status.DONE);
        Subtask subtask3 = new Subtask(10, 2, "Subtask 1", "Description 1", Status.NEW);

        assertEquals(subtask1, subtask2, "Subtasks with the same id should be equal regardless of epicId");
        assertNotEquals(subtask1, subtask3, "Subtasks with different ids should not be equal");
    }

    @Test
    void testHashCode() {
        Subtask subtask1 = new Subtask(10, 1, "Subtask 1", "Description 1", Status.NEW);
        Subtask subtask2 = new Subtask(20, 1, "Subtask 2", "Description 2", Status.DONE);

        assertEquals(subtask1.hashCode(), subtask2.hashCode(), "Hash codes should be equal if ids are equal");
    }

    @Test
    void testGetEpicId() {
        Subtask subtask = new Subtask(10, "Subtask 1", "Description 1", Status.NEW);

        assertEquals(10, subtask.getEpicId(), "Subtask should return the correct epic id");
    }

    @Test
    void testConstructors() {
        Subtask subtask1 = new Subtask(5, "Subtask 1", "Description 1", Status.NEW);
        assertEquals(5, subtask1.getEpicId());
        assertEquals("Subtask 1", subtask1.getName());
        assertEquals("Description 1", subtask1.getDescription());
        assertEquals(Status.NEW, subtask1.getStatus());
    }
}