import java.util.HashMap;
import java.util.ArrayList;

public class TaskManager {
    private static int id = 0;
    private final HashMap<Integer, Task> tasks = new HashMap<>();
    private final HashMap<Integer, Epic> epics = new HashMap<>();
    private final HashMap<Integer, Subtask> subtasks = new HashMap<>();

    public int generateId() {
        return ++id;
    }

    public void addTask(Task task) {
        tasks.put(task.getId(), task);
    }

    public void addEpic(Epic epic) {
        epics.put(epic.getId(), epic);
    }

    public void addSubtask(Subtask subtask) {
        if (!epics.containsKey(subtask.getEpicId())) {
            System.out.println("Эпик с ID " + subtask.getEpicId() + " не найден.");
            return;
        }

        subtasks.put(subtask.getId(), subtask);
        updateEpicStatus(subtask.getEpicId());
    }

    public void updateTask(Task task) {
        if (tasks.containsKey(task.getId())) {
            tasks.put(task.getId(), task);
        } else {
            System.out.println("Задача с ID " + task.getId() + " не найдена.");
        }
    }

    public void updateEpic(Epic epic) {
        if (epics.containsKey(epic.getId())) {
            epics.put(epic.getId(), epic);
        } else {
            System.out.println("Эпик с ID " + epic.getId() + " не найден.");
        }
    }

    public void updateSubtask(Subtask subtask) {
        if (subtasks.containsKey(subtask.getId())) {
            subtasks.put(subtask.getId(), subtask);
        } else {
            System.out.println("Подзадача с ID " + subtask.getId() + " не найдена.");
        }
        updateEpicStatus(subtask.getEpicId());
    }

    public Task getTaskById(int id) {
        if (tasks.containsKey(id)) return tasks.get(id);
        if (epics.containsKey(id)) return epics.get(id);
        if (subtasks.containsKey(id)) return subtasks.get(id);
        else {
            System.out.println("задачи с таким идентификатором не существует");
            return null;
        }
    }

    public void deleteTaskById(int id) {
        if (tasks.containsKey(id)) {
            tasks.remove(id);
            System.out.println("Задача с ID " + id + " удалена.");
        } else {
            System.out.println("Задача с таким ID не найдена.");
        }
    }

    public void deleteEpicById(int id) {
        if (epics.containsKey(id)) {

            ArrayList<Subtask> subtasksToRemove = getSubtasksByEpicId(id);

            for (Subtask subtask : subtasksToRemove) {
                subtasks.remove(subtask.getId());
            }
            epics.remove(id);
            System.out.println("Эпик с ID " + id + " удалён.");
        } else {
            System.out.println("Эпик с таким ID не найден.");
        }
    }

    public void deleteSubtaskById(int id) {
        if (subtasks.containsKey(id)) {
            subtasks.remove(id);
            System.out.println("Подзадача с ID " + id + " удалена.");
        } else {
            System.out.println("Подзадача с таким ID не найдена.");
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

    public void updateEpicStatus(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) return;
        ArrayList<Subtask> epicSubtasks = getSubtasksByEpicId(epicId);
        if (epicSubtasks.isEmpty()) {
            epic.setStatus(Status.NEW);
            return;
        }

        boolean allDone = true;
        boolean allNew = true;

        for (Subtask subtask : epicSubtasks) {
            if (subtask.getStatus() != Status.NEW) {
                allNew = false;
            }
            if (subtask.getStatus() != Status.DONE) {
                allDone = false;
            }
        }

        if (allDone) {
            epic.setStatus(Status.DONE);
        } else if (allNew) {
            epic.setStatus(Status.NEW);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }
    }

    public Status getTaskStatus(int taskId) {
        if (tasks.containsKey(taskId)) return tasks.get(taskId).getStatus();
        if (epics.containsKey(taskId)) return epics.get(taskId).getStatus();
        if (subtasks.containsKey(taskId)) return subtasks.get(taskId).getStatus();

        System.out.println("Задача с ID " + taskId + " не найдена.");
        return null;
    }

    public void printAllTasks() {
        System.out.println("Обычные задачи: " + tasks.values());
        System.out.println("Эпики: " + epics.values());
        System.out.println("Подзадачи: " + subtasks.values());
    }

    public void clearAllTasks() {
        tasks.clear();
        epics.clear();
        subtasks.clear();
        System.out.println("Все задачи были удалены.");
    }
}
