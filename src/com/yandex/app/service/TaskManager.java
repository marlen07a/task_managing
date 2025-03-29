package com.yandex.app.service;

import com.yandex.app.model.*;

import java.util.HashMap;
import java.util.ArrayList;

public class TaskManager {
    private static int id = 0;
    private final HashMap<Integer, Task> tasks = new HashMap<>();
    private final HashMap<Integer, Epic> epics = new HashMap<>();
    private final HashMap<Integer, Subtask> subtasks = new HashMap<>();

    private int generateId() {
        return ++id;
    }

    public int addTask(Task task) {
        final int id = generateId();
        task.setId(id);
        tasks.put(id, task);
        return id;
    }

    public int addEpic(Epic epic) {
        final int id = generateId();
        epic.setId(id);
        epics.put(id, epic);
        return id;
    }

    public int addSubtask(Subtask subtask) {
        final int id = generateId();
        subtask.setId(id);
        subtasks.put(id, subtask);

        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            epic.addSubtaskId(id);
            updateEpicStatus(epic);
        }

        return id;
    }

    public void updateTask(Task task) {
        if (tasks.containsKey(task.getId())) {
            tasks.put(task.getId(), task);
        }
    }

    public void updateEpic(Epic epic) {
        if (epics.containsKey(epic.getId())) {
            epics.put(epic.getId(), epic);
        }
    }

    public void updateSubtask(Subtask subtask) {
        if (subtasks.containsKey(subtask.getId())) {
            subtasks.put(subtask.getId(), subtask);

            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                updateEpicStatus(epic);
            }
        }
    }

    public Task getTask(int id) {
        if (tasks.containsKey(id)) {
            return tasks.get(id);
        }
        return null;
    }

    public Subtask getSubtask(int id) {
        if (subtasks.containsKey(id)) {
            return subtasks.get(id);
        }
        return null;
    }

    public Epic getEpic(int id) {
        if (epics.containsKey(id)) {
            return epics.get(id);
        }
        return null;
    }

    public void deleteTaskById(int id) {
        tasks.remove(id);
    }

    public void deleteEpicById(int id) {
        if (epics.containsKey(id)) {

            Epic epic = epics.get(id);

            for (int subtaskId : epic.getSubtaskIds()) {
                subtasks.remove(subtaskId);
            }

            epics.remove(id);
        }
    }

    public void deleteSubtaskById(int id) {
        if (subtasks.containsKey(id)) {

            Subtask subtask = subtasks.get(id);
            Epic epic = epics.get(subtask.getEpicId());

            epic.removeSubtaskId(id);
            subtasks.remove(id);

            updateEpicStatus(epic);
        }
    }

    public ArrayList<Subtask> getSubtasksByEpicId(int epicId) {
        ArrayList<Subtask> subtasksOfEpic = new ArrayList<>();
        for (Subtask subtask : subtasks.values()) {
            if (subtask.getEpicId() == epicId) {
                subtasksOfEpic.add(subtask);
            }
        }
        return subtasksOfEpic;
    }

    public void updateEpicStatus(Epic epic) {
        ArrayList<Integer> subtaskIds = epic.getSubtaskIds();
        if (subtaskIds.isEmpty()) {
            epic.setStatusEpic(Status.NEW);
            return;
        }

        boolean allDone = true;
        boolean InProgress = false;

        for (int subtaskId : subtaskIds) {
            Status status = subtasks.get(subtaskId).getStatus();
            if (status == Status.IN_PROGRESS) {
                InProgress = true;
            }
            if (status != Status.DONE) {
                allDone = false;
            }
        }

        if (allDone) {
            epic.setStatusEpic(Status.DONE);
        } else if (InProgress) {
            epic.setStatusEpic(Status.IN_PROGRESS);
        } else {
            epic.setStatusEpic(Status.NEW);
        }
    }

    public ArrayList<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    public ArrayList<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    public ArrayList<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    public void clearTasks() {
        tasks.clear();
    }

    public void clearSubtasks() {
        for (Epic epic : epics.values()) {
            epic.getSubtaskIds().clear();
        }
        subtasks.clear();
    }

    public void clearEpics() {
        epics.clear();
        subtasks.clear();
    }
}
