package taskmanager;

import java.util.ArrayList;
import java.util.HashMap;

import tasks.*;

public class TaskManager {
    private HashMap<Integer, Task> tasks;
    private HashMap<Integer, Epic> epics;
    private HashMap<Integer, Subtask> subtasks;
    private int newId = 0; //Очередной идентификатор задачи

    public TaskManager() {
        this.tasks = new HashMap<>();
        this.epics = new HashMap<>();
        this.subtasks = new HashMap<>();
    }

    // Создание (добавление) задачи
    public Task appendTask(Task task) {
        if (task == null) {
            return null;
        }
        if (this.getTask(task.getId()) != null) {
            //если пытаемся добавить объект идентификатор которого уже имеется в хранилище
            return null;
        }


        task.setID(this.generateID());     //генерируем идентификатор
        this.tasks.put(task.getId(), task); //добавляем задачу в хранилище

        return task;
    }

    // Создание (добавление) эпика
    public Epic appendEpic(Epic epic) {
        if (epic == null) {
            return null;
        }
        if (this.getEpic(epic.getId()) != null) {
            //если пытаемся добавить объект идентификатор которого уже имеется в хранилище
            return null;
        }

        //общая обработка добавления
        epic.setID(this.generateID());
        epic.clearSubtaskIds(); //очищаем, т.к. подзадачи должны добавляться после добавления эпика в менеджер
        this.epics.put(epic.getId(), epic);

        return epic;
    }

    // Создание (добавление) подзадачи
    public Subtask appendSubtask(Subtask subtask) {
        if (subtask == null) {
            return null;
        }
        if (this.getSubtask(subtask.getId()) != null) {
            //если пытаемся добавить объект идентификатор которого уже имеется в хранилище
            return null;
        }

        //общая обработка добавления
        subtask.setID(this.generateID());
        this.subtasks.put(subtask.getId(), subtask);
        this.updateEpicFromSubtasksInfo(this.getEpic(subtask.getEpicId())); //обновление эпика, связанное с добавлением подзадачи

        return subtask;
    }

    //Обновление
    public Task updateTask(Task task) {
        if (task == null) {
            return null;
        }
        if (this.getTask(task.getId()) == null) {
            //если пытаемся обновить объект, идентификатора которого нет в хранилище
            return null; // выходим
        }

        //удалить сабтаск из старого эпика
        if (this.tasks.containsKey(task.getId())) {
            this.tasks.replace(task.getId(), task);
            //this.checkSubtaskForUpdateEpic(task); //обновление эпика, связанное с обновлением подзадачи
            return task;
        }
        return null;
    }

    public Task updateEpic(Epic epic) {
        if (epic == null) {
            return null;
        }
        if (this.getEpic(epic.getId()) == null) {
            //если пытаемся обновить объект, идентификатора которого нет в хранилище
            return null; // выходим
        }

        //удалить сабтаск из старого эпика
        if (this.epics.containsKey(epic.getId())) {
            this.epics.replace(epic.getId(), epic);
            this.updateEpicFromSubtasksInfo(epic);
            return epic;
        }
        return null;
    }

    public Task updateSubtask(Subtask subtask) {
        if (subtask == null) {
            return null;
        }
        if (this.getSubtask(subtask.getId()) == null) {
            //если пытаемся обновить объект, идентификатора которого нет в хранилище
            return null; // выходим
        }

        if (this.subtasks.containsKey(subtask.getId())) {
            this.subtasks.replace(subtask.getId(), subtask);
            this.updateEpicFromSubtasksInfo(this.getEpic(subtask.getEpicId())); //обновление эпика, связанное с обновлением подзадачи
            return subtask;
        }
        return null;
    }

    public boolean delete(Integer id) {
        if (this.deleteTask(id) || this.deleteSubtask(id) || this.deleteEpic(id)) {
            return true;
        }
        return false;
    }

    //v3 Удаление задачи
    public boolean deleteTask(Integer id) {
        //Проверяем, что идентификатор указывает на реальный объект
        Task task = this.getTask(id);
        if ((task == null) || (!this.tasks.containsKey(id))) {
            return false;
        }

        this.tasks.remove(id);

        return true;
    }

    //v3 Удаление подзадачи
    public boolean deleteSubtask(Integer id) {
        //Проверяем, что идентификатор указывает на реальный объект
        Subtask subtask = this.getSubtask(id);
        if ((subtask == null) || (!this.subtasks.containsKey(id))) {
            return false;
        }

        this.subtasks.remove(id);

        //Удаляем идентификатор подзадачи из списка подзадач эпика
        this.updateEpicFromSubtasksInfo(this.getEpic(subtask.getEpicId()));

        return true;
    }

    //v3 удаление эпика
    public boolean deleteEpic(Integer id) {
        //Проверяем, что идентификатор указывает на реальный объект
        Epic epic = this.getEpic(id);
        if ((epic == null) || (!this.epics.containsKey(id))) {
            return false;
        }

        this.epics.remove(id);

        //Удаляем подзадачи
        for (Integer subtaskId : epic.getSubtaskIds()) {
            this.deleteSubtask(subtaskId);
        }
        return true;
    }

    //v3 Очистка всех задач, эпиков, подзадач
    public void clearTasks() {
        this.tasks.clear();
    }

    //v3
    public void clearEpics() {
        this.epics.clear();
        this.clearSubtasks(); //удаляем подзадачи, т.к. они не могут существовать без эпиков
    }

    //v3
    public void clearSubtasks() {
        this.subtasks.clear();
    }

    //v3
    public void clearAll() {
        this.clearTasks();
        this.clearEpics();
        this.clearSubtasks();
    }

    // Получить задачу (Task) из хранилища по идентификатору
    public Task getTask(Integer id) {
        return this.tasks.getOrDefault(id, null);
    }

    // Получить эпик (Epic) из хранилища по идентификатору
    public Epic getEpic(Integer id) {
        return this.epics.getOrDefault(id, null);
    }

    // Получить подзадачу (Subtask) из хранилища по идентификатору
    public Subtask getSubtask(Integer id) {
        return this.subtasks.getOrDefault(id, null);
    }

    // Получить эпики/подзадачи/задачи из хранилища менеджера
    public ArrayList<Task> getTasks() {
        return new ArrayList<>(this.tasks.values());
    }

    public ArrayList<Epic> getEpics() {
        return new ArrayList<>(this.epics.values());
    }

    public ArrayList<Subtask> getSubtasks() {
        return new ArrayList<>(this.subtasks.values());
    }

    //Служебный метод. получить сабтаски по списку идентификаторов
    private ArrayList<Subtask> getSubtasks(ArrayList<Integer> ids) {
        ArrayList<Subtask> result = new ArrayList<>();
        for (Integer id : this.subtasks.keySet()) {
            if (ids.contains(id)) {
                result.add(this.subtasks.get(id));
            }
        }
        return result;
    }


    // Служебный метод. Генерация идентификатора задачи и наследников
    private int generateID() {
        return newId++;
    }

    // Служебный метод. Обновление эпика, перечня связанных подзадач, статуса
    private void updateEpicFromSubtasksInfo(Epic epic) {
        if (epic == null) {
            //работаем только с задачами класса Epic
            return;
        }

        //Пересоставляем перечень подзадач
        epic.clearSubtaskIds();
        ArrayList<Subtask> subtasks = this.getSubtasks();
        for (Subtask subtask : subtasks) {
            if (subtask.getEpicId() == epic.getId()) {
                epic.addSubtaskIds(subtask.getId());
            }
        }

        //Обновляем статус
        epic.setStatus(TaskStatus.NEW); //По умолчанию статус NEW

        boolean isFirstStep = true; //Флаг для определения первого шага

        //Обходим подзадачи
        for (Subtask subtask : this.getSubtasks(epic.getSubtaskIds())) {
            if (isFirstStep) {
                //Если это первая итерация, присваиваем и идем дальше
                epic.setStatus(subtask.getStatus());
                isFirstStep = false;
            } else if (epic.getStatus() != subtask.getStatus()) {
                //Если статус при обходе меняется, значит подзадачи разнородные
                epic.setStatus(TaskStatus.IN_PROGRESS);
                return; // сразу выходим
            }
        }
        //если при обходе статус во всех подзадачах не отличался от первого, значит он и определяет статус эпика
    }

    @Override
    public String toString() {
        String result = this.getClass().toString() + "\n"
                + "{newId=" + this.newId + "}" + "\n"
                + "Epics:" + "\n";
        for (Task task : this.getEpics()) {
            result += task.toString() + "\n";
        }
        result += "Subtasks:" + "\n";
        for (Task task : this.getSubtasks()) {
            result += task.toString() + "\n";
        }
        result += "Tasks:" + "\n";
        for (Task task : this.getTasks()) {
            result += task.toString() + "\n";
        }
        result += "-".repeat(20);
        return result;
    }
}
