package com.yandex.app.model;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {
    @Test
    void testEquals() {
        Task task1 = new Task(1, "Task 1", "Description 1", Status.NEW, Duration.ofMinutes(60), LocalDateTime.now());
        Task task2 = new Task(1, "Task 2", "Description 2", Status.DONE, Duration.ofMinutes(30), LocalDateTime.now().plusHours(1));
        Task task3 = new Task(2, "Task 1", "Description 1", Status.NEW, Duration.ofMinutes(60), LocalDateTime.now());

        assertEquals(task1, task2, "Tasks with the same id should be equal");
        assertNotEquals(task1, task3, "Tasks with different ids should not be equal");
    }

    @Test
    void testHashCode() {
        Task task1 = new Task(1, "Task 1", "Description 1", Status.NEW, Duration.ofMinutes(60), LocalDateTime.now());
        Task task2 = new Task(1, "Task 2", "Description 2", Status.DONE, Duration.ofMinutes(30), LocalDateTime.now().plusHours(1));

        assertEquals(task1.hashCode(), task2.hashCode(), "Hash codes should be equal if IDs are equal");
    }

    @Test
    void testGettersAndSetters() {
        LocalDateTime startTime = LocalDateTime.of(2025, 6, 11, 10, 0);
        Task task = new Task("Task 1", "Description 1", Status.NEW, Duration.ofMinutes(60), startTime);
        task.setId(2);

        assertEquals(2, task.getId(), "ID should be set correctly");
        assertEquals("Task 1", task.getName(), "Name should be set correctly");
        assertEquals("Description 1", task.getDescription(), "Description should be set correctly");
        assertEquals(Status.NEW, task.getStatus(), "Status should be set correctly");
        assertEquals(Duration.ofMinutes(60), task.getDuration(), "Duration should be set correctly");
        assertEquals(startTime, task.getStartTime(), "Start time should be set correctly");
        assertEquals(startTime.plusMinutes(60), task.getEndTime(), "End time should be calculated correctly");

        task.setName("Task 2");
        task.setDescription("Description 2");
        task.setStatus(Status.DONE);
        task.setDuration(Duration.ofMinutes(30));
        task.setStartTime(startTime.plusHours(1));

        assertEquals("Task 2", task.getName(), "Name should be updated");
        assertEquals("Description 2", task.getDescription(), "Description should be updated");
        assertEquals(Status.DONE, task.getStatus(), "Status should be updated");
        assertEquals(Duration.ofMinutes(30), task.getDuration(), "Duration should be updated");
        assertEquals(startTime.plusHours(1), task.getStartTime(), "Start time should be updated");
        assertEquals(startTime.plusHours(1).plusMinutes(30), task.getEndTime(), "End time should be updated");
    }

    @Test
    void testToString() {
        LocalDateTime startTime = LocalDateTime.of(2025, 6, 11, 10, 0);
        Task task = new Task(1, "Task 1", "Description 1", Status.NEW, Duration.ofMinutes(60), startTime);
        String expected = "Task{id=1, name='Task 1', description='Description 1', status=NEW, duration=60, startTime=2025-06-11T10:00}";
        assertEquals(expected, task.toString(), "toString should return correct format");
    }
}