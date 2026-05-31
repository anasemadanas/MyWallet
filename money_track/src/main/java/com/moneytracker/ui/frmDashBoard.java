package com.moneytracker.ui;

import com.moneytracker.models.Budget;
import com.moneytracker.models.Transaction;
import com.moneytracker.models.User;
import com.moneytracker.services.BudgetService;
import com.moneytracker.services.DashBoardService;
import com.moneytracker.services.GoalService;
import com.moneytracker.services.TransactionService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSplitPane;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.util.List;

public class frmDashBoard extends JFrame {
    private final User user;
    private final BudgetService budgetService;
    private final TransactionService transactionService;
    private final GoalService goalService;
    private final DashBoardService dashBoardService;
    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final JLabel lblTotal = new JLabel("Total: 0.00");
    private final JLabel lblRemaining = new JLabel("Remaining: 0.00");
    private final JLabel lblSpent = new JLabel("Spent: 0.00");
    private final JLabel lblTransactions = new JLabel("Transactions: 0");
    private final JProgressBar spentBar = new JProgressBar(0, 100);
    private final JProgressBar savedBar = new JProgressBar(0, 100);
    private frmGoals goalsPanel;
    private frmListTransaction listPanel;

    public frmDashBoard() {
        this(new User("guest"));
    }

    public frmDashBoard(User user) {
        super("MyWallet Dashboard");
        this.user = user;
        this.budgetService = new BudgetService(user.getId());
        this.transactionService = new TransactionService(user.getId());
        this.goalService = new GoalService(user.getId());
        this.dashBoardService = new DashBoardService(user.getId());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(980, 640);
        setLocationRelativeTo(null);
        buildUi();
        refreshDashboard();
    }

    private void buildUi() {
        setLayout(new BorderLayout());
        add(buildHeader(), BorderLayout.NORTH);

        JPanel navigation = new JPanel(new GridLayout(0, 1, 8, 8));
        navigation.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        JButton dashboard = new JButton("Dashboard");
        JButton budget = new JButton("Add Budget");
        JButton transaction = new JButton("Add Transaction");
        JButton goals = new JButton("Goals");
        JButton list = new JButton("Transactions");
        JButton logout = new JButton("Logout");

        dashboard.addActionListener(event -> showDashboard());
        budget.addActionListener(event -> showCard("budget"));
        transaction.addActionListener(event -> showCard("transaction"));
        goals.addActionListener(event -> {
            goalsPanel.refresh();
            showCard("goals");
        });
        list.addActionListener(event -> {
            listPanel.refresh();
            showCard("list");
        });
        logout.addActionListener(event -> logout());

        navigation.add(dashboard);
        navigation.add(budget);
        navigation.add(transaction);
        navigation.add(goals);
        navigation.add(list);
        navigation.add(logout);

        goalsPanel = new frmGoals(goalService, this::showDashboard);
        listPanel = new frmListTransaction(transactionService, this::refreshDashboard, this::showDashboard);

        content.add(buildDashboardPanel(), "dashboard");
        content.add(new frmAddBudget(budgetService, this::refreshDashboard, this::showDashboard), "budget");
        content.add(new frmAddTransaction(transactionService, this::refreshDashboard, this::showDashboard), "transaction");
        content.add(goalsPanel, "goals");
        content.add(listPanel, "list");

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, navigation, content);
        splitPane.setDividerLocation(190);
        splitPane.setResizeWeight(0);
        add(splitPane, BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));
        JLabel title = new JLabel("MyWallet");
        title.setFont(new Font("Impact", Font.PLAIN, 44));
        title.setForeground(new Color(0, 45, 130));
        JLabel userLabel = new JLabel("Logged in as " + user.getUsername(), SwingConstants.RIGHT);
        header.add(title, BorderLayout.WEST);
        header.add(userLabel, BorderLayout.EAST);
        return header;
    }

    private JPanel buildDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(16, 16));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 36, 30, 36));

        JLabel title = new JLabel("Dashboard");
        title.setFont(new Font("Impact", Font.PLAIN, 42));
        panel.add(title, BorderLayout.NORTH);

        JPanel stats = new JPanel(new GridLayout(2, 2, 16, 16));
        stats.add(metricPanel(lblTotal));
        stats.add(metricPanel(lblRemaining));
        stats.add(metricPanel(lblSpent));
        stats.add(metricPanel(lblTransactions));
        panel.add(stats, BorderLayout.CENTER);

        JPanel bars = new JPanel(new GridLayout(2, 1, 8, 8));
        spentBar.setStringPainted(true);
        savedBar.setStringPainted(true);
        bars.add(spentBar);
        bars.add(savedBar);
        panel.add(bars, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel metricPanel(JLabel label) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 215, 224)),
                BorderFactory.createEmptyBorder(22, 22, 22, 22)
        ));
        label.setFont(new Font("Segoe UI", Font.BOLD, 22));
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    private void showDashboard() {
        refreshDashboard();
        showCard("dashboard");
    }

    private void showCard(String name) {
        cards.show(content, name);
    }

    private void refreshDashboard() {
        LocalDate today = LocalDate.now();
        Budget budget = budgetService.getBudget(today.getMonthValue(), today.getYear());
        List<Transaction> transactions = dashBoardService.getTransactionsForMonth(today.getMonthValue(), today.getYear());
        double spent = Math.max(0, budget.getTotalAmount() - budget.getAmount());
        int spentPercent = budget.getTotalAmount() <= 0 ? 0 : Math.min(100, (int) ((spent / budget.getTotalAmount()) * 100));
        int savePercent = budget.getTotalAmount() <= 0 ? 0 : Math.min(100, (int) ((budget.getAmount() / budget.getTotalAmount()) * 100));

        lblTotal.setText(String.format("Total: %.2f", budget.getTotalAmount()));
        lblRemaining.setText(String.format("Remaining: %.2f", budget.getAmount()));
        lblSpent.setText(String.format("Spent: %.2f", spent));
        lblTransactions.setText("Transactions: " + transactions.size());
        spentBar.setValue(spentPercent);
        spentBar.setString("Expense: " + spentPercent + "%");
        savedBar.setValue(savePercent);
        savedBar.setString("Saving: " + savePercent + "%");
    }

    private void logout() {
        int answer = JOptionPane.showConfirmDialog(this, "Logout now?", "Logout", JOptionPane.YES_NO_OPTION);
        if (answer == JOptionPane.YES_OPTION) {
            new frmLoginScreen().setVisible(true);
            dispose();
        }
    }

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> new frmDashBoard().setVisible(true));
    }
}
