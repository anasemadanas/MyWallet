package com.moneytracker.ui;

import com.moneytracker.models.Transaction;
import com.moneytracker.services.TransactionService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Font;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class frmListTransaction extends JPanel {
    private final TransactionService transactionService;
    private final Runnable onChanged;
    private final Runnable onClose;
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Amount", "Category", "Month", "Year"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(model);

    public frmListTransaction() {
        this(new TransactionService(), () -> {
        }, null);
    }

    public frmListTransaction(TransactionService transactionService, Runnable onChanged) {
        this(transactionService, onChanged, null);
    }

    public frmListTransaction(TransactionService transactionService, Runnable onChanged, Runnable onClose) {
        this.transactionService = transactionService;
        this.onChanged = onChanged;
        this.onClose = onClose;
        buildUi();
        refresh();
    }

    private void buildUi() {
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        JLabel title = new JLabel("Transactions");
        title.setFont(new Font("Impact", Font.PLAIN, 42));
        add(title, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton refresh = new JButton("Refresh");
        JButton edit = new JButton("Edit");
        JButton delete = new JButton("Delete");
        JButton export = new JButton("Export");
        JButton close = new JButton("Close");
        refresh.addActionListener(event -> refresh());
        edit.addActionListener(event -> editSelected());
        delete.addActionListener(event -> deleteSelected());
        export.addActionListener(event -> exportCsv());
        close.addActionListener(event -> closeWindow());

        JPanel buttons = new JPanel();
        buttons.add(refresh);
        buttons.add(edit);
        buttons.add(delete);
        buttons.add(export);
        buttons.add(close);
        add(buttons, BorderLayout.SOUTH);
    }

    public void refresh() {
        model.setRowCount(0);
        for (Transaction transaction : transactionService.getTransactions()) {
            model.addRow(new Object[]{
                    transaction.getId(),
                    transaction.getAmount(),
                    transaction.getCategory(),
                    transaction.getMonth(),
                    transaction.getYear()
            });
        }
    }

    private Integer selectedId() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a transaction first.");
            return null;
        }
        return (Integer) model.getValueAt(table.convertRowIndexToModel(row), 0);
    }

    private void editSelected() {
        Integer id = selectedId();
        if (id == null) {
            return;
        }
        int row = table.convertRowIndexToModel(table.getSelectedRow());
        String amount = JOptionPane.showInputDialog(this, "New amount", model.getValueAt(row, 1));
        String month = JOptionPane.showInputDialog(this, "New month", model.getValueAt(row, 3));
        String year = JOptionPane.showInputDialog(this, "New year", model.getValueAt(row, 4));
        if (amount == null || month == null || year == null) {
            return;
        }
        try {
            transactionService.editTransaction(
                    id,
                    Double.parseDouble(amount.trim()),
                    Integer.parseInt(month.trim()),
                    Integer.parseInt(year.trim())
            );
            refresh();
            onChanged.run();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Edit error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelected() {
        Integer id = selectedId();
        if (id == null) {
            return;
        }
        int answer = JOptionPane.showConfirmDialog(this, "Delete selected transaction?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }
        transactionService.deleteTransaction(id);
        refresh();
        onChanged.run();
    }

    private void exportCsv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File("transactions.csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try (PrintWriter writer = new PrintWriter(new FileWriter(chooser.getSelectedFile()))) {
            writer.println("id,amount,category,month,year");
            for (Transaction transaction : transactionService.getTransactions()) {
                writer.printf("%d,%.2f,%s,%d,%d%n",
                        transaction.getId(),
                        transaction.getAmount(),
                        transaction.getCategory().replace(",", " "),
                        transaction.getMonth(),
                        transaction.getYear());
            }
            JOptionPane.showMessageDialog(this, "Transactions exported.");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Export error", JOptionPane.ERROR_MESSAGE);
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
