package tasks;

public class Subtask extends Task {
    protected Integer epicId;

    public Subtask(Epic epic, String name, String discription) {
        super(name, discription);
        this.setEpicId(epic.getId());
    }

    public Subtask(Epic epic, String name, String discription, TaskStatus status, int id) {
        super(name, discription, status, id);
        this.setEpicId(epic.getId());
    }

    public Integer getEpicId() {
        return epicId;
    }

    public void setEpicId(Integer epicId) {
        if (epicId == -1) {
            throw new Error("Экземпляр класса Epic перед добавлением подзадачи предварительно не добавлен в менеджер");
        }
        this.epicId = epicId;
    }

    public String toString() {
        return this.getClass().toString() + "{" + // имя класса
                "status='" + this.getStatus().toString() + '\'' + // поле1=значение1
                ", id='" + this.getId() + '\'' + // поле1=значение1
                ", name='" + this.getName() + '\'' + // поле2=значение2
                ", description=" + this.getDiscription() + // поле3=значение3
                ", epicId=" + this.epicId + // поле3=значение3
                '}';
    }
}
