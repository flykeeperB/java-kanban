package model;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class Epic extends Task {
    protected ArrayList<Integer> subtaskIds = new ArrayList<>();

    protected LocalDateTime endTime;

    public Epic(String name, String discription) {
        super(name, discription);
    }

    public Epic(String name, String discription, TaskStatus status, int id) {
        super(name, discription, status, id);
        this.subtaskIds = new ArrayList<>();
    }

    public void clearSubtaskIds() {
        this.subtaskIds.clear();
    }

    public void addSubtaskIds(Integer subtaskId) {
        if (!this.subtaskIds.contains(subtaskId)) {
            this.subtaskIds.add(subtaskId);
        }
    }

    public void removeSubtaskIds(Integer subtaskId) {
        if (this.subtaskIds.contains(subtaskId)) {
            this.subtaskIds.remove(this.subtaskIds.indexOf(subtaskId));
        }
    }

    public void setSubtaskIds(ArrayList<Integer> ids) {
        subtaskIds.clear();
        subtaskIds.addAll(ids);
    }

    public ArrayList<Integer> getSubtaskIds() {
        return new ArrayList<>(this.subtaskIds);
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        return this.getClass().toString() + "{" + // имя класса
                super.fieldsToString() +
                (endTime != null ? (", endTime='" + endTime + '\'') : "") +
                ", subtaskIds=" + this.subtaskIds.toString() + // поле3=значение3
                '}';
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        for (Integer subtaskId : subtaskIds) {
            hash += subtaskId.hashCode();
        }
        return hash * 31;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Epic epic = (Epic) o;

        if (!super.equals(epic)) {
            return false;
        }

        return subtaskIds.equals(epic.subtaskIds);
    }

}
