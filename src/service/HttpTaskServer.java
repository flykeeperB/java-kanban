package service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import model.Epic;
import model.Subtask;
import model.Task;
import service.adapters.DurationAdapter;
import service.adapters.LocalDateTimeAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class HttpTaskServer {
    private final int MAX_SERVER_MESSAGE_LVL = 1;

    private final int PORT = 8080;
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    private final TaskManager taskManager = Managers.getDefault();
    private final HttpServer httpServer = HttpServer.create();

    public TaskManager getTaskManager() {
        return taskManager;
    }

    public HttpTaskServer() throws IOException {
        HttpServer httpServer = HttpServer.create();
        InetSocketAddress socket = new InetSocketAddress(PORT);

        httpServer.bind(socket, 0);

        httpServer.createContext("/tasks/history/", this::handleHistory);
        httpServer.createContext("/tasks/task/", this::handleTask);
        httpServer.createContext("/tasks/epic/", this::handleEpic);
        httpServer.createContext("/tasks/subtask/", this::handleSubtask);
        httpServer.createContext("/tasks/subtask/epic/", this::handleEpicSubtasks);
        httpServer.createContext("/tasks/", this::handleAllTasks);

        httpServer.start();

        msg(0, "", " запущен на " + PORT + " порту!");
    }

    private void msg(int lvl, String funcName, String msg) {
        if (lvl <= MAX_SERVER_MESSAGE_LVL) {
            System.out.println("HttpTaskServer (" + lvl + ")"
                    + (funcName.isEmpty() ? "" : ("." + funcName + "()"))
                    + ":  " + msg);
        }
    }

    private Gson createGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter());
        gsonBuilder.registerTypeAdapter(Duration.class, new DurationAdapter());
        gsonBuilder.setPrettyPrinting();
        return gsonBuilder.create();
    }

    private Map<String, String> queryToMap(String query) {
        Map<String, String> result = new HashMap<>();
        if (query == null) {
            return result;
        }

        String[] params = query.split("&");

        for (String param : params) {
            String[] entry = param.split("=");
            if (entry.length > 1) {
                result.put(entry[0], entry[1]);
            } else {
                result.put(entry[0], "");
            }
        }
        return result;
    }

    private void simpleResponse(HttpExchange httpExchange, int responseCode, String response) throws IOException {
        switch (responseCode) {
            case 204:
                httpExchange.sendResponseHeaders(responseCode, -1);
            default:
                httpExchange.sendResponseHeaders(responseCode, 0);
        }

        try (OutputStream os = httpExchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

    private void handleTask(HttpExchange httpExchange) throws IOException {
        Gson gson = createGson();

        Map<String, String> params = queryToMap(httpExchange.getRequestURI().getQuery());
        InputStream inputStream = httpExchange.getRequestBody();
        String body = new String(inputStream.readAllBytes(), DEFAULT_CHARSET);

        Integer id = null;
        //Проверка корректности идентификатора
        if (params.containsKey("id")) {
            try {
                id = Integer.parseInt(params.get("id"));
            } catch (NumberFormatException e) {
                msg(1, "handleTask", "Передан ошибочный идентификатор. Message: " + e.getMessage());
                simpleResponse(httpExchange, 400, "Передан ошибочный идентификатор");
                return;
            }
        }

        switch (httpExchange.getRequestMethod()) {
            case "POST": {
                Task task = gson.fromJson(body, Task.class);
                if (id == null) {
                    //создание записи, если нет параметра id
                    try {
                        task = taskManager.appendTask(task);
                        if (task != null) {
                            simpleResponse(httpExchange, 200,
                                    gson.toJson(task));
                        } else {
                            simpleResponse(httpExchange, 400,
                                    "Не добавлено, проверьте корректность данных");
                        }
                    } catch (TaskValidatorException e) {
                        msg(1, "handleTask", e.getMessage());
                        simpleResponse(httpExchange, 400,
                                "Не добавлено, задача имеет пересечения во времени с другими задачами");
                    }
                } else {
                    //обновление записи, если есть параметр id
                    try {
                        task.setID(id);
                        task = taskManager.updateTask(task);
                        if (task != null) {
                            simpleResponse(httpExchange, 200,
                                    gson.toJson(task));
                        } else {
                            simpleResponse(httpExchange, 400,
                                    "Не обновлено, проверьте корректность данных");
                        }
                    } catch (TaskValidatorException e) {
                        msg(1, "handleTask", e.getMessage());
                        simpleResponse(httpExchange, 400,
                                "Не добавлено, задача имеет пересечения во времени с другими задачами");
                    }
                }
            }
            break;

            case "GET": {
                if (id == null) {
                    //получение всех записей, если нет параметра id
                    simpleResponse(httpExchange, 200,
                            gson.toJson(this.taskManager.getTasks()));
                } else {
                    //получение конкретной записи, если есть параметр id
                    Task task = taskManager.getTask(id);
                    if (task != null) {
                        simpleResponse(httpExchange, 200,
                                gson.toJson(task));
                    } else {
                        msg(1, "handleTask", "Задача с заданным идентификатором отсутствует.");
                        simpleResponse(httpExchange, 400,
                                "Задача с заданным идентификатором отсутствует.");
                    }
                }
            }
            break;

            case "DELETE": {
                if (taskManager.deleteTask(id)) {
                    simpleResponse(httpExchange, 204, "");
                } else {
                    msg(1, "handleTask", "Задача с заданным идентификатором не удалена.");
                    simpleResponse(httpExchange, 400,
                            "Задача с заданным идентификатором не удалена.");
                }
            }
            break;
        }
    }

    private void handleEpic(HttpExchange httpExchange) throws IOException {
        Gson gson = createGson();

        Map<String, String> params = queryToMap(httpExchange.getRequestURI().getQuery());
        InputStream inputStream = httpExchange.getRequestBody();
        String body = new String(inputStream.readAllBytes(), DEFAULT_CHARSET);

        Integer id = null;
        //Проверка корректности идентификатора
        if (params.containsKey("id")) {
            try {
                id = Integer.parseInt(params.get("id"));
            } catch (NumberFormatException e) {
                msg(1, "handleEpic", "Передан ошибочный идентификатор. Message: " + e.getMessage());
                simpleResponse(httpExchange, 400, "Передан ошибочный идентификатор");
                return;
            }
        }

        switch (httpExchange.getRequestMethod()) {
            case "POST": {

                Epic epic = gson.fromJson(body, Epic.class);

                if (id == null) {
                    //создание записи, если нет параметра id
                    try {
                        epic = taskManager.appendEpic(epic);
                        if (epic != null) {
                            simpleResponse(httpExchange, 200,
                                    gson.toJson(epic));
                        } else {
                            simpleResponse(httpExchange, 400,
                                    "Не добавлено, проверьте корректность данных");
                        }
                    } catch (TaskValidatorException e) {
                        msg(1, "handleEpic", e.getMessage());
                        simpleResponse(httpExchange, 400,
                                "Не добавлено, эпик имеет пересечения во времени с другими эпиками");
                    }
                } else {
                    //обновление записи, если есть параметр id
                    try {
                        epic.setID(id);
                        epic = taskManager.updateEpic(epic);
                        if (epic != null) {
                            simpleResponse(httpExchange, 200,
                                    gson.toJson(epic));
                        } else {
                            msg(1, "handleEpic", "Неудачное обновление записи");
                            simpleResponse(httpExchange, 400,
                                    "Не обновлено, проверьте корректность данных");
                        }
                    } catch (TaskValidatorException e) {
                        msg(1, "handleEpic", e.getMessage());
                        simpleResponse(httpExchange, 400,
                                "Не добавлено, эпик имеет пересечения во времени с другими эпиками");
                    }
                }
            }
            break;

            case "GET": {
                if (id == null) {
                    //получение всех записей, если нет параметра id
                    simpleResponse(httpExchange, 200,
                            gson.toJson(this.taskManager.getEpics()));
                } else {
                    //получение конкретной записи, если есть параметр id
                    Epic epic = taskManager.getEpic(id);
                    if (epic != null) {
                        simpleResponse(httpExchange, 200,
                                gson.toJson(epic));
                    } else {
                        msg(1, "handleEpic", "Задача с заданным идентификатором отсутствует.");
                        simpleResponse(httpExchange, 400,
                                "Задача с заданным идентификатором отсутствует.");
                    }
                }
            }
            break;

            case "DELETE": {
                if (taskManager.deleteEpic(id)) {
                    simpleResponse(httpExchange, 204, "");
                } else {
                    msg(1, "handleEpic", "Задача с заданным идентификатором не удалена.");
                    simpleResponse(httpExchange, 400,
                            "Задача с заданным идентификатором не удалена.");
                }
            }
            break;
        }
    }

    private void handleSubtask(HttpExchange httpExchange) throws IOException {
        Gson gson = createGson();

        Map<String, String> params = queryToMap(httpExchange.getRequestURI().getQuery());
        InputStream inputStream = httpExchange.getRequestBody();
        String body = new String(inputStream.readAllBytes(), DEFAULT_CHARSET);

        Integer id = null;
        //Проверка корректности идентификатора
        if (params.containsKey("id")) {
            try {
                id = Integer.parseInt(params.get("id"));
            } catch (NumberFormatException e) {
                msg(1, "handleSubtask", "Передан ошибочный идентификатор. Message: " + e.getMessage());
                simpleResponse(httpExchange, 400, "Передан ошибочный идентификатор");
                return;
            }
        }

        switch (httpExchange.getRequestMethod()) {
            case "POST": {
                Subtask subtask = gson.fromJson(body, Subtask.class);
                if (id == null) {
                    //создание записи, если нет параметра id
                    try {
                        subtask = taskManager.appendSubtask(subtask);
                        if (subtask != null) {
                            simpleResponse(httpExchange, 200,
                                    gson.toJson(subtask));
                        } else {
                            msg(1, "handleSubtask", "Не добавлено, проверьте корректность данных");
                            simpleResponse(httpExchange, 400,
                                    "Не добавлено, проверьте корректность данных");
                        }
                    } catch (TaskValidatorException e) {
                        msg(1, "handleSubtask", e.getMessage());
                        simpleResponse(httpExchange, 400,
                                "Не добавлено, эпик имеет пересечения во времени с другими эпиками");
                    }
                } else {
                    //обновление записи, если есть параметр id
                    try {
                        subtask.setID(id);
                        subtask = taskManager.updateSubtask(subtask);
                        if (subtask != null) {
                            simpleResponse(httpExchange, 200,
                                    gson.toJson(subtask));
                        } else {
                            simpleResponse(httpExchange, 400,
                                    "Не обновлено, проверьте корректность данных");
                        }
                    } catch (TaskValidatorException e) {
                        msg(1, "handleSubtask", e.getMessage());
                        simpleResponse(httpExchange, 400,
                                "Не добавлено, сабтаск имеет пересечения во времени с другими сабтасками");
                    }
                }
            }
            break;

            case "GET": {
                if (id == null) {
                    //получение всех записей, если нет параметра id
                    simpleResponse(httpExchange, 200,
                            gson.toJson(this.taskManager.getSubtasks()));
                } else {
                    //получение конкретной записи, если есть параметр id
                    Subtask subtask = taskManager.getSubtask(id);
                    if (subtask != null) {
                        simpleResponse(httpExchange, 200,
                                gson.toJson(subtask));
                    } else {
                        msg(1, "handleSubtask", "Задача с заданным идентификатором отсутствует.");
                        simpleResponse(httpExchange, 400,
                                "Сабтаск с заданным идентификатором отсутствует.");
                    }
                }
            }
            break;

            case "DELETE": {
                if (taskManager.deleteSubtask(id)) {
                    simpleResponse(httpExchange, 204, "");
                } else {
                    msg(1, "handleSubtask", "Задача с заданным идентификатором не удалена.");
                    simpleResponse(httpExchange, 400,
                            "Задача с заданным идентификатором не удалена.");
                }
            }
            break;
        }
    }

    private void handleEpicSubtasks(HttpExchange httpExchange) throws IOException {
        Gson gson = createGson();

        Map<String, String> params = queryToMap(httpExchange.getRequestURI().getQuery());
        Integer id = null;
        //Проверка корректности идентификатора
        if (params.containsKey("id")) {
            try {
                id = Integer.parseInt(params.get("id"));
            } catch (NumberFormatException e) {
                msg(1, "handleSubtask", "Передан ошибочный идентификатор. Message: " + e.getMessage());
                simpleResponse(httpExchange, 400, "Передан ошибочный идентификатор");
                return;
            }
        } else {
            msg(1, "handleSubtask", "Идентификатор не задан");
            simpleResponse(httpExchange, 400, "Идентификатор не задан");
            return;
        }

        List<Subtask> subtasks = taskManager.getEpicSubtasks(taskManager.getEpic(id));
        if (subtasks != null) {
            simpleResponse((httpExchange), 200, gson.toJson(subtasks));
        } else {
            msg(1, "handleSubtask", "Список сабтасков не получен.");
            simpleResponse(httpExchange, 400,
                    "Список сабтасков не получен.");
        }
    }

    private void handleAllTasks(HttpExchange httpExchange) throws IOException {
        if (httpExchange.getRequestMethod().equals("GET")) {
            Gson gson = createGson();
            simpleResponse(httpExchange, 200,
                    gson.toJson(this.taskManager.getAllTasks()));
        } else {
            msg(1, "handleAllTasks", "Использован неверный метод для вызова");
        }

    }

    private void handleHistory(HttpExchange httpExchange) throws IOException {
        if (httpExchange.getRequestMethod().equals("GET")) {
            Gson gson = createGson();
            simpleResponse(httpExchange, 200,
                    gson.toJson(this.taskManager.getHistory()));
        } else {
            msg(1, "handleAllTasks", "Использован неверный метод для вызова");
        }
    }

    public void stop() {
        httpServer.stop(1);
        msg(0, "stop", " остановлен!");
    }

}
