package com.yandex.app.http;

import com.google.gson.Gson;
import com.yandex.app.model.Task;
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

public class HistoryHandlerTest {
    private TaskManager taskManager;
    private HttpTaskServer taskServer;
    private Gson gson;
    private HttpClient client;

    @BeforeEach
    public void setUp() throws IOException {
        taskManager = new InMemoryTaskManager();
        taskServer = new HttpTaskServer(taskManager);
        gson = HttpTaskServer.getGson();
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
    public void testGetHistory() throws IOException, InterruptedException {
        Task task1 = new Task("Task 1", "Description", Status.NEW, Duration.ofMinutes(60), LocalDateTime.now());
        Task task2 = new Task("Task 2", "Description", Status.NEW, Duration.ofMinutes(60), LocalDateTime.now().plusHours(2));
        int id1 = taskManager.addTask(task1);
        int id2 = taskManager.addTask(task2);
        taskManager.getTask(id1); // Add to history
        taskManager.getTask(id2); // Add to history

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/history"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Status code should be 200");

        Task[] history = gson.fromJson(response.body(), Task[].class);
        assertEquals(2, history.length, "History should contain 2 tasks");
        assertEquals("Task 1", history[0].getName(), "First task name should match");
        assertEquals("Task 2", history[1].getName(), "Second task name should match");
    }

    @Test
    public void testGetEmptyHistory() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/history"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Status code should be 200");

        Task[] history = gson.fromJson(response.body(), Task[].class);
        assertEquals(0, history.length, "History should be empty");
    }
}
