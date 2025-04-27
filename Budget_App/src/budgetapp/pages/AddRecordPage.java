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

public class AddRecordPage extends JFrame {
    private final int userId;
    private final Runnable refreshCallback;
    private final JComboBox<String> accountDropdown;
    private final Map<String, Integer> accountIdMap;
    private final JComboBox<String> categoryDropdown;
    private final Map<String, Integer> categoryIdMap;

    public AddRecordPage(int userId, Runnable refreshCallback) {
        this.userId = userId;
        this.refreshCallback = refreshCallback;
        this.accountIdMap = new HashMap<>();
        this.accountDropdown = new JComboBox<>();
        this.categoryIdMap = new HashMap<>();
        this.categoryDropdown = new JComboBox<>();
        initializeUI();
        loadAccounts();
        loadCategories();
    }

    private void initializeUI() {
        setTitle("Add Record");
        setSize(400, 350); // Adjusted size to accommodate the new field
        setLayout(new GridLayout(7, 2, 5, 5));

        // Add a date picker
        JDateChooser dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("yyyy-MM-dd");
        dateChooser.setDate(new java.util.Date()); // Prepopulate with today's date

        JTextField amountField = new JTextField();
        JComboBox<String> typeComboBox = new JComboBox<>(new String[]{"Expense", "Income"});
        JTextField categoryField = new JTextField();
        JTextField descriptionField = new JTextField(); // New description field

        add(new JLabel("Date (YYYY-MM-DD):"));
        add(dateChooser);
        add(new JLabel("Account:"));
        add(accountDropdown);
        add(new JLabel("Amount:"));
        add(amountField);
        add(new JLabel("Type:"));
        add(typeComboBox);
        add(new JLabel("Category:"));
        add(categoryDropdown);
        add(new JLabel("Description:")); // Label for description
        add(descriptionField);

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> {
            String selectedDate = ((JTextField) dateChooser.getDateEditor().getUiComponent()).getText();
            saveRecord(selectedDate.trim(), (String) accountDropdown.getSelectedItem(), amountField.getText().trim(), (String) typeComboBox.getSelectedItem(),(String) categoryDropdown.getSelectedItem(), descriptionField.getText().trim());
        });
        add(saveButton);

        setLocationRelativeTo(null);
        setVisible(true);
    }
    private void loadCategories() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT id, name FROM categories";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    int categoryId = rs.getInt("id");
                    String categoryName = rs.getString("name");
                    categoryIdMap.put(categoryName, categoryId); // Map category name to ID
                    categoryDropdown.addItem(categoryName); // Add category name to dropdown
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading categories: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
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

    private void saveRecord(String date, String account, String amount, String type, String category, String description) {
        if (date.isEmpty() || account == null || amount.isEmpty() || type.isEmpty() || category.isEmpty() || description.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            double amountValue = Double.parseDouble(amount);
            int accountId = accountIdMap.get(account);
            int categoryId = categoryIdMap.get(category); // Get category ID from the map
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "INSERT INTO expenses (user_id, expense_date, account_id, amount, type, category_id, description) VALUES (?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, userId);
                    stmt.setString(2, date);
                    stmt.setInt(3, accountId);
                    stmt.setDouble(4, amountValue);
                    stmt.setString(5, type);
                    stmt.setInt(6, categoryId);
                    stmt.setString(7, description); // Insert description
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