package com.yandex.app.model;

public class Subtask extends Task {
    private final int epicId;

    public Subtask(int epicId, int id, String name, String description, Status status) {
        super(id, name, description, status);
        this.epicId = epicId;
    }

    public Subtask(int epicId, String name, String description, Status status) {
        super(name, description, status);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }
}
