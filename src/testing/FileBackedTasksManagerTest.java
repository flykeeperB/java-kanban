package testing;

import model.Epic;
import model.Subtask;
import model.Task;

import service.FileBackedTasksManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static java.nio.file.Files.copy;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTasksManagerTest extends TaskManagerTest<FileBackedTasksManager> {

    @BeforeEach
    void setUp() {
        File file = new File("testfiles/kanban_test.csv");
        taskManager = new FileBackedTasksManager(file);
    }

    @Test
    void loadFromFullEmptyFile() {
        File file = new File("testfiles/full_empty_file_load_test.csv");

        //Загружаем пустой файл
        taskManager = FileBackedTasksManager.loadFromFile(file);

        //Проверяем результат
        List<Task> tasks = taskManager.getTasks();
        assertNotNull(tasks, "Задачи на возвращаются.");

        tasks.addAll(taskManager.getEpics());
        tasks.addAll(taskManager.getSubtasks());

        assertEquals(0, tasks.size(), "Неверное количество задач/эпиков/подзадач.");
    }

    @Test
    void loadFromEmptyFile() {
        File file = new File("testfiles/valid_empty_tasks_file.csv");

        //Загружаем файл, в котором отсутствуют данные
        taskManager = FileBackedTasksManager.loadFromFile(file);

        //Проверяем результат
        List<Task> tasks = taskManager.getTasks();
        assertNotNull(tasks, "Задачи на возвращаются.");

        tasks.addAll(taskManager.getEpics());
        tasks.addAll(taskManager.getSubtasks());

        assertEquals(0, tasks.size(), "Неверное количество задач/эпиков/подзадач.");
    }

    @Test
    void loadFromFileEmptyHistory() {
        //Создаем отдельный файл из заведомо верного экземпляра (чтобы не исполнить файл-источник)
        String validSourceFilename = "testfiles/valid_empty_history_file.csv";
        File file = new File("testfiles/test_empty_history_file.csv");

        try {
            copy(Path.of(validSourceFilename), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        taskManager = FileBackedTasksManager.loadFromFile(file);

        List<Task> history = taskManager.getHistory();

        assertNotNull(history, "История на возвращается.");
        assertEquals(0, history.size(), "История не пуста.");
    }

    @Test
    void loadFromFileEpicNoSubtasks() {
        //Создаем отдельный файл из заведомо верного экземпляра (чтобы не исполнить файл-источник)
        String validSourceFilename = "testfiles/valid_epic_file_save_test.csv";
        File file = new File("testfiles/test_epic_file_save_test.csv");

        try {
            copy(Path.of(validSourceFilename), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        taskManager = FileBackedTasksManager.loadFromFile(file);

        List<Epic> epics = taskManager.getEpics();

        assertNotNull(epics, "Список эпиков на возвращается.");
        assertEquals(1, epics.size(), "Неверное количество эпиков.");

        List<Subtask> subtasks = taskManager.getSubtasks();

        assertNotNull(subtasks, "Список сабтасков на возвращается.");
        assertEquals(0, subtasks.size(), "Неверное количество сабтасков.");

        List<Task> tasks = taskManager.getTasks();

        assertNotNull(tasks, "Список задач на возвращается.");
        assertEquals(0, tasks.size(), "Неверное количество задач.");
    }

    @Test
    void saveEmptyTasks() {
        final String filename = "testfiles/empty_tasks_file_save_test.csv";
        final String validFilename = "testfiles/valid_empty_tasks_file.csv";
        File file = new File(filename);
        taskManager = new FileBackedTasksManager(file);

        //Вызываем метод сохранения на пустом наборе задач
        taskManager.save();

        //Проверяем результат
        assertTrue(file.exists(), "Файл не был создан.");
        assertFalse(file.isDirectory(), "Найдена директория вместо файла.");

        try {
            String testFile = Files.readString(Path.of(filename));
            String validFile = Files.readString(Path.of(validFilename));
            assertEquals(validFile, testFile, "Содержимое файлов отличается.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void saveTasks() {
        final String filename = "testfiles/tasks_file_save_test.csv";
        final String validFilename = "testfiles/valid_tasks_file.csv";

        File file = new File(filename);
        taskManager = new FileBackedTasksManager(file);

        //Создаем набор задач и историю
        generateSomeTasksAndHistory("saveTasks");

        //Проверяем результат
        assertTrue(file.exists(), "Файл не был создан.");
        assertFalse(file.isDirectory(), "Найдена директория вместо файла.");

        try {
            String testFile = Files.readString(Path.of(filename));
            String validFile = Files.readString(Path.of(validFilename));
            assertEquals(validFile, testFile, "Содержимое файлов отличается.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void saveTasksWithEmptyHistory() {
        final String filename = "testfiles/empty_history_file_save_test.csv";
        final String validFilename = "testfiles/valid_empty_history_file.csv";

        File file = new File(filename);
        taskManager = new FileBackedTasksManager(file);

        //Создаем задачи, удаляем историю
        generateSomeTasksAndHistory("saveTasksWithEmptyHistory");
        taskManager.getHistoryManager().clear();
        taskManager.save(); //Вызываем сохранение, поскольку при очистке истории автоматически не сохраняется

        //Проверяем результат
        assertTrue(file.exists(), "Файл не был создан.");
        assertFalse(file.isDirectory(), "Найдена директория вместо файла.");

        try {
            String testFile = Files.readString(Path.of(filename));
            String validFile = Files.readString(Path.of(validFilename));
            assertEquals(validFile, testFile, "Содержимое файлов отличается.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void saveEpicNoSubtasks() {
        final String filename = "testfiles/epic_file_save_test.csv";
        final String validFilename = "testfiles/valid_epic_file_save_test.csv";

        File file = new File(filename);
        taskManager = new FileBackedTasksManager(file);

        //Добавляем эпик без подзадач
        Epic epic = generateEpicsForTest("saveEpicNoSubtasks", 1).get(0);
        taskManager.appendEpic(epic);

        //Проверяем результат
        assertTrue(file.exists(), "Файл не был создан.");
        assertFalse(file.isDirectory(), "Найдена директория вместо файла.");

        try {
            String testFile = Files.readString(Path.of(filename));
            String validFile = Files.readString(Path.of(validFilename));
            assertEquals(validFile, testFile, "Содержимое файлов отличается.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}