package service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import model.Epic;
import model.Subtask;
import model.Task;

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

        httpServer.bind(socket, 10);
        httpServer.createContext("/tasks", new TasksHandler(taskManager));
        httpServer.start();

        System.out.println("HttpTaskServer запущен на " + PORT + " порту!");
    }

    static class TasksHandler implements HttpHandler {
        private final TaskManager taskManager;

        TasksHandler(TaskManager taskManager) {
            this.taskManager = taskManager;
        }

        public Map<String, String> queryToMap(String query) {
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

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.serializeNulls();
            gsonBuilder.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter());
            gsonBuilder.registerTypeAdapter(Duration.class, new DurationAdapter());
            gsonBuilder.setPrettyPrinting();
            Gson gson = gsonBuilder.create();

            String response = "";
            int responseCode = 200;

            //Получаем нужные данные запроса
            String requestMethod = httpExchange.getRequestMethod();
            Map<String, String> params = queryToMap(httpExchange.getRequestURI().getQuery());
            InputStream inputStream = httpExchange.getRequestBody();
            String body = new String(inputStream.readAllBytes(), DEFAULT_CHARSET);
            String requestPath = httpExchange.getRequestURI().getPath();
            String[] pathElements = requestPath.split("/");

            if (pathElements.length < 3) {
                if (requestMethod.equals("GET")) {
                    response = gson.toJson(this.taskManager.getAllTasks());
                }
            } else {
                StringBuilder stringBuilder = new StringBuilder(requestMethod);
                for (int i = 2; i < pathElements.length; i++) {
                    if (!pathElements[i].isEmpty()) {
                        stringBuilder.append("_");
                        stringBuilder.append(pathElements[i].toUpperCase());
                    }
                }

                //Идентификатор получаем отдельно, потому что он - частый параметр
                Integer id = null;
                if (params.containsKey("id")) {
                    if (!params.get("id").isEmpty()) {
                        id = Integer.parseInt(params.get("id"));
                    } else {
                        stringBuilder.append("_EMPTYID");
                    }
                }
                if (requestMethod.equals("POST")) {
                    if (id != null) {
                        stringBuilder.append("_UPDATE");
                    } else {
                        stringBuilder.append("_CREATE");
                    }
                }

                String enpointName = stringBuilder.toString();

                switch (enpointName) {

                    case "GET_HISTORY":
                        response = gson.toJson(taskManager.getHistory());
                        break;

                    case "POST_TASK_CREATE": {
                        Task task = gson.fromJson(body, Task.class);
                        try {
                            Task newTask = taskManager.appendTask(task);
                            if (newTask != null) {
                                response = gson.toJson(newTask);
                            } else {
                                responseCode = 400;
                            }
                        } catch (TaskValidatorException e) {
                            responseCode = 500;
                            response = e.getMessage();
                        }
                    }
                    break;

                    case "POST_TASK_UPDATE": {
                        Task task = gson.fromJson(body, Task.class);
                        try {
                            task.setID(id);
                            Task updatedTask = taskManager.updateTask(task);
                            if (updatedTask != null) {
                                response = gson.toJson(updatedTask);
                            } else {
                                responseCode = 400;
                            }
                        } catch (TaskValidatorException e) {
                            responseCode = 500;
                            response = e.getMessage();
                        }
                    }
                    break;

                    case "DELETE_TASK": {
                        if (taskManager.deleteTask(id)) {
                            responseCode = 204;
                        } else {
                            responseCode = 400;
                        }
                    }
                    break;

                    case "GET_TASK": {
                        if (id != null) {
                            //Если задан идентификатор, возвращаем конкретную задачу
                            Task task = taskManager.getTask(id);
                            if (task != null) {
                                response = gson.toJson(task);
                            } else {
                                responseCode = 400;
                            }
                        } else {
                            //Если не задан идентификатор, возвращаем все задачи
                            response = gson.toJson(this.taskManager.getTasks());
                        }
                    }
                    break;

                    case "POST_EPIC_CREATE": {
                        Epic epic = gson.fromJson(body, Epic.class);
                        try {
                            Epic newEpic = taskManager.appendEpic(epic);
                            if (newEpic != null) {
                                response = gson.toJson(newEpic);
                            } else {
                                responseCode = 400;
                            }
                        } catch (TaskValidatorException e) {
                            responseCode = 500;
                            response = e.getMessage();
                        }
                    }
                    break;

                    case "POST_EPIC_UPDATE": {
                        Epic epic = gson.fromJson(body, Epic.class);
                        try {
                            epic.setID(id);
                            Task updatedEpic = taskManager.updateEpic(epic);
                            if (updatedEpic != null) {
                                response = gson.toJson(updatedEpic);
                            } else {
                                responseCode = 400;
                            }
                        } catch (TaskValidatorException e) {
                            responseCode = 500;
                            response = e.getMessage();
                        }
                    }
                    break;

                    case "DELETE_EPIC": {
                        if (taskManager.deleteEpic(id)) {
                            responseCode = 204;
                        } else {
                            responseCode = 400;
                        }
                    }
                    break;

                    case "GET_EPIC": {
                        if (id != null) {
                            //Если задан идентификатор, возвращаем конкретный эпик
                            Epic epic = taskManager.getEpic(id);
                            if (epic != null) {
                                response = gson.toJson(epic);
                            } else {
                                responseCode = 400;
                            }
                        } else {
                            //Если не задан идентификатор, возвращаем все эпики
                            response = gson.toJson(this.taskManager.getTasks());
                        }
                    }
                    break;

                    case "GET_SUBTASK_EPIC": {
                        List<Subtask> subtasks = taskManager.getEpicSubtasks(taskManager.getEpic(id));
                        if (subtasks != null) {
                            response = gson.toJson(subtasks);
                        } else {
                            responseCode = 400;
                        }
                    }

                    case "POST_SUBTASK_CREATE": {
                        Subtask subtask = gson.fromJson(body, Subtask.class);
                        if (subtask != null) {
                            Epic epic = taskManager.getEpicOfSubtask(subtask);
                            if (epic != null) {
                                try {
                                    Subtask newSubtask = taskManager.appendSubtask(subtask);
                                    if (newSubtask != null) {
                                        response = gson.toJson(newSubtask);
                                    } else {
                                        responseCode = 400;
                                    }
                                } catch (TaskValidatorException e) {
                                    responseCode = 500;
                                    response = e.getMessage();
                                }
                            } else {
                                responseCode = 400;
                            }
                        } else {
                            responseCode = 400;
                        }
                    }
                    break;

                    case "POST_SUBTASK_UPDATE": {
                        Subtask subtask = gson.fromJson(body, Subtask.class);
                        if (subtask != null) {
                            Epic epic = taskManager.getEpicOfSubtask(subtask);
                            if (epic != null) {
                                try {
                                    Subtask newSubtask = taskManager.updateSubtask(subtask);
                                    if (newSubtask != null) {
                                        response = gson.toJson(newSubtask);
                                    } else {
                                        responseCode = 400;
                                    }
                                } catch (TaskValidatorException e) {
                                    responseCode = 500;
                                    response = e.getMessage();
                                }
                            }
                        }
                    }
                    break;

                    case "DELETE_SUBTASK": {
                        if (taskManager.deleteSubtask(id)) {
                            responseCode = 204;
                        } else {
                            responseCode = 400;
                        }
                    }
                    break;

                    case "GET_SUBTASK": {
                        if (id != null) {
                            //Если задан идентификатор, возвращаем конкретный сабтаск
                            Subtask subtask = taskManager.getSubtask(id);
                            if (subtask != null) {
                                response = gson.toJson(subtask);
                            } else {
                                responseCode = 400;
                            }
                        } else {
                            //Если не задан идентификатор, возвращаем все сабтаски
                            response = gson.toJson(taskManager.getSubtasks());
                        }
                    }
                    break;

                    default:
                        responseCode = 400;
                        response = "Неверный запрос.";
                }
            }

            httpExchange.sendResponseHeaders(responseCode, 0);

            try (OutputStream os = httpExchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }

    public void stop() {
        httpServer.stop(1);
    }

}
