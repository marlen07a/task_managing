public class Epic extends Task {

    public Epic(int id, String name) {
        super(id, name, Status.NEW);
    }

    public void setStatus(Status status) {
        this.status = status;
    }

}
