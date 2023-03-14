package service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import model.Epic;
import model.Subtask;
import model.Task;
import service.adapters.DurationAdapter;
import service.adapters.LocalDateTimeAdapter;

public class HttpTaskManager extends FileBackedTasksManager {
    private final KVTaskClient kvTaskClient;

    public HttpTaskManager(String url) {
        super(null);
        this.kvTaskClient = new KVTaskClient(url);
        load();
    }

    private Gson createGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.serializeNulls();
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter());
        gsonBuilder.registerTypeAdapter(Duration.class, new DurationAdapter());
        gsonBuilder.setPrettyPrinting();
        return gsonBuilder.create();
    }

    @Override
    public void save() {
        Gson gson = createGson();
        kvTaskClient.put("tasks", gson.toJson(getTasks()));
        kvTaskClient.put("epics", gson.toJson(getEpics()));
        kvTaskClient.put("subtasks", gson.toJson(getSubtasks()));
        kvTaskClient.put("history", gson.toJson(getHistory()));
    }

    public void load() {
        Gson gson = createGson();
        List<Task> tasks = gson.fromJson(kvTaskClient.load("tasks"),
                new TypeToken<ArrayList<Task>>() {
                }.getType());
        if (tasks != null) {
            for (Task task : tasks) {
                this.importTask(task);
            }
        }
        List<Epic> epics = gson.fromJson(kvTaskClient.load("epics"),
                new TypeToken<ArrayList<Epic>>() {
                }.getType());
        if (epics != null) {
            for (Epic epic : epics) {
                this.importEpic(epic);
            }
        }
        List<Subtask> subtasks = gson.fromJson(kvTaskClient.load("subtask"),
                new TypeToken<ArrayList<Subtask>>() {
                }.getType());
        if (subtasks != null) {
            for (Subtask subtask : subtasks) {
                this.importSubtask(subtask);
            }
        }
        getHistoryManager().clear();
        List<Task> history = gson.fromJson(kvTaskClient.load("history"),
                new TypeToken<List<Task>>() {
                }.getType());
        if (history != null) {
            for (Task task : history) {
                getHistoryManager().add(task);
            }
        }
    }

}
