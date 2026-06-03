package com.projectsphere.todoapp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
    private final Path storageFile;
    private static final String STORAGE_FILE_NAME = "todoapp-data.json";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public TaskManager() {
        this(getDefaultStorageFile());
    }

    public TaskManager(Path storageFile) {
        this.storageFile = storageFile;
        for (TaskColor color : TaskColor.values()) {
            colorIndex.put(color, new HashSet<>());
        }
        loadFromFile(storageFile);
    }

    private static Path getDefaultStorageFile() {
        return Path.of(System.getProperty("user.dir")).resolve(STORAGE_FILE_NAME);
    }

    private void loadFromFile(Path file) {
        if (file == null || Files.notExists(file)) {
            return;
        }

        try {
            String json = Files.readString(file, StandardCharsets.UTF_8).trim();
            if (json.isEmpty()) {
                return;
            }

            Object parsed = JsonParser.parse(json);
            if (!(parsed instanceof Map<?, ?>)) {
                return;
            }

            Map<?, ?> rootMap = (Map<?, ?>) parsed;
            Object nextId = rootMap.get("nextTaskId");
            if (nextId instanceof Number) {
                nextTaskId = ((Number) nextId).intValue();
            } else if (nextId instanceof String) {
                nextTaskId = Integer.parseInt((String) nextId);
            }

            List<?> loadedTasks = castList(rootMap.get("tasks"));
            if (loadedTasks != null) {
                for (Object item : loadedTasks) {
                    if (!(item instanceof Map<?, ?>)) {
                        continue;
                    }

                    Map<?, ?> taskMap = (Map<?, ?>) item;
                    int taskId = ((Number) taskMap.get("taskId")).intValue();
                    String title = String.valueOf(taskMap.get("task"));
                    String createdTimeText = String.valueOf(taskMap.get("createdTime"));
                    Object dueTimeObject = taskMap.get("dueTime");
                    String dueTimeText = dueTimeObject == null || "null".equalsIgnoreCase(String.valueOf(dueTimeObject)) ? null : String.valueOf(dueTimeObject);
                    TaskStatus status = TaskStatus.valueOf(String.valueOf(taskMap.get("taskStatus")));
                    TaskColor color = taskMap.get("taskColor") == null || "null".equalsIgnoreCase(String.valueOf(taskMap.get("taskColor"))) ? null : TaskColor.valueOf(String.valueOf(taskMap.get("taskColor")));

                    LocalDateTime createdTime = LocalDateTime.parse(createdTimeText, DATE_TIME_FORMATTER);
                    LocalDateTime dueTime = dueTimeText == null ? null : LocalDateTime.parse(dueTimeText, DATE_TIME_FORMATTER);

                    Set<String> taskTags = new HashSet<>();
                    List<?> loadedTags = castList(taskMap.get("tags"));
                    if (loadedTags != null) {
                        for (Object tagObject : loadedTags) {
                            String normalized = normalizeTag(String.valueOf(tagObject));
                            if (normalized != null) {
                                taskTags.add(normalized);
                            }
                        }
                    }

                    Task task = new Task(taskId, title, createdTime, dueTime, status, color, taskTags);
                    tasksById.put(taskId, task);
                    indexTask(task);
                }
            }

            int maxTaskId = tasksById.keySet().stream().mapToInt(Integer::intValue).max().orElse(0);
            if (maxTaskId >= nextTaskId) {
                nextTaskId = maxTaskId + 1;
            }

            List<?> loadedGlobalTags = castList(rootMap.get("tags"));
            if (loadedGlobalTags != null) {
                for (Object tagObject : loadedGlobalTags) {
                    String normalized = normalizeTag(String.valueOf(tagObject));
                    if (normalized != null) {
                        tagIndex.computeIfAbsent(normalized, k -> new HashSet<>());
                    }
                }
            }
        } catch (IOException | RuntimeException e) {
            System.err.println("Unable to load todo data from " + file + ": " + e.getMessage());
        }
    }

    public void save() {
        persist();
    }

    private void saveToFile(Path file) throws IOException {
        Files.writeString(file, toJson(), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private void persist() {
        try {
            saveToFile(storageFile);
        } catch (IOException e) {
            System.err.println("Unable to save todo data to " + storageFile + ": " + e.getMessage());
        }
    }

    private String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        sb.append("\"nextTaskId\":").append(nextTaskId).append(',');
        sb.append("\"tasks\":[");

        boolean firstTask = true;
        for (Task task : tasksById.values()) {
            if (!firstTask) {
                sb.append(',');
            }
            firstTask = false;
            sb.append('{');
            sb.append("\"taskId\":").append(task.getTaskId()).append(',');
            sb.append("\"task\":").append(escapeJson(task.getTask())).append(',');
            sb.append("\"createdTime\":").append(escapeJson(DATE_TIME_FORMATTER.format(task.getCreatedTime()))).append(',');
            if (task.getDueTime() == null) {
                sb.append("\"dueTime\":null,");
            } else {
                sb.append("\"dueTime\":").append(escapeJson(DATE_TIME_FORMATTER.format(task.getDueTime()))).append(',');
            }
            sb.append("\"taskStatus\":").append(escapeJson(task.getTaskStatus().name())).append(',');
            if (task.getTaskColor() == null) {
                sb.append("\"taskColor\":null,");
            } else {
                sb.append("\"taskColor\":").append(escapeJson(task.getTaskColor().name())).append(',');
            }
            sb.append("\"tags\":[");
            boolean firstTag = true;
            for (String tag : task.getTags()) {
                if (!firstTag) {
                    sb.append(',');
                }
                firstTag = false;
                sb.append(escapeJson(tag));
            }
            sb.append(']');
            sb.append('}');
        }

        sb.append("],\"tags\":[");
        boolean firstTag = true;
        for (String tag : tagIndex.keySet()) {
            if (!firstTag) {
                sb.append(',');
            }
            firstTag = false;
            sb.append(escapeJson(tag));
        }
        sb.append(']');
        sb.append('}');
        return sb.toString();
    }

    private static String escapeJson(String value) {
        if (value == null) {
            return "null";
        }
        StringBuilder builder = new StringBuilder();
        builder.append('"');
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"' -> builder.append("\\\"");
                case '\\' -> builder.append("\\\\");
                case '\b' -> builder.append("\\b");
                case '\f' -> builder.append("\\f");
                case '\n' -> builder.append("\\n");
                case '\r' -> builder.append("\\r");
                case '\t' -> builder.append("\\t");
                default -> {
                    if (c < 0x20 || c > 0x7E) {
                        builder.append(String.format("\\u%04x", (int) c));
                    } else {
                        builder.append(c);
                    }
                }
            }
        }
        builder.append('"');
        return builder.toString();
    }

    @SuppressWarnings("unchecked")
    private static List<?> castList(Object value) {
        return value instanceof List<?> list ? list : null;
    }

    private static final class JsonParser {
        private final String text;
        private int pos;

        private JsonParser(String text) {
            this.text = text;
            this.pos = 0;
        }

        public static Object parse(String text) {
            JsonParser parser = new JsonParser(text);
            Object result = parser.parseValue();
            parser.skipWhitespace();
            return result;
        }

        private Object parseValue() {
            skipWhitespace();
            if (pos >= text.length()) {
                throw new IllegalArgumentException("Unexpected end of JSON text");
            }
            char c = text.charAt(pos);
            return switch (c) {
                case '{' -> parseObject();
                case '[' -> parseArray();
                case '"' -> parseString();
                case 't', 'f' -> parseBoolean();
                case 'n' -> parseNull();
                default -> parseNumber();
            };
        }

        private Map<String, Object> parseObject() {
            expect('{');
            Map<String, Object> values = new HashMap<>();
            skipWhitespace();
            if (peek() == '}') {
                next();
                return values;
            }
            while (true) {
                skipWhitespace();
                String key = parseString();
                skipWhitespace();
                expect(':');
                Object value = parseValue();
                values.put(key, value);
                skipWhitespace();
                if (peek() == ',') {
                    next();
                    continue;
                }
                break;
            }
            expect('}');
            return values;
        }

        private List<Object> parseArray() {
            expect('[');
            List<Object> values = new ArrayList<>();
            skipWhitespace();
            if (peek() == ']') {
                next();
                return values;
            }
            while (true) {
                Object value = parseValue();
                values.add(value);
                skipWhitespace();
                if (peek() == ',') {
                    next();
                    continue;
                }
                break;
            }
            expect(']');
            return values;
        }

        private String parseString() {
            expect('"');
            StringBuilder builder = new StringBuilder();
            while (pos < text.length()) {
                char c = next();
                if (c == '"') {
                    break;
                }
                if (c == '\\') {
                    char escaped = next();
                    switch (escaped) {
                        case '"' -> builder.append('"');
                        case '\\' -> builder.append('\\');
                        case '/' -> builder.append('/');
                        case 'b' -> builder.append('\b');
                        case 'f' -> builder.append('\f');
                        case 'n' -> builder.append('\n');
                        case 'r' -> builder.append('\r');
                        case 't' -> builder.append('\t');
                        case 'u' -> builder.append((char) Integer.parseInt(text.substring(pos, pos + 4), 16));
                        default -> builder.append(escaped);
                    }
                    continue;
                }
                builder.append(c);
            }
            return builder.toString();
        }

        private Object parseNumber() {
            int start = pos;
            if (peek() == '-') {
                pos++;
            }
            while (pos < text.length() && Character.isDigit(peek())) {
                pos++;
            }
            boolean isDecimal = false;
            if (peek() == '.') {
                isDecimal = true;
                pos++;
                while (pos < text.length() && Character.isDigit(peek())) {
                    pos++;
                }
            }
            if (peek() == 'e' || peek() == 'E') {
                isDecimal = true;
                pos++;
                if (peek() == '+' || peek() == '-') {
                    pos++;
                }
                while (pos < text.length() && Character.isDigit(peek())) {
                    pos++;
                }
            }
            String number = text.substring(start, pos);
            if (isDecimal) {
                return Double.parseDouble(number);
            }
            return Long.parseLong(number);
        }

        private Boolean parseBoolean() {
            if (text.startsWith("true", pos)) {
                pos += 4;
                return Boolean.TRUE;
            }
            if (text.startsWith("false", pos)) {
                pos += 5;
                return Boolean.FALSE;
            }
            throw new IllegalArgumentException("Invalid boolean value at position " + pos);
        }

        private Object parseNull() {
            if (text.startsWith("null", pos)) {
                pos += 4;
                return null;
            }
            throw new IllegalArgumentException("Invalid null value at position " + pos);
        }

        private void skipWhitespace() {
            while (pos < text.length() && Character.isWhitespace(text.charAt(pos))) {
                pos++;
            }
        }

        private char peek() {
            if (pos >= text.length()) {
                return '\0';
            }
            return text.charAt(pos);
        }

        private char next() {
            return text.charAt(pos++);
        }

        private void expect(char c) {
            if (peek() != c) {
                throw new IllegalArgumentException("Expected '" + c + "' at position " + pos);
            }
            pos++;
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
        persist();
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
        persist();
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
        persist();
    }

    // Remove a global tag if no tasks reference it. Returns true if removed.
    public boolean removeTag(String tag) {
        String normalized = normalizeTag(tag);
        if (normalized == null) return false;
        Set<Integer> ids = tagIndex.get(normalized);
        if (ids == null) return false;
        if (!ids.isEmpty()) return false;
        tagIndex.remove(normalized);
        persist();
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
            persist();
            return;
        }

        task.setTaskColor(newColor);
        colorIndex.get(newColor).add(taskId);
        persist();
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
        persist();
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
            }
        }
        persist();
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
                }
            }
        }
        for (String tag : task.getTags()) {
            tagIndex.computeIfAbsent(tag, k -> new HashSet<>()).add(taskId);
        }
        persist();
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
