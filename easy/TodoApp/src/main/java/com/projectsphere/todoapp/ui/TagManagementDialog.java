package com.projectsphere.todoapp.ui;

import com.projectsphere.todoapp.Task;
import com.projectsphere.todoapp.TaskManager;
import com.projectsphere.todoapp.util.UiUtils;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

public class TagManagementDialog extends JDialog {
    private JTextField tagTextField;
    private JList<String> tagList;
    private DefaultListModel<String> listModel;
    private Set<String> selectedTags;
    private JList<String> availableList;
    private DefaultListModel<String> availableModel;
    private boolean cancelled = false;

    public TagManagementDialog(JFrame parent, Task task, TaskManager taskManager) {
        super(parent, "Manage Tags for: " + task.getTask(), true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(400, 400);
        setLocationRelativeTo(parent);

        selectedTags = new HashSet<>(task.getTags());

        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Input panel
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        inputPanel.add(new JLabel("Add tag:"));
        tagTextField = new JTextField(15);
        inputPanel.add(tagTextField);
        
        JButton addButton = UiUtils.createGrayButton("Add");
        addButton.addActionListener(e -> addTag());
        inputPanel.add(addButton);

        contentPanel.add(inputPanel, BorderLayout.NORTH);

        // center area: left = task's tags, right = available global tags
        JPanel center = new JPanel(new java.awt.GridLayout(1, 2, 10, 0));

        listModel = new DefaultListModel<>();
        for (String tag : task.getTags()) {
            listModel.addElement(tag);
        }
        tagList = new JList<>(listModel);
        JScrollPane leftScroll = new JScrollPane(tagList);
        leftScroll.setBorder(BorderFactory.createTitledBorder("Task tags"));
        center.add(leftScroll);

        availableModel = new DefaultListModel<>();
        Set<String> globalTags = taskManager.getAllTags();
        for (String t : globalTags) {
            if (!selectedTags.contains(t)) availableModel.addElement(t);
        }
        availableList = new JList<>(availableModel);
        availableList.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane rightScroll = new JScrollPane(availableList);
        rightScroll.setBorder(BorderFactory.createTitledBorder("Available tags"));
        center.add(rightScroll);

        contentPanel.add(center, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        JButton removeButton = UiUtils.createGrayButton("Remove Selected");
        removeButton.addActionListener(e -> {
            int selectedIndex = tagList.getSelectedIndex();
            if (selectedIndex >= 0) {
                String tag = listModel.getElementAt(selectedIndex);
                selectedTags.remove(tag);
                listModel.remove(selectedIndex);
            }
        });
        buttonPanel.add(removeButton);

        JButton addFromAvailable = UiUtils.createGrayButton("Add Selected from Available");
        addFromAvailable.addActionListener(e -> {
            for (Object o : availableList.getSelectedValuesList()) {
                String tag = o.toString();
                if (!selectedTags.contains(tag)) {
                    selectedTags.add(tag);
                    listModel.addElement(tag);
                }
            }
            for (Object o : availableList.getSelectedValuesList()) availableModel.removeElement(o);
        });
        buttonPanel.add(addFromAvailable);

        JButton saveButton = UiUtils.createGrayButton("Save");
        saveButton.addActionListener(e -> {
            cancelled = false;
            taskManager.replaceTags(task.getTaskId(), selectedTags);
            dispose();
        });
        buttonPanel.add(saveButton);

        JButton cancelButton = UiUtils.createGrayButton("Cancel");
        cancelButton.addActionListener(e -> {
            cancelled = true;
            dispose();
        });
        buttonPanel.add(cancelButton);

        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(contentPanel);

        // save on close (unless cancelled via Cancel button)
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (!cancelled) {
                    taskManager.replaceTags(task.getTaskId(), selectedTags);
                }
            }
        });
    }

    private void addTag() {
        String tag = tagTextField.getText().trim().toLowerCase();
        if (!tag.isEmpty() && !selectedTags.contains(tag)) {
            selectedTags.add(tag);
            listModel.addElement(tag);
            tagTextField.setText("");
        }
    }
}
