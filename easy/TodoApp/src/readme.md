# TodoApp

A small Java Swing todo application with color, status, tag, and filter support.

What it does:

- Opens a desktop todo list UI
- Lets you create tasks with due dates, status, color, and tags
- Filters tasks by selected colors and tags
- Supports search, task creation, tag management, and task display

The purpose of me building this tool: to show I can build a simple interactive desktop app in Java from scratch.

# Required tools (Windows)

    JDK 8 or newer

# Instructions

From the project root directory:

    javac -d out src/main/java/com/projectsphere/todoapp/*.java src/main/java/com/projectsphere/todoapp/ui/*.java src/main/java/com/projectsphere/todoapp/util/*.java
    java -cp out com.projectsphere.todoapp.Main
