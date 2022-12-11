import java.util.ArrayList;
import java.util.HashMap;

public class TaskStorage {
    private HashMap<Task, HashMap<Integer, Task>> tasks;

    public TaskStorage() {
        this.tasks = new HashMap<>();
    }

    public void add(Task parent, Task task) {
        if (!tasks.containsKey(parent)) {
            tasks.put(parent, new HashMap<>());
        }
        tasks.get(parent).put(task.getId(), task);
    }

    public Task get(Integer id) {
        for (HashMap<Integer, Task> taskNode : this.tasks.values()) {
            if (taskNode.containsKey(id)) {
                return taskNode.get(id);
            }
        }
        return null;
    }

    public boolean remove(Integer id) {
        for (HashMap<Integer, Task> taskNode : this.tasks.values()) {
            if (taskNode.containsKey(id)) {
                taskNode.remove(id);
                return true;
            }
        }
        return false;
    }

    public boolean remove(Task task) {
        if (task == null) {
            return false;
        }
        return this.remove(task.getId());
    }

    public HashMap<Integer, Task> getNodeItems(Task parentTask) {
        return this.tasks.getOrDefault(parentTask, new HashMap<>());
    }

    public HashMap<Integer, Task> getNodeItems(Integer parentId) {
        if (!this.tasks.containsKey(parentId)) {
            return null; // если такого ключа нет, возвращаем null
        }
        return this.tasks.getOrDefault(this.get(parentId), new HashMap<>());
    }

    public ArrayList<Task> items() {
        ArrayList<Task> result = new ArrayList<>();
        for (HashMap<Integer, Task> taskNode : this.tasks.values()) {
            result.addAll(taskNode.values());
        }
        return result;
    }

    public ArrayList<Task> itemsOfClass(Class target) {
        ArrayList<Task> result = new ArrayList<>();
        for (HashMap<Integer, Task> taskNode : this.tasks.values()) {
            for (Task task : taskNode.values()) {
                if (task.getClass()==target) {
                    result.add(task);
                }
            }
        }
        return result;
    }

    public void clear() {
        this.tasks.clear();
    }

}
