package testing;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import model.Epic;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.*;
import service.*;

import java.net.http.*;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HttpTaskServerTest {
    static private HttpTaskServer globalHttpTaskServer;
    static private KVServer kvServer;
    private HttpTaskServer httpTaskServer = globalHttpTaskServer;

    static {
        try {
            kvServer = new KVServer();
            kvServer.start();
            globalHttpTaskServer = new HttpTaskServer();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private HttpClient client = HttpClient.newHttpClient();

    HttpTaskServerTest() throws IOException {
    }

    @BeforeEach
    public void setUp() {
        this.httpTaskServer = HttpTaskServerTest.globalHttpTaskServer;
        httpTaskServer.getTaskManager().clearAll();
    }

    @AfterAll
    public static void afterAll() {
        kvServer.stop();
    }

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
        TaskManager taskManager = httpTaskServer.getTaskManager();

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

    private Gson createGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.serializeNulls();
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter());
        gsonBuilder.registerTypeAdapter(Duration.class, new DurationAdapter());
        gsonBuilder.setPrettyPrinting();
        return gsonBuilder.create();
    }

    private HttpResponse<String> simplePOST(String json, URI uri) throws IOException, InterruptedException {
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
        HttpRequest request = HttpRequest.newBuilder().uri(uri).POST(body).build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> simpleGET(URI uri) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> simpleDELETE(URI uri) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder().uri(uri).DELETE().build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    public void appendAndGetTask() throws IOException, InterruptedException {
        //Создание тестовой задачи
        Task newTask = new Task("Тест задача", "Описание тест задачи");

        //Адрес ресурса
        URI uri = URI.create("http://localhost:8080/tasks/task/");

        Gson gson = createGson();

        //Отправляем запрос на добавление задачи
        String json = gson.toJson(newTask);

        HttpResponse<String> response = simplePOST(json, uri);

        //Общая проверка результата
        assertEquals(200, response.statusCode(), "Сервер вернул неверный код ответа. Сообщение: " + response.body());
        assertFalse(response.body().isEmpty(), "Сервер вернул пустое тело ответа.");

        //Проверяем результат добавления задачи
        Task resultTask = gson.fromJson(response.body(), Task.class);
        newTask.setID(resultTask.getId()); //Идентификатор присваивается в случае успешного добавления задачи
        assertEquals(newTask, resultTask, "Модельная задачи и загруженная из сервера отличаются.");

        //Получаем добавленную задачу
        response = getTaskById(resultTask.getId() + "");

        //Проверяем результат получения задачи
        assertEquals(200, response.statusCode(), "Сервер вернул неверный код ответа.");
        assertFalse(response.body().isEmpty(), "Сервер вернул пустое тело ответа.");
        Task recivedTask = gson.fromJson(response.body(), Task.class);

        assertEquals(resultTask, recivedTask, "Модельная задачи и загруженная из сервера отличаются.");

        //Получаем задачу с пустым идентификтором
        response = getTaskById("");

        //Общая проверка результата
        assertEquals(400, response.statusCode(), "Сервер вернул неверный код ответа.");

        //Получаем задачу с неверным идентификтором
        response = getTaskById("-2");

        //Общая проверка результата
        assertEquals(400, response.statusCode(), "Сервер вернул неверный код ответа.");

        //Получаем задачу с неверным идентификтором
        response = getTaskById("bcd");

        //Общая проверка результата
        assertEquals(400, response.statusCode(), "Сервер вернул неверный код ответа.");

    }

    @Test
    public void updateTask() throws IOException, InterruptedException {
        //Создание тестовой задачи
        Task newTask = new Task("Тест задача", "Описание тест задачи");

        //Адрес ресурса
        URI uri = URI.create("http://localhost:8080/tasks/task/");

        Gson gson = createGson();

        //Отправляем запрос на добавление задачи
        String json = gson.toJson(newTask);

        HttpResponse<String> response = simplePOST(json, uri);

        //Общая проверка результата
        assertEquals(200, response.statusCode(), "Сервер вернул неверный код ответа. Сообщение: " + response.body());
        assertFalse(response.body().isEmpty(), "Сервер вернул пустое тело ответа.");

        //Проверяем результат добавления задачи
        Task resultTask = gson.fromJson(response.body(), Task.class);
        newTask.setID(resultTask.getId()); //Идентификатор присваивается в случае успешного добавления задачи
        assertEquals(newTask, resultTask, "Модельная задачи и загруженная из сервера отличаются.");

        //Обновляем задачу, устанавливаем идентификтор существующей
        Task taskForUpdate = new Task("Обновленная задача", "Описание обновленной задачи");
        taskForUpdate.setID(resultTask.getId());

        //Сохраняем обновленную задачу на сервер
        json = gson.toJson(taskForUpdate);
        response = simplePOST(json, uri);

        //Общая проверка результата
        assertEquals(200, response.statusCode(), "Сервер вернул неверный код ответа.");

        //Проверяем результат обновления
        resultTask = gson.fromJson(response.body(), Task.class);
        assertNotNull(resultTask, "Обновленная задача не возвращается");
        assertEquals(taskForUpdate, resultTask, "Модельная задачи и загруженная из сервера отличаются.");
    }

    @Test
    public void deleteTask() throws IOException, InterruptedException {
        //Создание тестовой задачи
        Task newTask = new Task("Тест задача", "Описание тест задачи");

        //Адрес ресурса
        URI uri = URI.create("http://localhost:8080/tasks/task/");

        Gson gson = createGson();

        //Отправляем запрос на добавление задачи
        String json = gson.toJson(newTask);

        HttpResponse<String> response = simplePOST(json, uri);

        //Общая проверка результата
        assertEquals(200, response.statusCode(), "Сервер вернул неверный код ответа. Сообщение: " + response.body());
        assertFalse(response.body().isEmpty(), "Сервер вернул пустое тело ответа.");

        //Проверяем результат добавления задачи
        Task resultTask = gson.fromJson(response.body(), Task.class);
        newTask.setID(resultTask.getId()); //Идентификатор присваивается в случае успешного добавления задачи
        assertEquals(newTask, resultTask, "Модельная задачи и загруженная из сервера отличаются.");

        //Удаляем тестовую задачу
        String targetId = resultTask.getId().toString();

        response = simpleDELETE(URI.create("http://localhost:8080/tasks/task?id=" + targetId));
        assertEquals(204, response.statusCode(), "Сервер вернул неверный код ответа: " + response.statusCode());

        response = getTaskById(targetId);
        resultTask = gson.fromJson(response.body(), Task.class);
        assertNull(resultTask, "Задача, которая была удалена, загрузилась.");

        //Пытаемся удалить заведомо отсутствующую задачу
        targetId = "100";

        response = simpleDELETE(URI.create("http://localhost:8080/tasks/task?id=" + targetId));
        assertEquals(400, response.statusCode(), "Сервер вернул неверный код ответа: " + response.statusCode());
    }

    @Test
    public void appendAndGetEpic() throws IOException, InterruptedException {
        //Создание тестового эпика
        Epic newEpic = new Epic("Тест эпик", "Описание тест эпика");

        //Адрес ресурса
        URI uri = URI.create("http://localhost:8080/tasks/epic/");

        Gson gson = createGson();

        //Отправляем запрос на добавление задачи
        String json = gson.toJson(newEpic);
        HttpResponse<String> response = simplePOST(json, uri);

        //Общая проверка результата
        assertEquals(200, response.statusCode(), "Сервер вернул неверный код ответа.");
        assertFalse(response.body().isEmpty(), "Сервер вернул пустое тело ответа.");

        //Проверяем результат добавления эпика
        Epic resultEpic = gson.fromJson(response.body(), Epic.class);
        newEpic.setID(resultEpic.getId()); //Идентификатор присваивается в случае успешного добавления задачи
        assertEquals(newEpic, resultEpic, "Модельный эпик и загруженный из сервера отличаются.");

        //Получаем добавленную задачу
        response = getEpicById(resultEpic.getId() + "");

        //Проверяем результат получения задачи
        assertEquals(200, response.statusCode(), "Сервер вернул неверный код ответа.");
        assertFalse(response.body().isEmpty(), "Сервер вернул пустое тело ответа.");
        Epic recivedEpic = gson.fromJson(response.body(), Epic.class);

        assertEquals(resultEpic, recivedEpic, "Модельная задачи и загруженная из сервера отличаются.");

        //Получаем задачу с пустым идентификтором
        response = getEpicById("");

        //Общая проверка результата
        assertEquals(400, response.statusCode(), "Сервер вернул неверный код ответа.");

        //Получаем задачу с неверным идентификтором
        response = getEpicById("-2");

        //Общая проверка результата
        assertEquals(400, response.statusCode(), "Сервер вернул неверный код ответа.");

        //Получаем задачу с неверным идентификтором
        response = getEpicById("abc");

        //Общая проверка результата
        assertEquals(400, response.statusCode(), "Сервер вернул неверный код ответа.");

    }

    @Test
    public void updateEpic() throws IOException, InterruptedException {
        //Создание тестового эпика
        Epic newEpic = new Epic("Тест эпик", "Описание тест эпика");

        //Адрес ресурса
        URI uri = URI.create("http://localhost:8080/tasks/epic/");

        Gson gson = createGson();

        //Отправляем запрос на добавление задачи
        String json = gson.toJson(newEpic);
        HttpResponse<String> response = simplePOST(json, uri);

        //Общая проверка результата
        assertEquals(200, response.statusCode(), "Сервер вернул неверный код ответа.");
        assertFalse(response.body().isEmpty(), "Сервер вернул пустое тело ответа.");

        //Проверяем результат добавления эпика
        Epic resultEpic = gson.fromJson(response.body(), Epic.class);
        newEpic.setID(resultEpic.getId()); //Идентификатор присваивается в случае успешного добавления задачи
        assertEquals(newEpic, resultEpic, "Модельный эпик и загруженный из сервера отличаются.");

        //Обновляем эпик, устанавливаем идентификтор существующей
        Epic epicForUpdate = new Epic("Обновленный эпик", "Описание обновленного эпика");
        epicForUpdate.setID(resultEpic.getId());

        //Сохраняем обновленный эпик на сервер
        json = gson.toJson(epicForUpdate);
        response = simplePOST(json, uri);

        //Общая проверка результата
        assertEquals(200, response.statusCode(), "Сервер вернул неверный код ответа.");

        //Проверяем результат обновления
        resultEpic = gson.fromJson(response.body(), Epic.class);
        assertNotNull(resultEpic, "Обновленная задача не возвращается");
        assertEquals(epicForUpdate, resultEpic, "Модельная задачи и загруженная из сервера отличаются.");
    }

    @Test
    public void deleteEpic() throws IOException, InterruptedException {
        //Создание тестового эпика
        Epic newEpic = new Epic("Тест эпик", "Описание тест эпика");

        //Адрес ресурса
        URI uri = URI.create("http://localhost:8080/tasks/epic/");

        Gson gson = createGson();

        //Отправляем запрос на добавление задачи
        String json = gson.toJson(newEpic);
        HttpResponse<String> response = simplePOST(json, uri);

        //Общая проверка результата
        assertEquals(200, response.statusCode(), "Сервер вернул неверный код ответа.");
        assertFalse(response.body().isEmpty(), "Сервер вернул пустое тело ответа.");

        //Проверяем результат добавления эпика
        Epic resultEpic = gson.fromJson(response.body(), Epic.class);
        newEpic.setID(resultEpic.getId()); //Идентификатор присваивается в случае успешного добавления задачи
        assertEquals(newEpic, resultEpic, "Модельный эпик и загруженный из сервера отличаются.");

        //Удаляем тестовый эпик
        String targetId = resultEpic.getId().toString();

        response = simpleDELETE(URI.create("http://localhost:8080/tasks/epic?id=" + targetId));
        assertEquals(204, response.statusCode(), "Сервер вернул неверный код ответа: " + response.statusCode());

        response = getEpicById(targetId);
        resultEpic = gson.fromJson(response.body(), Epic.class);
        assertNull(resultEpic, "Эпик, который был удален, загрузился.");

        //Пытаемся удалить заведомо отсутствующий эпик
        targetId = "100";

        response = simpleDELETE(URI.create("http://localhost:8080/tasks/epic?id=" + targetId));
        assertEquals(400, response.statusCode(), "Сервер вернул неверный код ответа: " + response.statusCode());
    }

    @Test
    public void appendAndGetSubtask() throws IOException, InterruptedException {
        //Создание тестового эпика
        Epic newEpic = new Epic("Тест эпик", "Описание тест эпика");

        //Адрес ресурса для добавления эпика
        URI uri = URI.create("http://localhost:8080/tasks/epic/");

        Gson gson = createGson();

        //Отправляем запрос на добавление эпика
        String json = gson.toJson(newEpic);
        HttpResponse<String> response = simplePOST(json, uri);

        //Общая проверка результата
        assertEquals(200, response.statusCode(), "Сервер вернул неверный код ответа.");
        assertFalse(response.body().isEmpty(), "Сервер вернул пустое тело ответа.");

        //Проверяем результат добавления эпика
        Epic resultEpic = gson.fromJson(response.body(), Epic.class);
        newEpic.setID(resultEpic.getId()); //Идентификатор присваивается в случае успешного добавления задачи
        assertEquals(newEpic, resultEpic, "Модельный эпик и загруженный из сервера отличаются.");

        //Адрес ресурса для добавления сабтаска
        uri = URI.create("http://localhost:8080/tasks/subtask/");

        //Создаем подзадачу к эпику
        Subtask newSubtask = new Subtask(resultEpic, "Тест сабтаск", "Описание тест сабтаска");

        //Отправляем запрос на добавление сабтаска
        json = gson.toJson(newSubtask);
        response = simplePOST(json, uri);

        //Общая проверка результата
        assertEquals(200, response.statusCode(), "Сервер вернул неверный код ответа.");
        assertFalse(response.body().isEmpty(), "Сервер вернул пустое тело ответа.");

        //Проверяем результат добавления сабтаска
        Subtask resultSubtask = gson.fromJson(response.body(), Subtask.class);
        newSubtask.setID(resultSubtask.getId()); //Идентификатор присваивается в случае успешного добавления задачи
        assertEquals(newSubtask, resultSubtask, "Модельный сабтаск и загруженный из сервера отличаются.");

        //Получаем задачу с пустым идентификтором
        response = getSubtaskById("");

        //Общая проверка результата
        assertEquals(400, response.statusCode(), "Сервер вернул неверный код ответа. >");

        //Получаем задачу с неверным идентификтором
        response = getSubtaskById("-2");

        //Общая проверка результата
        assertEquals(400, response.statusCode(), "Сервер вернул неверный код ответа.");

        //Получаем задачу с неверным идентификтором
        response = getSubtaskById("aba");

        //Общая проверка результата
        assertEquals(400, response.statusCode(), "Сервер вернул неверный код ответа.");
    }

    @Test
    public void updateSubtask() throws IOException, InterruptedException {
        //Создание тестового эпика
        Epic newEpic = new Epic("Тест эпик", "Описание тест эпика");

        //Адрес ресурса для добавления эпика
        URI uri = URI.create("http://localhost:8080/tasks/epic/");

        Gson gson = createGson();

        //Отправляем запрос на добавление эпика
        String json = gson.toJson(newEpic);
        HttpResponse<String> response = simplePOST(json, uri);

        //Общая проверка результата
        assertEquals(200, response.statusCode(), "Сервер вернул неверный код ответа.");
        assertFalse(response.body().isEmpty(), "Сервер вернул пустое тело ответа.");

        //Проверяем результат добавления эпика
        Epic resultEpic = gson.fromJson(response.body(), Epic.class);
        newEpic.setID(resultEpic.getId()); //Идентификатор присваивается в случае успешного добавления задачи
        assertEquals(newEpic, resultEpic, "Модельный эпик и загруженный из сервера отличаются.");

        //Адрес ресурса для добавления сабтаска
        uri = URI.create("http://localhost:8080/tasks/subtask/");

        //Создаем подзадачу к эпику
        Subtask newSubtask = new Subtask(resultEpic, "Тест сабтаск", "Описание тест сабтаска");

        //Отправляем запрос на добавление сабтаска
        json = gson.toJson(newSubtask);
        response = simplePOST(json, uri);

        //Общая проверка результата
        assertEquals(200, response.statusCode(), "Сервер вернул неверный код ответа.");
        assertFalse(response.body().isEmpty(), "Сервер вернул пустое тело ответа.");

        //Проверяем результат добавления сабтаска
        Subtask resultSubtask = gson.fromJson(response.body(), Subtask.class);
        newSubtask.setID(resultSubtask.getId()); //Идентификатор присваивается в случае успешного добавления задачи
        assertEquals(newSubtask, resultSubtask, "Модельный сабтаск и загруженный из сервера отличаются.");

        //Обновляем эпик, устанавливаем идентификтор существующей
        Subtask subtaskForUpdate = new Subtask(resultEpic,
                "Обновленный эпик",
                "Описание обновленного эпика");
        subtaskForUpdate.setID(resultSubtask.getId());

        //Сохраняем обновленный эпик на сервер
        json = gson.toJson(subtaskForUpdate);
        response = simplePOST(json, uri);

        //Общая проверка результата
        assertEquals(200, response.statusCode(), "Сервер вернул неверный код ответа.");

        //Проверяем результат обновления
        resultSubtask = gson.fromJson(response.body(), Subtask.class);
        assertNotNull(resultSubtask, "Обновленная задача не возвращается");
        assertEquals(subtaskForUpdate, resultSubtask, "Модельная задачи и загруженная из сервера отличаются.");
    }

    @Test
    public void deleteSubtask() throws IOException, InterruptedException {
        //Создание тестового эпика
        Epic newEpic = new Epic("Тест эпик", "Описание тест эпика");

        //Адрес ресурса для добавления эпика
        URI uri = URI.create("http://localhost:8080/tasks/epic/");

        Gson gson = createGson();

        //Отправляем запрос на добавление эпика
        String json = gson.toJson(newEpic);
        HttpResponse<String> response = simplePOST(json, uri);

        //Общая проверка результата
        assertEquals(200, response.statusCode(), "Сервер вернул неверный код ответа.");
        assertFalse(response.body().isEmpty(), "Сервер вернул пустое тело ответа.");

        //Проверяем результат добавления эпика
        Epic resultEpic = gson.fromJson(response.body(), Epic.class);
        newEpic.setID(resultEpic.getId()); //Идентификатор присваивается в случае успешного добавления задачи
        assertEquals(newEpic, resultEpic, "Модельный эпик и загруженный из сервера отличаются.");

        //Адрес ресурса для добавления сабтаска
        uri = URI.create("http://localhost:8080/tasks/subtask/");

        //Создаем подзадачу к эпику
        Subtask newSubtask = new Subtask(resultEpic, "Тест сабтаск", "Описание тест сабтаска");

        //Отправляем запрос на добавление сабтаска
        json = gson.toJson(newSubtask);
        response = simplePOST(json, uri);

        //Общая проверка результата
        assertEquals(200, response.statusCode(), "Сервер вернул неверный код ответа.");
        assertFalse(response.body().isEmpty(), "Сервер вернул пустое тело ответа.");

        //Проверяем результат добавления сабтаска
        Subtask resultSubtask = gson.fromJson(response.body(), Subtask.class);
        newSubtask.setID(resultSubtask.getId()); //Идентификатор присваивается в случае успешного добавления задачи
        assertEquals(newSubtask, resultSubtask, "Модельный сабтаск и загруженный из сервера отличаются.");

        //Удаляем тестовый сабтаск
        String targetId = resultSubtask.getId().toString();

        response = simpleDELETE(URI.create("http://localhost:8080/tasks/subtask?id=" + targetId));
        assertEquals(204, response.statusCode(), "Сервер вернул неверный код ответа: " + response.statusCode());

        response = getSubtaskById(targetId);
        resultSubtask = gson.fromJson(response.body(), Subtask.class);
        assertNull(resultSubtask, "Сабтаск, который был удален, загрузился.");

        //Пытаемся удалить заведомо отсутствующий сабтаск
        targetId = "100";

        response = simpleDELETE(URI.create("http://localhost:8080/tasks/subtask?id=" + targetId));
        assertEquals(400, response.statusCode(), "Сервер вернул неверный код ответа: " + response.statusCode());
    }

    @Test
    public void getAllTasks() throws IOException, InterruptedException {
        httpTaskServer.getTaskManager().clearAll();
        generateSomeTasksAndHistory("getAllTasks");

        URI uri = URI.create("http://localhost:8080/tasks/");
        HttpResponse<String> response = simpleGET(uri);

        //Общая проверка результата
        assertEquals(200, response.statusCode(), "Сервер вернул неверный код ответа.");
        assertFalse(response.body().isEmpty(), "Сервер вернул пустое тело ответа.");

    }

    @Test
    public void getTasks() throws IOException, InterruptedException {
        httpTaskServer.getTaskManager().clearAll();
        generateSomeTasksAndHistory("getTasks");

        URI uri = URI.create("http://localhost:8080/tasks/task");
        HttpResponse<String> response = simpleGET(uri);

        //Общая проверка результата
        assertEquals(200, response.statusCode(), "Сервер вернул неверный код ответа.");
        assertFalse(response.body().isEmpty(), "Сервер вернул пустое тело ответа.");

    }

    @Test
    public void getEpics() throws IOException, InterruptedException {
        httpTaskServer.getTaskManager().clearAll();
        generateSomeTasksAndHistory("getEpics");

        URI uri = URI.create("http://localhost:8080/tasks/epic");
        HttpResponse<String> response = simpleGET(uri);

        //Общая проверка результата
        assertEquals(200, response.statusCode(), "Сервер вернул неверный код ответа.");
        assertFalse(response.body().isEmpty(), "Сервер вернул пустое тело ответа.");
    }

    @Test
    public void getSubtasks() throws IOException, InterruptedException {
        httpTaskServer.getTaskManager().clearAll();
        generateSomeTasksAndHistory("getSubtasks");

        URI uri = URI.create("http://localhost:8080/tasks/subtask");
        HttpResponse<String> response = simpleGET(uri);

        //Общая проверка результата
        assertEquals(200, response.statusCode(), "Сервер вернул неверный код ответа.");
        assertFalse(response.body().isEmpty(), "Сервер вернул пустое тело ответа.");
    }

    @Test
    public void getHistory() throws IOException, InterruptedException {
        httpTaskServer.getTaskManager().clearAll();
        generateSomeTasksAndHistory("getHistory");

        HttpClient client = HttpClient.newHttpClient();

        URI uri = URI.create("http://localhost:8080/tasks/history");
        HttpResponse<String> response = simpleGET(uri);

        //Общая проверка результата
        assertEquals(200, response.statusCode(), "Сервер вернул неверный код ответа.");
        assertFalse(response.body().isEmpty(), "Сервер вернул пустое тело ответа.");
    }

    private HttpResponse<String> getTaskById(String id) throws IOException, InterruptedException {
        URI uri = URI.create("http://localhost:8080/tasks/task?id=" + id);
        return simpleGET(uri);
    }

    private HttpResponse<String> getSubtaskById(String id) throws IOException, InterruptedException {
        URI uri = URI.create("http://localhost:8080/tasks/subtask?id=" + id);
        return simpleGET(uri);
    }

    private HttpResponse<String> getEpicById(String id) throws IOException, InterruptedException {
        URI uri = URI.create("http://localhost:8080/tasks/epic?id=" + id);
        return simpleGET(uri);
    }


}