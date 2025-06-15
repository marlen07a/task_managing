package com.yandex.app.service;

import com.yandex.app.model.*;

import java.time.LocalDateTime;

import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {
    private static int id = 0;
    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();
    private final HistoryManager historyManager = Managers.getDefaultHistory();
    private final TreeSet<Task> prioritizedTasks = new TreeSet<>((t1, t2) -> {
        if (t1.getStartTime() == null) return 1;
        if (t2.getStartTime() == null) return -1;
        return t1.getStartTime().compareTo(t2.getStartTime());
    });
    private final Map<LocalDateTime, Boolean> timeSlots = new HashMap<>();

    @Override
    public int addTask(Task task) {
        if (task.getStartTime() != null && task.getDuration() != null && hasOverlap(task)) {
            throw new IllegalStateException("Task overlaps with existing tasks");
        }

        reserveTimeSlots(task);

        int localId = task.getId();
        if (localId == 0) {
            localId = generateId();
            task.setId(localId);
        }
        tasks.put(localId, task);

        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
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
        if (subtask.getStartTime() != null && subtask.getDuration() != null && hasOverlap(subtask)) {
            throw new IllegalStateException("Subtask overlaps with existing tasks");
        }

        reserveTimeSlots(subtask);

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
            epic.updateTimeFields(this);
        }

        if (subtask.getStartTime() != null) {
            prioritizedTasks.add(subtask);
        }

        return localId;
    }

    @Override
    public void updateTask(Task task) {
        if (task == null || !tasks.containsKey(task.getId()) || !(task.getId() > 0)) {
            return;
        }
        if (task.getStartTime() != null && task.getDuration() != null && hasOverlap(task)) {
            throw new IllegalStateException("Updated task overlaps with existing tasks");
        }

        Task oldTask = tasks.get(task.getId());
        if (oldTask != null) {
            freeTimeSlots(oldTask);
        }

        reserveTimeSlots(task);

        tasks.put(task.getId(), task);
        prioritizedTasks.remove(oldTask);
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
    }

    @Override
    public void updateEpic(Epic epic) {
        if (epic != null && epics.containsKey(epic.getId()) && epic.getId() > 0) {
            epics.put(epic.getId(), epic);
            epic.updateTimeFields(this);
        }
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtask == null || !subtasks.containsKey(subtask.getId()) || !(subtask.getId() > 0)) {
            return;
        }
        if (subtask.getStartTime() != null && subtask.getDuration() != null && hasOverlap(subtask)) {
            throw new IllegalStateException("Updated subtask overlaps with existing tasks");
        }

        Subtask oldSubtask = subtasks.get(subtask.getId());
        if (oldSubtask != null) {
            freeTimeSlots(oldSubtask);
        }

        reserveTimeSlots(subtask);
        subtasks.put(subtask.getId(), subtask);

        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            updateEpicStatus(epic);
            epic.updateTimeFields(this);
        }

        prioritizedTasks.remove(oldSubtask);
        if (subtask.getStartTime() != null) {
            prioritizedTasks.add(subtask);
        }
    }

    @Override
    public Optional<Task> getTask(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.add(task);
        }
        return Optional.ofNullable(task);
    }

    @Override
    public Optional<Subtask> getSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            historyManager.add(subtask);
        }
        return Optional.ofNullable(subtask);
    }

    @Override
    public Optional<Epic> getEpic(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.add(epic);
        }
        return Optional.ofNullable(epic);
    }

    @Override
    public void deleteTaskById(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            freeTimeSlots(task);
            prioritizedTasks.remove(task);
            tasks.remove(id);
            historyManager.remove(id);
        }
    }

    @Override
    public void deleteEpicById(int id) {
        if (epics.containsKey(id)) {
            Epic epic = epics.get(id);

            for (int subtaskId : epic.getSubtaskIds()) {
                freeTimeSlots(subtasks.get(subtaskId));
                prioritizedTasks.remove(subtasks.get(subtaskId));
                subtasks.remove(subtaskId);
                historyManager.remove(subtaskId);
            }

            epics.remove(id);
            historyManager.remove(id);
        }
    }

    @Override
    public void deleteSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            freeTimeSlots(subtask);
            prioritizedTasks.remove(subtask);
            int epicId = subtask.getEpicId();
            subtasks.remove(id);
            historyManager.remove(id);
            Epic epic = epics.get(epicId);
            if (epic != null) {
                epic.removeSubtaskId(id);
                updateEpicStatus(epic);
                epic.updateTimeFields(this);
            }
        }
    }

    @Override
    public ArrayList<Subtask> getSubtasksByEpicId(int epicId) {
        return subtasks.values().stream()
                .filter(subtask -> subtask.getEpicId() == epicId)
                .collect(Collectors.toCollection(ArrayList::new));
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
        for (Task task : tasks.values()) {
            freeTimeSlots(task);
            prioritizedTasks.remove(task);
            historyManager.remove(task.getId());
        }
        tasks.clear();
    }

    @Override
    public void clearSubtasks() {
        for (Subtask subtask : subtasks.values()) {
            freeTimeSlots(subtask);
            prioritizedTasks.remove(subtask);
            historyManager.remove(subtask.getId());
        }
        for (Epic epic : epics.values()) {
            epic.getSubtaskIds().clear();
            updateEpicStatus(epic);
            epic.updateTimeFields(this);
        }
        subtasks.clear();
    }

    @Override
    public void clearEpics() {
        for (Integer id : subtasks.keySet()) {
            freeTimeSlots(subtasks.get(id));
            prioritizedTasks.remove(subtasks.get(id));
            historyManager.remove(id);
        }
        for (Integer id : epics.keySet()) {
            historyManager.remove(id);
        }
        epics.clear();
        subtasks.clear();
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    private boolean hasOverlap(Task task) {
        return prioritizedTasks.stream()
                .filter(t -> t.getStartTime() != null && t.getDuration() != null)
                .anyMatch(t -> isOverlapping(task, t));
    }

    private boolean isOverlapping(Task task1, Task task2) {
        if (task1.getStartTime() == null || task1.getDuration() == null ||
                task2.getStartTime() == null || task2.getDuration() == null) {
            return false;
        }
        LocalDateTime start1 = task1.getStartTime();
        LocalDateTime end1 = task1.getEndTime();
        LocalDateTime start2 = task2.getStartTime();
        LocalDateTime end2 = task2.getEndTime();
        return !(end1.isBefore(start2) || end2.isBefore(start1));
    }

    private int generateId() {
        return ++id;
    }

    private void reserveTimeSlots(Task task) {
        if (task.getStartTime() == null || task.getDuration() == null) return;
        LocalDateTime start = task.getStartTime();
        LocalDateTime end = task.getEndTime();
        LocalDateTime current = start.truncatedTo(ChronoUnit.MINUTES);
        while (current.isBefore(end) || current.equals(end)) {
            if (timeSlots.getOrDefault(current, false)) {
                throw new IllegalStateException("Time slot already reserved");
            }
            timeSlots.put(current, true);
            current = current.plusMinutes(15);
        }
    }

    private void freeTimeSlots(Task task) {
        if (task.getStartTime() == null || task.getDuration() == null) return;
        LocalDateTime start = task.getStartTime();
        LocalDateTime end = task.getEndTime();
        LocalDateTime current = start.truncatedTo(ChronoUnit.MINUTES);
        while (current.isBefore(end) || current.equals(end)) {
            timeSlots.remove(current);
            current = current.plusMinutes(15);
        }
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
