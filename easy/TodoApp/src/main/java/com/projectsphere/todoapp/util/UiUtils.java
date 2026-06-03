package com.projectsphere.todoapp.util;

import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.JButton;

public final class UiUtils {
    private UiUtils() {
    }

    public static JButton createGrayButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(new Color(225, 225, 225));
        button.setForeground(Color.DARK_GRAY);
        button.setOpaque(true);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        return button;
    }
}
