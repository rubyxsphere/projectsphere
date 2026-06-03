package com.projectsphere.todoapp;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Task {
    private int taskId;
    private String task;
    private LocalDateTime createdTime;
    private LocalDateTime dueTime;
    private TaskStatus taskStatus;
    private TaskColor taskColor;
    private Set<String> tags = new HashSet<>();

    public Task(int taskId, String task, LocalDateTime createdTime, LocalDateTime dueTime, TaskStatus taskStatus, TaskColor taskColor) {
        this.taskId = taskId;
        this.task = task;
        this.createdTime = createdTime;
        this.dueTime = dueTime;
        this.taskStatus = taskStatus;
        this.taskColor = taskColor;
        this.tags = new HashSet<>();
    }

    public Task(int taskId, String task, LocalDateTime createdTime, LocalDateTime dueTime, TaskStatus taskStatus, TaskColor taskColor, Set<String> tags) {
        this(taskId, task, createdTime, dueTime, taskStatus, taskColor);
        setTags(tags);
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public LocalDateTime getDueTime() {
        return dueTime;
    }

    public void setDueTime(LocalDateTime dueTime) {
        this.dueTime = dueTime;
    }

    public TaskStatus getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(TaskStatus taskStatus) {
        this.taskStatus = taskStatus;
    }

    public TaskColor getTaskColor() {
        return taskColor;
    }

    public void setTaskColor(TaskColor taskColor) {
        this.taskColor = taskColor;
    }

    public Set<String> getTags() {
        return Collections.unmodifiableSet(tags);
    }

    public void setTags(Set<String> tags) {
        this.tags = tags == null ? new HashSet<>() : new HashSet<>(tags);
    }

    public void addTag(String tag) {
        if (tag != null && !tag.isBlank()) {
            this.tags.add(tag.trim().toLowerCase());
        }
    }

    public void removeTag(String tag) {
        if (tag != null) {
            this.tags.remove(tag.trim().toLowerCase());
        }
    }
}
