package budgetapp.pages;

import budgetapp.connection.DatabaseConnection;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class EditAccountPage extends JFrame {
    private final int userId;
    private final Runnable refreshCallback;
    private final JComboBox<String> accountDropdown;
    private final JTextField nameField;
    private final JTextField balanceField;
    private final Map<String, Integer> accountIdMap;

    public EditAccountPage(int userId, Runnable refreshCallback) {
        this.userId = userId;
        this.refreshCallback = refreshCallback;
        this.accountIdMap = new HashMap<>();
        this.accountDropdown = new JComboBox<>();
        this.nameField = new JTextField();
        this.balanceField = new JTextField();
        initializeUI();
        loadAccounts();
    }

    private void initializeUI() {
        setTitle("Edit Account");
        setSize(400, 300);
        setLayout(new BorderLayout());

        // Form panel
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        formPanel.add(new JLabel("Select Account:"));
        formPanel.add(accountDropdown);
        formPanel.add(new JLabel("Account Name:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Account Balance:"));
        formPanel.add(balanceField);

        // Add listeners
        accountDropdown.addActionListener(e -> loadAccountDetails());

        // Save button
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> updateAccount());
        add(formPanel, BorderLayout.CENTER);
        add(saveButton, BorderLayout.SOUTH);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void loadAccounts() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT id, account_name FROM accounts WHERE user_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    int accountId = rs.getInt("id");
                    String accountName = rs.getString("account_name");
                    accountIdMap.put(accountName, accountId);
                    accountDropdown.addItem(accountName);
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading accounts: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadAccountDetails() {
        String selectedAccount = (String) accountDropdown.getSelectedItem();
        if (selectedAccount == null) return;

        int accountId = accountIdMap.get(selectedAccount);
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT account_name, balance FROM accounts WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, accountId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    nameField.setText(rs.getString("account_name"));
                    balanceField.setText(String.valueOf(rs.getDouble("balance")));
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading account details: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateAccount() {
        String selectedAccount = (String) accountDropdown.getSelectedItem();
        if (selectedAccount == null) return;

        int accountId = accountIdMap.get(selectedAccount);
        String newName = nameField.getText().trim();
        String newBalanceStr = balanceField.getText().trim();

        if (newName.isEmpty() || newBalanceStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Account name and balance cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            double newBalance = Double.parseDouble(newBalanceStr);
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "UPDATE accounts SET account_name = ?, balance = ? WHERE id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, newName);
                    stmt.setDouble(2, newBalance);
                    stmt.setInt(3, accountId);
                    stmt.executeUpdate();
                }
                JOptionPane.showMessageDialog(this, "Account updated successfully!");
                refreshCallback.run(); // Refresh the accounts list
                dispose(); // Close the form
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid balance. Please enter a numeric value.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error updating account: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}