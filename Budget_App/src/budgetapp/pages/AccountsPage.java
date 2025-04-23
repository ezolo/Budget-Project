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
            List<String> accounts = loadAccountsList(conn, userId);
            for (String account : accounts) {
                String[] accountDetails = account.replace("[", "").replace("]", "").split(", ");
                String accountName = accountDetails[0];
                String balance = accountDetails[1];
                accountsPanel.add(createAccountCard(accountName, balance));
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
        JButton addButton = createAddButton();
        footerPanel.add(addButton);
        contentPanel.add(footerPanel, BorderLayout.SOUTH);
    }
    private JButton createAddButton() {
        JButton button = new JButton("+ Add New Account");
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
    private JButton createEditButton(){
    // Edit button
    JButton editButton = new JButton("Edit");

        editButton.setFont(new Font("SansSerif", Font.BOLD, 12));
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
    private JPanel createAccountCard(String accountName,String balance) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(242, 243, 247));
        card.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15)); // Add padding
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80)); // Full width

        // Left panel for account details
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setBackground(new Color(242, 243, 247));

        JLabel nameLabel = new JLabel("AccountName: " + accountName);
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        nameLabel.setForeground(new Color(80, 80, 80));

        JLabel balanceLabel = new JLabel("Balance: $" + balance);
        balanceLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        balanceLabel.setForeground(new Color(80, 80, 80));
        detailsPanel.add(nameLabel);
        detailsPanel.add(balanceLabel);

        card.add(detailsPanel, BorderLayout.CENTER);
        JButton addButton = createEditButton();
        card.add(addButton, BorderLayout.EAST);
        return card;


    }

    private List<String> loadAccountsList(Connection conn, int userId) throws SQLException {
        List<String> accounts = new ArrayList<>();
        String sql = "SELECT name, balance FROM accounts WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String name = rs.getString("name");
                String balance = String.format("%.2f", rs.getDouble("balance"));
                accounts.add(Arrays.toString(new String[]{name, balance}));
            }
        }
        return accounts;
    }

    public static void showAccountPanel(int userId) {
        new AccountsPage(userId);
    }
}