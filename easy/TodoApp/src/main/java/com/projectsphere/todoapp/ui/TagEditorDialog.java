package com.projectsphere.todoapp.ui;

import com.projectsphere.todoapp.TaskManager;
import com.projectsphere.todoapp.util.UiUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Set;

public class TagEditorDialog extends JDialog {
    private TaskManager taskManager;
    private DefaultListModel<String> listModel;
    private JList<String> tagList;
    private JTextField inputField;

    public TagEditorDialog(Frame owner, TaskManager taskManager) {
        super(owner, "Manage Tags", true);
        this.taskManager = taskManager;
        setSize(420, 320);
        setLocationRelativeTo(owner);

        JPanel main = new JPanel(new BorderLayout(8,8));
        main.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        listModel = new DefaultListModel<>();
        tagList = new JList<>(listModel);
        refreshList();
        main.add(new JScrollPane(tagList), BorderLayout.CENTER);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        inputField = new JTextField(16);
        JButton add = UiUtils.createGrayButton("Add Tag");
        add.addActionListener(e -> {
            String t = inputField.getText().trim();
            if (!t.isEmpty()) {
                taskManager.createTag(t);
                inputField.setText("");
                refreshList();
            }
        });
        top.add(new JLabel("New tag:"));
        top.add(inputField);
        top.add(add);
        main.add(top, BorderLayout.NORTH);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 6));
        JButton remove = UiUtils.createGrayButton("Remove Selected");
        remove.addActionListener(e -> {
            String selected = tagList.getSelectedValue();
            if (selected == null) return;
            boolean ok = taskManager.removeTag(selected);
            if (!ok) {
                JOptionPane.showMessageDialog(this, "Cannot remove tag while tasks reference it.", "Remove Tag", JOptionPane.WARNING_MESSAGE);
            }
            refreshList();
        });
        JButton close = UiUtils.createGrayButton("Close");
        close.addActionListener(e -> dispose());
        bottom.add(remove);
        bottom.add(close);
        main.add(bottom, BorderLayout.SOUTH);

        setContentPane(main);
    }

    private void refreshList() {
        listModel.clear();
        Set<String> tags = taskManager.getAllTags();
        for (String t : tags) listModel.addElement(t);
    }
}
