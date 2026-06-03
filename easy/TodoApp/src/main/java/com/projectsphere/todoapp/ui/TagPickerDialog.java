package com.projectsphere.todoapp.ui;

import com.projectsphere.todoapp.TaskManager;
import com.projectsphere.todoapp.util.UiUtils;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;

public class TagPickerDialog extends JDialog {
    private final TaskManager taskManager;
    private final DefaultListModel<String> model = new DefaultListModel<>();
    private final JList<String> tagList = new JList<>(model);
    private boolean confirmed = false;

    public TagPickerDialog(Frame owner, TaskManager taskManager, Set<String> initiallySelected) {
        super(owner, "Select Tags", true);
        this.taskManager = taskManager;
        setSize(420, 320);
        setLocationRelativeTo(owner);

        JPanel main = new JPanel(new BorderLayout(8,8));
        main.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        tagList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        refreshList();
        if (initiallySelected != null) {
            java.util.List<Integer> indices = new java.util.ArrayList<>();
            for (int i = 0; i < model.getSize(); i++) {
                String s = model.getElementAt(i);
                if (initiallySelected.contains(s)) indices.add(i);
            }
            int[] idx = indices.stream().mapToInt(Integer::intValue).toArray();
            tagList.setSelectedIndices(idx);
        }
        main.add(new JScrollPane(tagList), BorderLayout.CENTER);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        JTextField newTagField = new JTextField(16);
        JButton add = UiUtils.createGrayButton("Add Tag");
        add.addActionListener(e -> {
            String t = newTagField.getText().trim();
            if (!t.isEmpty()) {
                taskManager.createTag(t);
                newTagField.setText("");
                refreshList();
            }
        });
        top.add(new JLabel("New tag:"));
        top.add(newTagField);
        top.add(add);
        main.add(top, BorderLayout.NORTH);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 6));
        JButton ok = UiUtils.createGrayButton("OK");
        ok.addActionListener(e -> {
            confirmed = true;
            dispose();
        });
        JButton cancel = UiUtils.createGrayButton("Cancel");
        cancel.addActionListener(e -> dispose());
        bottom.add(ok);
        bottom.add(cancel);
        main.add(bottom, BorderLayout.SOUTH);

        setContentPane(main);
    }

    private void refreshList() {
        model.clear();
        Set<String> tags = taskManager.getAllTags();
        for (String t : tags) model.addElement(t);
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public Set<String> getSelection() {
        Set<String> out = new HashSet<>();
        for (String s : tagList.getSelectedValuesList()) out.add(s);
        return out;
    }
}
