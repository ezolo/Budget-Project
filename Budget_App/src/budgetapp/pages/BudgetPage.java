package budgetapp.pages;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;
import budgetapp.connection.DatabaseConnection;

public class BudgetPage extends BaseFrame {
    private JTextField incomeField, needsField, wantsField, savingsField;
    private JLabel needsAmountLabel, wantsAmountLabel, savingsAmountLabel;
    private JComboBox<String> monthComboBox;
    private JComboBox<Integer> yearComboBox;
    private int userId;
    private JPanel monthSelectionPanel;

    public BudgetPage(int userId) {
        super("Budget", userId);
        this.userId = userId;
        initUI();
        loadBudgetData();
    }

    @Override
    protected void initUI() {
        contentPanel.setBackground(new Color(242, 243, 247));
        contentPanel.setLayout(new BorderLayout());

        // Create the green banner
        JPanel bannerPanel = new JPanel(new BorderLayout());
        bannerPanel.setBackground(new Color(0, 128, 0));

        // Month selection panel (light gray)
        monthSelectionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        monthSelectionPanel.setBackground(new Color(220, 220, 220));

        // Month dropdown
        String[] months = new String[12];
        for (int i = 0; i < 12; i++) {
            months[i] = Month.of(i+1).getDisplayName(TextStyle.FULL, Locale.getDefault());
        }
        monthComboBox = new JComboBox<>(months);
        monthComboBox.setSelectedIndex(LocalDate.now().getMonthValue() - 1);

        // Year dropdown (current year and next few years)
        Integer[] years = {LocalDate.now().getYear(), LocalDate.now().getYear() + 1, LocalDate.now().getYear() + 2};
        yearComboBox = new JComboBox<>(years);
        yearComboBox.setSelectedItem(LocalDate.now().getYear());

        // Add month/year selection to panel
        monthSelectionPanel.add(new JLabel("Select Month:"));
        monthSelectionPanel.add(monthComboBox);
        monthSelectionPanel.add(new JLabel("Year:"));
        monthSelectionPanel.add(yearComboBox);

        // Add refresh button to load data for selected month/year
        JButton refreshButton = new JButton("Load");
        refreshButton.addActionListener(e -> loadBudgetData());
        monthSelectionPanel.add(refreshButton);

        // Title label
        JLabel bannerLabel = new JLabel("Budget");
        bannerLabel.setForeground(Color.WHITE);
        bannerLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        bannerLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Add components to banner panel
        bannerPanel.add(bannerLabel, BorderLayout.CENTER);
        bannerPanel.add(monthSelectionPanel, BorderLayout.SOUTH);

        contentPanel.add(bannerPanel, BorderLayout.NORTH);

        // Create a panel for the form and set its layout
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridLayout(0, 1, 10, 10));
        formPanel.setBackground(new Color(242, 243, 247));

        // Income Input
        formPanel.add(new JLabel("Enter Monthly After-Tax Income:"));
        incomeField = new JTextField();
        formPanel.add(incomeField);

        // Budget Percentages
        formPanel.add(new JLabel("Enter Budget Percentages:"));
        formPanel.add(new JLabel("Needs (%)"));
        needsField = new JTextField();
        formPanel.add(needsField);

        formPanel.add(new JLabel("Wants (%)"));
        wantsField = new JTextField();
        formPanel.add(wantsField);

        formPanel.add(new JLabel("Savings (%)"));
        savingsField = new JTextField();
        formPanel.add(savingsField);

        // Descriptions
        formPanel.add(new JLabel("Needs: Rent/Mortgage, Car Payment, Utilities, Insurance"));
        formPanel.add(new JLabel("Wants: Entertainment, Subscriptions, Vacations"));
        formPanel.add(new JLabel("Savings: 401K & IRA Contributions, Emergency Funds"));

        // Budget Calculation Section
        formPanel.add(new JLabel("Budget Calculation:"));
        needsAmountLabel = new JLabel("Needs: $0.00");
        wantsAmountLabel = new JLabel("Wants: $0.00");
        savingsAmountLabel = new JLabel("Savings: $0.00");
        formPanel.add(needsAmountLabel);
        formPanel.add(wantsAmountLabel);
        formPanel.add(savingsAmountLabel);

        // Buttons Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(new Color(200, 200, 200));

        JButton calculateButton = new JButton("Calculate Budget");
        styleButton(calculateButton, new Color(0, 122, 204));
        calculateButton.addActionListener(e -> calculateBudget());
        buttonPanel.add(calculateButton);

        JButton saveButton = new JButton("Save Budget");
        styleButton(saveButton, new Color(0, 153, 76));
        saveButton.addActionListener(e -> saveBudgetData());
        buttonPanel.add(saveButton);

        // Gray bar as menu
        JPanel grayBar = new JPanel();
        grayBar.setLayout(new BorderLayout());
        grayBar.setBackground(new Color(200, 200, 200));
        grayBar.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20)); // Padding for the menu
        grayBar.add(buttonPanel, BorderLayout.CENTER); // Add buttons to the gray bar

        // Add the form panel and gray bar to the content panel
        contentPanel.add(formPanel, BorderLayout.CENTER);
        contentPanel.add(grayBar, BorderLayout.SOUTH);
    }

    private String getSelectedMonthYear() {
        String month = (String) monthComboBox.getSelectedItem();
        int year = (int) yearComboBox.getSelectedItem();
        return month + "-" + year;
    }

    private void styleButton(JButton button, Color backgroundColor) {
        button.setFont(new Font("SansSerif", Font.PLAIN, 14));
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        button.setPreferredSize(new Dimension(150, 40));
    }

    private void calculateBudget() {
        try {
            double income = Double.parseDouble(incomeField.getText());
            double needsPercent = Double.parseDouble(needsField.getText());
            double wantsPercent = Double.parseDouble(wantsField.getText());
            double savingsPercent = Double.parseDouble(savingsField.getText());

            if (needsPercent + wantsPercent + savingsPercent != 100) {
                JOptionPane.showMessageDialog(this, "Percentages must add up to 100%");
                return;
            }

            double needsAmount = income * (needsPercent / 100);
            double wantsAmount = income * (wantsPercent / 100);
            double savingsAmount = income * (savingsPercent / 100);

            needsAmountLabel.setText(String.format("Needs: $%.2f", needsAmount));
            wantsAmountLabel.setText(String.format("Wants: $%.2f", wantsAmount));
            savingsAmountLabel.setText(String.format("Savings: $%.2f", savingsAmount));
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers for income and percentages.");
        }
    }


        private void saveBudgetData() {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "INSERT INTO budget (user_id, month_year, income, needs_percent, wants_percent, savings_percent, budget_set) " +
                        "VALUES (?, ?, ?, ?, ?, ?, TRUE) " +
                        "ON DUPLICATE KEY UPDATE income = ?, needs_percent = ?, wants_percent = ?, savings_percent = ?, budget_set = TRUE";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                stmt.setString(2, getSelectedMonthYear());
                stmt.setDouble(3, Double.parseDouble(incomeField.getText()));
                stmt.setDouble(4, Double.parseDouble(needsField.getText()));
                stmt.setDouble(5, Double.parseDouble(wantsField.getText()));
                stmt.setDouble(6, Double.parseDouble(savingsField.getText()));
                stmt.setDouble(7, Double.parseDouble(incomeField.getText()));
                stmt.setDouble(8, Double.parseDouble(needsField.getText()));
                stmt.setDouble(9, Double.parseDouble(wantsField.getText()));
                stmt.setDouble(10, Double.parseDouble(savingsField.getText()));

                stmt.executeUpdate();
                    // Check if this is current month's budget
                    String currentMonthYear = LocalDate.now().getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault())
                            + "-" + LocalDate.now().getYear();

                    if (getSelectedMonthYear().equals(currentMonthYear)) {
                        // Update challenge status if this is current month
                        updateBudgetChallengeStatus(conn);
                    }

                    JOptionPane.showMessageDialog(this, "Budget saved successfully for " + getSelectedMonthYear() + "!");
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error saving budget: " + e.getMessage());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Please enter valid numbers for all fields.");
            }
        }

    private void updateBudgetChallengeStatus(Connection conn) throws SQLException {
        String sql = "INSERT INTO user_challenges (user_id, challenge_name, status) " +
                "VALUES (?, 'Plan a Monthly Budget', 'completed') " +
                "ON DUPLICATE KEY UPDATE status = 'completed'";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }

    private void loadBudgetData() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT income, needs_percent, wants_percent, savings_percent FROM budget WHERE user_id = ? AND month_year = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                stmt.setString(2, getSelectedMonthYear());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    incomeField.setText(String.valueOf(rs.getDouble("income")));
                    needsField.setText(String.valueOf(rs.getDouble("needs_percent")));
                    wantsField.setText(String.valueOf(rs.getDouble("wants_percent")));
                    savingsField.setText(String.valueOf(rs.getDouble("savings_percent")));
                    calculateBudget();
                } else {
                    // Clear fields if no budget found for selected month/year
                    incomeField.setText("");
                    needsField.setText("");
                    wantsField.setText("");
                    savingsField.setText("");
                    needsAmountLabel.setText("Needs: $0.00");
                    wantsAmountLabel.setText("Wants: $0.00");
                    savingsAmountLabel.setText("Savings: $0.00");
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading budget: " + e.getMessage());
        }
    }
}