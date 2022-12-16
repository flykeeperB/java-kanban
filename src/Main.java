import taskmanager.TaskManager;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import tasks.TaskStatus;

public class Main {

    public static void main(String[] args) {
        TaskManager taskManager = new TaskManager();

        //тестирование
        Task task1 = new Task("Задача 1", "Самая первая задача");
        Task task2 = new Task("Задача 2", "Вторая задача");
        taskManager.appendTask(task1);
        taskManager.appendTask(task2);

        Epic epic1 = new Epic("Эпик 1", "Описание эпической задачи 1");
        taskManager.appendEpic(epic1);
        Subtask subtask1OfEpic1 = new Subtask(epic1, "Подзадача 1", "Описание подзадачи 1 эпика 1");
        taskManager.appendSubtask(subtask1OfEpic1);
        Subtask subtask2OfEpic1 = new Subtask(epic1, "Подзадача 2", "Описание подзадачи 2 эпика 1");
        taskManager.appendSubtask(subtask2OfEpic1);

        Epic epic2 = new Epic("Эпик 2", "Описание эпической задачи 2");
        taskManager.appendEpic(epic2);
        Subtask subtask1OfEpic2 = new Subtask(epic2, "Подзадача 1", "Описание подзадачи 1 эпика 2");
        taskManager.appendSubtask(subtask1OfEpic2);

        System.out.println("");
        System.out.println(taskManager);
        System.out.println("");

        System.out.println("Изменяем статус подзадачи 1 эпика 1");
        subtask1OfEpic1.setStatus(TaskStatus.DONE);
        taskManager.updateSubtask(subtask1OfEpic1);
        System.out.println("Изменяем статус подзадачи 1 эпика 2");
        subtask1OfEpic2.setStatus(TaskStatus.DONE);
        taskManager.updateSubtask(subtask1OfEpic2);

        System.out.println("");
        System.out.println(taskManager);
        System.out.println("");

        System.out.println("Удаляем задачу 2");
        taskManager.delete(1);
        System.out.println("Удаляем эпик 2");
        taskManager.delete(5);

        System.out.println("");
        System.out.println(taskManager);
        System.out.println("");

        //дополнительные тесты
        /*System.out.println("Обновление несуществующей задачи (не должно происходить)");
        Task task20 = new Task("Задача 20", "Задача не добавлялась в менеджер", TaskStatus.NEW, 20);
        taskManager.update(task20);
        System.out.println("Удаление несуществующей задачи (не должно происходить)");
        taskManager.delete(task20.getId());

        System.out.println("");
        System.out.println(taskManager);
        System.out.println("");*/
    }
}
