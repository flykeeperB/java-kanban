package service;

import model.*;

import java.util.List;

public interface HistoryManager {
    void add(Task task);

    void remove(Integer id);

    void clear();

    List<Task> getHistory();
}
