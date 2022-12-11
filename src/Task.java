import java.util.ArrayList;

public class Task {
    private int id;
    private String name;
    private String description;
    private TaskStatus status;
    protected TaskStorage taskStorage;

    //=====================================================
    //
    //  Всякие конструкторы
    //
    //======================================================

    //Task() {
    //}

    public Task(String name, String description) {
        this.name = name;
        this.description = description;
        this.id = 0;
        this.status = TaskStatus.NEW;
    }

    public Task(String name, String description, TaskStatus status, int id) {
        this(name, description);
        this.id = id;
        this.status = status;
    }


    //======================================================
    //
    //  Геттеры-сеттеры
    //
    //======================================================


    public void setTaskStorage(TaskStorage taskStorage) {
        this.taskStorage = taskStorage;
    }

    public Integer getId() {
        return id;
    }

    public void setID(int id) {
        this.id = id;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDiscription() {
        return description;
    }

    public void setDiscription(String discription) {
        this.description = discription;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return this.getClass().toString()+"{" + // имя класса
                "status='" + status.toString() + '\'' + // поле1=значение1
                ", id='" + id + '\'' + // поле1=значение1
                ", name='" + name + '\'' + // поле2=значение2
                ", description=" + description + // поле3=значение3
                '}';
    }
}
