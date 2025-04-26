package budgetapp.pages;

import budgetapp.connection.DatabaseConnection;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class EditRecordPage extends JFrame {
    private final int userId; // Added userId field
    private final int recordId;
    private final Runnable refreshCallback;
    private final JComboBox<String> accountDropdown;
    private final JComboBox<String> categoryDropdown;
    private final Map<String, Integer> accountIdMap;
    private final Map<String, Integer> categoryIdMap;

    private final JDateChooser dateChooser;
    private final JTextField amountField;
    private final JTextField descriptionField;

    public EditRecordPage(int userId, int recordId, Runnable refreshCallback) { // Updated constructor
        this.userId = userId; // Initialize userId
        this.recordId = recordId;
        this.refreshCallback = refreshCallback;
        this.accountIdMap = new HashMap<>();
        this.categoryIdMap = new HashMap<>();
        this.accountDropdown = new JComboBox<>();
        this.categoryDropdown = new JComboBox<>();
        this.dateChooser = new JDateChooser();
        this.amountField = new JTextField();
        this.descriptionField = new JTextField();

        initializeUI();
        loadRecordDetails();
    }

    private void initializeUI() {
        setTitle("Edit Record");
        setSize(400, 350);
        setLayout(new GridLayout(7, 2, 5, 5));

        dateChooser.setDateFormatString("yyyy-MM-dd");

        add(new JLabel("Date (YYYY-MM-DD):"));
        add(dateChooser);
        add(new JLabel("Account:"));
        add(accountDropdown);
        add(new JLabel("Amount:"));
        add(amountField);
        add(new JLabel("Category:"));
        add(categoryDropdown);
        add(new JLabel("Description:"));
        add(descriptionField);

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> saveChanges());
        add(saveButton);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());
        add(cancelButton);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void loadRecordDetails() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT e.expense_date, e.account_id, e.amount, e.description, e.category_id " +
                    "FROM expenses e WHERE e.id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, recordId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    dateChooser.setDate(rs.getDate("expense_date"));
                    amountField.setText(rs.getString("amount"));
                    descriptionField.setText(rs.getString("description"));

                    loadAccounts(conn, rs.getInt("account_id"));
                    loadCategories(conn, rs.getInt("category_id"));
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading record details: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadAccounts(Connection conn, int selectedAccountId) throws SQLException {
        String sql = "SELECT id, account_name FROM accounts WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId); // Use the userId field
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int accountId = rs.getInt("id");
                String accountName = rs.getString("account_name");
                accountIdMap.put(accountName, accountId);
                accountDropdown.addItem(accountName);

                if (accountId == selectedAccountId) {
                    accountDropdown.setSelectedItem(accountName);
                }
            }
        }
    }

    private void loadCategories(Connection conn, int selectedCategoryId) throws SQLException {
        String sql = "SELECT id, name FROM categories";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int categoryId = rs.getInt("id");
                String categoryName = rs.getString("name");
                categoryIdMap.put(categoryName, categoryId);
                categoryDropdown.addItem(categoryName);

                if (categoryId == selectedCategoryId) {
                    categoryDropdown.setSelectedItem(categoryName);
                }
            }
        }
    }

    private void saveChanges() {
        String date = ((JTextField) dateChooser.getDateEditor().getUiComponent()).getText().trim();
        String account = (String) accountDropdown.getSelectedItem();
        String amount = amountField.getText().trim();
        String category = (String) categoryDropdown.getSelectedItem();
        String description = descriptionField.getText().trim();

        if (date.isEmpty() || account == null || amount.isEmpty() || category == null || description.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            double amountValue = Double.parseDouble(amount);
            int accountId = accountIdMap.get(account);
            int categoryId = categoryIdMap.get(category);

            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "UPDATE expenses SET expense_date = ?, account_id = ?, amount = ?, description = ?, category_id = ? WHERE id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, date);
                    stmt.setInt(2, accountId);
                    stmt.setDouble(3, amountValue);
                    stmt.setString(4, description);
                    stmt.setInt(5, categoryId);
                    stmt.setInt(6, recordId);
                    stmt.executeUpdate();
                }
                JOptionPane.showMessageDialog(this, "Record updated successfully!");
                refreshCallback.run();
                dispose();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid amount. Please enter a numeric value.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error updating record: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}