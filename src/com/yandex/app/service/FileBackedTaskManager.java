package com.yandex.app.service;

import com.yandex.app.Main;
import com.yandex.app.model.*;

import java.io.*;
import java.nio.file.Files;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

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
        return String.format("%d,%s,%s,%s,%s,%s",
                task.getId(), task.getType().name(), task.getName(), task.getStatus(), task.getDescription(), epicId);
    }

    private Task fromString(String value) {
        String[] parts = value.split(",");
        int id = Integer.parseInt(parts[0]);
        TaskType type = TaskType.valueOf(parts[1]);
        String name = parts[2];
        Status status = Status.valueOf(parts[3]);
        String description = parts[4];

        switch (type) {
            case TASK:
                return new Task(id, name, description, status);
            case EPIC:
                return new Epic(id, name, description);
            case SUBTASK:
                int epicId = Integer.parseInt(parts[5]);
                return new Subtask(epicId, id, name, description, status);
            default:
                throw new IllegalArgumentException("Unknown task type: " + type);
        }
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
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

        Task task1 = new Task("Task1", "Description task1", Status.NEW);
        Epic epic1 = new Epic("Epic1", "Description epic1");
        Subtask subtask1 = new Subtask(0, "Subtask1", "Description subtask1", Status.DONE);

        int task1Id = manager.addTask(task1);
        int epic1Id = manager.addEpic(epic1);
        subtask1 = new Subtask(epic1Id, "Subtask1", "Description subtask1", Status.DONE);
        manager.addSubtask(subtask1);

        System.out.println("Original manager tasks:");
        Main.printAllTasks(manager);

        FileBackedTaskManager newManager = loadFromFile(file);
        System.out.println("\nRestored manager tasks:");
        Main.printAllTasks(newManager);
    }
}
