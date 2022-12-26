package taskmanager;

import tasks.Task;

import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {

    List<Task> history;
    final int MAX_SIZE_OF_HISTORY = 10;

    public InMemoryHistoryManager(){
        history = new ArrayList<>();
    }

    @Override
    public void add(Task task){
        history.add(task);
        if (history.size()>MAX_SIZE_OF_HISTORY) {
            history.remove(0);
        }
    }

    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(history);
    }
}
