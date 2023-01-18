package service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import model.*;

public class InMemoryTaskManager implements TaskManager {
    private final HashMap<Integer, Task> tasks;
    private final HashMap<Integer, Epic> epics;
    private final HashMap<Integer, Subtask> subtasks;
    HistoryManager historyManager;
    private int newId = 1; //Очередной идентификатор задачи

    public InMemoryTaskManager() {
        tasks = new HashMap<>();
        epics = new HashMap<>();
        subtasks = new HashMap<>();
        historyManager = Managers.getDefaultHistory();
    }

    // Создание (добавление) задачи
    @Override
    public Task appendTask(Task task) {
        if (task == null) {
            return null;
        }
        if (getTask(task.getId(), false) != null) {
            //если пытаемся добавить объект идентификатор которого уже имеется в хранилище
            return null;
        }


        task.setID(generateID());     //генерируем идентификатор
        tasks.put(task.getId(), task); //добавляем задачу в хранилище

        return task;
    }

    // Создание (добавление) эпика
    @Override
    public Epic appendEpic(Epic epic) {
        if (epic == null) {
            return null;
        }
        if (getEpic(epic.getId(), false) != null) {
            //если пытаемся добавить объект идентификатор которого уже имеется в хранилище
            return null;
        }

        //общая обработка добавления
        epic.setID(generateID());
        epic.clearSubtaskIds(); //очищаем, т.к. подзадачи должны добавляться после добавления эпика в менеджер
        epics.put(epic.getId(), epic);

        return epic;
    }

    // Создание (добавление) подзадачи
    @Override
    public Subtask appendSubtask(Subtask subtask) {
        if (subtask == null) {
            return null;
        }
        if (getSubtask(subtask.getId(), false) != null) {
            //если пытаемся добавить объект идентификатор которого уже имеется в хранилище
            return null;
        }

        //общая обработка добавления
        subtask.setID(generateID());
        subtasks.put(subtask.getId(), subtask);
        updateEpicFromSubtasksInfo(getEpic(subtask.getEpicId(), false)); //обновление эпика, связанное с добавлением подзадачи

        return subtask;
    }

    // Обновление записи задачи
    @Override
    public Task updateTask(Task task) {
        if (task == null) {
            return null;
        }
        if (getTask(task.getId(), false) == null) {
            //если пытаемся обновить объект, идентификатора которого нет в хранилище
            return null; // выходим
        }

        //удалить сабтаск из старого эпика
        if (tasks.containsKey(task.getId())) {
            tasks.replace(task.getId(), task);
            return task;
        }
        return null;
    }

    // Обновление записи эпика
    @Override
    public Task updateEpic(Epic epic) {
        if (epic == null) {
            return null;
        }
        if (getEpic(epic.getId(), false) == null) {
            //если пытаемся обновить объект, идентификатора которого нет в хранилище
            return null; // выходим
        }

        //удалить сабтаск из старого эпика
        if (epics.containsKey(epic.getId())) {
            epics.replace(epic.getId(), epic);
            updateEpicFromSubtasksInfo(epic);
            return epic;
        }
        return null;
    }

    // Обновление записи подзадачи
    @Override
    public Task updateSubtask(Subtask subtask) {
        if (subtask == null) {
            return null;
        }
        if (getSubtask(subtask.getId(), false) == null) {
            //если пытаемся обновить объект, идентификатора которого нет в хранилище
            return null; // выходим
        }

        if (subtasks.containsKey(subtask.getId())) {
            subtasks.replace(subtask.getId(), subtask);
            updateEpicFromSubtasksInfo(getEpic(subtask.getEpicId(), false)); //обновление эпика, связанное с обновлением подзадачи
            return subtask;
        }
        return null;
    }

    // Удаление задачи/эпика/подзадачи по идентификатору
    @Override
    public boolean delete(Integer id) {
        return deleteTask(id) || deleteSubtask(id) || deleteEpic(id);
    }

    // Удаление задачи по идентификатору
    @Override
    public boolean deleteTask(Integer id) {
        //Проверяем, что идентификатор указывает на реальный объект
        Task task = getTask(id, false);
        if ((task == null) || (!tasks.containsKey(id))) {
            return false;
        }

        tasks.remove(id);
        historyManager.remove(id); //удаляем задачу из истории

        return true;
    }

    // Удаление подзадачи по идентификатору
    @Override
    public boolean deleteSubtask(Integer id) {
        //Проверяем, что идентификатор указывает на реальный объект
        Subtask subtask = getSubtask(id, false);
        if ((subtask == null) || (!subtasks.containsKey(id))) {
            return false;
        }

        subtasks.remove(id);
        historyManager.remove(id); //удаляем подзадачу из истории

        //Удаляем идентификатор подзадачи из списка подзадач эпика
        updateEpicFromSubtasksInfo(getEpic(subtask.getEpicId(), false));

        return true;
    }

    // Удаление эпика по идентификатору
    @Override
    public boolean deleteEpic(Integer id) {
        //Проверяем, что идентификатор указывает на реальный объект
        Epic epic = getEpic(id, false);
        if ((epic == null) || (!epics.containsKey(id))) {
            return false;
        }

        epics.remove(id);

        //Удаляем подзадачи
        for (Integer subtaskId : epic.getSubtaskIds()) {
            deleteSubtask(subtaskId);
        }

        historyManager.remove(id); //удаляем эпик из истории

        return true;
    }

    // Удаление всех задач, эпиков, подзадач
    @Override
    public void clearTasks() {
        tasks.clear();
    }

    // Удаление всех эпиков
    @Override
    public void clearEpics() {
        epics.clear();
        clearSubtasks(); //удаляем подзадачи, т.к. они не могут существовать без эпиков
    }

    // Удаление всех подзадач
    @Override
    public void clearSubtasks() {
        subtasks.clear();
    }

    // Удаление всех подзадач
    @Override
    public void clearAll() {
        clearTasks();
        clearEpics();
        clearSubtasks();
    }

    // Получить задачу (Task) из хранилища по идентификатору
    @Override
    public Task getTask(Integer id) {
        return getTask(id, true);
    }

    // Получить задачу (Task) из хранилища по идентификатору
    // Опционально внести запись в историю просмотров (может быть отключена для служебных запросов)
    public Task getTask(Integer id, boolean useHistory) {
        Task result = tasks.getOrDefault(id, null);
        if (result != null && useHistory) {
            historyManager.add(result);
        }
        return result;
    }

    // Плучить эпик (Epic) из хранилища по идентификатору
    @Override
    public Epic getEpic(Integer id) {
        return getEpic(id, true);
    }

    // Получить эпик (Epic) из хранилища по идентификатору
    // Опционально внести запись в историю просмотров (может быть отключена для служебных запросов)
    public Epic getEpic(Integer id, boolean useHistory) {
        Epic result = epics.getOrDefault(id, null);
        if (result != null && useHistory) {
            historyManager.add(result);
        }
        return result;
    }

    // Получить подзадачу (Subtask) из хранилища по идентификатору
    @Override
    public Subtask getSubtask(Integer id) {
        return getSubtask(id, true);
    }

    // Получить подзадачу (Subtask) из хранилища по идентификатору
    // Опционально внести запись в историю просмотров (может быть отключена для служебных запросов)
    public Subtask getSubtask(Integer id, boolean useHistory) {
        Subtask result = subtasks.getOrDefault(id, null);
        if (result != null && useHistory) {
            historyManager.add(result);
        }
        return result;
    }

    // Получить список задач
    @Override
    public ArrayList<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    // Получить список эпиков
    @Override
    public ArrayList<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    // Получить список подзадач
    @Override
    public ArrayList<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    // Получить историю обращений к задачам/эпикам/подзадачам
    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    //Служебный метод. получить сабтаски по списку идентификаторов
    //upd ТЗ-4. Результаты вызова метода не заносятся в историю просмотров задач
    private ArrayList<Subtask> getSubtasks(ArrayList<Integer> ids) {
        ArrayList<Subtask> result = new ArrayList<>();
        for (Integer id : subtasks.keySet()) {
            if (ids.contains(id)) {
                result.add(subtasks.get(id));
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
        ArrayList<Subtask> subtasks = getSubtasks();
        for (Subtask subtask : subtasks) {
            if (epic.getId().equals(subtask.getEpicId())) {
                epic.addSubtaskIds(subtask.getId());
            }
        }

        //Обновляем статус
        epic.setStatus(TaskStatus.NEW); //По умолчанию статус NEW

        boolean isFirstStep = true; //Флаг для определения первого шага

        //Обходим подзадачи
        for (Subtask subtask : getSubtasks(epic.getSubtaskIds())) {
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
        String result = getClass().toString() + "\n"
                + "{newId=" + newId + "}" + "\n"
                + "Epics:" + "\n";
        for (Task task : getEpics()) {
            result += task.toString() + "\n";
        }
        result += "Subtasks:" + "\n";
        for (Task task : getSubtasks()) {
            result += task.toString() + "\n";
        }
        result += "Tasks:" + "\n";
        for (Task task : getTasks()) {
            result += task.toString() + "\n";
        }
        result += "-".repeat(20);
        return result;
    }
}

