package testing;

import model.Task;
import service.HistoryManager;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HistoryManagerTest {

    InMemoryTaskManagerTest taskManagerTester = new InMemoryTaskManagerTest();
    HistoryManager historyManager;

    protected List<Integer> generateSomeTasksAndHistory(String testname) {
        return taskManagerTester.generateSomeTasksAndHistory(testname);
    }

    @BeforeEach
    void setUp() {
        taskManagerTester.setUp();
        historyManager = taskManagerTester.taskManager.getHistoryManager();
    }

    @Test
    void addOnEmptyHistory() {
        //Проверяем добавление задачи в пустую историю задач
        Task task = taskManagerTester.generateTasksForTest("addOnEmptyHistory", 1).get(0);
        taskManagerTester.taskManager.appendTask(task);
        taskManagerTester.taskManager.getTask(task.getId());
        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "Неверное количество элементов в истории");
    }

    @Test
    void addOnSomeHistory() {
        //Создаем наполнение истории
        List<Integer> ids = generateSomeTasksAndHistory("addOnSomeHistory");

        //Проверяем добавление задачи в непустую историю задач
        Task task = taskManagerTester.generateTasksForTest("add", 1).get(0);
        taskManagerTester.taskManager.appendTask(task);
        taskManagerTester.taskManager.getTask(task.getId());

        //Проверяем результат
        List<Task> history = historyManager.getHistory();
        assertEquals(ids.size() + 1, history.size(), "Неверное количество элементов в истории");
    }

    @Test
    void duplications() {
        //Проверка на наличие дублей в истории
        List<Integer> ids = generateSomeTasksAndHistory("addOnSomeHistory");
        //Обращаемся к задаче, представленной в истории
        Task foundTask = taskManagerTester.taskManager.getById(ids.get(1));
        List<Task> history = historyManager.getHistory();
        history.remove(history.indexOf(foundTask));
        //Проверяем наличие дублей
        assertFalse(history.contains(foundTask), "В истории имеются дубли задачи");
    }

    @Test
    void removeOnEmptyHistory() {
        //Генерируем задачу
        Task task = taskManagerTester.generateTasksForTest("add", 1).get(0);
        taskManagerTester.taskManager.appendTask(task);

        historyManager.remove(task.getId());

        List<Task> history = historyManager.getHistory();
        assertNotNull(history, "История не возвращается.");
        assertEquals(0, history.size(), "Неверное количество элементов в истории.");
    }

    @Test
    void remove() {
        //Генерируем историю, добавляем тестовую задачу
        generateSomeTasksAndHistory("remove");
        List<Task> history = historyManager.getHistory();

        Task firstTask = history.get(0);
        Task taskFromMiddle = history.get(history.size() / 2);
        Task lastTask = history.get(history.size() - 1);

        //Проверяем удаление задачи из начала истории
        historyManager.remove(firstTask.getId());

        history = historyManager.getHistory();
        assertNotNull(history, "История не возвращается.");
        assertEquals(-1, history.indexOf(firstTask), "Задача не удалилась из истории");

        //Проверяем удаление задачи из середины истории
        historyManager.remove(taskFromMiddle.getId());

        history = historyManager.getHistory();
        assertNotNull(history, "История не возвращается.");
        assertEquals(-1, history.indexOf(firstTask), "Задача не удалилась из истории");

        //Проверяем удаление задачи из конца истории
        historyManager.remove(lastTask.getId());

        history = historyManager.getHistory();
        assertNotNull(history, "История не возвращается.");
        assertEquals(-1, history.indexOf(firstTask), "Задача не удалилась из истории");
    }

    @Test
    void clear() {
        generateSomeTasksAndHistory("clear").size();
        historyManager.clear();
        List<Task> history = historyManager.getHistory();
        assertNotNull(history, "История не возвращается.");
        assertEquals(0, history.size(), "Неверное количество элементов в истории.");
    }

    @Test
    void getEmptyHistory() {
        //Проверяем получаение пустой истории
        List<Task> history = historyManager.getHistory();
        assertNotNull(history, "История не возвращается.");
        assertEquals(0, history.size(), "История не пуста.");
    }

    @Test
    void getHistory() {
        //Получаем историю
        int size = generateSomeTasksAndHistory("getHistory").size();
        List<Task> history = historyManager.getHistory();
        assertNotNull(history, "История не возвращается.");
        assertEquals(size, history.size(), "Неверное количество элементов в истории.");
    }
}