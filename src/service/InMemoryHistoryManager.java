package service;

import model.Task;

import java.util.LinkedList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {

    final int MAX_SIZE_OF_HISTORY = 10;
    List<Task> history;

    public InMemoryHistoryManager() {
        history = new LinkedList<>();
    }

    @Override
    public void add(Task task) {
        history.add(task);
        if (history.size() > MAX_SIZE_OF_HISTORY) {
            history.remove(0);
        }
    }

    @Override
    public List<Task> getHistory() {
        return new LinkedList<>(history);
    }
}
