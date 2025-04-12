package com.yandex.app.service;

import com.yandex.app.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryTaskManager implements TaskManager {
    private static int id = 0;
    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();
    private final HistoryManager historyManager = Managers.getDefaultHistory();

    private int generateId() {
        return ++id;
    }

    @Override
    public int addTask(Task task) {
        int localId = task.getId();
        if (localId == 0) {
            localId = generateId();
            task.setId(localId);
        }
        tasks.put(localId, task);
        return localId;
    }

    @Override
    public int addEpic(Epic epic) {
        int localId = epic.getId();
        if (localId == 0) {
            localId = generateId();
            epic.setId(localId);
        }
        epics.put(localId, epic);
        return localId;
    }

    @Override
    public int addSubtask(Subtask subtask) {
        int localId = subtask.getId();
        if (localId == subtask.getEpicId()) {
            return -1;
        }

        if (localId == 0) {
            localId = generateId();
            subtask.setId(localId);
        }
        subtasks.put(localId, subtask);

        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            epic.addSubtaskId(localId);
            updateEpicStatus(epic);
        }

        return localId;
    }

    @Override
    public void updateTask(Task task) {
        if (tasks.containsKey(task.getId())) {
            tasks.put(task.getId(), task);
        }
    }

    @Override
    public void updateEpic(Epic epic) {
        if (epics.containsKey(epic.getId())) {
            epics.put(epic.getId(), epic);
        }
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtasks.containsKey(subtask.getId())) {
            subtasks.put(subtask.getId(), subtask);

            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                updateEpicStatus(epic);
            }
        }
    }

    @Override
    public Task getTask(int id) {
        Task task = tasks.get(id);
        historyManager.add(task);
        return task;
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        historyManager.add(subtask);
        return subtask;
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = epics.get(id);
        historyManager.add(epic);
        return epic;
    }

    @Override
    public void deleteTaskById(int id) {
        tasks.remove(id);
    }

    @Override
    public void deleteEpicById(int id) {
        if (epics.containsKey(id)) {
            Epic epic = epics.get(id);

            for (int subtaskId : epic.getSubtaskIds()) {
                subtasks.remove(subtaskId);
            }

            epics.remove(id);
        }
    }

    @Override
    public void deleteSubtaskById(int id) {
        if (subtasks.containsKey(id)) {
            Subtask subtask = subtasks.get(id);
            Epic epic = epics.get(subtask.getEpicId());

            epic.removeSubtaskId(id);
            subtasks.remove(id);

            updateEpicStatus(epic);
        }
    }

    @Override
    public ArrayList<Subtask> getSubtasksByEpicId(int epicId) {
        ArrayList<Subtask> subtasksOfEpic = new ArrayList<>();
        for (Subtask subtask : subtasks.values()) {
            if (subtask.getEpicId() == epicId) {
                subtasksOfEpic.add(subtask);
            }
        }
        return subtasksOfEpic;
    }

    @Override
    public ArrayList<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public ArrayList<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public ArrayList<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public void clearTasks() {
        tasks.clear();
    }

    @Override
    public void clearSubtasks() {
        for (Epic epic : epics.values()) {
            epic.getSubtaskIds().clear();
            updateEpicStatus(epic);
        }
        subtasks.clear();
    }

    @Override
    public void clearEpics() {
        epics.clear();
        subtasks.clear();
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    private void updateEpicStatus(Epic epic) {
        ArrayList<Integer> subtaskIds = epic.getSubtaskIds();
        if (subtaskIds.isEmpty()) {
            epic.setStatusEpic(Status.NEW);
            return;
        }

        boolean allNew = true;
        boolean allDone = true;

        for (int subtaskId : subtaskIds) {
            Status status = subtasks.get(subtaskId).getStatus();
            if (status != Status.NEW) {
                allNew = false;
            }
            if (status != Status.DONE) {
                allDone = false;
            }
        }

        if (allDone) {
            epic.setStatusEpic(Status.DONE);
        } else if (allNew) {
            epic.setStatusEpic(Status.NEW);
        } else {
            epic.setStatusEpic(Status.IN_PROGRESS);
        }
    }
}
