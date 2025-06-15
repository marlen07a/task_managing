package com.yandex.app.http.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.yandex.app.model.Task;
import com.yandex.app.service.TaskManager;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class TasksHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;

    public TasksHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            if (method.equals("GET")) {
                if (path.equals("/tasks")) {
                    sendText(exchange, gson.toJson(taskManager.getAllTasks()), HttpURLConnection.HTTP_OK);
                } else if (path.matches("/tasks/\\d+")) {
                    int id = getIdFromPath(exchange);
                    Optional<Task> task = taskManager.getTask(id);
                    if (task.isPresent()) {
                        sendText(exchange, gson.toJson(task.get()), HttpURLConnection.HTTP_OK);
                    } else {
                        sendNotFound(exchange);
                    }
                }
            } else if (method.equals("POST") && path.equals("/tasks")) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Task task = gson.fromJson(body, Task.class);
                try {
                    int id = taskManager.addTask(task);
                    sendText(exchange, "{\"id\": " + id + "}", HttpURLConnection.HTTP_CREATED);
                } catch (IllegalStateException e) {
                    sendHasInteractions(exchange);
                }
            } else if (method.equals("DELETE") && path.matches("/tasks/\\d+")) {
                int id = getIdFromPath(exchange);
                taskManager.deleteTaskById(id);
                sendText(exchange, "{\"message\": \"Task deleted\"}", HttpURLConnection.HTTP_OK);
            } else {
                sendNotFound(exchange);
            }
        } catch (Exception e) {
            sendServerError(exchange, "Server error: " + e.getMessage());
        }
    }
}
