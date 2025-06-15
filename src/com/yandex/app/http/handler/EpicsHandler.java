package com.yandex.app.http.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.yandex.app.model.Epic;
import com.yandex.app.service.TaskManager;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;

public class EpicsHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;

    public EpicsHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        System.out.println("Handling request: " + method + " " + path);

        try {
            if (method.equals("GET")) {
                if (path.equals("/epics")) {
                    sendText(exchange, gson.toJson(taskManager.getAllEpics()), HttpURLConnection.HTTP_OK);
                } else if (path.matches("/epics/\\d+")) {
                    int id = getIdFromPath(exchange);
                    if (id == -1) {
                        sendNotFound(exchange);
                        return;
                    }
                    Optional<Epic> epic = taskManager.getEpic(id);
                    if (epic.isPresent()) {
                        sendText(exchange, gson.toJson(epic.get()), HttpURLConnection.HTTP_OK);
                    } else {
                        sendNotFound(exchange);
                    }
                } else if (path.matches("/epics/\\d+/subtasks")) {
                    System.out.println("Matched path: " + path);
                    int id = getEpicIdFromSubtasksPath(exchange);
                    if (id == -1) {
                        sendNotFound(exchange);
                        return;
                    }
                    Optional<Epic> epic = taskManager.getEpic(id);
                    if (epic.isPresent()) {
                        sendText(exchange, gson.toJson(taskManager.getSubtasksByEpicId(id)), HttpURLConnection.HTTP_OK);
                    } else {
                        sendNotFound(exchange);
                    }
                } else {
                    sendNotFound(exchange);
                }
            } else if (method.equals("POST") && path.equals("/epics")) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Epic epic = gson.fromJson(body, Epic.class);
                int id = taskManager.addEpic(epic);
                sendText(exchange, "{\"id\": " + id + "}", HttpURLConnection.HTTP_CREATED);
            } else if (method.equals("DELETE") && path.matches("/epics/\\d+")) {
                int id = getIdFromPath(exchange);
                if (id == -1) {
                    sendNotFound(exchange);
                    return;
                }
                taskManager.deleteEpicById(id);
                sendText(exchange, "{\"message\": \"Epic deleted\"}", HttpURLConnection.HTTP_OK);
            } else {
                sendNotFound(exchange);
            }
        } catch (Exception e) {
            sendServerError(exchange, "Server error: " + e.getMessage());
        }
    }

    private int getEpicIdFromSubtasksPath(HttpExchange exchange) {
        String path = exchange.getRequestURI().getPath();
        System.out.println("Parsing path: " + path);
        path = path.replaceAll("/+$", "");
        String[] segments = path.split("/");
        System.out.println("Path segments: " + Arrays.toString(segments));
        if (segments.length < 4) {
            System.err.println("Invalid path: too few segments");
            return -1;
        }
        try {
            int id = Integer.parseInt(segments[2]);
            System.out.println("Parsed epic ID: " + id);
            return id;
        } catch (NumberFormatException e) {
            System.err.println("Invalid epic ID format: " + segments[2] + ", error: " + e.getMessage());
            return -1;
        }
    }
}