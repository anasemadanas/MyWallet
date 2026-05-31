package com.mywallet.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.List;

public class MainActivity extends Activity {
    private WalletDatabase database;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = new WalletDatabase(this);
        showLogin();
    }

    private void showLogin() {
        LinearLayout root = baseLayout();
        root.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView title = title("MyWallet");
        root.addView(title);

        EditText username = input("Username", InputType.TYPE_CLASS_TEXT);
        EditText password = input("Password", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        root.addView(username);
        root.addView(password);

        Button login = button("Login");
        Button create = button("Create User");
        Button forgot = button("Forgot Password");
        root.addView(login);
        root.addView(create);
        root.addView(forgot);

        login.setOnClickListener(v -> {
            User user = database.login(text(username), text(password));
            if (user == null) {
                toast("Invalid username or password");
                return;
            }
            currentUser = user;
            showDashboard();
        });
        create.setOnClickListener(v -> showCreateUserDialog());
        forgot.setOnClickListener(v -> showForgotPasswordDialog());

        setContentView(scroll(root));
    }

    private void showDashboard() {
        LinearLayout root = baseLayout();
        root.addView(title("Dashboard"));
        root.addView(label("Logged in as " + currentUser.username));

        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR);
        Budget budget = database.getBudget(currentUser.id, month, year);
        List<WalletTransaction> transactions = database.getTransactions(currentUser.id);
        double spent = Math.max(0, budget.totalAmount - budget.amount);
        int expensePercent = budget.totalAmount <= 0 ? 0 : Math.min(100, (int) ((spent / budget.totalAmount) * 100));
        int savingPercent = budget.totalAmount <= 0 ? 0 : Math.min(100, (int) ((budget.amount / budget.totalAmount) * 100));

        root.addView(metric("This month total", money(budget.totalAmount)));
        root.addView(metric("Remaining", money(budget.amount)));
        root.addView(metric("Spent", money(spent)));
        root.addView(metric("Transactions", String.valueOf(transactions.size())));
        root.addView(progress("Expense " + expensePercent + "%", expensePercent));
        root.addView(progress("Saving " + savingPercent + "%", savingPercent));

        Button addBudget = button("Add Budget");
        Button addTransaction = button("Add Transaction");
        Button listTransactions = button("Transactions");
        Button goals = button("Goals");
        Button logout = button("Logout");
        root.addView(addBudget);
        root.addView(addTransaction);
        root.addView(listTransactions);
        root.addView(goals);
        root.addView(logout);

        addBudget.setOnClickListener(v -> showAddBudget());
        addTransaction.setOnClickListener(v -> showAddTransaction());
        listTransactions.setOnClickListener(v -> showTransactions());
        goals.setOnClickListener(v -> showGoals());
        logout.setOnClickListener(v -> {
            currentUser = null;
            showLogin();
        });

        setContentView(scroll(root));
    }

    private void showAddBudget() {
        LinearLayout root = baseLayout();
        root.addView(title("Add Budget"));

        Calendar calendar = Calendar.getInstance();
        EditText year = input("Year", InputType.TYPE_CLASS_NUMBER);
        EditText month = input("Month", InputType.TYPE_CLASS_NUMBER);
        EditText amount = input("Amount", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        year.setText(String.valueOf(calendar.get(Calendar.YEAR)));
        month.setText(String.valueOf(calendar.get(Calendar.MONTH) + 1));
        root.addView(year);
        root.addView(month);
        root.addView(amount);

        Button save = button("Save Budget");
        Button back = button("Back");
        root.addView(save);
        root.addView(back);

        save.setOnClickListener(v -> {
            try {
                double parsedAmount = parseAmount(amount);
                int parsedMonth = parseMonth(month);
                int parsedYear = parseYear(year);
                database.addBudget(currentUser.id, parsedAmount, parsedMonth, parsedYear);
                toast("Budget saved");
                showDashboard();
            } catch (RuntimeException ex) {
                toast(ex.getMessage());
            }
        });
        back.setOnClickListener(v -> showDashboard());
        setContentView(scroll(root));
    }

    private void showAddTransaction() {
        LinearLayout root = baseLayout();
        root.addView(title("Add Transaction"));

        Calendar calendar = Calendar.getInstance();
        EditText year = input("Year", InputType.TYPE_CLASS_NUMBER);
        EditText month = input("Month", InputType.TYPE_CLASS_NUMBER);
        EditText category = input("Category", InputType.TYPE_CLASS_TEXT);
        EditText amount = input("Amount", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        year.setText(String.valueOf(calendar.get(Calendar.YEAR)));
        month.setText(String.valueOf(calendar.get(Calendar.MONTH) + 1));
        root.addView(year);
        root.addView(month);
        root.addView(category);
        root.addView(amount);

        Button save = button("Save Transaction");
        Button back = button("Back");
        root.addView(save);
        root.addView(back);

        save.setOnClickListener(v -> {
            try {
                String parsedCategory = text(category);
                if (parsedCategory.isEmpty()) {
                    throw new IllegalArgumentException("Category is required");
                }
                database.addTransaction(currentUser.id, parseAmount(amount), parsedCategory, parseMonth(month), parseYear(year));
                toast("Transaction saved");
                showDashboard();
            } catch (RuntimeException ex) {
                toast(ex.getMessage());
            }
        });
        back.setOnClickListener(v -> showDashboard());
        setContentView(scroll(root));
    }

    private void showTransactions() {
        LinearLayout root = baseLayout();
        root.addView(title("Transactions"));
        List<WalletTransaction> transactions = database.getTransactions(currentUser.id);
        ArrayAdapter<WalletTransaction> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, transactions);
        ListView list = new ListView(this);
        list.setAdapter(adapter);
        list.setOnItemLongClickListener((parent, view, position, id) -> {
            WalletTransaction transaction = transactions.get(position);
            new AlertDialog.Builder(this)
                    .setTitle("Delete transaction")
                    .setMessage(transaction.toString())
                    .setPositiveButton("Delete", (dialog, which) -> {
                        database.deleteTransaction(currentUser.id, transaction);
                        showTransactions();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            return true;
        });
        root.addView(list, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));
        Button back = button("Back");
        root.addView(back);
        back.setOnClickListener(v -> showDashboard());
        setContentView(root);
    }

    private void showGoals() {
        LinearLayout root = baseLayout();
        root.addView(title("Goals"));
        Button add = button("Add Goal");
        root.addView(add);

        List<Goal> goals = database.getGoals(currentUser.id);
        ArrayAdapter<Goal> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, goals);
        ListView list = new ListView(this);
        list.setAdapter(adapter);
        list.setOnItemClickListener((parent, view, position, id) -> showGoalActions(goals.get(position)));
        root.addView(list, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));

        Button back = button("Back");
        root.addView(back);
        add.setOnClickListener(v -> showAddGoalDialog());
        back.setOnClickListener(v -> showDashboard());
        setContentView(root);
    }

    private void showCreateUserDialog() {
        EditText username = input("Username", InputType.TYPE_CLASS_TEXT);
        EditText password = input("Password", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        EditText confirm = input("Confirm Password", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        EditText recovery = input("Recovery Answer", InputType.TYPE_CLASS_TEXT);
        LinearLayout form = form(username, password, confirm, recovery);
        new AlertDialog.Builder(this)
                .setTitle("Create User")
                .setView(form)
                .setPositiveButton("Create", (dialog, which) -> {
                    try {
                        validateUser(username, password, confirm, recovery);
                        if (database.usernameExists(text(username))) {
                            throw new IllegalArgumentException("Username already exists");
                        }
                        currentUser = database.createUser(text(username), text(password), text(recovery));
                        toast("User created");
                        showDashboard();
                    } catch (RuntimeException ex) {
                        toast(ex.getMessage());
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showForgotPasswordDialog() {
        EditText username = input("Username", InputType.TYPE_CLASS_TEXT);
        EditText recovery = input("Recovery Answer", InputType.TYPE_CLASS_TEXT);
        EditText password = input("New Password", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        EditText confirm = input("Confirm Password", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        LinearLayout form = form(username, recovery, password, confirm);
        new AlertDialog.Builder(this)
                .setTitle("Reset Password")
                .setView(form)
                .setPositiveButton("Reset", (dialog, which) -> {
                    try {
                        validatePassword(password, confirm);
                        boolean changed = database.resetPassword(text(username), text(recovery), text(password));
                        toast(changed ? "Password updated" : "Username or recovery answer is wrong");
                    } catch (RuntimeException ex) {
                        toast(ex.getMessage());
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showAddGoalDialog() {
        EditText name = input("Goal name", InputType.TYPE_CLASS_TEXT);
        EditText target = input("Target amount", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        EditText saved = input("Initial saved", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        saved.setText("0");
        LinearLayout form = form(name, target, saved);
        new AlertDialog.Builder(this)
                .setTitle("Add Goal")
                .setView(form)
                .setPositiveButton("Save", (dialog, which) -> {
                    try {
                        if (text(name).isEmpty()) {
                            throw new IllegalArgumentException("Goal name is required");
                        }
                        double targetAmount = parseAmount(target);
                        double savedAmount = parseAmount(saved);
                        if (savedAmount > targetAmount) {
                            throw new IllegalArgumentException("Saved amount cannot exceed target");
                        }
                        database.addGoal(currentUser.id, text(name), targetAmount, savedAmount);
                        showGoals();
                    } catch (RuntimeException ex) {
                        toast(ex.getMessage());
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showGoalActions(Goal goal) {
        new AlertDialog.Builder(this)
                .setTitle(goal.name)
                .setItems(new String[]{"Add Savings", "Delete"}, (dialog, which) -> {
                    if (which == 0) {
                        showAddSavingsDialog(goal);
                    } else {
                        database.deleteGoal(currentUser.id, goal);
                        showGoals();
                    }
                })
                .show();
    }

    private void showAddSavingsDialog(Goal goal) {
        EditText amount = input("Amount", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        new AlertDialog.Builder(this)
                .setTitle("Add Savings")
                .setView(amount)
                .setPositiveButton("Save", (dialog, which) -> {
                    try {
                        database.addSavings(currentUser.id, goal, parseAmount(amount));
                        showGoals();
                    } catch (RuntimeException ex) {
                        toast(ex.getMessage());
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private LinearLayout baseLayout() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(20), dp(20), dp(20));
        return root;
    }

    private ScrollView scroll(View child) {
        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(child);
        return scrollView;
    }

    private TextView title(String value) {
        TextView title = new TextView(this);
        title.setText(value);
        title.setTextSize(34);
        title.setTextColor(0xFF0B3F8A);
        title.setGravity(Gravity.CENTER_HORIZONTAL);
        title.setPadding(0, 0, 0, dp(18));
        return title;
    }

    private TextView label(String value) {
        TextView label = new TextView(this);
        label.setText(value);
        label.setTextSize(16);
        label.setPadding(0, dp(6), 0, dp(6));
        return label;
    }

    private TextView metric(String name, String value) {
        TextView metric = label(name + ": " + value);
        metric.setTextSize(20);
        return metric;
    }

    private ProgressBar progress(String label, int value) {
        ProgressBar progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setMax(100);
        progressBar.setProgress(value);
        progressBar.setContentDescription(label);
        progressBar.setPadding(0, dp(8), 0, dp(8));
        return progressBar;
    }

    private EditText input(String hint, int inputType) {
        EditText editText = new EditText(this);
        editText.setHint(hint);
        editText.setInputType(inputType);
        editText.setSingleLine(true);
        editText.setPadding(dp(12), dp(10), dp(12), dp(10));
        editText.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        return editText;
    }

    private Button button(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setAllCaps(false);
        button.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        return button;
    }

    private LinearLayout form(EditText... fields) {
        LinearLayout form = baseLayout();
        for (EditText field : fields) {
            form.addView(field);
        }
        return form;
    }

    private String text(EditText editText) {
        return editText.getText().toString().trim();
    }

    private void validateUser(EditText username, EditText password, EditText confirm, EditText recovery) {
        String name = text(username).toLowerCase();
        if (name.length() < 3) {
            throw new IllegalArgumentException("Username must be at least 3 characters");
        }
        if (!name.matches("[a-z0-9_]+")) {
            throw new IllegalArgumentException("Username can use letters, numbers, and underscore only");
        }
        validatePassword(password, confirm);
        if (text(recovery).length() < 2) {
            throw new IllegalArgumentException("Recovery answer must be at least 2 characters");
        }
    }

    private void validatePassword(EditText password, EditText confirm) {
        if (text(password).length() < 4) {
            throw new IllegalArgumentException("Password must be at least 4 characters");
        }
        if (!text(password).equals(text(confirm))) {
            throw new IllegalArgumentException("Passwords do not match");
        }
    }

    private double parseAmount(EditText editText) {
        double value = Double.parseDouble(text(editText));
        if (value <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }
        return value;
    }

    private int parseMonth(EditText editText) {
        int value = Integer.parseInt(text(editText));
        if (value < 1 || value > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12");
        }
        return value;
    }

    private int parseYear(EditText editText) {
        int value = Integer.parseInt(text(editText));
        if (value < 2020 || value > 2100) {
            throw new IllegalArgumentException("Year must be between 2020 and 2100");
        }
        return value;
    }

    private String money(double value) {
        return String.format("%.2f", value);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
