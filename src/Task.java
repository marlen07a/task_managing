import java.util.Objects;

public class Task {
    private final int id;
    private final String name;
    protected Status status;

    public Task(int id, String name, Status status) {
        this.id = id;
        this.name = name;
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Task {id = " + id + ", name = " + name + ", status = " + status + '}';
    }

    public Status getStatus() {
        return status;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
