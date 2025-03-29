package com.yandex.app;

import com.yandex.app.model.*;
import com.yandex.app.service.TaskManager;

public class Main {
    public static void main(String[] args) {
        TaskManager manager = new TaskManager();

        Task task1 = new Task("Jump", "Jump for 10 minutes", Status.NEW);
        Task task2 = new Task("Run", "Run 5km", Status.NEW);
        manager.addTask(task1);
        manager.addTask(task2);

        Epic epic1 = new Epic("Clean", "House cleaning tasks");
        manager.addEpic(epic1);

        Subtask subtask1 = new Subtask(epic1.getId(), "Wash clothes", "Do the laundry", Status.NEW);
        Subtask subtask2 = new Subtask(epic1.getId(), "Vacuum the house", "Clean all rooms", Status.NEW);
        manager.addSubtask(subtask1);
        manager.addSubtask(subtask2);

        Epic epic2 = new Epic("Study", "Study related tasks");
        manager.addEpic(epic2);

        Subtask subtask3 = new Subtask(epic2.getId(), "Do homework", "Complete math exercises", Status.NEW);
        manager.addSubtask(subtask3);

        System.out.println("Initial State");
        printAllTasks(manager);

        task1.setStatus(Status.DONE);
        manager.updateTask(task1);

        subtask1.setStatus(Status.DONE);
        subtask2.setStatus(Status.DONE);
        manager.updateSubtask(subtask1);
        manager.updateSubtask(subtask2);

        subtask3.setStatus(Status.IN_PROGRESS);
        manager.updateSubtask(subtask3);

        System.out.println("\nAfter updating Statuses");
        printAllTasks(manager);

        manager.deleteTaskById(task2.getId());
        manager.deleteEpicById(epic2.getId());

        System.out.println("\nAfter deleting a Task and an Epic");
        printAllTasks(manager);

        manager.clearEpics();
        System.out.println("\nAfter clearing only Epics");
        printAllTasks(manager);
    }

    public static void printAllTasks(TaskManager taskManager) {
        System.out.println("Все задачи:");

        for (Task task : taskManager.getAllTasks()) {
            System.out.println(task);
        }

        System.out.println("Все эпики:");
        for (Epic epic : taskManager.getAllEpics()) {
            System.out.println(epic);
        }

        System.out.println("Все подзадачи:");
        for (Subtask subtask : taskManager.getAllSubtasks()) {
            System.out.println(subtask);
        }
    }
}
