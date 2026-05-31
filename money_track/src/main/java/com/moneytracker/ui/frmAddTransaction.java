package com.moneytracker.ui;

import com.moneytracker.services.TransactionService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;

public class frmAddTransaction extends JPanel {
    private final TransactionService transactionService;
    private final Runnable onSaved;
    private final Runnable onClose;
    private final JTextField txtYear = new JTextField(8);
    private final JTextField txtMonth = new JTextField(8);
    private final JTextField txtCategory = new JTextField(14);
    private final JTextField txtAmount = new JTextField(8);

    public frmAddTransaction() {
        this(new TransactionService(), () -> {
        }, null);
    }

    public frmAddTransaction(TransactionService transactionService, Runnable onSaved) {
        this(transactionService, onSaved, null);
    }

    public frmAddTransaction(TransactionService transactionService, Runnable onSaved, Runnable onClose) {
        this.transactionService = transactionService;
        this.onSaved = onSaved;
        this.onClose = onClose;
        buildUi();
    }

    private void buildUi() {
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(28, 38, 28, 38));

        JLabel title = new JLabel("Add Transaction");
        title.setFont(new Font("Impact", Font.PLAIN, 42));
        add(title, BorderLayout.NORTH);

        LocalDate today = LocalDate.now();
        txtYear.setText(String.valueOf(today.getYear()));
        txtMonth.setText(String.valueOf(today.getMonthValue()));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
        c.fill = GridBagConstraints.HORIZONTAL;
        addRow(form, c, 0, "Year", txtYear);
        addRow(form, c, 1, "Month", txtMonth);
        addRow(form, c, 2, "Category", txtCategory);
        addRow(form, c, 3, "Amount", txtAmount);
        add(form, BorderLayout.CENTER);

        JButton save = new JButton("Save");
        JButton close = new JButton("Close");
        save.addActionListener(event -> saveTransaction());
        close.addActionListener(event -> closeWindow());
        JPanel buttons = new JPanel();
        buttons.add(save);
        buttons.add(close);
        add(buttons, BorderLayout.SOUTH);
    }

    private void addRow(JPanel form, GridBagConstraints c, int row, String label, JTextField field) {
        c.gridx = 0;
        c.gridy = row;
        form.add(new JLabel(label), c);
        c.gridx = 1;
        form.add(field, c);
    }

    private void saveTransaction() {
        try {
            double amount = Double.parseDouble(txtAmount.getText().trim());
            int month = Integer.parseInt(txtMonth.getText().trim());
            int year = Integer.parseInt(txtYear.getText().trim());
            String category = txtCategory.getText().trim();
            if (category.isEmpty()) {
                throw new IllegalArgumentException("Category cannot be empty");
            }
            String warning = transactionService.getBudgetWarning(amount, month, year);
            if (warning != null) {
                int answer = JOptionPane.showConfirmDialog(this, warning, "Budget warning", JOptionPane.YES_NO_OPTION);
                if (answer != JOptionPane.YES_OPTION) {
                    return;
                }
            }
            transactionService.addTransaction(amount, category, month, year);
            txtAmount.setText("");
            txtCategory.setText("");
            JOptionPane.showMessageDialog(this, "Transaction saved.");
            onSaved.run();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Transaction error", JOptionPane.ERROR_MESSAGE);
        }
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
