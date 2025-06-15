package com.yandex.app.http.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.yandex.app.http.adapter.DurationAdapter;
import com.yandex.app.http.adapter.LocalDateTimeAdapter;

import java.time.Duration;
import java.time.LocalDateTime;

public class GsonUtils {
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .registerTypeAdapter(Duration.class, new DurationAdapter())
            .create();

    private GsonUtils() {
    }

    public static Gson getGson() {
        return GSON;
    }
}
