package model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public class Task {
    protected Integer id;
    protected String name;
    protected String description;
    protected TaskStatus status;
    protected Duration duration;
    protected LocalDateTime startTime;

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public Task(String name, String discription) {
        this.setName(name);
        this.setDiscription(discription);
        this.setID(-1);
        this.setStatus(TaskStatus.NEW);
    }

    public Task(String name, String discription, TaskStatus status, int id) {
        this(name, discription);
        this.setID(id);
        this.setStatus(status);
    }

    public Integer getId() {
        return id;
    }

    public void setID(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDiscription() {
        return description;
    }

    public void setDiscription(String discription) {
        this.description = discription;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public LocalDateTime getEndTime() {
        return startTime == null ? null : startTime.plus(duration);
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    protected String fieldsToString() {
        return "status='" + status.toString() + '\'' + // поле1=значение1
                ", id='" + id + '\'' + // поле1=значение1
                ", name='" + name + '\'' + // поле2=значение2
                ", description='" + description + '\'' + // поле3=значение3
                (startTime != null ? (", startTime='" + startTime + '\'') : "") +
                (duration != null ? (", duration='" + duration + '\'') : "");
    }

    @Override
    public String toString() {
        return this.getClass().toString() + "{" + // имя класса
                fieldsToString() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return Objects.equals(id, task.id)
                && Objects.equals(name, task.name)
                && Objects.equals(description, task.description)
                && status == task.status && Objects.equals(duration, task.duration)
                && Objects.equals(startTime, task.startTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, status, duration, startTime);
    }

    /*@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id.equals(task.id) && Objects.equals(name, task.name) &&
                Objects.equals(description, task.description) &&
                status == task.status &&
                startTime!=null?(startTime.equals(task.getStartTime())):(task.getStartTime()==null) &&
                duration!=null?(duration.equals(task.getDuration())):(task.getDuration()==null);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, status, startTime, duration);
    }*/
}
