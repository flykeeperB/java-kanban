package testing;

import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;
import service.TaskManager;
import service.TaskValidatorException;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

abstract class TaskManagerTest<T extends TaskManager> {

    protected T taskManager;

    protected List<Task> generateTasksForTest(String testName, int count) {
        List<Task> result = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            Task task = new Task(i + ". Task. Test " + testName, i + ". Task. Test " + testName + " description");
            result.add(task);
        }
        return result;
    }

    protected List<Epic> generateEpicsForTest(String testName, int count) {
        List<Epic> result = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            Epic epic = new Epic(i + ". Epic. Test " + testName, i + ". Epic. Test " + testName + " description");
            result.add(epic);
        }
        return result;
    }

    protected List<Subtask> generateSubtasksForTest(String testName, Epic epic, int count) {
        List<Subtask> result = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            Subtask subtask = new Subtask(epic, i + ". Subtask of epic id = " + epic.getId() + ". Test " + testName, i + ". Subtask of epic id = " + epic.getId() + ". Test " + testName + " description");
            result.add(subtask);
        }
        return result;
    }

    protected List<Integer> generateSomeTasksAndHistory(String testname) {
        //Генерируем тестовый набор задач/эпиков/подзадач
        List<Task> tasks;
        List<Epic> epics;
        List<Subtask> subtasks;

        tasks = generateTasksForTest(testname, 8);
        for (Task task : tasks) {
            taskManager.appendTask(task);
        }

        epics = generateEpicsForTest(testname, 2);
        for (Epic epic : epics) {
            taskManager.appendEpic(epic);
        }

        subtasks = generateSubtasksForTest(testname, epics.get(0), 6);
        for (Subtask subtask : subtasks) {
            taskManager.appendSubtask(subtask);
        }

        //Создаем список идентификаторов для обращения к истории
        List<Integer> ids = new ArrayList<>();

        ids.add(tasks.get(4).getId());
        ids.add(tasks.get(2).getId());
        ids.add(tasks.get(5).getId());
        ids.add(tasks.get(0).getId());
        ids.add(tasks.get(3).getId());
        ids.add(tasks.get(7).getId());

        ids.add(epics.get(0).getId());

        ids.add(subtasks.get(0).getId());
        ids.add(subtasks.get(2).getId());
        ids.add(subtasks.get(3).getId());
        ids.add(subtasks.get(4).getId());
        ids.add(subtasks.get(5).getId());

        //Обращаемся к таскам/подзадачам/эпикам для формирования истории
        for (Integer id : ids) {
            if (taskManager.getTask(id) == null) {
                if (taskManager.getEpic(id) == null) {
                    taskManager.getSubtask(id);
                }
            }
        }

        return ids;
    }

    @Test
    void appendNewTask() {
        Task task = new Task("Test appendNewTask", "Test appendNewEpic description");

        taskManager.appendTask(task);
        final Task savedTask = taskManager.getTask(task.getId());

        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(task, savedTask, "Задачи не совпадают.");

        final List<Task> tasks = taskManager.getTasks();

        assertNotNull(tasks, "Задачи на возвращаются.");
        assertEquals(1, tasks.size(), "Неверное количество задач.");
        assertEquals(task, tasks.get(0), "Задачи не совпадают.");
    }

    @Test
    void appendNewTaskWithNull() {

        final Task appendedTask = taskManager.appendTask(null);
        assertNull(appendedTask, "Задача найдена.");

        final List<Task> tasks = taskManager.getTasks();
        assertEquals(0, tasks.size(), "Неверное количество задач.");
    }

    @Test
    void appendNewEpic() {
        Epic epic = new Epic("Test appendNewEpic", "Test appendNewEpic description");
        taskManager.appendEpic(epic);

        final Epic savedEpic = taskManager.getEpic(epic.getId());

        assertNotNull(savedEpic, "Эпик не найден.");
        assertEquals(epic, savedEpic, "Эпики не совпадают.");

        final List<Epic> epics = taskManager.getEpics();

        assertNotNull(epics, "Эпики на возвращается.");
        assertEquals(1, epics.size(), "Неверное количество эпиков.");
        assertEquals(epic, epics.get(0), "Эпики не совпадают.");
    }

    @Test
    void appendNewEpicWithNull() {

        final Epic appendedEpic = taskManager.appendEpic(null);
        assertNull(appendedEpic, "Эпик найден.");

        final List<Epic> epics = taskManager.getEpics();
        assertEquals(0, epics.size(), "Неверное количество эпиков. ");
    }

    @Test
    void appendNewSubtask() {
        Epic epic = new Epic("Epic for testing appendNewSubtask", "Epic for testing appendNewSubtask description");
        taskManager.appendEpic(epic);

        Subtask subtask = new Subtask(epic, "Test appendNewSubtask", "Test appendNewSubtask description");
        taskManager.appendSubtask(subtask);

        final Subtask savedSubtask = taskManager.getSubtask(subtask.getId());

        assertNotNull(savedSubtask, "Сабтаск не найден.");
        assertEquals(subtask, savedSubtask, "Сабтаски не совпадают.");

        final List<Subtask> subtasks = taskManager.getSubtasks();

        assertNotNull(subtasks, "Сабтаски на возвращается.");
        assertEquals(1, subtasks.size(), "Неверное количество сабтасков.");
        assertEquals(subtask, subtasks.get(0), "Сабтаски не совпадают.");
    }

    @Test
    void appendNewSubtaskWithNull() {

        final Subtask appendedSubtask = taskManager.appendSubtask(null);
        assertNull(appendedSubtask, "Сабтаск найден.");

        final List<Subtask> subtasks = taskManager.getSubtasks();
        assertEquals(0, subtasks.size(), "Неверное количество сабтаской.");
    }

    @Test
    void updateTask() {
        Task task = new Task("Test updateTask", "Test updateTask description");
        taskManager.appendTask(task);

        Task updatedTask = new Task("Updated task", "Test updateTask description after update");
        updatedTask.setID(task.getId());
        taskManager.updateTask(updatedTask);

        final Task savedTask = taskManager.getTask(updatedTask.getId());
        assertNotNull(savedTask, "Таск не найден.");
        assertEquals(updatedTask, savedTask, "Обновленный таск и таск, сохраненный в менеджере, не совпадают.");
    }

    @Test
    void updateTaskOnEmptyTasks() {
        Task task = new Task("Test updateTask", "Test updateTask description");
        taskManager.appendTask(task);
        taskManager.clearTasks();

        Task updatedTask = new Task("Updated task", "Test updateTask description after update");
        updatedTask.setID(task.getId());
        taskManager.updateTask(updatedTask);

        final Task savedTask = taskManager.getTask(updatedTask.getId());
        assertNull(savedTask, "Таск найден.");
        assertNotEquals(updatedTask, savedTask, "Обновленный таск и таск, сохраненный в менеджере, совпадают.");
    }

    @Test
    void updateTaskWithoutId() {
        Task task = new Task("Test updateTask", "Test updateTask description");
        taskManager.appendTask(task);

        Task updatedTask = new Task("Updated task", "Test updateTask description after update");
        taskManager.updateTask(updatedTask);

        final Task savedTask = taskManager.getTask(updatedTask.getId());
        assertNull(savedTask, "Таск найден.");
        assertNotEquals(updatedTask, savedTask, "Обновленный таск и таск, сохраненный в менеджере, совпадают.");
    }

    @Test
    void updateEpic() {
        Epic epic = new Epic("Test updateEpic", "Test updateEpic description");
        taskManager.appendEpic(epic);

        Epic updatedEpic = new Epic("Updated epic", "Test updateEpic description after update");
        updatedEpic.setID(epic.getId());
        taskManager.updateEpic(updatedEpic);

        final Epic savedEpic = taskManager.getEpic(updatedEpic.getId());
        assertNotNull(savedEpic, "Эпик не найден.");
        assertEquals(updatedEpic, savedEpic, "Обновленный эпик и эпик, сохраненный в менеджере, не совпадают.");
    }

    @Test
    void updateEpicOnEmptyEpic() {
        Epic epic = new Epic("Test updateEpic", "Test updateEpic description");
        taskManager.appendEpic(epic);
        taskManager.clearEpics();

        Epic updatedEpic = new Epic("Updated epic", "Test updateEpic description after update");
        updatedEpic.setID(epic.getId());
        taskManager.updateEpic(updatedEpic);

        final Epic savedEpic = taskManager.getEpic(updatedEpic.getId());
        assertNull(savedEpic, "Эпик найден.");
        assertNotEquals(updatedEpic, savedEpic, "Обновленный эпик и эпик, сохраненный в менеджере, совпадают.");
    }

    @Test
    void updateEpicWithoutId() {
        Epic epic = new Epic("Test updateEpic", "Test updateEpic description");
        taskManager.appendEpic(epic);

        Epic updatedEpic = new Epic("Updated epic", "Test updateEpic description after update");
        taskManager.updateEpic(updatedEpic);

        final Epic savedEpic = taskManager.getEpic(updatedEpic.getId());
        assertNull(savedEpic, "Эпик найден.");
        assertNotEquals(updatedEpic, savedEpic, "Обновленный эпик и эпик, сохраненный в менеджере, совпадают.");
    }

    @Test
    void updateSubtask() {
        Epic epic = new Epic("Epic for test updateSubtask", "Epic for test updateSubtask description");
        taskManager.appendEpic(epic);

        Subtask subtask = new Subtask(epic, "Test updateSubtask", "Test updateSubtask description");
        taskManager.appendSubtask(subtask);

        Subtask updatedSubtask = new Subtask(epic, "Updated updateSubtask", "Test updateSubtask description after update");
        updatedSubtask.setID(subtask.getId());
        taskManager.updateSubtask(updatedSubtask);

        final Subtask savedSubtask = taskManager.getSubtask(updatedSubtask.getId());
        assertNotNull(savedSubtask, "Сабтаск не найден.");
        assertEquals(updatedSubtask, savedSubtask, "Обновленный сабтаск и сабтаск, сохраненный в менеджере, не совпадают.");
    }

    @Test
    void updateSubtaskOnEmptySubtasks() {
        Epic epic = new Epic("Epic for test updateSubtask", "Epic for test updateSubtask description");
        taskManager.appendEpic(epic);

        Subtask subtask = new Subtask(epic, "Test updateSubtask", "Test updateSubtask description");
        taskManager.appendSubtask(subtask);
        taskManager.clearAll();

        Subtask updatedSubtask = new Subtask(epic, "Updated updateSubtask", "Test updateSubtask description after update");
        updatedSubtask.setID(subtask.getId());
        taskManager.updateSubtask(updatedSubtask);

        final Subtask savedSubtask = taskManager.getSubtask(updatedSubtask.getId());
        assertNull(savedSubtask, "Сабтаск найден.");
        assertNotEquals(updatedSubtask, savedSubtask, "Обновленный сабтаск и сабтаск, сохраненный в менеджере, совпадают.");
    }

    @Test
    void updateSubtaskWithoutId() {
        Epic epic = new Epic("Epic for test updateSubtask", "Epic for test updateSubtask description");
        taskManager.appendEpic(epic);

        Subtask subtask = new Subtask(epic, "Test updateSubtask", "Test updateSubtask description");
        taskManager.appendSubtask(subtask);

        Subtask updatedSubtask = new Subtask(epic, "Updated updateSubtask", "Test updateSubtask description after update");
        taskManager.updateSubtask(updatedSubtask);

        final Subtask savedSubtask = taskManager.getSubtask(updatedSubtask.getId());
        assertNull(savedSubtask, "Сабтаск найден.");
        assertNotEquals(updatedSubtask, savedSubtask, "Обновленный сабтаск и сабтаск, сохраненный в менеджере, совпадают.");
    }

    @Test
    void deleteTask() {
        Task task = new Task("Test deleteTask", "Test deleteTask description");

        taskManager.appendTask(task);

        int targetId = task.getId();

        taskManager.deleteTask(targetId);

        final Task deletedTask = taskManager.getTask(targetId);

        assertNull(deletedTask, "Задача найдена.");

        final List<Task> tasks = taskManager.getTasks();

        assertNotNull(tasks, "Задачи на возвращаются.");
        assertEquals(0, tasks.size(), "Неверное количество задач.");
    }

    @Test
    void deleteTaskWithWrongId() {
        Task task = new Task("Test deleteTaskWithWrongId", "Test deleteTaskWithWrongId description");

        taskManager.appendTask(task);

        int targetId = 100;

        taskManager.deleteTask(targetId);

        final Task finded = taskManager.getTask(task.getId());

        assertNotNull(finded, "Задача не найдена.");

        final List<Task> tasks = taskManager.getTasks();

        assertNotNull(tasks, "Задачи на возвращаются.");
        assertEquals(1, tasks.size(), "Неверное количество задач.");
    }

    @Test
    void deleteTaskWithNullId() {
        Task task = new Task("Test deleteTaskWithNullId", "Test deleteTaskWithNullId description");

        taskManager.appendTask(task);

        taskManager.deleteTask(null);

        final Task found = taskManager.getTask(task.getId());

        assertNotNull(found, "Задача не найдена.");

        final List<Task> tasks = taskManager.getTasks();

        assertNotNull(tasks, "Задачи на возвращаются.");
        assertEquals(1, tasks.size(), "Неверное количество задач.");
    }

    @Test
    void deleteSubtask() {
        Epic epic = new Epic("Epic for testing deleteSubtask", "Epic for testing deleteSubtask description");
        taskManager.appendEpic(epic);

        Subtask subtask = new Subtask(epic, "Test deleteSubtask", "Test deleteSubtask description");

        taskManager.appendSubtask(subtask);

        int targetId = subtask.getId();

        taskManager.deleteSubtask(targetId);

        final Subtask deletedSubtask = taskManager.getSubtask(targetId);

        assertNull(deletedSubtask, "Сабтаск найдена.");

        final List<Subtask> subtasks = taskManager.getSubtasks();

        assertNotNull(subtasks, "Подзадачи на возвращаются.");
        assertEquals(0, subtasks.size(), "Неверное количество подзадач.");

        List<Integer> subtaskIdsOfEpic = epic.getSubtaskIds();
        assertFalse(subtaskIdsOfEpic.contains(targetId), "Идентификатор подзадачи найден в списке подзадач эпика");
    }

    @Test
    void deleteSubtaskWithWrongId() {
        Epic epic = new Epic("Epic for testing deleteSubtaskWithWrongId",
                "Epic for testing deleteSubtaskWithWrongId description");
        taskManager.appendEpic(epic);

        Subtask subtask = new Subtask(epic, "Test deleteSubtaskWithWrongId",
                "Test deleteSubtaskWithWrongId description");

        taskManager.appendSubtask(subtask);

        int targetId = 100;

        taskManager.deleteSubtask(targetId);

        final Subtask deletedSubtask = taskManager.getSubtask(subtask.getId());

        assertNotNull(deletedSubtask, "Сабтаск не найдена.");

        final List<Subtask> subtasks = taskManager.getSubtasks();

        assertNotNull(subtasks, "Подзадачи на возвращаются.");
        assertEquals(1, subtasks.size(), "Неверное количество подзадач.");

        List<Integer> subtaskIdsOfEpic = epic.getSubtaskIds();

        assertTrue(subtaskIdsOfEpic.contains(subtask.getId()), "Идентификатор подзадачи не найден в списке подзадач эпика");
    }

    @Test
    void deleteSubtaskWithNullId() {
        Epic epic = new Epic("Epic for testing deleteSubtaskWithWrongId",
                "Epic for testing deleteSubtaskWithWrongId description");
        taskManager.appendEpic(epic);

        Subtask subtask = new Subtask(epic, "Test deleteSubtaskWithWrongId",
                "Test deleteSubtaskWithWrongId description");

        taskManager.appendSubtask(subtask);

        taskManager.deleteSubtask(null);

        final Subtask deletedSubtask = taskManager.getSubtask(subtask.getId());

        assertNotNull(deletedSubtask, "Сабтаск не найдена.");

        final List<Subtask> subtasks = taskManager.getSubtasks();

        assertNotNull(subtasks, "Подзадачи на возвращаются.");
        assertEquals(1, subtasks.size(), "Неверное количество подзадач.");

        List<Integer> subtaskIdsOfEpic = epic.getSubtaskIds();
        assertTrue(subtaskIdsOfEpic.contains(subtask.getId()), "Идентификатор подзадачи не найден в списке подзадач эпика");
    }

    @Test
    void deleteEpic() {
        Epic epic = new Epic("Testing deleteEpic", "Testing deleteEpic description");
        taskManager.appendEpic(epic);

        Subtask subtask = new Subtask(epic, "Test deleteEpic",
                "Test deleteEpic description");

        taskManager.appendSubtask(subtask);

        int targetId = epic.getId();

        taskManager.deleteEpic(targetId);

        final Epic deletedEpic = taskManager.getEpic(targetId);

        assertNull(deletedEpic, "Эпик найден.");

        final List<Epic> epics = taskManager.getEpics();

        assertNotNull(epics, "Эпики на возвращаются.");
        assertEquals(0, epics.size(), "Неверное количество эпиков.");

        final Subtask deletedSubtask = taskManager.getSubtask(subtask.getId());
        assertNull(deletedSubtask, "Сабтаск удаленного эпика найден.");
    }

    @Test
    void deleteEpicWithWrongId() {
        Epic epic = new Epic("Testing deleteEpic", "Testing deleteEpic description");
        taskManager.appendEpic(epic);

        int targetId = 100;

        taskManager.deleteEpic(targetId);

        final Epic deletedEpic = taskManager.getEpic(epic.getId());

        assertNotNull(deletedEpic, "Эпик не найден.");

        final List<Epic> epics = taskManager.getEpics();

        assertNotNull(epics, "Эпики на возвращаются.");
        assertEquals(1, epics.size(), "Неверное количество эпиков.");
    }

    @Test
    void deleteEpicWithNullId() {
        Epic epic = new Epic("Testing deleteEpicWithNullId", "Testing deleteEpicWithNullId description");
        taskManager.appendEpic(epic);

        int targetId = epic.getId();

        taskManager.deleteEpic(null);

        final Epic deletedEpic = taskManager.getEpic(targetId);

        assertNotNull(deletedEpic, "Эпик не найден.");

        final List<Epic> epics = taskManager.getEpics();

        assertNotNull(epics, "Эпики на возвращаются.");
        assertEquals(1, epics.size(), "Неверное количество эпиков.");
    }

    @Test
    void importTask() {
        int id = 1;

        //Генерируем и импортируем задачи для теста
        List<Task> tasks = generateTasksForTest("importTask", 3);

        for (Task task : tasks) {
            task.setID(id);
            id++;
            taskManager.importTask(task);
        }

        //Проверяем успешность импорта задач
        for (Task task : tasks) {
            Task importedTask = taskManager.getTask(task.getId());
            assertNotNull(importedTask, "Задача " + task.getId() + " не найдена.");
            assertEquals(task, importedTask, "Задачи не совпадают.");
        }

        final List<Task> savedTasks = taskManager.getTasks();
        assertNotNull(savedTasks, "Задачи на возвращаются.");
        assertEquals(3, savedTasks.size(), "Неверное количество задач.");
    }

    @Test
    void importEpic() {
        int id = 1;

        //Генерируем и импортируем эпики для теста
        List<Epic> epics = generateEpicsForTest("importEpic", 3);

        for (Epic epic : epics) {
            epic.setID(id);
            id++;
            taskManager.importEpic(epic);
        }

        //Проверяем результат импорта эпиков
        for (Epic epic : epics) {
            Epic importedEpic = taskManager.getEpic(epic.getId());
            assertNotNull(importedEpic, "Эпик не найден.");
            assertEquals(epic, importedEpic, "Эпики не совпадают.");
        }
    }

    @Test
    void importSubtask() {
        int id = 1;

        //Генерируем и импортируем эпики для теста
        List<Epic> epics = generateEpicsForTest("importSubtask", 3);

        for (Epic epic : epics) {
            epic.setID(id);
            id++;
            taskManager.importEpic(epic);
        }

        //Генерируем и импортируем сабтаски для теста
        List<Subtask> subtasks = generateSubtasksForTest("importSubtask", epics.get(1), 3);

        for (Subtask subtask : subtasks) {
            subtask.setID(id);
            id++;
            taskManager.importSubtask(subtask);
        }

        //Проверяем результат импорта сабтасков
        for (Subtask subtask : subtasks) {
            Subtask importedSubtask = taskManager.getSubtask(subtask.getId());
            assertNotNull(importedSubtask, "Сабтаск не найден.");
            assertEquals(subtask, importedSubtask, "Сабтаски не совпадают.");
        }
    }

    @Test
    void clearTasks() {
        //Генерируем задачи для теста
        List<Task> tasks = generateTasksForTest("clearTasks", 3);

        for (Task task : tasks) {
            taskManager.appendTask(task);
        }

        //Выполняем очистку задач
        taskManager.clearTasks();

        //Проверяем результат очистки задач
        tasks = taskManager.getTasks();

        assertNotNull(tasks, "Задачи на возвращаются.");
        assertEquals(0, tasks.size(), "Неверное количество задач.");

    }

    @Test
    void clearEpics() {
        //Генерируем эпики для теста
        List<Epic> epics = generateEpicsForTest("clearEpics", 3);

        for (Epic epic : epics) {
            taskManager.appendEpic(epic);
        }

        //Генерируем сабтаски для теста
        List<Subtask> subtasks = generateSubtasksForTest("clearEpics", epics.get(1), 3);

        for (Subtask subtask : subtasks) {
            taskManager.appendSubtask(subtask);
        }

        //Выполняем очистку эпиков
        taskManager.clearEpics();

        //Получаем списко эпиков из менеджера
        epics = taskManager.getEpics();

        //Проверяем результат
        assertNotNull(epics, "Эпики на возвращаются.");
        assertEquals(0, epics.size(), "Неверное количество эпиков.");

        //Дополнительно проверяем факт удаления сабтасков при удалении эпиков
        subtasks = taskManager.getSubtasks();

        assertNotNull(subtasks, "Подзадачи на возвращаются.");
        assertEquals(0, subtasks.size(), "Неверное количество подзадач после удаления эпиков.");
    }

    @Test
    void clearSubtasks() {
        //Генерируем эпики для теста сабтасков
        List<Epic> epics = generateEpicsForTest("clearSubtasks", 2);

        for (Epic epic : epics) {
            taskManager.appendEpic(epic);
        }

        //Генерируем сабтаски для теста
        List<Subtask> subtasks = generateSubtasksForTest("clearSubtasks", epics.get(1), 3);

        for (Subtask subtask : subtasks) {
            taskManager.appendSubtask(subtask);
        }

        //Проверяем очистку сабтасков
        taskManager.clearSubtasks();

        subtasks = taskManager.getSubtasks();

        assertNotNull(subtasks, "Подзадачи на возвращаются.");
        assertEquals(0, subtasks.size(), "Неверное количество подзадач после удаления эпиков.");

        //Дополнительно проверяем наличие эпиков после очистки сабтасков
        final List<Epic> epicsAfterClearSubtasks = taskManager.getEpics();

        assertNotNull(epicsAfterClearSubtasks, "Эпики на возвращаются.");
        assertEquals(2, epicsAfterClearSubtasks.size(), "Неверное количество эпиков.");

    }

    @Test
    void getTask() {
        //Поиск в пустом наборе
        Task found = taskManager.getTask(1);
        assertNull(found, "Задача найдена.");

        //Генерируем задачи для теста
        List<Task> tasks = generateTasksForTest("getTask", 3);
        for (Task task : tasks) {
            taskManager.appendTask(task);
        }

        //Проверяем получение по валидному идентификатору
        found = taskManager.getTask(tasks.get(0).getId());
        assertNotNull(found, "Задача не найдена.");
        assertEquals(tasks.get(0), found, "Найденная задача отличается");

        //Проверяем получение по null идентификатору
        found = taskManager.getTask(null);
        assertNull(found, "Задача найдена.");

        //Проверяем получение по несуществующему идентификатору
        found = taskManager.getTask(100);
        assertNull(found, "Задача найдена.");
    }

    @Test
    void getEpic() {
        //Поиск в пустом наборе
        Epic found = taskManager.getEpic(1);
        assertNull(found, "Эпик найден.");

        //Генерируем эпики для теста
        List<Epic> epics = generateEpicsForTest("getEpic", 3);
        for (Epic epic : epics) {
            taskManager.appendEpic(epic);
        }

        //Проверяем получение по валидному идентификатору
        found = taskManager.getEpic(epics.get(1).getId());
        assertNotNull(found, "Эпик не найден.");
        assertEquals(epics.get(1), found, "Найденный эпик отличается");

        //Проверяем получение по null идентификатору
        found = taskManager.getEpic(null);
        assertNull(found, "Эпик найден.");

        //Проверяем получение по несуществующему идентификатору
        found = taskManager.getEpic(100);
        assertNull(found, "Эпик найден.");
    }

    @Test
    void getSubtask() {
        //Поиск в пустом наборе
        Subtask found = taskManager.getSubtask(2);
        assertNull(found, "Сабтаск найден.");

        //Генерируем эпики для теста сабтасков
        List<Epic> epics = generateEpicsForTest("getSubtask", 2);
        for (Epic epic : epics) {
            taskManager.appendEpic(epic);
        }

        //Генерируем сабтаски для теста
        List<Subtask> subtasks = generateSubtasksForTest("getSubtask", epics.get(1), 3);
        for (Subtask subtask : subtasks) {
            taskManager.appendSubtask(subtask);
        }

        //Проверяем получение сабтаска
        found = taskManager.getSubtask(subtasks.get(2).getId());
        assertNotNull(found, "Сабтаск не найден.");
        assertEquals(subtasks.get(2), found, "Найденный сабтаск отличается");

        //Проверяем получение сабтаска с null идентификатором
        found = taskManager.getSubtask(null);
        assertNull(found, "Сабтаск найден.");

        //Проверяем получение сабтаска с несуществующем идентификатором
        found = taskManager.getSubtask(100);
        assertNull(found, "Сабтаск найден.");
    }

    @Test
    void getTasks() {
        final int TASK_COUNT = 15;
        List<Task> tasks;

        //Проверяем работу метода получения списка задач на пустом наборе
        tasks = taskManager.getTasks();
        assertNotNull(tasks, "Задачи на возвращаются.");
        assertEquals(0, tasks.size(), "Неверное количество задач.");

        //Генерируем задачи для теста
        tasks = generateTasksForTest("getTasks", TASK_COUNT);
        for (Task task : tasks) {
            taskManager.appendTask(task);
        }

        //Проверяем работу метода получения списка задач
        tasks = taskManager.getTasks();
        assertNotNull(tasks, "Задачи на возвращаются.");
        assertEquals(TASK_COUNT, tasks.size(), "Неверное количество задач.");
    }

    @Test
    void getEpics() {
        final int EPICS_COUNT = 15;
        List<Epic> epics;

        //Проверяем работу метода получения списка задач на пустом наборе
        epics = taskManager.getEpics();
        assertNotNull(epics, "Эпики на возвращаются.");
        assertEquals(0, epics.size(), "Неверное количество эпиков.");

        //Генерируем эпики для теста
        epics = generateEpicsForTest("getEpics", EPICS_COUNT);
        for (Epic epic : epics) {
            taskManager.appendEpic(epic);
        }

        //Проверяем работу метода получения списка задач
        epics = taskManager.getEpics();
        assertNotNull(epics, "Эпики не возвращаются.");
        assertEquals(EPICS_COUNT, epics.size(), "Неверное количество эпиков.");
    }

    @Test
    void getSubtasks() {
        final int SUBTASKS_COUNT = 15;
        List<Epic> epics;
        List<Subtask> subtasks;

        //Проверяем работу метода получения списка задач на пустом наборе
        subtasks = taskManager.getSubtasks();
        assertNotNull(subtasks, "Сабтаски на возвращаются.");
        assertEquals(0, subtasks.size(), "Неверное количество сабтасков.");

        //Генерируем эпики для теста сабтасков
        epics = generateEpicsForTest("getSubtasks", 2);
        for (Epic epic : epics) {
            taskManager.appendEpic(epic);
        }

        //Генерируем сабтаски для теста
        subtasks = generateSubtasksForTest("getSubtasks", epics.get(1), SUBTASKS_COUNT);
        for (Subtask subtask : subtasks) {
            taskManager.appendSubtask(subtask);
        }

        //Проверяем работу метода получения списка сабтасков
        subtasks = taskManager.getSubtasks();
        assertNotNull(subtasks, "Сабтаски не возвращаются.");
        assertEquals(SUBTASKS_COUNT, subtasks.size(), "Неверное количество сабтасков.");
    }

    @Test
    void getHistory() {
        List<Task> tasks;
        List<Epic> epics;
        List<Subtask> subtasks;
        List<Task> history;

        //Проверяем получение пустой истории
        history = taskManager.getHistory();
        assertNotNull(history, "История не возвращаются.");
        assertEquals(0, history.size(), "Неверное количество задач в истории.");

        //Генерируем задачи для теста
        tasks = generateTasksForTest("getTasks", 10);
        for (Task task : tasks) {
            taskManager.appendTask(task);
        }

        //Генерируем эпики для теста сабтасков
        epics = generateEpicsForTest("getSubtasks", 2);
        for (Epic epic : epics) {
            taskManager.appendEpic(epic);
        }

        //Генерируем сабтаски для теста
        subtasks = generateSubtasksForTest("getSubtasks", epics.get(1), 5);
        for (Subtask subtask : subtasks) {
            taskManager.appendSubtask(subtask);
        }

        //Обращаемся к задачам/эпикам/сабтаскам для формирования истории
        taskManager.getTask(tasks.get(2).getId());
        taskManager.getTask(tasks.get(3).getId());
        taskManager.getTask(tasks.get(7).getId());
        taskManager.getEpic(epics.get(1).getId());
        taskManager.getSubtask(subtasks.get(3).getId());

        //Проверяем получение истории и соответствие количества элементов
        history = taskManager.getHistory();
        assertNotNull(history, "История не возвращаются.");
        assertEquals(5, history.size(), "Неверное количество задач в истории.");
    }

    @Test
    void epicStatus() {
        List<Subtask> subtasks;

        //Генерируем эпик для теста
        Epic epic = generateEpicsForTest("epicStatus", 1).get(0);
        taskManager.appendEpic(epic);

        //Проверяем статус эпика при отсутствии сабтасков
        assertEquals(TaskStatus.NEW, epic.getStatus(), "Неверный статус эпика при отсутствии сабтасков");

        //Генерируем сабтаски для теста
        subtasks = generateSubtasksForTest("epicStatus", epic, 3);
        for (Subtask subtask : subtasks) {
            taskManager.appendSubtask(subtask);
        }

        //Проверяем статус эпика, когда все сабтаски NEW
        assertEquals(TaskStatus.NEW, epic.getStatus(), "Неверный статус эпика, когда все сабтаски NEW");

        //Проверяем статус эпика, когда все сабтаски DONE
        for (Subtask subtask : subtasks) {
            subtask.setStatus(TaskStatus.DONE);
            taskManager.updateSubtask(subtask);
        }
        assertEquals(TaskStatus.DONE, epic.getStatus(), "Неверный статус эпика, когда все сабтаски DONE");

        //Проверяем статус эпика (IN_PROGRESS), когда отдельные сабтаски NEW
        subtasks.get(1).setStatus(TaskStatus.NEW);
        taskManager.updateSubtask(subtasks.get(1));
        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus(),
                "Неверный статус эпика, когда отдельные сабтаски NEW");

        //Проверяем статус эпика (IN_PROGRESS), когда отдельные сабтаски IN_PROGRESS
        subtasks.get(2).setStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateSubtask(subtasks.get(2));
        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus(),
                "Неверный статус эпика, когда отдельные сабтаски IN_PROGRESS");

        //Проверяем статус эпика, когда все сабтаски IN_PROGRESS
        for (Subtask subtask : subtasks) {
            subtask.setStatus(TaskStatus.IN_PROGRESS);
            taskManager.updateSubtask(subtask);
        }
        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus(),
                "Неверный статус эпика, когда все сабтаски IN_PROGRESS");
    }

    @Test
    void epicTimes() {
        final int SUBTASK_COUNT = 2;
        final Duration SUBTASK_DURATION = Duration.ofHours(1);
        List<Subtask> subtasks;

        //Генерируем эпик для теста
        Epic epic = generateEpicsForTest("epicTimes", 1).get(0);
        taskManager.appendEpic(epic);

        //Проверяем время начала эпика при отсутствии сабтасков (null)
        assertNull(epic.getStartTime(), "Время начала эпика не null");

        //Генерируем сабтаски для теста, заполняем время проведения
        subtasks = generateSubtasksForTest("epicTimes", epic, SUBTASK_COUNT);
        LocalDateTime startTime = LocalDateTime.of(2023, 3, 6, 18, 20);
        Duration duration = SUBTASK_DURATION;
        for (Subtask subtask : subtasks) {
            subtask.setStartTime(startTime);
            startTime = startTime.plus(duration);
            subtask.setDuration(duration);
            taskManager.appendSubtask(subtask);
        }

        //Проверяем правильность расчета продолжительности эпика
        assertEquals(duration.multipliedBy(SUBTASK_COUNT), epic.getDuration(),
                "Неверная продолжительность эпика");

        //Проверяем правильность расчета продолжительности эпика
        assertEquals(subtasks.get(0).getStartTime(), epic.getStartTime(),
                "Неверное время начала эпика");
    }

    @Test
    void timesIntersectionsOnAppendTasks() {
        List<Task> tasks;
        List<Epic> epics;
        List<Subtask> subtasks;
        LocalDateTime startTime = LocalDateTime.of(2023, 3, 6, 18, 20);
        Duration duration = Duration.ofMinutes(30);
        Duration durationBetweenTasks = Duration.ofMinutes(6);

        //Генерируем задачи для теста с разницей времени начала в duration
        tasks = generateTasksForTest("timesIntersectionsOnAppendTasks", 10);
        for (Task task : tasks) {
            task.setStartTime(startTime);
            task.setDuration(duration);
            startTime = startTime.plus(duration).plus(durationBetweenTasks);
            taskManager.appendTask(task);
        }

        //Генерируем эпики для теста сабтасков
        Epic epic = generateEpicsForTest("timesIntersectionsOnAppendTasks", 1).get(0);
        taskManager.appendEpic(epic);

        //Сбрасываем время для формирования пересечений по времени задач и подзадач
        startTime = LocalDateTime.of(2023, 3, 6, 18, 20);
        //Генерируем сабтаски для теста
        subtasks = generateSubtasksForTest("timesIntersectionsOnAppendTasks", epic, 1);
        for (Subtask subtask : subtasks) {
            subtask.setStartTime(startTime);
            subtask.setDuration(duration);
            assertThrows(TaskValidatorException.class, () -> taskManager.appendSubtask(subtask),
                    "Пересечения подзадачи и задачи не обнаружены");
        }
    }
}