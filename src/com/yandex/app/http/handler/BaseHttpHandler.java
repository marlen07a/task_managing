package com.yandex.app.http.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.yandex.app.http.util.GsonUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;

public abstract class BaseHttpHandler {
    protected final Gson gson = GsonUtils.getGson();

    protected BaseHttpHandler() {
    }

    protected void sendText(HttpExchange exchange, String text, int statusCode) throws IOException {
        byte[] response = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }

    protected void sendNotFound(HttpExchange exchange) throws IOException {
        sendText(exchange, "{\"error\": \"Resource not found\"}", HttpURLConnection.HTTP_NOT_FOUND);
    }

    protected void sendHasInteractions(HttpExchange exchange) throws IOException {
        sendText(exchange, "{\"error\": \"Task overlaps with existing tasks\"}", HttpURLConnection.HTTP_NOT_ACCEPTABLE);
    }

    protected void sendServerError(HttpExchange exchange, String message) throws IOException {
        sendText(exchange, "{\"error\": \"" + message + "\"}", HttpURLConnection.HTTP_INTERNAL_ERROR);
    }

    protected int getIdFromPath(HttpExchange exchange) {
        String path = exchange.getRequestURI().getPath();
        String[] segments = path.split("/");
        try {
            return Integer.parseInt(segments[segments.length - 1]);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}



