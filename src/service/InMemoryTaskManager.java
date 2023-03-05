package service;

import java.util.*;

import model.*;

public class InMemoryTaskManager implements TaskManager {
    private final HashMap<Integer, Task> tasks;
    private final HashMap<Integer, Epic> epics;
    private final HashMap<Integer, Subtask> subtasks;
    HistoryManager historyManager;
    private int newId = 1; //Очередной идентификатор задачи
    private final List<TaskValidator> validators; //Хранилище валидаторов задач
    TreeSet<Task> prioritizedTasks = new TreeSet<>((aTask, bTask)->{
        if ((aTask.getStartTime()==null)&&(bTask.getStartTime()==null)) {
            //Если у задач не определено время, сравниваем по id, чтобы не потерялись...
            return aTask.getId()-bTask.getId();
        }
        //Обрабатываем случаи, когда время одной из задач не определено
        if (aTask.getStartTime() == null) {
            return 1;
        }
        if (bTask.getStartTime() == null) {
            return -1;
        }
        //При одинаковом времени сортируем по id, чтобы не потерялись задачи
        if (aTask.getStartTime().equals(bTask.getStartTime())) {
            return aTask.getId()-bTask.getId();
        }
        //Во всех прочих случаях сравниваем по времени
        return (aTask.getStartTime().isBefore(bTask.getStartTime())?-1:1);
    });

    public void appendValidator(TaskValidator validator) {
        validators.add(validator);
    }

    private boolean validateTask (Task task) {
        for (TaskValidator validator : validators) {
            try {
                validator.validate(this,task);
            } catch (TaskValidatorException e) {
                System.out.println(e.getMessage());
                return false;
            }
            continue;
        }
        return true;
    }

    private void onAddTask (Task task) {
        for (TaskValidator validator : validators) {
            validator.onAddTask(task);
        }
    }

    private void onUpdateTask (Task task) {
        onAddTask(task);
    }

    private void onRemoveTask (Task task) {
        for (TaskValidator validator : validators) {
            validator.onRemoveTask(task);
        }
    }

    private void onRemoveTasks (List<Task> tasks) {
        for (TaskValidator validator : validators) {
            validator.onRemoveTasks(tasks);
        }
    }

    public InMemoryTaskManager() {
        tasks = new HashMap<>();
        epics = new HashMap<>();
        subtasks = new HashMap<>();
        validators = new ArrayList<>();
        historyManager = Managers.getDefaultHistory();
    }

    // Создание (добавление) задачи
    @Override
    public Task appendTask(Task task) {
        if (task == null) {
            return null;
        }
        if (tasks.get(task.getId()) != null) {
            //если пытаемся добавить объект идентификатор которого уже имеется в хранилище
            return null;
        }

        task.setID(generateID());     //генерируем идентификатор
        if (!validateTask(task)) {
            return null;
        }
        tasks.put(task.getId(), task); //добавляем задачу в хранилище
        prioritizedTasks.add(task);
        onAddTask (task);

        return task;
    }

    private void updateNewIdOnImport (Task task) {
        if (this.newId <= task.getId()) {
            newId = task.getId()+1;
        }
    }

    @Override
    public Task importTask (Task task) {
        if (task.getId() < 1) {
            return null;
        }
        tasks.put(task.getId(), task);
        prioritizedTasks.add(task);
        updateNewIdOnImport(task);
        onAddTask (task);
        return task;
    }

    // Создание (добавление) эпика
    @Override
    public Epic appendEpic(Epic epic) {
        if (epic == null) {
            return null;
        }
        if (epics.get(epic.getId()) != null) {
            //если пытаемся добавить объект идентификатор которого уже имеется в хранилище
            return null;
        }

        //общая обработка добавления
        epic.setID(generateID());
        if (!validateTask(epic)) {
            return null;
        }
        epic.clearSubtaskIds(); //очищаем, т.к. подзадачи должны добавляться после добавления эпика в менеджер
        epics.put(epic.getId(), epic);
        onAddTask (epic);

        return epic;
    }

    @Override
    public Task importEpic (Epic epic) {
        if (epic.getId() < 1) {
            return null;
        }
        epics.put(epic.getId(), epic);
        updateNewIdOnImport(epic);
        onAddTask (epic);
        return epic;
    }

    // Создание (добавление) подзадачи
    @Override
    public Subtask appendSubtask(Subtask subtask) {
        if (subtask == null) {
            return null;
        }
        if (subtasks.get(subtask.getId()) != null) {
            //если пытаемся добавить объект идентификатор которого уже имеется в хранилище
            return null;
        }

        //общая обработка добавления
        subtask.setID(generateID());
        if (!validateTask(subtask)) {
            return null;
        }
        subtasks.put(subtask.getId(), subtask);
        prioritizedTasks.add(subtask);
        updateEpicFromSubtasksInfo(getEpic(subtask.getEpicId())); //обновление эпика, связанное с добавлением подзадачи
        onAddTask (subtask);

        return subtask;
    }

    @Override
    public Task importSubtask (Subtask subtask) {
        if (subtask.getId() < 1) {
            return null;
        }

        subtasks.put(subtask.getId(), subtask);
        prioritizedTasks.add(subtask);
        updateNewIdOnImport(subtask);
        updateEpicFromSubtasksInfo(getEpic(subtask.getEpicId()));
        onAddTask (subtask);

        return subtask;
    }

    // Обновление записи задачи
    @Override
    public Task updateTask(Task task) {
        if (task == null) {
            return null;
        }
        if (tasks.get(task.getId()) == null) {
            //если пытаемся обновить объект, идентификатора которого нет в хранилище
            return null; // выходим
        }
        if (!validateTask(task)) {
            return null;
        }

        //замена таска в хранилище на обновленный
        if (tasks.containsKey(task.getId())) {
            prioritizedTasks.remove(getTask(task.getId()));
            tasks.replace(task.getId(), task);
            prioritizedTasks.add(task);
            onUpdateTask (task);
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
        if (epics.get(epic.getId()) == null) {
            //если пытаемся обновить объект, идентификатора которого нет в хранилище
            return null; // выходим
        }
        if (!validateTask(epic)) {
            return null;
        }

        //заменить эпик в наборе на обновленный вариант
        if (epics.containsKey(epic.getId())) {
            epics.replace(epic.getId(), epic);
            updateEpicFromSubtasksInfo(epic);
            onUpdateTask (epic);
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
        if (subtasks.get(subtask.getId()) == null) {
            //если пытаемся обновить объект, идентификатора которого нет в хранилище
            return null; // выходим
        }
        if (!validateTask(subtask)) {
            return null;
        }

        if (subtasks.containsKey(subtask.getId())) {
            prioritizedTasks.remove(getSubtask(subtask.getId()));
            subtasks.replace(subtask.getId(), subtask);
            prioritizedTasks.add(subtask);
            updateEpicFromSubtasksInfo(epics.get(subtask.getEpicId())); //обновление эпика, связанное с обновлением подзадачи
            onUpdateTask(subtask);
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
        Task task = tasks.get(id);
        if ((task == null) || (!tasks.containsKey(id))) {
            return false;
        }

        prioritizedTasks.remove(getTask(id));
        tasks.remove(id);
        historyManager.remove(id); //удаляем задачу из истории
        onRemoveTask(task);

        return true;
    }

    // Удаление подзадачи по идентификатору
    @Override
    public boolean deleteSubtask(Integer id) {
        //Проверяем, что идентификатор указывает на реальный объект
        Subtask subtask = subtasks.get(id);
        if ((subtask == null) || (!subtasks.containsKey(id))) {
            return false;
        }

        prioritizedTasks.remove(getSubtask(id));
        subtasks.remove(id);
        historyManager.remove(id); //удаляем подзадачу из истории
        onRemoveTask(subtask);

        //Удаляем идентификатор подзадачи из списка подзадач эпика
        updateEpicFromSubtasksInfo(getEpic(subtask.getEpicId()));

        return true;
    }

    // Удаление эпика по идентификатору
    @Override
    public boolean deleteEpic(Integer id) {
        //Проверяем, что идентификатор указывает на реальный объект
        Epic epic = epics.get(id);
        if ((epic == null) || (!epics.containsKey(id))) {
            return false;
        }

        epics.remove(id);

        //Удаляем подзадачи
        for (Integer subtaskId : epic.getSubtaskIds()) {
            deleteSubtask(subtaskId);
        }

        onRemoveTask(epic);

        historyManager.remove(id); //удаляем эпик из истории

        return true;
    }

    // Удаление всех задач, эпиков, подзадач
    @Override
    public void clearTasks() {
        onRemoveTasks(new ArrayList<>(tasks.values()));
        tasks.clear();
    }

    // Удаление всех эпиков
    @Override
    public void clearEpics() {
        clearSubtasks(); //удаляем подзадачи, т.к. они не могут существовать без эпиков
        onRemoveTasks(new ArrayList<>(epics.values()));
        epics.clear();

    }

    // Удаление всех подзадач
    @Override
    public void clearSubtasks() {
        onRemoveTasks(new ArrayList<>(subtasks.values()));
        subtasks.clear();
    }

    // Удаление всех задач, эпиков, подзадач
    @Override
    public void clearAll() {
        clearTasks();
        clearEpics();
        clearSubtasks();
    }

    // Получить задачу (Task) из хранилища по идентификатору
    @Override
    public Task getTask(Integer id) {
        Task result = tasks.get(id);
        historyManager.add(result);
        return result;
    }

    // Получить эпик (Epic) из хранилища по идентификатору
    @Override
    public Epic getEpic(Integer id) {
        Epic result = epics.get(id);
        historyManager.add(result);
        return result;
    }

    // Получить подзадачу (Subtask) из хранилища по идентификатору
    @Override
    public Subtask getSubtask(Integer id) {
        Subtask result = subtasks.get(id);
        historyManager.add(result);
        return result;
    }

    public Task getById (Integer id) {
        return (getTask(id)==null?(getEpic(id)==null?getSubtask(id):getEpic(id)):getTask(id));
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

    public HistoryManager getHistoryManager() {
        return historyManager;
    }

    // Служебный метод. Получить сабтаски по списку идентификаторов
    // Результаты вызова метода не заносятся в историю просмотров задач
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

    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    private boolean removeTaskByIdFromPrioritizedTasks (int taskId) {
        Task target = null;
        for (Task prioritizedTask : prioritizedTasks) {
            if (prioritizedTask.getId()==taskId) {
                target = prioritizedTask;
            }
        }
        if (target!=null) {
            prioritizedTasks.remove(target);
            return true;
        } else {
            return false;
        }
    }
    private void updateEpicTimes(Epic epic,Subtask subtask) {
        if (subtask.getStartTime()!=null) {
            if (epic.getStartTime()==null) {
                epic.setStartTime(subtask.getStartTime());
            } else {
                if (subtask.getStartTime().isBefore(epic.getStartTime())) {
                    epic.setStartTime(subtask.getStartTime());
                }
            }
        }
        if (subtask.getEndTime()!=null) {
            if (epic.getEndTime()==null) {
                epic.setEndTime(subtask.getEndTime());
            } else {
                if (subtask.getEndTime().isAfter(epic.getEndTime())) {
                    epic.setEndTime(subtask.getEndTime());
                }
            }
        }
        if (subtask.getDuration()!=null) {
            if (epic.getDuration()==null) {
                epic.setDuration(subtask.getDuration());
            } else {
                epic.setDuration(epic.getDuration().plus(subtask.getDuration()));
            }
        }

    }

    private void updateEpicStatus(Epic epic, Subtask subtask, boolean isFirstIteration) {
        if (isFirstIteration) {
            //Если это первая итерация, присваиваем и идем дальше
            epic.setStatus(subtask.getStatus());
        } else if ((epic.getStatus() != TaskStatus.IN_PROGRESS)&&
                (epic.getStatus() != subtask.getStatus())) {
            //Если статус при обходе меняется, значит подзадачи разнородные
            epic.setStatus(TaskStatus.IN_PROGRESS);
        }
    }

    // Служебный метод. Обновление эпика, перечня связанных подзадач, статуса
    private void updateEpicFromSubtasksInfo(Epic epic) {
        if (epic == null) {
            //работаем только с ненулевыми
            return;
        }

        //Обновляем статус
        epic.setStatus(TaskStatus.NEW); //По умолчанию статус NEW
        epic.setStartTime(null);
        epic.setEndTime(null);
        epic.setDuration(null);
        boolean isFirstIteration = true; //Флаг для определения первого шага

        //Пересоставляем перечень подзадач
        epic.clearSubtaskIds();
        ArrayList<Subtask> subtasks = getSubtasks();
        for (Subtask subtask : subtasks) {
            if (epic.getId().equals(subtask.getEpicId())) {
                updateEpicTimes(epic,subtask);
                updateEpicStatus(epic, subtask, isFirstIteration);
                isFirstIteration = false;
                epic.addSubtaskIds(subtask.getId());
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
        result += "prioritizedTasks: [" + "\n";
        for (Task prioritizedTask : this.prioritizedTasks) {
            result += prioritizedTask.toString() + "\n";
        }
        result += "]" + "\n";

        result += "-".repeat(20);
        return result;
    }
}

