import java.util.ArrayList;

public class Epic extends Task {
    public ArrayList<Integer> subtaskIds;

    public Epic(String name, String discription) {
        super(name, discription);
        this.subtaskIds = new ArrayList<>();
    }

    public Epic(String name, String discription, TaskStatus status, int id) {
        super(name, discription, status, id);
        this.subtaskIds = new ArrayList<>();
    }

    public ArrayList<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    @Override
    public String toString() {
        return this.getClass().toString() + "{" + // имя класса
                "status='" + this.getStatus().toString() + '\'' + // поле1=значение1
                ", id='" + this.getId() + '\'' + // поле1=значение1
                ", name='" + this.getName() + '\'' + // поле2=значение2
                ", description=" + this.getDiscription() + // поле3=значение3
                ", subtaskIds=" + this.subtaskIds.toString() + // поле3=значение3
                '}';
    }

}
