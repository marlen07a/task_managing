package com.yandex.app.http.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.yandex.app.model.Task;
import com.yandex.app.service.TaskManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class TasksHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;

    public TasksHandler(TaskManager taskManager, Gson gson) {
        super(gson);
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            if (method.equals("GET")) {
                if (path.equals("/tasks")) {
                    sendText(exchange, gson.toJson(taskManager.getAllTasks()), 200);
                } else if (path.matches("/tasks/\\d+")) {
                    int id = getIdFromPath(exchange);
                    Optional<Task> task = taskManager.getTask(id);
                    if (task.isPresent()) {
                        sendText(exchange, gson.toJson(task.get()), 200);
                    } else {
                        sendNotFound(exchange);
                    }
                }
            } else if (method.equals("POST") && path.equals("/tasks")) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Task task = gson.fromJson(body, Task.class);
                try {
                    int id = taskManager.addTask(task);
                    sendText(exchange, "{\"id\": " + id + "}", 201);
                } catch (IllegalStateException e) {
                    sendHasInteractions(exchange);
                }
            } else if (method.equals("DELETE") && path.matches("/tasks/\\d+")) {
                int id = getIdFromPath(exchange);
                taskManager.deleteTaskById(id);
                sendText(exchange, "{\"message\": \"Task deleted\"}", 200);
            } else {
                sendNotFound(exchange);
            }
        } catch (Exception e) {
            sendServerError(exchange, "Server error: " + e.getMessage());
        }
    }
}
