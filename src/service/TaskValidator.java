package service;

import model.Task;

import java.util.List;

public interface TaskValidator {

    void validate (TaskManager manager, Task task);
    //Метод, вызываемый в случае добавления задачи в менеджер
    void onAddTask(Task task);

    //Метод, вызываемый в случае удаления отдельной задачи из менеджера
    void onRemoveTask (Task task);

    //Метод, вызываемый в случие удаления набора задач из менеджера
    void onRemoveTasks (List<Task> tasks);

}
