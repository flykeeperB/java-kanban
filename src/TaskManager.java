import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {
    private TaskStorage taskStorage;
    private int newId = 0; //Последний сформированный менеджером идентификатор задачи.

    public TaskManager() {
        taskStorage = new TaskStorage();
    }

    //Создание идентификатора задачи при её создании
    private int generateID() {
        return newId++;
    }

    //Добавление. Используется при создании или обновлении задачи
    private void addToStorage(Task task) {
        if (task.getClass() == Subtask.class) {
            this.taskStorage.add(((Subtask) task).getEpic(), task); //Если это подзадача, добавляем её к эпику
        } else {
            this.taskStorage.add(null, task); //Если это задача или эпик, добавляем её в корень хранилища
        }
    }

    //Создание. Сам объект должен передаваться в качестве параметра
    public void createTask(Task task) {
        if (task==null) {
            return; //Выходим, если передана пустой объект
        }
        task.setID(this.generateID());  //При создании задачи всегда создаем ей новый идентификатор
        task.setStatus(TaskStatus.NEW); //Статус новой задачи всегда NEW
        task.setTaskStorage(this.taskStorage);
        this.addToStorage(task);
    }

    //Получение по идентификатору
    public Task getTaskById(Integer id) {
        return this.taskStorage.get(id);
    }

    //Удаление по идентификатору
    public boolean removeById(Integer id) {
        return this.taskStorage.remove(id);
    }

    //Обновление. Новая версия объекта с верным идентификатором передаётся в виде параметра.
    public boolean updateTask(Task task) {
        Integer id = task.getId();
        //удаление старой версии задачи позволяет изменить тип новой задачи
        if (this.taskStorage.remove(id)) {
            this.addToStorage(task);
            return true;
        };
        return false;
    }

    //Получение списка всех задач
    public ArrayList<Task> getAllTasks() {
        return this.taskStorage.items();
    }

    public ArrayList<Task> getAllSubtasks() {
        return this.taskStorage.itemsOfClass(Subtask.class);
    }

    public ArrayList<Task> getAllEpic() {
        return this.taskStorage.itemsOfClass(Epic.class);
    }

    //Удаление всех задач
    public void clearTasks() {
        this.taskStorage.clear();
    }

    @Override
    public String toString() {
        String result = this.getClass().toString()+"\n"
        +"{newId="+this.newId+"}"+"\n"
        +"Tasks:"+"\n";
        for (Task task : this.taskStorage.items()) {
            result += task.toString()+"\n";
        }
        result += "-".repeat(20);
        return result;
    }
}
