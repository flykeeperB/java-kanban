import taskmanager.Managers;
import taskmanager.TaskManager;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import tasks.TaskStatus;

import java.sql.SQLOutput;
import java.util.List;
import java.util.Random;

public class Main {

    public static void main(String[] args) {
        TaskManager taskManager = Managers.getDefault();

        //ТЕСТИРОВАНИЕ

        //создадим двадцать задач
        for (int i = 1; i < 11; i++) {
            taskManager.appendTask(new Task("Задача "+i, "Описание задачи "+i));
        }

        //создадим пять эпиков cо случайным числом подзадач в диапазоне от 0 до 5
        for (int i = 1; i < 6; i++) {
            Epic epic = new Epic("Эпик "+i, "Описание эпика "+i);
            taskManager.appendEpic(epic);
            int max = new Random().nextInt(4);
            for (int j = 1; j < 1+max; j++) {
                taskManager.appendSubtask(new Subtask(epic, "Подзадача " + j, "Описание подзадачи " + j + " эпика " + i));
            }
        }

        System.out.println("");
        System.out.println(taskManager);
        System.out.println("");

        List<Task> tasks;

        System.out.println("");
        System.out.println("Проверка сохранения истории:");
        //для проверки истории обращаемся к 12 случайным задачам/эпикам/сабтаскам
        tasks = taskManager.getTasks();
        tasks.addAll(taskManager.getEpics());
        tasks.addAll(taskManager.getSubtasks());
        for (int i = 0; i < 12 ; i++) {
            Task task = tasks.get(new Random().nextInt(tasks.size()-1));
            System.out.println("Обращаемся через get к "+task.getClass().toString()+" id = "+task.getId());
            if (task instanceof Epic) {
                taskManager.getEpic(task.getId());
            } else if (task instanceof Subtask) {
                taskManager.getSubtask(task.getId());
            } else {
                taskManager.getTask(task.getId());
            }
        }

        System.out.println("");
        System.out.println("История:");
        List<Task> history = taskManager.getHistory();
        for (int i = 0; i < history.size(); i++) {
            System.out.println((i+1)+" - "+history.get(i).toString());
        }

        // Проверяем работоспособность методов удаления
        System.out.println("");
        System.out.println("Проверка удаления записей:");
        //удаляем случайные 8 элементов
        tasks = taskManager.getTasks();
        tasks.addAll(taskManager.getEpics());
        tasks.addAll(taskManager.getSubtasks());
        for (int i = 0; i < 8 ; i++) {
            Task task = tasks.get(new Random().nextInt(tasks.size()-1));
            System.out.println("Удаляем "+task.getClass().toString()+" id = "+task.getId());
            if (task instanceof Epic) {
                taskManager.deleteEpic(task.getId());
            } else if (task instanceof Subtask) {
                taskManager.deleteSubtask(task.getId());
            } else {
                taskManager.deleteTask(task.getId());
            }
        }

        // Проверяем работоспособность методов обновления
        System.out.println("");
        System.out.println("Проверка обновления записей:");
        //обновляем наименование и описание случайных элементов
        tasks = taskManager.getTasks();
        tasks.addAll(taskManager.getEpics());
        tasks.addAll(taskManager.getSubtasks());
        for (int i = 0; i < 8 ; i++) {
            Task task = tasks.get(new Random().nextInt(tasks.size()-1));
            if (task instanceof Epic) {
                System.out.println("Обновляем "+task.getClass().toString()+" id = "+task.getId());
                Epic epic = new Epic(task.getName()+" (upd)",task.getDiscription()+" ОБНОВЛЕНО",task.getStatus(), task.getId());
                taskManager.updateEpic(epic);
            } else if (task instanceof Subtask) {

            } else {
                System.out.println("Обновляем "+task.getClass().toString()+" id = "+task.getId());
                Task updtask = new Task(task.getName()+" (upd)",task.getDiscription()+" ОБНОВЛЕНО",TaskStatus.DONE, task.getId());
                taskManager.updateTask(updtask);
            }
        }

        System.out.println("");
        System.out.println("Вывод результатов");
        System.out.println(taskManager);
        System.out.println("");

        System.out.println("");
        System.out.println("Проверка что история не изменилась. Удаление/обновление не должны обновлят историю:");
        System.out.println("История:");
        history = taskManager.getHistory();
        for (int i = 0; i < history.size(); i++) {
            System.out.println((i+1)+" - "+history.get(i).toString());
        }

    }
}
