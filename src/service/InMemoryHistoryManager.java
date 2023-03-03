package service;

import model.Task;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class InMemoryHistoryManager implements HistoryManager {
    private final Map<Integer, Node> nodes = new HashMap<>();
    private Node head;
    private Node tail;

    //newstyle

    @Override
    public void add(Task task) {
        if (task==null) {
            //при попытке добавить пустую задачу выходим
            return;
        }
        remove(task.getId()); //удаляем из истории задачи по идентификатору для исключения повторов
        nodes.put(task.getId(), linkLast(task)); // добавляем задачу в связный список и хешмапу (ускорение поиска)
    }

    @Override
    public void remove(Integer id) {
        if (isEmpty()) {
            // Удаление возможно лишь в непустой истории
            return;
        }

        if (removeNode(nodes.get(id))) {
            nodes.remove(id);
        }
    }

    @Override
    public List<Task> getHistory() {
        return getTasks();
    }

    @Override
    public void clear() {
        head = null;
        tail = null;
        nodes.clear();
    }

    public boolean removeNode(Node node) {
        if (node == null) {
            //пустой элемент удалять нельзя
            return false;
        }

        //если удаляется не крайний элемент
        if ((node != head) && (node != tail)) {
            //выполняем взаимную перепривязку предыдущих и следующих элементов
            node.prev.next = node.next;
            node.next.prev = node.prev;
        }

        //если удаляется head
        if (head == node) {
            head = node.prev;
            if (head != null) {
                head.next = null;
            }
        }
        //если удаляется tail
        if (tail == node) {
            tail = node.next;
            if (tail != null) {
                tail.prev = null;
            }
        }

        return true;
    }

    public Node linkLast(Task task) {
        Node node = new Node(task);

        if (isEmpty()) {
            //если добавляется узел в пустой набор, он станет и хвостовым (и головным - далее)
            tail = node;
        } else {
            //если добавляется узел не первый, он устанавливает связи с головным, чтобы далее занять его место
            head.next = node;
            node.prev = head;
        }
        head = node; //вновь добавленный узел всегда становится головным
        return node;
    }

    public List<Task> getTasks() {
        ArrayList<Task> result = new ArrayList<>();

        //Обходим дерево через поле next, собираем задачи в ArrayList
        if (!isEmpty()) {
            Node node = this.tail;
            while (node != null) {
                result.add(node.task);
                node = node.next;
            }
        }
        return result;
    }

    public boolean isEmpty() {
        return this.head == null;
    }

    static class Node {

        public Node prev;
        public Node next;
        public Task task;

        public Node(Task task) {
            this.task = task;
        }

        @Override
        public int hashCode() {
            int hash = 15 * task.hashCode();
            if (prev != null) {
                hash += System.identityHashCode(prev);
            }
            if (next != null) {
                hash += System.identityHashCode(next);
            }
            return hash * 31;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Node node = (Node) o;
            return (task.equals(node.task)) &&
                    (next == node.next) &&
                    (prev == node.prev);
        }

        @Override
        public String toString() {
            return this.getClass().toString() + "{" +
                    "task.id='" + task.getId() + '\'' +
                    (prev == null ? (", prev=null" + '\'') : (", prev (task.id)='" + prev.task.getId())) + '\'' +
                    (next == null ? (", next=null" + '\'') : (", next (task.id)='" + next.task.getId())) + '\'' +
                    '}';
        }
    }
    @Override
    public String toString() {
        String result = this.getClass().toString()+": [";
            Node node = this.tail;
            while (node!=null) {
                result += node.task.getId();
                if (node!=this.head) {
                    result += ",";
                }
                node = node.next;
            }
        result += "]";
        return result;
    }
}
