package model;

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

    @Override
    public String toString() {
        return this.getClass().toString() + "{" + // имя класса
                super.fieldsToString() +
                ", epicId='" + this.epicId + '\'' + // поле3=значение3
                '}';
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash += epicId.hashCode();
        return hash * 31;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Subtask subtask = (Subtask) o;

        //Если основные поля не совпадут, сравнение можно не продолжать
        if (!super.equals(subtask)) {
            return false;
        }

        //Если ранее все было идентично, итоговый результат определится равенством перечней подзадач
        return this.epicId.equals(subtask.epicId);
    }
}
