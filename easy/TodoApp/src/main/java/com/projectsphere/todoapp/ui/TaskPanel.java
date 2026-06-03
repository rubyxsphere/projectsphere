package com.projectsphere.todoapp.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.projectsphere.todoapp.Task;
import com.projectsphere.todoapp.TaskStatus;
import com.projectsphere.todoapp.TaskManager;
import com.projectsphere.todoapp.TaskColor;
import com.projectsphere.todoapp.util.ColorUtility;
import com.projectsphere.todoapp.util.UiUtils;

public class TaskPanel extends JPanel {
    private Task task;
    private TaskManager taskManager;
    private Runnable onTaskUpdated;
    private Runnable onTaskDeleted;

    public TaskPanel(Task task, TaskManager taskManager, Runnable onTaskUpdated, Runnable onTaskDeleted) {
        this.task = task;
        this.taskManager = taskManager;
        this.onTaskUpdated = onTaskUpdated;
        this.onTaskDeleted = onTaskDeleted;

        setLayout(new BorderLayout(10, 8));
        setPreferredSize(new Dimension(760, 72));
        // keep height fixed while allowing width to expand
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(210, 210, 210)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        setBackground(new Color(247, 249, 252));
        setOpaque(true);

        // Task info panel
        JPanel infoPanel = new JPanel(new BorderLayout(10, 6));
        infoPanel.setOpaque(false);

        // Task text
        JLabel taskLabel = new JLabel(task.getTask());
        taskLabel.setFont(taskLabel.getFont().deriveFont(Font.BOLD, 15f));
        taskLabel.setForeground(new Color(35, 35, 35));
        infoPanel.add(taskLabel, BorderLayout.NORTH);

        // Tags and status
        JPanel detailsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        detailsPanel.setOpaque(false);

        // Color indicator
        JPanel colorBox = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
                );
                int diameter = Math.min(getWidth(), getHeight()) - 2;
                int x = (getWidth() - diameter) / 2;
                int y = (getHeight() - diameter) / 2;
                if (task.getTaskColor() != null) {
                    g2.setColor(ColorUtility.getColorForTaskColor(task.getTaskColor()));
                    g2.fillOval(x, y, diameter, diameter);
                }
                g2.setColor(Color.DARK_GRAY);
                g2.drawOval(x, y, diameter, diameter);
                g2.dispose();
            }
        };
        colorBox.setOpaque(false);
        colorBox.setPreferredSize(new Dimension(18, 18));
        colorBox.setAlignmentY(CENTER_ALIGNMENT);
        detailsPanel.add(colorBox);

        // Status
        JLabel statusLabel = new JLabel("[" + task.getTaskStatus() + "]");
        statusLabel.setOpaque(true);
        statusLabel.setBackground(new Color(230, 236, 242));
        statusLabel.setForeground(new Color(70, 70, 90));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        statusLabel.setFont(statusLabel.getFont().deriveFont(11f));
        detailsPanel.add(statusLabel);

        // Tags
        if (!task.getTags().isEmpty()) {
            JLabel tagsLabel = new JLabel("Tags: " + String.join(", ", task.getTags()));
            tagsLabel.setOpaque(true);
            tagsLabel.setBackground(new Color(242, 243, 247));
            tagsLabel.setForeground(new Color(80, 80, 130));
            tagsLabel.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
            tagsLabel.setFont(tagsLabel.getFont().deriveFont(11f));
            detailsPanel.add(tagsLabel);
        }

        infoPanel.add(detailsPanel, BorderLayout.CENTER);
        add(infoPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setAlignmentY(CENTER_ALIGNMENT);

        JButton tagsButton = UiUtils.createGrayButton("Tags");
        tagsButton.addActionListener(e -> openTagDialog());
        buttonPanel.add(tagsButton);

        JButton colorButton = UiUtils.createGrayButton("Color");
        colorButton.addActionListener(e -> openColorSelector());
        buttonPanel.add(colorButton);

        JButton statusButton = UiUtils.createGrayButton("Status");
        statusButton.addActionListener(e -> openStatusSelector());
        buttonPanel.add(statusButton);

        JButton deleteButton = UiUtils.createGrayButton("Delete");
        deleteButton.setForeground(Color.RED);
        deleteButton.addActionListener(e -> deleteTask());
        buttonPanel.add(deleteButton);

        // wrap buttonPanel in a GridBagLayout panel so the buttons are vertically centered
        JPanel eastWrapper = new JPanel(new GridBagLayout());
        eastWrapper.setOpaque(false);
        eastWrapper.add(buttonPanel);
        add(eastWrapper, BorderLayout.EAST);
    }

    private void openTagDialog() {
        // This will be called from the parent, so we'll pass it up
        // For now, we'll handle it in a simple way
        java.awt.Window window = javax.swing.SwingUtilities.getWindowAncestor(this);
        if (window instanceof javax.swing.JFrame frame) {
            TagManagementDialog dialog = new TagManagementDialog(frame, task, taskManager);
            dialog.setVisible(true);
            if (onTaskUpdated != null) {
                onTaskUpdated.run();
            }
        }
    }

    private void openColorSelector() {
        Frame owner = (Frame) javax.swing.SwingUtilities.getWindowAncestor(this);
        ColorSelectionDialog dialog = new ColorSelectionDialog(owner, task.getTaskColor());
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            TaskColor newColor = dialog.getSelection();
            // allow clearing the color (null)
            taskManager.updateTaskColor(task.getTaskId(), newColor);
            task.setTaskColor(newColor);
            if (onTaskUpdated != null) {
                onTaskUpdated.run();
            }
        }
    }

    private void openStatusSelector() {
        TaskStatus[] statuses = TaskStatus.values();
        String[] statusNames = new String[statuses.length];
        for (int i = 0; i < statuses.length; i++) {
            statusNames[i] = statuses[i].toString();
        }

        String selected = (String) javax.swing.JOptionPane.showInputDialog(
            javax.swing.SwingUtilities.getWindowAncestor(this),
            "Select status:",
            "Choose Status",
            javax.swing.JOptionPane.PLAIN_MESSAGE,
            null,
            statusNames,
            task.getTaskStatus().toString()
        );

        if (selected != null) {
            TaskStatus newStatus = TaskStatus.valueOf(selected);
            task.setTaskStatus(newStatus);
            if (onTaskUpdated != null) {
                onTaskUpdated.run();
            }
        }
    }

    private void deleteTask() {
        int confirm = javax.swing.JOptionPane.showConfirmDialog(
            javax.swing.SwingUtilities.getWindowAncestor(this),
            "Delete task: " + task.getTask() + "?",
            "Confirm Delete",
            javax.swing.JOptionPane.YES_NO_OPTION
        );

        if (confirm == javax.swing.JOptionPane.YES_OPTION) {
            taskManager.removeTask(task.getTaskId());
            if (onTaskDeleted != null) {
                onTaskDeleted.run();
            }
        }
    }
}
