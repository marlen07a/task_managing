public class Main {
    public static void main(String[] args) {
        TaskManager manager = new TaskManager();

        Task task1 = new Task(manager.generateId(), "Jump", Status.NEW);
        Task task2 = new Task(manager.generateId(), "Run", Status.NEW);
        manager.addTask(task1);
        manager.addTask(task2);

        Epic epic1 = new Epic(manager.generateId(), "Clean");
        manager.addEpic(epic1);

        Subtask subtask1 = new Subtask(epic1.getId(), manager.generateId(), "Wash clothes", Status.NEW);
        Subtask subtask2 = new Subtask(epic1.getId(), manager.generateId(), "Vacuum the house", Status.NEW);
        manager.addSubtask(subtask1);
        manager.addSubtask(subtask2);

        Epic epic2 = new Epic(manager.generateId(), "Study");
        manager.addEpic(epic2);

        Subtask subtask3 = new Subtask(epic2.getId(), manager.generateId(), "Do homework", Status.NEW);
        manager.addSubtask(subtask3);

        System.out.println("Initial State");
        manager.printAllTasks();

        task1.status = Status.DONE;
        manager.updateTask(task1);

        subtask1.status = Status.DONE;
        subtask2.status = Status.DONE;
        manager.updateSubtask(subtask1);
        manager.updateSubtask(subtask2);

        subtask3.status = Status.IN_PROGRESS;
        manager.updateSubtask(subtask3);

        System.out.println("After updating Statuses");
        manager.printAllTasks();

        manager.deleteTaskById(task2.getId());
        manager.deleteEpicById(epic2.getId());

        System.out.println("After deleting a Task and an Epic");
        manager.printAllTasks();

        manager.clearAllTasks();
        System.out.println("After clearing all Tasks");
        manager.printAllTasks();
    }
}
