package com.yandex.app.service;

import com.yandex.app.model.*;

import java.util.ArrayList;
import java.util.List;

public interface TaskManager {
    int addTask(Task task);

    int addEpic(Epic epic);

    int addSubtask(Subtask subtask);

    void updateTask(Task task);

    void updateEpic(Epic epic);

    void updateSubtask(Subtask subtask);

    Task getTask(int id);

    Subtask getSubtask(int id);

    Epic getEpic(int id);

    void deleteTaskById(int id);

    void deleteEpicById(int id);

    void deleteSubtaskById(int id);

    ArrayList<Task> getAllTasks();

    ArrayList<Epic> getAllEpics();

    ArrayList<Subtask> getAllSubtasks();

    ArrayList<Subtask> getSubtasksByEpicId(int epicId);

    void clearTasks();

    void clearSubtasks();

    void clearEpics();

    List<Task> getHistory();
}