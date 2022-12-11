import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {

        TaskManager taskManager = new TaskManager();
        taskManager.createTask(new Task("Задача 1","Самая первая задача"));
        taskManager.createTask(new Task("Задача 2","Вторая задача"));
        taskManager.createTask(new Task("Задача 3","Третья задача"));
        Epic epic1 = new Epic("Эпическая задача 1","Описание эпической задачи 1");
        taskManager.createTask(epic1);

        Subtask subtask1 = new Subtask(epic1,"Подзадача 1","Описание подзадачи 1");
        taskManager.createTask(subtask1);
        Subtask subtask2 = new Subtask(epic1,"Подзадача 2","Описание подзадачи 2");
        taskManager.createTask(subtask2);

        //skManager.toString();

        Task task = taskManager.getTaskById(10);
        if (task!=null) {
            System.out.println(task.toString());
            System.out.println("=".repeat(20));
        };
        System.out.println(taskManager.toString());

        /*if (taskManager.updateTask(new Task("Задачка","Была подзадача, стала задача",TaskStatus.NEW,5))) {
            System.out.println("успешно обновлено");
        } else {
            System.out.println("не обновлено, элемент не найден");
        }*/

        System.out.println("=".repeat(20));
        //taskManager.removeById(3);

        System.out.println(taskManager.toString());
    }
}
