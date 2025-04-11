package com.yandex.app.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TaskTest {
    @Test
    void testEquals() {
        Task task1 = new Task(1, "Task 1", "Description 1", Status.NEW);
        Task task2 = new Task(1, "Task 2", "Description 2", Status.DONE);
        Task task3 = new Task(2, "Task 1", "Description 1", Status.NEW);

        assertEquals(task1, task2, "Tasks with the same id should be equal");
        assertNotEquals(task1, task3, "Tasks with different ids should not be equal");
    }

    @Test
    void testHashCode() {
        Task task1 = new Task(1, "Task 1", "Description 1", Status.NEW);
        Task task2 = new Task(1, "Task 2", "Description 2", Status.DONE);

        assertEquals(task1.hashCode(), task2.hashCode(), "Hash codes should be equal if ID are equal");
    }

    @Test
    void testGettersAndSetters() {
        Task task = new Task("Task 1", "Description 1", Status.NEW);
        task.setId(2);

        assertEquals(2, task.getId());
        assertEquals("Task 1", task.getName());
        assertEquals("Description 1", task.getDescription());
        assertEquals(Status.NEW, task.getStatus());

        task.setName("Task 2");
        task.setDescription("Description 2");
        task.setStatus(Status.DONE);

        assertEquals("Task 2", task.getName());
        assertEquals("Description 2", task.getDescription());
        assertEquals(Status.DONE, task.getStatus());
    }

    @Test
    void testToString() {
        Task task = new Task(1, "Task 1", "Description 1", Status.NEW);
        String expected = "Task{id=1, name='Task 1', description='Description 1', status=NEW}";
        assertEquals(expected, task.toString());
    }
}