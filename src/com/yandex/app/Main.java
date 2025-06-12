package com.yandex.app;

import com.yandex.app.model.*;
import com.yandex.app.service.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        TaskManager manager = Managers.getDefault();

        LocalDateTime now = LocalDateTime.now();
        Task task1 = new Task("Jump", "Jump for 10 minutes", Status.NEW, Duration.ofMinutes(10), now);
        Task task2 = new Task("Run", "Run 5km", Status.NEW, Duration.ofMinutes(30), now.plusHours(1));
        int task1Id = manager.addTask(task1);
        int task2Id = manager.addTask(task2);

        Epic epic1 = new Epic("Clean", "House cleaning tasks");
        int epic1Id = manager.addEpic(epic1);

        Subtask subtask1 = new Subtask(epic1Id, "Wash clothes", "Do the laundry", Status.NEW, Duration.ofMinutes(60), now.plusHours(2));
        Subtask subtask2 = new Subtask(epic1Id, "Vacuum the house", "Clean all rooms", Status.IN_PROGRESS, Duration.ofMinutes(45), now.plusHours(4));
        Subtask subtask3 = new Subtask(epic1Id, "Clean the kitchen", "Wash dishes", Status.DONE, Duration.ofMinutes(30), now.plusHours(6));
        int subtask1Id = manager.addSubtask(subtask1);
        int subtask2Id = manager.addSubtask(subtask2);
        int subtask3Id = manager.addSubtask(subtask3);

        Epic epic2 = new Epic("Study", "Study related tasks");
        int epic2Id = manager.addEpic(epic2);

        System.out.println("Initial state:");
        printAllTasks(manager);

        System.out.println("\nHistory after accessing task1, epic1, subtask1:");
        manager.getTask(task1Id);
        manager.getEpic(epic1Id);
        manager.getSubtask(subtask1Id);
        printHistory(manager);

        System.out.println("\nHistory after accessing task2, subtask2, epic2:");
        manager.getTask(task2Id);
        manager.getSubtask(subtask2Id);
        manager.getEpic(epic2Id);
        printHistory(manager);

        System.out.println("\nHistory after accessing task1 again:");
        manager.getTask(task1Id);
        printHistory(manager);

        System.out.println("\nHistory after deleting task1:");
        manager.deleteTaskById(task1Id);
        printHistory(manager);

        System.out.println("\nHistory after deleting epic1 (should remove epic1 and its subtasks):");
        manager.deleteEpicById(epic1Id);
        printHistory(manager);

        System.out.println("\nPrioritized tasks:");
        for (Task task : manager.getPrioritizedTasks()) {
            System.out.println(task + ", EndTime: " + task.getEndTime());
        }
    }

    public static void printAllTasks(TaskManager taskManager) {
        System.out.println("Все задачи:");
        for (Task task : taskManager.getAllTasks()) {
            System.out.println(task);
        }

        System.out.println("Все эпики:");
        for (Epic epic : taskManager.getAllEpics()) {
            System.out.println(epic + ", Duration: " + epic.getDuration() + ", StartTime: " + epic.getStartTime() + ", EndTime: " + epic.getEndTime());
        }

        System.out.println("Все подзадачи:");
        for (Subtask subtask : taskManager.getAllSubtasks()) {
            System.out.println(subtask);
        }
    }

    public static void printHistory(TaskManager taskManager) {
        List<Task> history = taskManager.getHistory();
        if (history.isEmpty()) {
            System.out.println("History is empty");
        } else {
            for (Task task : history) {
                System.out.println(task);
            }
        }
        System.out.println();
    }
}
