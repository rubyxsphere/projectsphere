package com.projectsphere.todoapp.ui;

import com.projectsphere.todoapp.Task;
import com.projectsphere.todoapp.TaskColor;
import com.projectsphere.todoapp.util.UiUtils;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

public class FilterDialog extends JDialog {
    private Set<TaskColor> selectedColors = new HashSet<>();
    private Set<String> selectedTags = new HashSet<>();
    private boolean cancelled = true;

    public FilterDialog(JFrame parent, List<Task> allTasks) {
        super(parent, "Filter Tasks", true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(400, 500);
        setLocationRelativeTo(parent);

        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JTabbedPane tabbedPane = new JTabbedPane();

        // Color filter tab
        JPanel colorPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        colorPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        for (TaskColor color : TaskColor.values()) {
            JCheckBox checkbox = new JCheckBox(color.toString());
            checkbox.addActionListener(e -> {
                if (checkbox.isSelected()) {
                    selectedColors.add(color);
                } else {
                    selectedColors.remove(color);
                }
            });
            colorPanel.add(checkbox);
        }
        tabbedPane.addTab("Filter by Color", new JScrollPane(colorPanel));

        // Tag filter tab
        JPanel tagPanel = new JPanel(new BorderLayout(10, 10));
        tagPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Get all unique tags from tasks
        Set<String> allTags = new HashSet<>();
        for (Task task : allTasks) {
            allTags.addAll(task.getTags());
        }

        DefaultListModel<String> tagListModel = new DefaultListModel<>();
        for (String tag : allTags) {
            tagListModel.addElement(tag);
        }
        JList<String> tagList = new JList<>(tagListModel);
        tagList.addListSelectionListener(e -> {
            selectedTags.clear();
            for (int index : tagList.getSelectedIndices()) {
                selectedTags.add(tagListModel.getElementAt(index));
            }
        });

        tagPanel.add(new JLabel("Select tags to filter:"), BorderLayout.NORTH);
        tagPanel.add(new JScrollPane(tagList), BorderLayout.CENTER);
        
        tabbedPane.addTab("Filter by Tag", tagPanel);

        contentPanel.add(tabbedPane, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel();
        JButton applyButton = UiUtils.createGrayButton("Apply");
        applyButton.addActionListener(e -> {
            cancelled = false;
            dispose();
        });
        buttonPanel.add(applyButton);

        JButton cancelButton = UiUtils.createGrayButton("Cancel");
        cancelButton.addActionListener(e -> {
            cancelled = true;
            dispose();
        });
        buttonPanel.add(cancelButton);

        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(contentPanel);
    }

    public Set<TaskColor> getSelectedColors() {
        return selectedColors;
    }

    public Set<String> getSelectedTags() {
        return selectedTags;
    }

    public boolean isCancelled() {
        return cancelled;
    }
}
