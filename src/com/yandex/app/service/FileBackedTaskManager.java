package com.yandex.app.service;

import com.yandex.app.Main;
import com.yandex.app.model.*;

import java.io.*;
import java.nio.file.Files;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    public static class ManagerSaveException extends RuntimeException {
        public ManagerSaveException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    protected void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("id,type,name,status,description,epic\n");

            for (Task task : getAllTasks()) {
                writer.write(toString(task) + "\n");
            }

            for (Epic epic : getAllEpics()) {
                writer.write(toString(epic) + "\n");
            }

            for (Subtask subtask : getAllSubtasks()) {
                writer.write(toString(subtask) + "\n");
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Error saving tasks to file: " + file.getPath(), e);
        }
    }

    private String toString(Task task) {
        String epicId = task instanceof Subtask ? String.valueOf(((Subtask) task).getEpicId()) : "";
        String duration = task.getDuration() != null ? String.valueOf(task.getDuration().toMinutes()) : "";
        String startTime = task.getStartTime() != null ? task.getStartTime().format(FORMATTER) : "";
        return String.format("%d,%s,%s,%s,%s,%s,%s,%s",
                task.getId(), task.getType().name(), task.getName(), task.getStatus(), task.getDescription(), duration, epicId, startTime);
    }

    private Task fromString(String value) {
        String[] parts = value.split(",");
        int id = Integer.parseInt(parts[0]);
        TaskType type = TaskType.valueOf(parts[1]);
        String name = parts[2];
        Status status = Status.valueOf(parts[3]);
        String description = parts[4];
        Duration duration = parts[5].isEmpty() ? null : Duration.ofMinutes(Long.parseLong(parts[5]));
        int epicId = type == TaskType.SUBTASK ? Integer.parseInt(parts[6]) : 0;
        LocalDateTime startTime = parts.length > 7 && !parts[7].isEmpty() ? LocalDateTime.parse(parts[7], FORMATTER) : null;

        switch (type) {
            case TASK:
                return new Task(id, name, description, status, duration, startTime);
            case EPIC:
                return new Epic(id, name, description);
            case SUBTASK:
                return new Subtask(epicId, id, name, description, status, duration, startTime);
            default:
                throw new IllegalArgumentException("Unknown task type: " + type);
        }
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        if (!file.exists() || file.length() == 0) {
            return manager;
        }
        try {
            String content = Files.readString(file.toPath());
            String[] lines = content.split("\n");

            for (int i = 1; i < lines.length; i++) {
                String line = lines[i].trim();
                if (!line.isEmpty()) {
                    Task task = manager.fromString(line);
                    if (task instanceof Task && !(task instanceof Epic || task instanceof Subtask)) {
                        manager.addTask(task);
                    } else if (task instanceof Epic) {
                        manager.addEpic((Epic) task);
                    } else if (task instanceof Subtask) {
                        manager.addSubtask((Subtask) task);
                    }
                }
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Error loading tasks from file: " + file.getPath(), e);
        }
        return manager;
    }

    @Override
    public int addTask(Task task) {
        int id = super.addTask(task);
        save();
        return id;
    }

    @Override
    public int addEpic(Epic epic) {
        int id = super.addEpic(epic);
        save();
        return id;
    }

    @Override
    public int addSubtask(Subtask subtask) {
        int id = super.addSubtask(subtask);
        save();
        return id;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void deleteTaskById(int id) {
        super.deleteTaskById(id);
        save();
    }

    @Override
    public void deleteEpicById(int id) {
        super.deleteEpicById(id);
        save();
    }

    @Override
    public void deleteSubtaskById(int id) {
        super.deleteSubtaskById(id);
        save();
    }

    @Override
    public void clearTasks() {
        super.clearTasks();
        save();
    }

    @Override
    public void clearSubtasks() {
        super.clearSubtasks();
        save();
    }

    @Override
    public void clearEpics() {
        super.clearEpics();
        save();
    }

    public static void main(String[] args) throws IOException {
        File file = File.createTempFile("tasks", ".csv");
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        Task task1 = new Task("Task1", "Description task1", Status.NEW, Duration.ofMinutes(60), LocalDateTime.now());
        Epic epic1 = new Epic("Epic1", "Description epic1");
        int epic1Id = manager.addEpic(epic1);
        Subtask subtask1 = new Subtask(epic1Id, "Subtask1", "Description subtask1", Status.DONE, Duration.ofMinutes(30), LocalDateTime.now().plusHours(2));


        int task1Id = manager.addTask(task1);
        int subtask1Id = manager.addSubtask(subtask1);

        System.out.println("Original manager tasks:");
        Main.printAllTasks(manager);

        FileBackedTaskManager newManager = loadFromFile(file);
        System.out.println("\nRestored manager tasks:");
        Main.printAllTasks(newManager);
    }
}
