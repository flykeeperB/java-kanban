package service;

import model.*;

import java.io.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FileBackedTasksManager extends InMemoryTaskManager {
    static final String BOM_MARKER = "\ufeff"; //для корректности открытия файла в ином ПО (EXCEL..)
    static final String FILE_HEADER = BOM_MARKER + "id,type,name,status,description,epic,startTime,duration";
    static final String LINE_SEPARATOR = System.getProperty("line.separator");
    static final String CSV_DELIMITER = ",";
    final private File file;

    public static void main(String[] args) {
        File file = new File("kanban.csv");
        FileBackedTasksManager taskManager = new FileBackedTasksManager(file);

        //1. Заведите несколько разных задач, эпиков и подзадач
        Task task1 = new Task("Задача 1", "Описание задачи 1");
        task1.setStartTime(LocalDateTime.now().plus(Duration.ofHours(10)));
        task1.setDuration(Duration.ofHours(2));
        taskManager.appendTask(task1);
        Task task2 = new Task("Задача 2", "Описание задачи 2");
        taskManager.appendTask(task2);
        Epic epic1 = new Epic("Эпик 1", "Описание эпика 1");
        taskManager.appendEpic(epic1);
        Subtask subtask1 = new Subtask(epic1, "Подзадача 1", "Описание подзадачи 1 эпика 1 \"Зачётная\",\"Классная\"");
        subtask1.setStartTime(LocalDateTime.now().minus(Duration.ofHours(1)));
        subtask1.setDuration(Duration.ofMinutes(30));
        taskManager.appendSubtask(subtask1);
        Subtask subtask2 = new Subtask(epic1, "Подзадача 2", "Описание подзадачи 2 эпика 1");
        subtask2.setStartTime(LocalDateTime.now().minus(Duration.ofMinutes(10)));
        subtask2.setDuration(Duration.ofHours(1));
        taskManager.appendSubtask(subtask2);
        Epic epic2 = new Epic("Эпик 2", "Описание эпика 2");
        taskManager.appendEpic(epic2);

        //2. Запросите некоторые из них, чтобы заполнилась история просмотра.
        taskManager.getEpic(epic1.getId());
        taskManager.getTask(task1.getId());
        taskManager.getEpic(epic2.getId());
        taskManager.getSubtask(subtask2.getId());
        taskManager.getTask(task1.getId());
        taskManager.getTask(task2.getId());
        taskManager.getSubtask(subtask1.getId());
        taskManager.getSubtask(subtask2.getId());

        //Выводим состояние менеджера
        System.out.println(taskManager);
        System.out.println(taskManager.historyManager.toString());

        //3. Создайте новый FileBackedTasksManager менеджер из этого же файла.
        FileBackedTasksManager restoredTaskManager = FileBackedTasksManager.loadFromFile(file);

        //4. Проверьте, что история просмотра восстановилась верно и все задачи, эпики, подзадачи,
        //   которые были в старом, есть в новом менеджере.
        System.out.println(restoredTaskManager);
        System.out.println(restoredTaskManager.historyManager.toString());
    }

    public FileBackedTasksManager(File file) {
        super();
        this.file = file;
    }

    public static FileBackedTasksManager loadFromFile(File file) {
        FileBackedTasksManager fileBackedTasksManager = new FileBackedTasksManager(file);
        try (BufferedReader fileReader = new BufferedReader(new FileReader(file))) {
            while (fileReader.ready()) {
                String line = fileReader.readLine();
                if (line.equals(FILE_HEADER)) {
                    //Пропускаем заголовок данных в файле
                    continue;
                }
                if (line.isEmpty()) {
                    //Пустая строка указывает на начало записи истории
                    line = fileReader.readLine();
                    if (!line.isEmpty()) {
                        //Загрузка истории
                        fileBackedTasksManager.historyManager.clear();

                        List<Integer> ids = historyFromString(line);
                        for (Integer id : ids) {
                            fileBackedTasksManager.historyManager.add(fileBackedTasksManager.getById(id));
                        }
                    }
                    break;

                } else {
                    //Выполняем заполнение задачей из строки
                    fileBackedTasksManager.fromString(line);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return fileBackedTasksManager;
    }

    public void save() {
        StringBuilder fileContent = new StringBuilder(FILE_HEADER);

        //Собираем в общий список задачи/эпики/подзадачи
        List<Task> tasks = this.getTasks();
        tasks.addAll(this.getEpics());
        tasks.addAll(this.getSubtasks());

        //Подготавливаем содержимое файла
        for (Task task : tasks) {
            fileContent.append(LINE_SEPARATOR);
            fileContent.append(FileBackedTasksManager.toString(task));
        }

        //Подготавливаем историю к записи
        String stringHistory = FileBackedTasksManager.historyToString(this.historyManager);

        if (!stringHistory.isEmpty()) {
            fileContent.append(LINE_SEPARATOR);
            fileContent.append(LINE_SEPARATOR);
            fileContent.append(stringHistory);
        }

        //Пишем файл
        try (Writer fileWriter = new FileWriter(file)) {
            fileWriter.write(fileContent.toString());
        } catch (IOException e) {
            throw new ManagerSaveException(e.getMessage());
        }
    }

    static String historyToString(HistoryManager manager) {
        List<String> historyIds = new ArrayList<>();
        List<Task> history = manager.getHistory();
        for (Task task : history) {
            historyIds.add(task.getId().toString());
        }
        return String.join(CSV_DELIMITER, historyIds);
    }

    static List<Integer> historyFromString(String value) {
        if (value==null) {
            return new ArrayList<>();
        }
        String[] parts = value.split(CSV_DELIMITER);
        List<Integer> ids = new ArrayList<>();
        for (String part : parts) {
            ids.add(Integer.parseInt(part));
        }
        return ids;
    }

    // Добавляем пары двойных кавычек ("->"") для хранения в CSV файле
    private static String twinQuotes(String input) {
        return input.replaceAll("\"", "\"\"");
    }

    // Убираем пары двойных кавычек (""->"), добавленных для хранения в CSV файле
    private static String removeTwinQuotes(String input) {
        return input.replaceAll("\"\"", "\"");
    }

    static String toString(Task task) {
        List<String> taskFields = new ArrayList<>();

        taskFields.add(task.getId().toString());
        taskFields.add(task.getClass().getSimpleName().toUpperCase());
        taskFields.add(twinQuotes(task.getName()));
        taskFields.add(task.getStatus().toString());
        taskFields.add(twinQuotes(task.getDiscription()));
        if (task.getClass() == Subtask.class) {
            taskFields.add(((Subtask) task).getEpicId().toString());
        } else {
            taskFields.add("");
        }
        if (task.getClass() != Epic.class) {
            taskFields.add((task.getStartTime()!=null?task.getStartTime().toString():""));
            taskFields.add((task.getDuration()!=null?task.getDuration().toString():""));
        } else {
            taskFields.add("");
            taskFields.add("");
        }
        return '"' + String.join("\"" + CSV_DELIMITER + "\"", taskFields) + '"';
    }

    //Определяем количество повторов символа с конца строки
    static int tailQuotesReps(String value) {
        int i = value.length();
        if (i == 0) {
            return 0;
        }
        while (value.charAt(i - 1) == '"') {
            i--;
        }
        return value.length() - i;
    }

    private List<String> valuesFromCSVString(String CSVLine) {
        //Разбиваем строку по запятой
        String[] parts = CSVLine.split(CSV_DELIMITER);
        //Склеиваем обратно строки, которые были разделены на предыдущем этапе, но
        //такое разделение было выполнено по разделителям "внутри" значения поля.
        //Признаком будет нечетное количество кавычек в конце части строки (остается после "лишнего" разделения).
        //Пример строки: "Описание подзадачи 1 эпика 1 ""Зачётная"",""Классная"""
        //Решение через регулярные выражения было слишком громоздким и трудночитаемым
        List<String> values = new ArrayList<>();
        StringBuilder fieldValue = new StringBuilder();

        for (String part : parts) {
            part = part.substring(1, part.length() - 1);
            if (tailQuotesReps(part) % 2 == 0) {
                //Четное количество кавычек или их отсутствие - признак правильного разделения
                fieldValue.append(part);
                values.add(removeTwinQuotes(fieldValue.toString()));
                fieldValue.setLength(0);
            } else {
                //Нечетное количество кавычек. Присоединяем этот кусок строки, через
                //удаленный на предыдщуем этапе разделитель

                fieldValue.append(part);
                fieldValue.append("\"");
                fieldValue.append(CSV_DELIMITER);
                fieldValue.append("\"");
            }
        }
        values.add(fieldValue.toString());
        return values;
    }

    private Task fromString(String value) {

        //Нормализуем строку, превращаем в набор значений для полей
        List<String> values = valuesFromCSVString(value);

        int id = Integer.parseInt(values.get(0));
        TaskTypes type = TaskTypes.valueOf(values.get(1));
        String name = values.get(2);
        TaskStatus status = TaskStatus.valueOf(values.get(3));
        String description = values.get(4);
        LocalDateTime startTime = null;
        if (!values.get(6).isEmpty()) {
            startTime = LocalDateTime.parse(values.get(6));
        }
        Duration duration = null;
        if (!values.get(7).isEmpty()) {
            duration = Duration.parse(values.get(7));
        }

        switch (type) {
            case TASK:
                Task task = new Task(name, description, status, id);
                task.setStartTime(startTime);
                task.setDuration(duration);
                super.importTask(task);
                return task;
            case EPIC:
                Epic epic = new Epic(name, description, status, id);
                super.importEpic(epic);
                return epic;
            case SUBTASK:
                Epic parentEpic = super.getEpicAnonimusly(Integer.parseInt(values.get(5)));
                Subtask subtask = new Subtask(parentEpic, name, description, status, id);
                subtask.setStartTime(startTime);
                subtask.setDuration(duration);
                super.importSubtask(subtask);
                return subtask;
        }

        return null;
    }

    @Override
    public Task appendTask(Task task) {
        if (super.appendTask(task) != null) {
            save();
        }
        return task;
    }

    @Override
    public Epic appendEpic(Epic epic) {
        if (super.appendEpic(epic) != null) {
            save();
        }
        return epic;
    }

    @Override
    public Subtask appendSubtask(Subtask subtask) {
        if (super.appendSubtask(subtask) != null) {
            save();
        }
        return subtask;
    }

    @Override
    public Task updateTask(Task task) {
        if (super.updateTask(task) != null) {
            save();
        }
        return task;
    }

    @Override
    public Task updateEpic(Epic epic) {
        if (super.updateEpic(epic) != null) {
            save();
        }
        return epic;
    }

    @Override
    public Task updateSubtask(Subtask subtask) {
        if (super.updateSubtask(subtask) != null) {
            save();
        }
        return subtask;
    }

    @Override
    public boolean delete(Integer id) {
        if (super.deleteTask(id) || super.deleteSubtask(id) || super.deleteEpic(id)) {
            save();
            return true;
        }
        return false;
    }

    @Override
    public boolean deleteTask(Integer id) {
        if (super.deleteTask(id)) {
            save();
            return true;
        }
        return false;
    }

    @Override
    public boolean deleteSubtask(Integer id) {
        if (super.deleteSubtask(id)) {
            save();
            return true;
        }
        return false;
    }

    @Override
    public boolean deleteEpic(Integer id) {
        if (super.deleteEpic(id)) {
            save();
            return true;
        }
        return false;
    }

    @Override
    public void clearTasks() {
        super.clearTasks();
        save();
    }

    @Override
    public void clearEpics() {
        super.clearEpics();
        save();
    }

    @Override
    public void clearSubtasks() {
        super.clearSubtasks();
        save();
    }

    @Override
    public void clearAll() {
        super.clearAll();
        save();
    }

    @Override
    public Task getTask(Integer id) {
        Task result = super.getTask(id);
        if (result != null) {
            save();
        }
        return result;
    }

    @Override
    public Epic getEpic(Integer id) {
        Epic result = super.getEpic(id);
        if (result != null) {
            save();
        }
        return result;
    }

    @Override
    public Subtask getSubtask(Integer id) {
        Subtask result = super.getSubtask(id);
        if (result != null) {
            save();
        }
        return result;
    }


}


