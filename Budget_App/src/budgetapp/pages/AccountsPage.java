package budgetapp.pages;

import budgetapp.connection.DatabaseConnection;
import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class AccountsPage extends BaseFrame {
    public AccountsPage(int userId) {
        super("Accounts", userId);
        this.userId = userId;
        initUI();
        setVisible(true);
    }

    @Override
    protected void initUI() {
        contentPanel.setLayout(new BorderLayout());
        contentPanel.setBackground(new Color(242, 243, 247));

        // Title bar
        JPanel titleBarPanel = new JPanel(new BorderLayout());
        titleBarPanel.setBackground(new Color(0, 150, 0));
        titleBarPanel.setPreferredSize(new Dimension(getWidth(), 60));

        JLabel titleLabel = new JLabel("Your Accounts", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        titleBarPanel.add(titleLabel, BorderLayout.CENTER);
        contentPanel.add(titleBarPanel, BorderLayout.NORTH);

        // Accounts panel
        JPanel accountsPanel = new JPanel();
        accountsPanel.setLayout(new BoxLayout(accountsPanel, BoxLayout.Y_AXIS));
        accountsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        accountsPanel.setBackground(Color.WHITE);

        // Load accounts and add cards
        try (Connection conn = DatabaseConnection.getConnection()) {
            List<String[]> accounts = loadAccountsList(conn, userId);
            for (String[] account : accounts) {
                String accountName = account[0];
                String balance = account[1];
                String totalExpenses = account[2];
                accountsPanel.add(createAccountCard(accountName, balance, totalExpenses));
                accountsPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Add spacing between cards
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading accounts: " + e.getMessage());
        }

        // Add scroll pane
        JScrollPane scrollPane = new JScrollPane(accountsPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        // Footer
        JPanel footerPanel = new JPanel();
        footerPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        footerPanel.setBackground(Color.LIGHT_GRAY);

        // Create buttons
        JButton addButton = createAddButton();
        styleButton(addButton, new Color(0, 150, 0)); // Green for Add Account

        JButton editButton = createEditButton();
        styleButton(editButton, new Color(70, 130, 180)); // Blue for Edit

        JButton deleteButton = createDeleteButton();
        styleButton(deleteButton, new Color(255, 69, 58)); // Red for Delete

        // Add buttons to footer
        footerPanel.add(addButton);
        footerPanel.add(editButton);
        footerPanel.add(deleteButton);
        contentPanel.add(footerPanel, BorderLayout.SOUTH);
    }

    private void styleButton(JButton button, Color backgroundColor) {
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 25, 8, 25));
        button.setPreferredSize(new Dimension(150, 40)); // Set uniform size
    }

    private JButton createAddButton() {
        JButton button = new JButton("+ Add Account");
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setBackground(new Color(70, 130, 180));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(50, 50, 50), 1),
                BorderFactory.createEmptyBorder(8, 25, 8, 25)
        ));

        button.addActionListener(e -> {
            new AddAccountPage(userId, () -> {
                dispose();
                new AccountsPage(userId);
            }).setVisible(true);
        });

        return button;
    }

    private JButton createEditButton() {
        // Edit button
        JButton editButton = new JButton("Edit");

        editButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        editButton.setBackground(new Color(70, 130, 180));
        editButton.setForeground(Color.WHITE);
        editButton.setFocusPainted(false);
        editButton.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));

        //card.add(editButton);
        editButton.addActionListener(e -> {
            new EditAccountPage(userId, () -> {
                dispose();
                new AccountsPage(userId);
            }).setVisible(true);
        });
        return editButton;
    }

    private JButton createDeleteButton() {
        JButton deleteButton = new JButton("Delete Account");
        deleteButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        deleteButton.setBackground(new Color(255, 69, 58)); // Red color
        deleteButton.setForeground(Color.WHITE);
        deleteButton.setFocusPainted(false);
        deleteButton.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));

        deleteButton.addActionListener(e -> {
            JComboBox<String> accountDropdown = new JComboBox<>();
            Map<String, Integer> accountIdMap = new HashMap<>();

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
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error loading accounts: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int result = JOptionPane.showConfirmDialog(this, accountDropdown, "Select Account to Delete", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                String selectedAccount = (String) accountDropdown.getSelectedItem();
                if (selectedAccount != null) {
                    int accountId = accountIdMap.get(selectedAccount);

                    int confirm = JOptionPane.showConfirmDialog(this,
                            "Are you sure you want to delete the account \"" + selectedAccount + "\"?",
                            "Confirm Deletion",
                            JOptionPane.YES_NO_OPTION);

                    if (confirm == JOptionPane.YES_OPTION) {
                        try (Connection conn = DatabaseConnection.getConnection()) {
                            String sql = "DELETE FROM accounts WHERE id = ?";
                            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                                stmt.setInt(1, accountId);
                                stmt.executeUpdate();
                            }
                            JOptionPane.showMessageDialog(this, "Account deleted successfully!");
                            dispose();
                            new AccountsPage(userId); // Refresh the accounts page
                        } catch (SQLException ex) {
                            JOptionPane.showMessageDialog(this, "Account Deletion failed, as there are expenses linked to this account", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
        });

        return deleteButton;
    }

    private JPanel createAccountCard(String accountName, String balance, String totalExpenses) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(242, 243, 247));
        card.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        // Left panel for account details
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setBackground(new Color(242, 243, 247));

        JLabel nameLabel = new JLabel("Account Name: " + accountName);
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        nameLabel.setForeground(new Color(80, 80, 80));

        JLabel balanceLabel = new JLabel("Balance: $" + balance);
        balanceLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        balanceLabel.setForeground(new Color(80, 80, 80));

        JLabel expensesLabel = new JLabel("Total Expenses: $" + totalExpenses);
        expensesLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        expensesLabel.setForeground(new Color(255, 69, 58));

        detailsPanel.add(nameLabel);
        detailsPanel.add(balanceLabel);
        detailsPanel.add(expensesLabel);
        card.add(detailsPanel, BorderLayout.CENTER);

        return card;
    }

    private List<String[]> loadAccountsList(Connection conn, int userId) throws SQLException {
        List<String[]> accounts = new ArrayList<>();
        String sql = "SELECT a.account_name, " +
                "(SELECT COALESCE(SUM(e.amount), 0) FROM expenses e WHERE e.account_id = a.id AND e.type = 'income') - " +
                "(SELECT COALESCE(SUM(e.amount), 0) FROM expenses e WHERE e.account_id = a.id AND e.type = 'expense') AS balance, " +
                "(SELECT COALESCE(SUM(e.amount), 0) FROM expenses e WHERE e.account_id = a.id AND e.type = 'expense') AS total_expenses " +
                "FROM accounts a WHERE a.user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String accountName = rs.getString("account_name");
                double balance = rs.getDouble("balance");
                double totalExpenses = rs.getDouble("total_expenses");
                accounts.add(new String[]{accountName, String.format("%.2f", balance), String.format("%.2f", totalExpenses)});
            }
        }
        return accounts;
    }

}