package com.yandex.app.http;

import com.google.gson.Gson;
import com.yandex.app.http.util.GsonUtils;
import com.yandex.app.model.Epic;
import com.yandex.app.model.Status;
import com.yandex.app.model.Subtask;
import com.yandex.app.service.InMemoryTaskManager;
import com.yandex.app.service.TaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class SubtasksHandlerTest {
    private TaskManager taskManager;
    private HttpTaskServer taskServer;
    private Gson gson;
    private HttpClient client;

    @BeforeEach
    public void setUp() throws IOException {
        taskManager = new InMemoryTaskManager();
        taskServer = new HttpTaskServer(taskManager);
        gson = GsonUtils.getGson();
        client = HttpClient.newHttpClient();
        taskManager.clearTasks();
        taskManager.clearSubtasks();
        taskManager.clearEpics();
        taskServer.start();
    }

    @AfterEach
    public void tearDown() {
        taskServer.stop();
    }

    @Test
    public void testGetAllSubtasks() throws IOException, InterruptedException {
        Epic epic = new Epic("Test Epic", "Description");
        int epicId = taskManager.addEpic(epic);
        Subtask subtask = new Subtask(epicId, "Test Subtask", "Description", Status.NEW, Duration.ofMinutes(30), LocalDateTime.now());
        taskManager.addSubtask(subtask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Status code should be 200");

        Subtask[] subtasks = gson.fromJson(response.body(), Subtask[].class);
        assertEquals(1, subtasks.length, "Should return 1 subtask");
        assertEquals("Test Subtask", subtasks[0].getName(), "Subtask name should match");
    }

    @Test
    public void testGetSubtaskById() throws IOException, InterruptedException {
        Epic epic = new Epic("Test Epic", "Description");
        int epicId = taskManager.addEpic(epic);
        Subtask subtask = new Subtask(epicId, "Test Subtask", "Description", Status.NEW, Duration.ofMinutes(30), LocalDateTime.now());
        int id = taskManager.addSubtask(subtask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks/" + id))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Status code should be 200");

        Subtask retrieved = gson.fromJson(response.body(), Subtask.class);
        assertEquals("Test Subtask", retrieved.getName(), "Subtask name should match");
    }

    @Test
    public void testGetSubtaskByIdNotFound() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks/999"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode(), "Status code should be 404");
    }

    @Test
    public void testAddSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("Test Epic", "Description");
        int epicId = taskManager.addEpic(epic);
        Subtask subtask = new Subtask(epicId, "Test Subtask", "Description", Status.NEW, Duration.ofMinutes(30), LocalDateTime.now());
        String subtaskJson = gson.toJson(subtask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode(), "Status code should be 201");

        assertEquals(1, taskManager.getAllSubtasks().size(), "Subtask count should be 1");
        assertEquals("Test Subtask", taskManager.getAllSubtasks().get(0).getName(), "Subtask name should match");
    }

    @Test
    public void testAddSubtaskOverlap() throws IOException, InterruptedException {
        Epic epic = new Epic("Test Epic", "Description");
        int epicId = taskManager.addEpic(epic);
        Subtask subtask1 = new Subtask(epicId, "Subtask 1", "Description", Status.NEW, Duration.ofMinutes(30), LocalDateTime.now());
        taskManager.addSubtask(subtask1);

        Subtask subtask2 = new Subtask(epicId, "Subtask 2", "Description", Status.NEW, Duration.ofMinutes(30), LocalDateTime.now());
        String subtaskJson = gson.toJson(subtask2);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(406, response.statusCode(), "Status code should be 406 for overlapping subtask");
    }

    @Test
    public void testDeleteSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("Test Epic", "Description");
        int epicId = taskManager.addEpic(epic);
        Subtask subtask = new Subtask(epicId, "Test Subtask", "Description", Status.NEW, Duration.ofMinutes(30), LocalDateTime.now());
        int id = taskManager.addSubtask(subtask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks/" + id))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Status code should be 200");

        assertEquals(0, taskManager.getAllSubtasks().size(), "Subtask count should be 0");
    }
}