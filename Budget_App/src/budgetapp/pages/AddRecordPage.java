/*package budgetapp.pages;

public class AddRecordPage {
}
*/package budgetapp.pages;

import budgetapp.connection.DatabaseConnection;
import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AddRecordPage extends JFrame {
    private final int userId;
    private final Runnable refreshCallback;

    public AddRecordPage(int userId, Runnable refreshCallback) {
        this.userId = userId;
        this.refreshCallback = refreshCallback;
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Add Record");
        setSize(400, 300);
        setLayout(new GridLayout(6, 2, 5, 5));

        JTextField dateField = new JTextField();
        JTextField accountField = new JTextField();
        JTextField amountField = new JTextField();
        JComboBox<String> typeComboBox = new JComboBox<>(new String[]{"Expense", "Income"});
        JTextField categoryField = new JTextField();

        add(new JLabel("Date (YYYY-MM-DD):"));
        add(dateField);
        add(new JLabel("Account:"));
        add(accountField);
        add(new JLabel("Amount:"));
        add(amountField);
        add(new JLabel("Type:"));
        add(typeComboBox);
        add(new JLabel("Category:"));
        add(categoryField);

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> saveRecord(dateField.getText().trim(), accountField.getText().trim(), amountField.getText().trim(), (String) typeComboBox.getSelectedItem(), categoryField.getText().trim()));
        add(saveButton);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void saveRecord(String date, String account, String amount, String type, String category) {
        if (date.isEmpty() || account.isEmpty() || amount.isEmpty() || type.isEmpty() || category.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            double amountValue = Double.parseDouble(amount);
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "INSERT INTO records (user_id, date, account, amount, type, category) VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, userId);
                    stmt.setString(2, date);
                    stmt.setString(3, account);
                    stmt.setDouble(4, amountValue);
                    stmt.setString(5, type);
                    stmt.setString(6, category);
                    stmt.executeUpdate();
                }
                JOptionPane.showMessageDialog(this, "Record added successfully!");
                refreshCallback.run();
                dispose();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid amount. Please enter a numeric value.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error adding record: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
