package com.projectsphere.todoapp.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.SwingConstants;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.projectsphere.todoapp.Task;
import com.projectsphere.todoapp.TaskColor;
import com.projectsphere.todoapp.TaskManager;
import com.projectsphere.todoapp.TaskStatus;
import com.projectsphere.todoapp.util.ColorUtility;
import com.projectsphere.todoapp.util.UiUtils;

public class TaskCreationDialog extends JDialog {
    private JTextField taskTextField;
    private TaskColor selectedColor = null;
    private Task createdTask;

    public TaskCreationDialog(JFrame parent, TaskManager taskManager) {
        super(parent, "Create New Task", true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(520, 300);
        setLocationRelativeTo(parent);

        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel inputPanel = new JPanel(new BorderLayout(8,8));

        JPanel topRow = new JPanel(new GridLayout(2, 2, 10, 10));
        topRow.add(new JLabel("Task:"));
        taskTextField = new JTextField();
        topRow.add(taskTextField);

        topRow.add(new JLabel("Color:"));
        // panel with color swatches
        JPanel swatchPanel = new JPanel(new GridLayout(0, 5, 8, 8));
        for (TaskColor tc : TaskColor.values()) {
            JPanel sw = new JPanel();
            sw.setPreferredSize(new java.awt.Dimension(56, 36));
            sw.setBackground(ColorUtility.getColorForTaskColor(tc));
            sw.setBorder(BorderFactory.createLineBorder(java.awt.Color.DARK_GRAY));
            sw.setToolTipText(tc.toString());
            sw.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    selectedColor = tc;
                    // visually indicate selection by thick border
                    for (java.awt.Component c : swatchPanel.getComponents()) {
                        if (c instanceof JPanel) {
                            ((JPanel) c).setBorder(BorderFactory.createLineBorder(java.awt.Color.DARK_GRAY));
                        }
                    }
                    sw.setBorder(BorderFactory.createLineBorder(java.awt.Color.BLACK, 3));
                }
            });
            swatchPanel.add(sw);
        }
        JPanel none = new JPanel();
        none.setPreferredSize(new java.awt.Dimension(56, 36));
        none.setBorder(BorderFactory.createLineBorder(java.awt.Color.DARK_GRAY));
        none.add(new JLabel("None", SwingConstants.CENTER));
        none.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                selectedColor = null;
                for (java.awt.Component c : swatchPanel.getComponents()) {
                    if (c instanceof JPanel) {
                        ((JPanel) c).setBorder(BorderFactory.createLineBorder(java.awt.Color.DARK_GRAY));
                    }
                }
                none.setBorder(BorderFactory.createLineBorder(java.awt.Color.BLACK, 3));
            }
        });
        swatchPanel.add(none);

        topRow.add(swatchPanel);

        inputPanel.add(topRow, BorderLayout.NORTH);

        // Tags selection control: open a TagPickerDialog to choose tags
        JPanel pickTagsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        JLabel chosenTagsLabel = new JLabel("No tags selected");
        JButton pickTagsButton = UiUtils.createGrayButton("Select tags...");
        final Set<String> selectedTags = new HashSet<>();
        pickTagsButton.addActionListener(e -> {
            TagPickerDialog picker = new TagPickerDialog(parent, taskManager, selectedTags);
            picker.setVisible(true);
            if (picker.isConfirmed()) {
                selectedTags.clear();
                selectedTags.addAll(picker.getSelection());
                if (selectedTags.isEmpty()) chosenTagsLabel.setText("No tags selected");
                else chosenTagsLabel.setText(String.join(", ", selectedTags));
            }
        });
        pickTagsPanel.add(new JLabel("Tags:"));
        pickTagsPanel.add(chosenTagsLabel);
        pickTagsPanel.add(pickTagsButton);

        inputPanel.add(pickTagsPanel, BorderLayout.CENTER);

        contentPanel.add(inputPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton createButton = UiUtils.createGrayButton("Create");
        JButton cancelButton = UiUtils.createGrayButton("Cancel");

        createButton.addActionListener(e -> {
            String taskText = taskTextField.getText().trim();
            if (!taskText.isEmpty()) {
                createdTask = taskManager.createTask(
                    taskText,
                    LocalDateTime.now(),
                    null,
                    TaskStatus.NOT_STARTED,
                    selectedColor,
                    selectedTags.isEmpty() ? null : selectedTags
                );
                dispose();
            }
        });

        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(createButton);
        buttonPanel.add(cancelButton);

        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(contentPanel);
    }

    public Task getCreatedTask() {
        return createdTask;
    }
}
