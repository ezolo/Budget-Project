package budgetapp.pages;

import budgetapp.connection.DatabaseConnection;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AddAccountPage extends JFrame {
    private final int userId;
    private final Runnable refreshCallback;

    public AddAccountPage(int userId, Runnable refreshCallback) {
        this.userId = userId;
        this.refreshCallback = refreshCallback;
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Add New Account");
        setSize(400, 250);
        setLayout(new BorderLayout());

        // Form panel
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        JTextField nameField = new JTextField();
        JTextField balanceField = new JTextField();

        formPanel.add(new JLabel("Account Name:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Account Balance:"));
        formPanel.add(balanceField);

        add(formPanel, BorderLayout.CENTER);

        // Save button
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> saveAccount(nameField.getText().trim(), balanceField.getText().trim()));
        add(saveButton, BorderLayout.SOUTH);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void saveAccount(String accountName, String accountBalance) {
        if (accountName.isEmpty() || accountBalance.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Account name and balance cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            double balance = Double.parseDouble(accountBalance);
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "INSERT INTO accounts (user_id, account_name, balance) VALUES (?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, userId);
                    stmt.setString(2, accountName);
                    stmt.setDouble(3, balance);
                    stmt.executeUpdate();
                }
                JOptionPane.showMessageDialog(this, "Account added successfully!");
                refreshCallback.run(); // Refresh the accounts list
                dispose(); // Close the form
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid balance. Please enter a numeric value.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error adding account: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}