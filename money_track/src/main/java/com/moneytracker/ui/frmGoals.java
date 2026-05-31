package com.moneytracker.ui;

import com.moneytracker.models.Goal;
import com.moneytracker.services.GoalService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Font;

public class frmGoals extends JPanel {
    private final GoalService goalService;
    private final Runnable onClose;
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Name", "Target", "Saved", "Progress"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(model);

    public frmGoals() {
        this(new GoalService(), null);
    }

    public frmGoals(GoalService goalService) {
        this(goalService, null);
    }

    public frmGoals(GoalService goalService, Runnable onClose) {
        this.goalService = goalService;
        this.onClose = onClose;
        buildUi();
        refresh();
    }

    private void buildUi() {
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        JLabel title = new JLabel("Goals");
        title.setFont(new Font("Impact", Font.PLAIN, 42));
        add(title, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton add = new JButton("Add Goal");
        JButton savings = new JButton("Add Savings");
        JButton delete = new JButton("Delete");
        JButton close = new JButton("Close");
        add.addActionListener(event -> addGoal());
        savings.addActionListener(event -> addSavings());
        delete.addActionListener(event -> deleteGoal());
        close.addActionListener(event -> closeWindow());

        JPanel buttons = new JPanel();
        buttons.add(add);
        buttons.add(savings);
        buttons.add(delete);
        buttons.add(close);
        add(buttons, BorderLayout.SOUTH);
    }

    public void refresh() {
        model.setRowCount(0);
        for (Goal goal : goalService.getAllGoals()) {
            model.addRow(new Object[]{
                    goal.getId(),
                    goal.getName(),
                    goal.getTargetAmount(),
                    goal.getSavedAmount(),
                    goal.getProgressPercent() + "%"
            });
        }
    }

    private void addGoal() {
        String name = JOptionPane.showInputDialog(this, "Goal name");
        String target = JOptionPane.showInputDialog(this, "Target amount");
        String saved = JOptionPane.showInputDialog(this, "Initial saved amount", "0");
        if (name == null || target == null || saved == null) {
            return;
        }
        try {
            goalService.addGoal(name, Double.parseDouble(target.trim()), Double.parseDouble(saved.trim()));
            refresh();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Goal error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addSavings() {
        Integer id = selectedId();
        if (id == null) {
            return;
        }
        String amount = JOptionPane.showInputDialog(this, "Amount to add");
        if (amount == null) {
            return;
        }
        try {
            goalService.addSavings(id, Double.parseDouble(amount.trim()));
            refresh();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Goal error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteGoal() {
        Integer id = selectedId();
        if (id == null) {
            return;
        }
        int answer = JOptionPane.showConfirmDialog(this, "Delete selected goal?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (answer == JOptionPane.YES_OPTION) {
            goalService.deleteGoal(id);
            refresh();
        }
    }

    private Integer selectedId() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a goal first.");
            return null;
        }
        return (Integer) model.getValueAt(table.convertRowIndexToModel(row), 0);
    }

    private void closeWindow() {
        if (onClose != null) {
            onClose.run();
            return;
        }
        java.awt.Window window = SwingUtilities.getWindowAncestor(this);
        if (window != null) {
            window.dispose();
        }
    }
}
