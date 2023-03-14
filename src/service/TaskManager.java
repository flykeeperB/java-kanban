package service;

import java.util.ArrayList;
import java.util.List;

import model.*;

public interface TaskManager {

    // Создание (добавление) задачи
    Task appendTask(Task task);

    // Создание (добавление) эпика
    Epic appendEpic(Epic epic);

    // Создание (добавление) подзадачи
    Subtask appendSubtask(Subtask subtask);

    // Обновление записи задачи
    Task updateTask(Task task);

    // Обновление записи эпика
    Epic updateEpic(Epic epic);

    // Обновление записи подзадачи
    Subtask updateSubtask(Subtask subtask);

    // Удаление задачи/эпика/подзадачи по идентификатору
    boolean delete(Integer id);

    // Удаление задачи
    boolean deleteTask(Integer id);

    // Удаление подзадачи
    boolean deleteSubtask(Integer id);

    // Удаление эпика
    boolean deleteEpic(Integer id);

    // Импорт задач без генерации идентификатора
    Task importTask (Task task);
    Task importEpic (Epic epic);
    Task importSubtask (Subtask subtask);

    // Очистка всех задач
    void clearTasks();

    // Удаление всех эпиков
    void clearEpics();

    // Удаление всех подзадач
    void clearSubtasks();

    // Удаление всех задач, эпиков, подзадач
    void clearAll();

    // Получить задачу (Task) из хранилища по идентификатору
    Task getTask(Integer id);

    // Получить эпик (Epic) из хранилища по идентификатору
    Epic getEpic(Integer id);

    // Получить подзадачу (Subtask) из хранилища по идентификатору
    Subtask getSubtask(Integer id);

    // Получить список задач
    List<Task> getTasks();

    // Получить список эпиков
    List<Epic> getEpics();

    // Получить список подзадач
    List<Subtask> getSubtasks();
    List<Subtask> getEpicSubtasks(Epic epic);
    Epic getEpicOfSubtask(Subtask subtask);

    public List<Task> getPrioritizedTasks();

    // Получить историю
    List<Task> getHistory();

    //Получение списка задач/эпиков/подзадач
    default List<Task> getAllTasks() {
        List<Task> tasks = this.getTasks();
        tasks.addAll(this.getEpics());
        tasks.addAll(this.getSubtasks());
        return tasks;
    }
}
