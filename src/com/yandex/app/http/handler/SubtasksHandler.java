package com.yandex.app.http.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.yandex.app.model.Subtask;
import com.yandex.app.service.TaskManager;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class SubtasksHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;

    public SubtasksHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            if (method.equals("GET")) {
                if (path.equals("/subtasks")) {
                    sendText(exchange, gson.toJson(taskManager.getAllSubtasks()), HttpURLConnection.HTTP_OK);
                } else if (path.matches("/subtasks/\\d+")) {
                    int id = getIdFromPath(exchange);
                    Optional<Subtask> subtask = taskManager.getSubtask(id);
                    if (subtask.isPresent()) {
                        sendText(exchange, gson.toJson(subtask.get()), HttpURLConnection.HTTP_OK);
                    } else {
                        sendNotFound(exchange);
                    }
                }
            } else if (method.equals("POST") && path.equals("/subtasks")) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Subtask subtask = gson.fromJson(body, Subtask.class);
                try {
                    int id = taskManager.addSubtask(subtask);
                    sendText(exchange, "{\"id\": " + id + "}", HttpURLConnection.HTTP_CREATED);
                } catch (IllegalStateException e) {
                    sendHasInteractions(exchange);
                }
            } else if (method.equals("DELETE") && path.matches("/subtasks/\\d+")) {
                int id = getIdFromPath(exchange);
                taskManager.deleteSubtaskById(id);
                sendText(exchange, "{\"message\": \"Subtask deleted\"}", HttpURLConnection.HTTP_OK);
            } else {
                sendNotFound(exchange);
            }
        } catch (Exception e) {
            sendServerError(exchange, "Server error: " + e.getMessage());
        }
    }
}
