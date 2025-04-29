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
        setSize(400, 200);
        setLayout(new BorderLayout());

        // Form panel
        JPanel formPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        JTextField nameField = new JTextField();

        formPanel.add(new JLabel("Account Name:"));
        formPanel.add(nameField);

        add(formPanel, BorderLayout.CENTER);

        // Save button
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> saveAccount(nameField.getText().trim()));
        add(saveButton, BorderLayout.SOUTH);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void saveAccount(String accountName) {
        if (accountName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Account name cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO accounts (user_id, account_name, balance) VALUES (?, ?, 0)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                stmt.setString(2, accountName);
                stmt.executeUpdate();
            }
            JOptionPane.showMessageDialog(this, "Account added successfully!");
            refreshCallback.run(); // Refresh the accounts list
            dispose(); // Close the form
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error adding account: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}