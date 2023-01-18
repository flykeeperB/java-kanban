import service.Managers;
import service.TaskManager;
import model.Epic;
import model.Subtask;
import model.Task;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        TaskManager taskManager = Managers.getDefault();

        //ТЕСТЫ

        //создайте две задачи, эпик с тремя подзадачами и эпик без подзадач
        System.out.println("создайте две задачи, эпик с тремя подзадачами и эпик без подзадач");
        System.out.println("-".repeat(15));

        Task task1 = new Task("Задача 1", "Описание задачи 1");
        taskManager.appendTask(task1);
        Task task2 = new Task("Задача 2", "Описание задачи 2");
        taskManager.appendTask(task2);
        Epic epic1 = new Epic("Эпик 1", "Описание эпика 1");
        taskManager.appendEpic(epic1);
        Subtask subtask1 = new Subtask(epic1, "Подзадача 1", "Описание подзадачи 1 эпика 1");
        taskManager.appendSubtask(subtask1);
        Subtask subtask2 = new Subtask(epic1, "Подзадача 2", "Описание подзадачи 2 эпика 1");
        taskManager.appendSubtask(subtask2);
        Epic epic2 = new Epic("Эпик 2", "Описание эпика 2");
        taskManager.appendEpic(epic2);

        //запросите созданные задачи несколько раз в разном порядке
        //после каждого запроса выведите историю и убедитесь, что в ней нет повторов
        System.out.println("\n");
        System.out.println("запросите созданные задачи несколько раз в разном порядке");
        System.out.println("после каждого запроса выведите историю и убедитесь, что в ней нет повторов");
        System.out.println("-".repeat(15));

        taskManager.getEpic(epic1.getId());
        taskManager.getTask(task1.getId());
        taskManager.getEpic(epic2.getId());
        taskManager.getSubtask(subtask2.getId());
        taskManager.getTask(task1.getId());
        taskManager.getTask(task2.getId());
        taskManager.getSubtask(subtask1.getId());
        taskManager.getSubtask(subtask2.getId());

        //выводим историю
        System.out.println("\n");
        System.out.println("История 1:");
        List<Task> history1 = taskManager.getHistory();
        for (int i = 0; i < history1.size(); i++) {
            System.out.println((i + 1) + " - " + history1.get(i).toString());
        }

        taskManager.getEpic(epic1.getId());
        taskManager.getTask(task1.getId());
        taskManager.getEpic(epic2.getId());
        taskManager.getSubtask(subtask2.getId());
        taskManager.getSubtask(subtask1.getId());
        taskManager.getTask(task1.getId());
        taskManager.getEpic(epic1.getId());
        taskManager.getTask(task2.getId());
        taskManager.getTask(task1.getId());

        //выводим историю
        System.out.println("\n");
        System.out.println("История 2:");
        List<Task> history2 = taskManager.getHistory();
        for (int i = 0; i < history2.size(); i++) {
            System.out.println((i + 1) + " - " + history2.get(i).toString());
        }

        //удалите задачу, которая есть в истории, и проверьте, что при печати она не будет выводиться
        System.out.println("\n");
        System.out.println("удалите задачу, которая есть в истории, и проверьте," +
                " что при печати она не будет выводиться");
        System.out.println("-".repeat(15));
        taskManager.deleteTask(task1.getId());

        //выводим историю
        System.out.println("\n");
        System.out.println("История 3:");
        List<Task> history3 = taskManager.getHistory();
        for (int i = 0; i < history3.size(); i++) {
            System.out.println((i + 1) + " - " + history3.get(i).toString());
        }

        //удалите эпик с тремя подзадачами и убедитесь, что из истории удалился как сам эпик, так и все его подзадачи
        System.out.println("\n");
        System.out.println("удалите эпик с тремя подзадачами и убедитесь, " +
                "что из истории удалился как сам эпик, так и все его подзадачи");
        System.out.println("-".repeat(15));
        taskManager.deleteEpic(epic1.getId());

        //выводим историю
        System.out.println("\n");
        System.out.println("История 4:");
        List<Task> history4 = taskManager.getHistory();
        for (int i = 0; i < history4.size(); i++) {
            System.out.println((i + 1) + " - " + history4.get(i).toString());
        }
    }
}
