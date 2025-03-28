import java.util.ArrayList;

public class Subtask extends Task {
    private final int epicId;

    public Subtask(int epicId, int id, String name, Status status) {
        super(id, name, status);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }
}
