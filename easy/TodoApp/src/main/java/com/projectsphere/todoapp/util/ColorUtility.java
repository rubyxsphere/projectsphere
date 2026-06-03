package com.projectsphere.todoapp.util;

import java.awt.Color;
import com.projectsphere.todoapp.TaskColor;

public class ColorUtility {
    public static Color getColorForTaskColor(TaskColor taskColor) {
        return switch (taskColor) {
            case RED -> new Color(255, 100, 100);
            case GREEN -> new Color(100, 255, 100);
            case BLUE -> new Color(100, 150, 255);
            case YELLOW -> new Color(255, 255, 100);
            case PINK -> new Color(255, 150, 200);
            case ORANGE -> new Color(255, 180, 100);
            case VIOLET -> new Color(200, 150, 255);
        };
    }
}
