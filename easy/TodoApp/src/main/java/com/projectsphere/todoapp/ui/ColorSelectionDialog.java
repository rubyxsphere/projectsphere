package com.projectsphere.todoapp.ui;

import com.projectsphere.todoapp.TaskColor;
import com.projectsphere.todoapp.util.ColorUtility;
import com.projectsphere.todoapp.util.UiUtils;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ColorSelectionDialog extends JDialog {
    private TaskColor selection;
    private boolean confirmed = false;

    public ColorSelectionDialog(Frame owner, TaskColor current) {
        super(owner, "Select Color", true);
        this.selection = current;

        JPanel main = new JPanel(new BorderLayout(8, 8));
        main.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel grid = new JPanel(new GridLayout(0, 4, 8, 8));
        grid.setOpaque(true);

        // create swatches for each TaskColor
        for (TaskColor tc : TaskColor.values()) {
            JPanel swatch = createSwatch(tc);
            if (tc == current) {
                swatch.setBorder(new LineBorder(Color.BLACK, 3));
            }
            grid.add(swatch);
        }

        // 'No color' option
        JPanel none = new JPanel(new BorderLayout());
        none.setPreferredSize(new Dimension(40, 40));
        none.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        JLabel noneLabel = new JLabel("None", SwingConstants.CENTER);
        noneLabel.setFont(noneLabel.getFont().deriveFont(10f));
        none.add(noneLabel, BorderLayout.CENTER);
        none.setOpaque(true);
        none.setBackground(new Color(240, 240, 240));
        if (current == null) {
            none.setBorder(new LineBorder(Color.BLACK, 3));
        }
        none.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selection = null;
                updateBorders(grid, none);
                if (e.getClickCount() == 2) {
                    confirmed = true;
                    dispose();
                }
            }
        });
        grid.add(none);

        main.add(grid, BorderLayout.CENTER);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton ok = UiUtils.createGrayButton("OK");
        ok.addActionListener(e -> {
            confirmed = true;
            dispose();
        });
        JButton cancel = UiUtils.createGrayButton("Cancel");
        cancel.addActionListener(e -> {
            confirmed = false;
            dispose();
        });
        controls.add(ok);
        controls.add(cancel);

        main.add(controls, BorderLayout.SOUTH);

        setContentPane(main);
        pack();
        setResizable(false);
    }

    private JPanel createSwatch(TaskColor tc) {
        Color c = ColorUtility.getColorForTaskColor(tc);
        JPanel p = new JPanel();
        p.setPreferredSize(new Dimension(40, 40));
        p.setBackground(c);
        p.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        p.setOpaque(true);
        p.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selection = tc;
                updateBorders((JPanel) p.getParent(), null);
                if (e.getClickCount() == 2) {
                    confirmed = true;
                    dispose();
                }
            }
        });
        return p;
    }

    private void updateBorders(JPanel grid, JPanel nonePanel) {
        Component[] comps = grid.getComponents();
        for (Component comp : comps) {
            if (comp instanceof JPanel) {
                JPanel jp = (JPanel) comp;
                // mark selected
                if (jp == nonePanel && selection == null) {
                    jp.setBorder(new LineBorder(Color.BLACK, 3));
                } else if (jp != nonePanel && jp.getBackground() != null && selection != null
                        && jp.getBackground().equals(ColorUtility.getColorForTaskColor(selection))) {
                    jp.setBorder(new LineBorder(Color.BLACK, 3));
                } else {
                    jp.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
                }
            }
        }
    }

    public TaskColor getSelection() {
        return selection;
    }

    public boolean isConfirmed() {
        return confirmed;
    }
}
