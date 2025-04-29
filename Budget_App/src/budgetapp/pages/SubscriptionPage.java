package budgetapp.pages;

import java.sql.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import javax.swing.border.*;
import static budgetapp.connection.DatabaseConnection.getConnection;

public class SubscriptionPage extends BaseFrame {
    private DefaultTableModel model;
    private JLabel totalCostLabel;
    private RecordsPage recordsPage;

    public SubscriptionPage(int userId, RecordsPage recordsPage) {
        super("Subscriptions", userId);
        this.recordsPage = recordsPage;
        initUI();
    }

    @Override
    protected void initUI() {
        contentPanel.setLayout(new BorderLayout(10, 10));
        contentPanel.setBackground(new Color(35, 128, 60));

        model = new DefaultTableModel(new String[]{"Icon", "Service", "Cost ($)", "Date"}, 0);
        JTable table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 22));
        table.setRowHeight(80);
        table.setIntercellSpacing(new Dimension(10, 10));
        table.getColumnModel().getColumn(0).setPreferredWidth(100);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 20));
        table.setBackground(new Color(45, 138, 75));
        table.setForeground(Color.WHITE);

        table.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = new JLabel(value != null ? value.toString() : "", SwingConstants.CENTER);
                label.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
                label.setOpaque(true);
                label.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
                label.setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
                return label;
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        totalCostLabel = new JLabel("Total Monthly Cost: $0.00");
        totalCostLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        totalCostLabel.setForeground(Color.WHITE);
        totalCostLabel.setHorizontalAlignment(SwingConstants.CENTER);
        contentPanel.add(totalCostLabel, BorderLayout.NORTH);

        JPanel inputPanel = createInputPanel(table);
        contentPanel.add(inputPanel, BorderLayout.SOUTH);

        loadSubscriptions();
    }

    private JPanel createInputPanel(JTable table) {
        JPanel inputPanel = new JPanel(new GridLayout(4, 3, 10, 10));
        inputPanel.setBackground(new Color(40, 138, 70));
        inputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JTextField serviceField = new JTextField();
        JTextField costField = new JTextField();
        JTextField dateField = new JTextField(java.time.LocalDate.now().toString()); // Default to today's date
        JComboBox<String> iconCombo = new JComboBox<>(new String[]{
                "🎬", "🎵", "📚", "🎮", "📺", "💻", "🚗", "🍔", "🛒", "📱", "🏠", "✈️", "🛍️", "⚽", "🎉"
        });
        iconCombo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));

        JButton addBtn = new JButton("➕ Add");
        JButton removeBtn = new JButton("🗑️ Remove");

        styleButton(addBtn);
        styleButton(removeBtn);

        inputPanel.add(new JLabel("Service Name:", JLabel.RIGHT)).setForeground(Color.WHITE);
        inputPanel.add(serviceField);
        inputPanel.add(iconCombo);
        inputPanel.add(new JLabel("Cost ($):", JLabel.RIGHT)).setForeground(Color.WHITE);
        inputPanel.add(costField);
        inputPanel.add(new JLabel(""));
        inputPanel.add(new JLabel("Date (YYYY-MM-DD):", JLabel.RIGHT)).setForeground(Color.WHITE);
        inputPanel.add(dateField);
        inputPanel.add(new JLabel(""));
        inputPanel.add(addBtn);
        inputPanel.add(removeBtn);

        addBtn.addActionListener(e -> addSubscription(serviceField, costField, dateField, iconCombo, table));
        removeBtn.addActionListener(e -> removeSubscription(table));

        return inputPanel;
    }

    private void styleButton(JButton button) {
        button.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 27));
        button.setMargin(new Insets(10, 20, 10, 20));
        button.setBackground(new Color(50, 130, 70));
        button.setForeground(Color.WHITE);
    }

    private void addSubscription(JTextField serviceField, JTextField costField, JTextField dateField, JComboBox<String> iconCombo, JTable table) {
        String serviceName = serviceField.getText().trim();
        String costText = costField.getText().trim();
        String dateText = dateField.getText().trim();
        if (serviceName.isEmpty() || costText.isEmpty() || dateText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "⚡ Please fill in all fields.", "Missing Input", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            // Insert into subscriptions
            PreparedStatement subStmt = conn.prepareStatement(
                    "INSERT INTO subscriptions (user_id, service_name, cost, icon) VALUES (?, ?, ?, ?)"
            );
            subStmt.setInt(1, userId);
            subStmt.setString(2, serviceName);
            subStmt.setDouble(3, Double.parseDouble(costText));
            subStmt.setString(4, (String) iconCombo.getSelectedItem());
            subStmt.executeUpdate();

            // Insert into expenses
            PreparedStatement expStmt = conn.prepareStatement(
                    "INSERT INTO expenses (user_id, account_id, category_id, expense_date, month, amount, type, description) " +
                            "VALUES (?, ?, ?, ?, DATE_FORMAT(?, '%M %Y'), ?, 'expense', ?)"
            );
            expStmt.setInt(1, userId);
            expStmt.setInt(2, 1); // account_id
            expStmt.setInt(3, 10); // category_id
            expStmt.setString(4, dateText);
            expStmt.setString(5, dateText);
            expStmt.setDouble(6, Double.parseDouble(costText));
            expStmt.setString(7, serviceName);
            expStmt.executeUpdate();

            conn.commit();

            model.addRow(new Object[]{iconCombo.getSelectedItem(), serviceName, Double.parseDouble(costText), dateText});
            serviceField.setText("");
            costField.setText("");
            dateField.setText(java.time.LocalDate.now().toString());
            updateTotalCost();

            if (recordsPage != null) {
                recordsPage.refreshTransactions();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void removeSubscription(JTable table) {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "⚠️ Please select a subscription to remove.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String service = model.getValueAt(row, 1).toString();
        String date = model.getValueAt(row, 3).toString();
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            // Delete from subscriptions
            PreparedStatement subStmt = conn.prepareStatement(
                    "DELETE FROM subscriptions WHERE user_id = ? AND service_name = ?"
            );
            subStmt.setInt(1, userId);
            subStmt.setString(2, service);
            subStmt.executeUpdate();

            // Delete from expenses matching service name and date
            PreparedStatement expStmt = conn.prepareStatement(
                    "DELETE FROM expenses WHERE user_id = ? AND description = ? AND expense_date = ?"
            );
            expStmt.setInt(1, userId);
            expStmt.setString(2, service);
            expStmt.setString(3, date);
            expStmt.executeUpdate();

            conn.commit();

            model.removeRow(row);
            updateTotalCost();

            if (recordsPage != null) {
                recordsPage.refreshTransactions();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void loadSubscriptions() {
        model.setRowCount(0);
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT service_name, cost, icon FROM subscriptions WHERE user_id = ?"
            );
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{rs.getString("icon"), rs.getString("service_name"), rs.getDouble("cost"), java.time.LocalDate.now().toString()});
            }
            updateTotalCost();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateTotalCost() {
        double total = 0;
        for (int i = 0; i < model.getRowCount(); i++) {
            total += Double.parseDouble(model.getValueAt(i, 2).toString());
        }
        totalCostLabel.setText("Total Monthly Cost: $" + String.format("%.2f", total));
    }
}