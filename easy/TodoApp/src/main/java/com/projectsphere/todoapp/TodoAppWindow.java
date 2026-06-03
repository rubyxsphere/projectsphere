package com.projectsphere.todoapp;

import com.projectsphere.todoapp.ui.*;
import com.projectsphere.todoapp.util.UiUtils;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

public class TodoAppWindow {

    private static volatile TodoAppWindow instance;
    private final JFrame frame;
    private TaskManager taskManager;
    private JPanel tasksPanel;
    private JScrollPane tasksScrollPane;
    private JTextField searchField;
    private List<Task> currentFilteredTasks;
    private javax.swing.JLabel activeFiltersLabel;
    private boolean isFiltering = false;
    private java.util.Set<TaskColor> activeFilterColors =
        new java.util.HashSet<>();
    private java.util.Set<String> activeFilterTags = new java.util.HashSet<>();

    private TodoAppWindow() {
        taskManager = new TaskManager();
        currentFilteredTasks = new ArrayList<>();
        frame = createFrame();
        frame.setVisible(true);
    }

    public static TodoAppWindow getInstance() {
        if (instance == null) {
            synchronized (TodoAppWindow.class) {
                if (instance == null) {
                    instance = new TodoAppWindow();
                }
            }
        }
        return instance;
    }

    private JFrame createFrame() {
        JFrame frame = new JFrame();
        frame.setTitle("Just another TodoApp");
        frame.setUndecorated(true);
        // allow programmatic resizing; we'll add custom edge drag handlers
        frame.setResizable(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);

        Color background = new Color(255, 250, 209);
        frame.getContentPane().setBackground(background);
        frame.getContentPane().setLayout(new BorderLayout());
        frame
            .getContentPane()
            .add(createTitleBar(frame, background), BorderLayout.NORTH);
        frame
            .getContentPane()
            .add(createMainContent(background), BorderLayout.CENTER);
        frame
            .getRootPane()
            .setBorder(BorderFactory.createLineBorder(Color.GRAY));

        java.net.URL iconUrl = TodoAppWindow.class.getResource("/logo.png");
        if (iconUrl != null) {
            frame.setIconImage(new ImageIcon(iconUrl).getImage());
        } else {
            frame.setIconImage(new ImageIcon("src/logo.png").getImage());
        }

        frame.addWindowListener(
            new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    taskManager.save();
                }

                @Override
                public void windowClosed(WindowEvent e) {
                    synchronized (TodoAppWindow.class) {
                        instance = null;
                    }
                }
            }
        );
        return frame;
    }

    private JPanel createMainContent(Color background) {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(background);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        toolbar.setBackground(background);

        JButton createTaskButton = UiUtils.createGrayButton("+ Task");
        createTaskButton.addActionListener(e -> createNewTask());
        toolbar.add(createTaskButton);

        JButton createTagButton = UiUtils.createGrayButton("+ Tag");
        createTagButton.addActionListener(e -> {
            // open global tag editor allowing creation of tags
            TagEditorDialog dialog = new TagEditorDialog(frame, taskManager);
            dialog.setVisible(true);
        });
        toolbar.add(createTagButton);

        toolbar.add(Box.createHorizontalGlue());

        JLabel searchLabel = new JLabel("Search:");
        toolbar.add(searchLabel);
        searchField = new JTextField(15);
        toolbar.add(searchField);

        JButton searchButton = UiUtils.createGrayButton("Search");
        searchButton.addActionListener(e -> performSearch());
        toolbar.add(searchButton);

        JButton filterButton = UiUtils.createGrayButton("Filter");
        filterButton.addActionListener(e -> openFilterDialog());
        toolbar.add(filterButton);

        JButton clearButton = UiUtils.createGrayButton("Clear");
        clearButton.addActionListener(e -> clearFilters());
        toolbar.add(clearButton);

        // active filters label below toolbar
        activeFiltersLabel = new JLabel("");
        activeFiltersLabel.setForeground(Color.DARK_GRAY);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(background);
        topPanel.add(toolbar, BorderLayout.NORTH);
        topPanel.add(activeFiltersLabel, BorderLayout.SOUTH);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        // Tasks display area
        tasksPanel = new JPanel();
        tasksPanel.setLayout(
            new javax.swing.BoxLayout(tasksPanel, javax.swing.BoxLayout.Y_AXIS)
        );
        // use the same background as the frame so task panels appear seamless
        tasksPanel.setBackground(background);

        tasksScrollPane = new JScrollPane(tasksPanel);
        tasksScrollPane.getViewport().setBackground(background);
        mainPanel.add(tasksScrollPane, BorderLayout.CENTER);

        refreshTaskList();
        return mainPanel;
    }

    private void createNewTask() {
        TaskCreationDialog dialog = new TaskCreationDialog(frame, taskManager);
        dialog.setVisible(true);
        Task createdTask = dialog.getCreatedTask();
        if (createdTask != null) {
            refreshTaskList();
        }
    }

    private void showAvailableTags() {
        List<Task> allTasks = taskManager.getAllTasks();
        Set<String> allTags = new java.util.HashSet<>();
        for (Task task : allTasks) {
            allTags.addAll(task.getTags());
        }

        if (allTags.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(
                frame,
                "No tags available yet. Create tasks and add tags to them!"
            );
        } else {
            String tagsList = String.join("\n", allTags);
            javax.swing.JOptionPane.showMessageDialog(
                frame,
                "Available tags:\n" + tagsList,
                "All Tags",
                javax.swing.JOptionPane.INFORMATION_MESSAGE
            );
        }
    }

    private void performSearch() {
        String searchText = searchField.getText().trim().toLowerCase();
        if (searchText.isEmpty()) {
            refreshTaskList();
            return;
        }

        List<Task> allTasks = taskManager.getAllTasks();
        currentFilteredTasks = allTasks
            .stream()
            .filter(task -> task.getTask().toLowerCase().contains(searchText))
            .collect(Collectors.toList());
        updateTasksDisplay();
    }

    private void openFilterDialog() {
        List<Task> allTasks = taskManager.getAllTasks();
        // toggle: if currently filtering, clear filters and return to default
        if (isFiltering) {
            clearFilters();
            return;
        }

        FilterDialog dialog = new FilterDialog(frame, allTasks);
        dialog.setVisible(true);

        if (!dialog.isCancelled()) {
            Set<TaskColor> selectedColors = dialog.getSelectedColors();
            Set<String> selectedTags = dialog.getSelectedTags();

            // store active filters for visibility
            activeFilterColors.clear();
            activeFilterColors.addAll(selectedColors);
            activeFilterTags.clear();
            activeFilterTags.addAll(selectedTags);

            isFiltering = true;
            // build label text
            StringBuilder sb = new StringBuilder("Filtering by:");
            if (!activeFilterColors.isEmpty()) {
                sb.append(" colors: ");
                sb.append(
                    activeFilterColors
                        .stream()
                        .map(Enum::toString)
                        .collect(Collectors.joining(", "))
                );
            }
            if (!activeFilterTags.isEmpty()) {
                if (!activeFilterColors.isEmpty()) sb.append(";");
                sb.append(" tags: ");
                sb.append(String.join(", ", activeFilterTags));
            }
            activeFiltersLabel.setText(sb.toString());

            currentFilteredTasks = allTasks
                .stream()
                .filter(task -> {
                    boolean matchesColor =
                        selectedColors.isEmpty() ||
                        selectedColors.contains(task.getTaskColor());
                    boolean matchesTags =
                        selectedTags.isEmpty() ||
                        task
                            .getTags()
                            .stream()
                            .anyMatch(selectedTags::contains);
                    return (
                        matchesColor && (selectedTags.isEmpty() || matchesTags)
                    );
                })
                .collect(Collectors.toList());
            updateTasksDisplay();
        }
    }

    private void clearFilters() {
        searchField.setText("");
        // clear any active filter state too
        isFiltering = false;
        activeFilterColors.clear();
        activeFilterTags.clear();
        activeFiltersLabel.setText("");
        refreshTaskList();
    }

    private void refreshTaskList() {
        currentFilteredTasks = taskManager.getAllTasks();
        updateTasksDisplay();
    }

    private void updateTasksDisplay() {
        tasksPanel.removeAll();

        if (currentFilteredTasks.isEmpty()) {
            JLabel noTasksLabel = new JLabel("No tasks found");
            noTasksLabel.setForeground(Color.GRAY);
            tasksPanel.add(noTasksLabel);
        } else {
            for (Task task : currentFilteredTasks) {
                TaskPanel taskPanel = new TaskPanel(
                    task,
                    taskManager,
                    this::refreshTaskList,
                    this::refreshTaskList
                );
                tasksPanel.add(taskPanel);
                tasksPanel.add(Box.createVerticalStrut(5));
            }
        }

        tasksPanel.revalidate();
        tasksPanel.repaint();
    }

    private JPanel createTitleBar(JFrame frame, Color background) {
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setBackground(background);
        titleBar.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));

        JLabel title = new JLabel("Just another TodoApp");
        title.setHorizontalAlignment(SwingConstants.LEFT);
        title.setForeground(Color.DARK_GRAY);
        titleBar.add(title, BorderLayout.WEST);

        JButton minimize = createTitleButton(
            "—",
            background,
            new Color(230, 230, 230)
        );
        JButton close = createTitleButton(
            "X",
            background,
            new Color(232, 90, 80)
        );

        minimize.addActionListener(e -> frame.setState(Frame.ICONIFIED));
        close.addActionListener(e -> frame.dispose());

        JPanel controls = new JPanel();
        controls.setOpaque(false);
        controls.setLayout(
            new javax.swing.BoxLayout(controls, javax.swing.BoxLayout.X_AXIS)
        );
        controls.add(minimize);
        controls.add(Box.createRigidArea(new Dimension(10, 0)));
        controls.add(close);

        titleBar.add(controls, BorderLayout.EAST);

        addDragSupport(frame, titleBar);
        return titleBar;
    }

    private void addDragSupport(JFrame frame, JPanel titleBar) {
        final Point[] draggingPoint = { null };
        titleBar.addMouseListener(
            new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    draggingPoint[0] = e.getPoint();
                }
            }
        );
        titleBar.addMouseMotionListener(
            new MouseAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    Point location = frame.getLocation();
                    frame.setLocation(
                        location.x + e.getX() - draggingPoint[0].x,
                        location.y + e.getY() - draggingPoint[0].y
                    );
                }
            }
        );
    }

    private JButton createTitleButton(
        String text,
        Color background,
        Color hoverBackground
    ) {
        JButton button = new JButton(text);
        button.setFocusable(false);
        button.setPreferredSize(new Dimension(36, 28));
        button.setBackground(background);
        button.setForeground(Color.DARK_GRAY);
        button.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
        button.setFocusPainted(false);

        button.addMouseListener(
            new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    button.setBackground(hoverBackground);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    button.setBackground(background);
                }
            }
        );

        return button;
    }
}
