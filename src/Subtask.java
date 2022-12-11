public class Subtask extends Task {
    private Epic epic;

    //======================================================
    //
    //  Конструкторы
    //
    //======================================================

    public Subtask(Epic epic, String name, String description) {
        super(name, description);
        this.epic = epic;
    }

    public Subtask(Epic epic, String name, String description, TaskStatus status, int id) {
        super(name, description, status, id);
        this.epic = epic;
    }

    //======================================================
    //
    //  Геттеры-сеттеры
    //
    //======================================================

    public Subtask(String name, String description, Epic epic) {
        super(name, description);
        this.epic = epic;
    }

    public Epic getEpic() {
        return epic;
    }

    public void setEpic(Epic epic) {
        this.epic = epic;
    }
}
