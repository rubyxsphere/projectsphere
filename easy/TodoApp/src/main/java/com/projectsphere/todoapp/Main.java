package com.projectsphere.todoapp;

import java.time.LocalDateTime;
import java.util.Set;
import javax.swing.SwingUtilities;

public class Main {
    static TaskManager taskManager;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TodoAppWindow.getInstance();
        });
    }
}