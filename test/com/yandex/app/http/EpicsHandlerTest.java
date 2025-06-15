package com.yandex.app.http;

import com.google.gson.Gson;
import com.yandex.app.http.util.GsonUtils;
import com.yandex.app.model.Epic;
import com.yandex.app.model.Subtask;
import com.yandex.app.model.Status;
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

public class EpicsHandlerTest {
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
    public void testGetAllEpics() throws IOException, InterruptedException {
        Epic epic = new Epic("Test Epic", "Description");
        taskManager.addEpic(epic);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Status code should be 200");

        Epic[] epics = gson.fromJson(response.body(), Epic[].class);
        assertEquals(1, epics.length, "Should return 1 epic");
        assertEquals("Test Epic", epics[0].getName(), "Epic name should match");
    }

    @Test
    public void testGetEpicById() throws IOException, InterruptedException {
        Epic epic = new Epic("Test Epic", "Description");
        int id = taskManager.addEpic(epic);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/" + id))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Status code should be 200");

        Epic retrieved = gson.fromJson(response.body(), Epic.class);
        assertEquals("Test Epic", retrieved.getName(), "Epic name should match");
    }

    @Test
    public void testGetEpicByIdNotFound() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/999"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode(), "Status code should be 404");
    }

    @Test
    public void testGetEpicSubtasks() throws IOException, InterruptedException {
        Epic epic = new Epic("Test Epic", "Description");
        int epicId = taskManager.addEpic(epic);
        Subtask subtask = new Subtask(epicId, "Test Subtask", "Description", Status.NEW, Duration.ofMinutes(30), LocalDateTime.now());
        taskManager.addSubtask(subtask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/" + epicId + "/subtasks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Status code should be 200");

        Subtask[] subtasks = gson.fromJson(response.body(), Subtask[].class);
        assertEquals(1, subtasks.length, "Should return 1 subtask");
        assertEquals("Test Subtask", subtasks[0].getName(), "Subtask name should match");
    }

    @Test
    public void testGetEpicSubtasksNotFound() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/999/subtasks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode(), "Status code should be 404");
    }

    @Test
    public void testAddEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Test Epic", "Description");
        String epicJson = gson.toJson(epic);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode(), "Status code should be 201");

        assertEquals(1, taskManager.getAllEpics().size(), "Epic count should be 1");
        assertEquals("Test Epic", taskManager.getAllEpics().get(0).getName(), "Epic name should match");
    }

    @Test
    public void testDeleteEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Test Epic", "Description");
        int id = taskManager.addEpic(epic);
        Subtask subtask = new Subtask(id, "Test Subtask", "Description", Status.NEW, Duration.ofMinutes(30), LocalDateTime.now());
        taskManager.addSubtask(subtask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/" + id))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Status code should be 200");

        assertEquals(0, taskManager.getAllEpics().size(), "Epic count should be 0");
        assertEquals(0, taskManager.getAllSubtasks().size(), "Subtask count should be 0");
    }
}
