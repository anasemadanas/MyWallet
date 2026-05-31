package com.moneytracker.ui;

import com.moneytracker.models.User;
import com.moneytracker.services.UserService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public class frmLoginScreen extends JFrame {
    private final UserService userService = new UserService();
    private final JTextField txtUsername = new JTextField(18);
    private final JPasswordField txtPassword = new JPasswordField(18);

    public frmLoginScreen() {
        super("MyWallet Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(500, 360);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        add(buildContent(), BorderLayout.CENTER);
    }

    private JPanel buildContent() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(24, 34, 24, 34));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
        c.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("MyWallet", SwingConstants.CENTER);
        title.setFont(new Font("Impact", Font.PLAIN, 46));
        title.setForeground(new Color(0, 45, 130));
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        panel.add(title, c);

        c.gridwidth = 1;
        c.gridy++;
        c.gridx = 0;
        panel.add(new JLabel("Username"), c);
        c.gridx = 1;
        panel.add(txtUsername, c);

        c.gridy++;
        c.gridx = 0;
        panel.add(new JLabel("Password"), c);
        c.gridx = 1;
        panel.add(txtPassword, c);

        JButton btnLogin = new JButton("Login");
        JButton btnCreateUser = new JButton("Create User");
        JButton btnForgotPassword = new JButton("Forgot Password");
        JButton btnClose = new JButton("Close");
        btnLogin.addActionListener(event -> login());
        btnCreateUser.addActionListener(event -> createUser());
        btnForgotPassword.addActionListener(event -> forgotPassword());
        btnClose.addActionListener(event -> dispose());
        txtPassword.addActionListener(event -> login());

        JPanel buttons = new JPanel();
        buttons.add(btnLogin);
        buttons.add(btnCreateUser);
        buttons.add(btnForgotPassword);
        buttons.add(btnClose);
        c.gridy++;
        c.gridx = 0;
        c.gridwidth = 2;
        panel.add(buttons, c);
        return panel;
    }

    private void login() {
        try {
            User user = userService.login(txtUsername.getText(), new String(txtPassword.getPassword()));
            if (user == null) {
                JOptionPane.showMessageDialog(this, "Invalid username or password.", "Login failed", JOptionPane.WARNING_MESSAGE);
                return;
            }
            new frmDashBoard(user).setVisible(true);
            dispose();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Login failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void createUser() {
        JTextField username = new JTextField(16);
        JPasswordField password = new JPasswordField(16);
        JPasswordField confirm = new JPasswordField(16);
        JTextField recovery = new JTextField(16);
        JPanel panel = dialogPanel(
                new String[]{"Username", "Password", "Confirm Password", "Recovery Answer"},
                new java.awt.Component[]{username, password, confirm, recovery}
        );

        int answer = JOptionPane.showConfirmDialog(this, panel, "Create User", JOptionPane.OK_CANCEL_OPTION);
        if (answer != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            User user = userService.createUser(
                    username.getText(),
                    new String(password.getPassword()),
                    new String(confirm.getPassword()),
                    recovery.getText()
            );
            JOptionPane.showMessageDialog(this, "User created. You are logged in now.");
            new frmDashBoard(user).setVisible(true);
            dispose();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Create user failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void forgotPassword() {
        JTextField username = new JTextField(16);
        JTextField recovery = new JTextField(16);
        JPasswordField password = new JPasswordField(16);
        JPasswordField confirm = new JPasswordField(16);
        JPanel panel = dialogPanel(
                new String[]{"Username", "Recovery Answer", "New Password", "Confirm Password"},
                new java.awt.Component[]{username, recovery, password, confirm}
        );

        int answer = JOptionPane.showConfirmDialog(this, panel, "Reset Password", JOptionPane.OK_CANCEL_OPTION);
        if (answer != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            boolean changed = userService.resetPassword(
                    username.getText(),
                    recovery.getText(),
                    new String(password.getPassword()),
                    new String(confirm.getPassword())
            );
            if (changed) {
                JOptionPane.showMessageDialog(this, "Password updated. You can login now.");
            } else {
                JOptionPane.showMessageDialog(this, "Username or recovery answer is wrong.", "Reset failed", JOptionPane.WARNING_MESSAGE);
            }
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Reset failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel dialogPanel(String[] labels, java.awt.Component[] fields) {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;
        for (int i = 0; i < labels.length; i++) {
            c.gridx = 0;
            c.gridy = i;
            panel.add(new JLabel(labels[i]), c);
            c.gridx = 1;
            panel.add(fields[i], c);
        }
        return panel;
    }
}
