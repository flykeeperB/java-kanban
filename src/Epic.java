import java.util.HashMap;

public class Epic extends Task {

    //=====================================================
    //
    //  Всякие конструкторы
    //
    //======================================================

    public Epic(String name, String description) {
        super(name, description);
    }

    public Epic(String name, String description, TaskStatus status, int id) {
        super(name, description, status, id);
    }

    //======================================================
    //
    //  Геттеры-сеттеры
    //
    //======================================================

    @Override
    public TaskStatus getStatus() {
        if (this.getSubtask().isEmpty()) {
            //Если список подзадач пуст, то статус NEW
            return TaskStatus.NEW;
        }

        TaskStatus status = null; //Промежуточное значение статуса

        //Обходим подзадачи
        for (Task subtask : this.getSubtask().values()) {
            if (status == null) {
                //Если статус ранее не определялся, значит это первая итерация, присваиваем и идем дальше
                status = subtask.getStatus();
            } else if (status != subtask.getStatus()) {
                //Если статус при обходе меняется, значит задачи разнородные
                return TaskStatus.IN_PROGRESS;
            }
        }
        //Если не попались разнородные задачи, значит status содержит статус всех подзадач
        return status;
    }

    @Override
    public void setStatus(TaskStatus status) {
        //блокируем ручное изменение статуса, переопределяя его
    }

    public HashMap<Integer, Task> getSubtask() {
        return this.taskStorage.getNodeItems(this);
    }

}
