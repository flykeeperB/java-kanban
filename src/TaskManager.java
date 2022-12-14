import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

public class TaskManager {
    //Хранение во вложенных хешмапах по ключу-классу задачи позволяет сократить объемы изменения кода
    //при добавлении новых классов задач и не перебирать все хранилище при необходимости выборки по классам задач
    private HashMap<Class, HashMap<Integer, Task>> tasks;
    private int newId = 0; //Очередной идентификатор задачи

    public TaskManager() {
        this.tasks = new HashMap<>();
    }

    //Создание (добавление) задачи/эпика/подзадачи
    public boolean append(Task task) {
        if (task == null) {
            return false;
        }
        if (this.get(task.getId()) != null) {
            //если пытаемся добавить объект идентификатор которого уже имеется в хранилище
            return false;
        }

        //общая обработка добавления
        task.setID(this.generateID());
        HashMap<Integer, Task> groupTasks = tasks.getOrDefault(task.getClass(), new HashMap<>());
        groupTasks.put(task.getId(), task);
        tasks.put(task.getClass(), groupTasks);

        this.checkSubtaskForUpdateEpic(task); //обновление эпика, связанное с добавлением подзадачи

        return true;
    }

    //Обновление задачи/эпика/подзадачи
    public boolean update(Task task) {
        if (task == null) {
            return false;
        }
        if (this.get(task.getId()) == null) {
            //если пытаемся обновить объект, идентификатора которого нет в хранилище
            return false; // выходим
        }

        //удалить сабтаск из старого эпика
        if (this.tasks.containsKey(task.getClass())) {
            this.tasks.get(task.getClass()).replace(task.getId(), task);
            this.checkSubtaskForUpdateEpic(task); //обновление эпика, связанное с обновлением подзадачи
            return true;
        }
        return false;
    }

    //Удаление задачи/эпика/подзадачи
    public boolean delete(Integer id) {
        //Проверяем, что идентификатор указывает на реальный объект
        Task task = this.get(id);
        if (task == null) {
            return false;
        }

        if (this.tasks.containsKey(task.getClass())) {
            //Удаляем подзадачи эпика
            if (task instanceof Epic) {
                this.deleteSubtasksFromEpic((Epic) task);
            }

            //Общий код для всех классов задач
            this.tasks.get(task.getClass()).remove(id);
            this.checkSubtaskForUpdateEpic(task); //обновление эпика, связанное с удалением подзадачи
            return true;
        }
        return false;
    }

    //Очистка всех задач, эпиков, подзадач
    public void clear() {
        this.tasks.clear();
    }

    //Получить задачу (Task) из хранилища по идентификатору
    public Task getTask(Integer id) {
        Task task = this.getOfClass(id, Task.class);
        return task;
    }

    //Получить эпик (Epic) из хранилища по идентификатору
    public Epic getEpic(Integer id) {
        Task task = this.getOfClass(id, Epic.class);
        return (Epic) task;
    }

    //Получить подзадачу (Subtask) из хранилища по идентификатору
    public Subtask getSubtask(Integer id) {
        Task task = this.getOfClass(id, Epic.class);
        return (Subtask) get(id);
    }

    //Получить все эпики/подзадачи/задачи из хранилища менеджера
    public ArrayList<Task> getAll() {
        ArrayList<Task> result = new ArrayList<>();
        for (HashMap<Integer, Task> taskGroup : this.tasks.values()) {
            result.addAll(taskGroup.values());
        }
        return result;
    }

    //Получение перечня задач (Task)
    public ArrayList<Task> getTasks() {
        return get(Task.class);
    }

    //Получение перечня эпиков (Epic)
    public ArrayList<Epic> getEpics() {
        ArrayList<Epic> result = new ArrayList<>();
        for (Task task : get(Epic.class)) {
            result.add((Epic) task);
        }
        return result;
    }

    //Получение перечня подзадач (Subtask)
    public ArrayList<Subtask> getSubtasks() {
        ArrayList<Subtask> result = new ArrayList<>();
        for (Task task : get(Subtask.class)) {
            result.add((Subtask) task);
        }
        return result;
    }

    //Служебный метод. Генерация идентификатора задачи и наследников
    private int generateID() {
        return newId++;
    }

    //Служебный метод. Проверка класса подзадачи для обновления связанного эпика
    private void checkSubtaskForUpdateEpic(Task task) {
        if (task instanceof Subtask) {
            this.updateEpic(this.getEpic(((Subtask) task).getEpicId()));
        }
    }

    //Служебный метод. Обновление эпика, перечня связанных подзадач, статуса
    private void updateEpic(Epic epic) {
        if (epic == null) {
            //работаем только с задачами класса Epic
            return;
        }

        //Пересоставляем перечень подзадач
        epic.getSubtaskIds().clear();
        ArrayList<Subtask> subtasks = this.getSubtasks();
        for (Subtask subtask : subtasks) {
            if (subtask.getEpicId() == epic.getId()) {
                epic.getSubtaskIds().add(subtask.getId());
            }
        }

        //Обновляем статус
        epic.setStatus(TaskStatus.NEW); //По умолчанию статус NEW

        boolean isFirstStep = true; //Флаг для определения первого шага

        //Обходим подзадачи
        for (Task subtask : this.get(epic.getSubtaskIds())) {
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

    //Служебный метод. Получение любого экземпляра задачи или наследников по идентификатору
    private Task get(Integer id) {
        for (HashMap<Integer, Task> taskGroup : this.tasks.values()) {
            if (taskGroup.containsKey(id)) {
                return taskGroup.get(id);
            }
        }
        return null;
    }

    //Служебный метод. Получение задачи или наследников по опредленному классу и идентификатору
    private Task getOfClass(Integer id, Class targetClass) {
        HashMap<Integer, Task> taskGroup = this.tasks.getOrDefault(targetClass, null);
        if (taskGroup != null) {
            if (taskGroup.containsKey(id)) {
                return taskGroup.get(id);
            }
        }
        return null;
    }

    //Служебный метод. Удаляем из хранилища менеджера подзадачи, связанные с удаляемым эпиком
    private boolean deleteSubtasksFromEpic(Epic epic) {
        ArrayList<Integer> subtaskIds = new ArrayList<>(epic.getSubtaskIds());
        for (Integer subtaskId : subtaskIds) {
            this.delete(subtaskId);
        }
        return true;
    }

    //Служебный метод. Получаем перечень задач по списку идентификаторов
    private ArrayList<Task> get(List<Integer> ids) {
        ArrayList<Task> result = new ArrayList<>();
        for (HashMap<Integer, Task> taskGroup : this.tasks.values()) {
            for (Task task : taskGroup.values()) {
                if (ids.contains(task.getId())) {
                    result.add(task);
                }
            }
        }
        return result;
    }

    //Служебный метод. Получаем перечень задач по классу
    private ArrayList<Task> get(Class target) {
        return new ArrayList<Task>(this.tasks.getOrDefault(target, new HashMap<Integer, Task>()).values());
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
