package com.projectsphere.todoapp;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class TaskManager {
    private final Map<Integer, Task> tasksById = new HashMap<>();
    private final EnumMap<TaskColor, Set<Integer>> colorIndex = new EnumMap<>(TaskColor.class);
    private final Map<String, Set<Integer>> tagIndex = new HashMap<>();
    private int nextTaskId = 1;

    public TaskManager() {
        for (TaskColor color : TaskColor.values()) {
            colorIndex.put(color, new HashSet<>());
        }
    }

    public void addTask(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null");
        }

        int taskId = allocateTaskId(task.getTaskId());
        task.setTaskId(taskId);

        Task existing = tasksById.put(taskId, task);
        if (existing != null) {
            deindexTask(existing);
        }
        indexTask(task);
    }

    public Task createTask(String task, LocalDateTime createdTime, LocalDateTime dueTime, TaskStatus taskStatus, TaskColor taskColor) {
        return createTask(task, createdTime, dueTime, taskStatus, taskColor, null);
    }

    public Task createTask(String task, LocalDateTime createdTime, LocalDateTime dueTime, TaskStatus taskStatus, TaskColor taskColor, Set<String> tags) {
        Task newTask = new Task(0, task, createdTime, dueTime, taskStatus, taskColor, tags);
        addTask(newTask);
        return newTask;
    }

    private int allocateTaskId(int requestedId) {
        int candidate = requestedId > 0 ? requestedId : nextTaskId;
        while (tasksById.containsKey(candidate)) {
            candidate++;
        }
        nextTaskId = candidate + 1;
        return candidate;
    }

    public boolean removeTask(int taskId) {
        Task removed = tasksById.remove(taskId);
        if (removed == null) {
            return false;
        }
        deindexTask(removed);
        return true;
    }

    public Task getTask(int taskId) {
        return tasksById.get(taskId);
    }

    public List<Task> getTasksByColor(TaskColor color) {
        Set<Integer> ids = colorIndex.get(color);
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return ids.stream()
                .map(tasksById::get)
                .filter(task -> task != null)
                .collect(Collectors.toList());
    }

    public List<Task> getTasksByTag(String tag) {
        String normalized = normalizeTag(tag);
        if (normalized == null) {
            return List.of();
        }
        Set<Integer> ids = tagIndex.get(normalized);
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return ids.stream()
                .map(tasksById::get)
                .filter(task -> task != null)
                .collect(Collectors.toList());
    }

    // Create a tag globally (no task assigned). If tag already exists, does nothing.
    public void createTag(String tag) {
        String normalized = normalizeTag(tag);
        if (normalized == null) return;
        tagIndex.computeIfAbsent(normalized, k -> new HashSet<>());
    }

    // Remove a global tag if no tasks reference it. Returns true if removed.
    public boolean removeTag(String tag) {
        String normalized = normalizeTag(tag);
        if (normalized == null) return false;
        Set<Integer> ids = tagIndex.get(normalized);
        if (ids == null) return false;
        if (!ids.isEmpty()) return false;
        tagIndex.remove(normalized);
        return true;
    }

    public Set<String> getAllTags() {
        return java.util.Collections.unmodifiableSet(tagIndex.keySet());
    }

    public void updateTaskColor(int taskId, TaskColor newColor) {
        Task task = tasksById.get(taskId);
        if (task == null) {
            return;
        }

        TaskColor previousColor = task.getTaskColor();
        if (previousColor == newColor) {
            return;
        }

        if (previousColor != null) {
            Set<Integer> previousSet = colorIndex.get(previousColor);
            if (previousSet != null) {
                previousSet.remove(taskId);
            }
        }

        // If newColor is null we simply clear the color mapping for this task.
        if (newColor == null) {
            task.setTaskColor(null);
            return;
        }

        task.setTaskColor(newColor);
        colorIndex.get(newColor).add(taskId);
    }

    public void addTagsToTask(int taskId, Set<String> tags) {
        Task task = tasksById.get(taskId);
        if (task == null || tags == null || tags.isEmpty()) {
            return;
        }

        for (String rawTag : tags) {
            String tag = normalizeTag(rawTag);
            if (tag == null || task.getTags().contains(tag)) {
                continue;
            }
            task.addTag(tag);
            tagIndex.computeIfAbsent(tag, k -> new HashSet<>()).add(taskId);
        }
    }

    public void removeTagsFromTask(int taskId, Set<String> tags) {
        Task task = tasksById.get(taskId);
        if (task == null || tags == null || tags.isEmpty()) {
            return;
        }

        for (String rawTag : tags) {
            String tag = normalizeTag(rawTag);
            if (tag == null || !task.getTags().contains(tag)) {
                continue;
            }
            task.removeTag(tag);
            Set<Integer> ids = tagIndex.get(tag);
            if (ids != null) {
                ids.remove(taskId);
                if (ids.isEmpty()) {
                    tagIndex.remove(tag);
                }
            }
        }
    }

    public void replaceTags(int taskId, Set<String> tags) {
        Task task = tasksById.get(taskId);
        if (task == null) {
            return;
        }

        Set<String> previous = Set.copyOf(task.getTags());
        task.setTags(tags);
        for (String rawTag : previous) {
            if (!task.getTags().contains(rawTag)) {
                Set<Integer> ids = tagIndex.get(rawTag);
                if (ids != null) {
                    ids.remove(taskId);
                    if (ids.isEmpty()) {
                        tagIndex.remove(rawTag);
                    }
                }
            }
        }
        for (String tag : task.getTags()) {
            tagIndex.computeIfAbsent(tag, k -> new HashSet<>()).add(taskId);
        }
    }

    private void indexTask(Task task) {
        if (task.getTaskColor() != null) {
            colorIndex.get(task.getTaskColor()).add(task.getTaskId());
        }

        for (String tag : task.getTags()) {
            tagIndex.computeIfAbsent(tag, k -> new HashSet<>()).add(task.getTaskId());
        }
    }

    private void deindexTask(Task task) {
        if (task.getTaskColor() != null) {
            Set<Integer> ids = colorIndex.get(task.getTaskColor());
            if (ids != null) {
                ids.remove(task.getTaskId());
            }
        }

        for (String tag : task.getTags()) {
            Set<Integer> ids = tagIndex.get(tag);
            if (ids != null) {
                ids.remove(task.getTaskId());
                if (ids.isEmpty()) {
                    tagIndex.remove(tag);
                }
            }
        }
    }

    private static String normalizeTag(String tag) {
        if (tag == null) {
            return null;
        }
        String normalized = tag.trim().toLowerCase();
        return normalized.isEmpty() ? null : normalized;
    }

    public List<Task> getAllTasks() {
        return new java.util.ArrayList<>(tasksById.values());
    }
}
