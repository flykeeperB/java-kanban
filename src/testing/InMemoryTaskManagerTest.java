package testing;

import model.Epic;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.InMemoryTaskManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {
    @BeforeEach
    void setUp() {
        this.taskManager = new InMemoryTaskManager();
    }

    @Test
    void getPrioritizedTasks() {
        final Duration TASK_DURATION = Duration.ofHours(1);
        LocalDateTime startTime = LocalDateTime.of(2023, 3, 6, 18, 20);

        //Создаем тестовый набор задач. Для части задач (с 3 по 7) задаем время и продолжительность
        List<Task> tasks = generateTasksForTest("getPrioritizedTasks",10);
        for (int i = 2; i < 8; i++) {
            Task task = tasks.get(i);
            task.setStartTime(startTime);
            startTime = startTime.plus(TASK_DURATION);
            task.setDuration(TASK_DURATION);
        }

        //Для ревалентности выборки присваиваем задаче первой задаче самое большое время начала
        Task firstTask = tasks.get(0);
        firstTask.setStartTime(startTime);
        taskManager.updateTask(firstTask);

        for (Task task : tasks) {
            taskManager.appendTask(task);
        }

        //Создаем тестовый набор эпиков.
        List<Epic> epics = generateEpicsForTest("getPrioritizedTasks",2);
        for (Epic epic : epics) {
            taskManager.appendEpic(epic);
        }

        //Создаем тестовый набор подтасков. Для части подзадач (с 2 по 3) задаем время и продолжительность
        List<Subtask> subtasks = generateSubtasksForTest("getPrioritizedTasks",epics.get(0), 3);
        for (int i = 0; i < 2; i++) {
            Subtask subtask = subtasks.get(i);
            subtask.setStartTime(startTime);
            startTime = startTime.plus(TASK_DURATION);
            subtask.setDuration(TASK_DURATION);
        }

        for (Subtask subtask : subtasks) {
            taskManager.appendSubtask(subtask);
        }

        //Получаем задачи в порядке приоритетов
        List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();

        assertNotNull(prioritizedTasks, "Не возвращается набор задач.");
        assertEquals(1,  prioritizedTasks.get(6).getId(),
                "Неправильный элемент (не id=1) в позиции 7.");
        assertEquals(15,  prioritizedTasks.get(12).getId(),
                "Неправильный элемент (не id=1) в позиции 12.");

        for (Epic epic : epics) {
            assertFalse(prioritizedTasks.contains(epic),"Перечень задач/подзадач включает эпик.");
        }

    }
}
