package com.yandex.app.model;

import com.yandex.app.service.InMemoryTaskManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

public class Epic extends Task {

    private final ArrayList<Integer> subtaskIds = new ArrayList<>();
    private LocalDateTime endTime;

    public Epic(int id, String name, String description) {
        super(id, name, description, Status.NEW, null, null);
    }

    public Epic(String name, String description) {
        super(name, description, Status.NEW, null, null);
    }

    public ArrayList<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    public void addSubtaskId(int subtaskId) {
        subtaskIds.add(subtaskId);
    }

    public void removeSubtaskId(int subtaskId) {
        subtaskIds.remove((Integer) subtaskId);
    }

    public void clearSubtasks() {
        subtaskIds.clear();
    }

    public void setStatusEpic(Status status) {
        this.setStatus(status);
    }

    public void updateTimeFields(InMemoryTaskManager manager) {
        if (subtaskIds.isEmpty()) {
            setDuration(null);
            setStartTime(null);
            endTime = null;
            return;
        }

        Duration totalDuration = Duration.ZERO;
        LocalDateTime earliestStart = null;
        LocalDateTime latestEnd = null;

        for (Integer subtaskId : subtaskIds) {
            Optional<Subtask> subtaskOpt = manager.getSubtask(subtaskId);
            if (subtaskOpt.isPresent()) {
                Subtask subtask = subtaskOpt.get();
                if (subtask.getStartTime() != null && subtask.getDuration() != null) {
                    totalDuration = totalDuration.plus(subtask.getDuration());
                    if (earliestStart == null || subtask.getStartTime().isBefore(earliestStart)) {
                        earliestStart = subtask.getStartTime();
                    }
                    LocalDateTime subtaskEnd = subtask.getEndTime();
                    if (subtaskEnd != null && (latestEnd == null || subtaskEnd.isAfter(latestEnd))) {
                        latestEnd = subtaskEnd;
                    }
                }
            }
        }

        setDuration(totalDuration);
        setStartTime(earliestStart);
        endTime = latestEnd;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    @Override
    public TaskType getType() {
        return TaskType.EPIC;
    }
}
