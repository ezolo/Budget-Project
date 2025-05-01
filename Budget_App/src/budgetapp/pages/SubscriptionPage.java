package budgetapp.pages;

import java.sql.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import javax.swing.border.*;
import java.awt.event.*;
import static budgetapp.connection.DatabaseConnection.getConnection;

// SubscriptionPage handles the GUI and logic for managing user subscriptions.
public class SubscriptionPage extends BaseFrame {
    private DefaultTableModel model;
    private JLabel totalCostLabel;
    private RecordsPage recordsPage;

    // Constructor initializes the page with user ID and reference to RecordsPage for syncing.
    public SubscriptionPage(int userId, RecordsPage recordsPage) {
        super("Subscriptions", userId);
        this.recordsPage = recordsPage;
        initUI(); // Set up the UI
    }

    // Set up the UI layout and components
    @Override
    protected void initUI() {
        contentPanel.setLayout(new BorderLayout(10, 10));
        contentPanel.setBackground(new Color(35, 128, 60));

        // Set up table model and JTable to display subscriptions
        model = new DefaultTableModel(new String[]{"Icon", "Service", "Cost ($)", "Date"}, 0);
        JTable table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 22));
        table.setRowHeight(80);
        table.setIntercellSpacing(new Dimension(10, 10));
        table.getColumnModel().getColumn(0).setPreferredWidth(100);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 20));
        table.setBackground(new Color(45, 138, 75));
        table.setForeground(Color.WHITE);

        // Array to track which row is hovered for hover effect
        final int[] hoveredRow = {-1};

        // Change hoveredRow when mouse moves
        table.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row != hoveredRow[0]) {
                    hoveredRow[0] = row;
                    table.repaint(); // Repaint table to show hover effect
                }
            }
        });

        // Reset hover when mouse leaves table
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                hoveredRow[0] = -1;
                table.repaint();
            }
        });

        // Custom cell renderer for hover and emoji formatting
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setFont(column == 0 ? new Font("Segoe UI Emoji", Font.PLAIN, 48) : new Font("Segoe UI", Font.PLAIN, 22));
                label.setHorizontalAlignment(column == 0 ? SwingConstants.CENTER : SwingConstants.LEFT);
                label.setOpaque(true);
                if (isSelected) {
                    label.setBackground(table.getSelectionBackground());
                } else if (row == hoveredRow[0]) {
                    label.setBackground(new Color(60, 160, 100)); // Hover background
                } else {
                    label.setBackground(new Color(45, 138, 75)); // Normal background
                }
                label.setForeground(Color.WHITE);
                return label;
            }
        });

        // Add table to scrollable view
        JScrollPane scrollPane = new JScrollPane(table);
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        // Total cost label shown above table
        totalCostLabel = new JLabel("Total Monthly Cost: $0.00");
        totalCostLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        totalCostLabel.setForeground(Color.WHITE);
        totalCostLabel.setHorizontalAlignment(SwingConstants.CENTER);
        contentPanel.add(totalCostLabel, BorderLayout.NORTH);

        // Create and add input panel below table
        JPanel inputPanel = createInputPanel(table);
        contentPanel.add(inputPanel, BorderLayout.SOUTH);

        // Load subscription data from database
        loadSubscriptions();
    }

    // Create input fields and buttons to add/edit subscriptions
    private JPanel createInputPanel(JTable table) {
        JPanel inputPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        inputPanel.setBackground(new Color(40, 138, 70));
        inputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Input fields
        JTextField serviceField = new JTextField();
        JTextField costField = new JTextField();
        JTextField dateField = new JTextField(java.time.LocalDate.now().toString());

        // Icon dropdown
        JComboBox<String> iconCombo = new JComboBox<>(new String[]{
                "🎬", "🎵", "📚", "🎮", "📺", "💻", "🚗", "🍔", "🛒", "📱", "🏠", "✈️", "🛍️", "⚽", "🎉"
        });
        iconCombo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));

        // Buttons for actions
        JButton addBtn = new JButton("➕ Add");
        JButton removeBtn = new JButton("🗑️ Remove");
        JButton editBtn = new JButton("✏️ Update Cost");

        // Style buttons
        styleButton(addBtn);
        styleButton(removeBtn);
        styleButton(editBtn);

        // Row 1: Service name and icon
        JPanel row1 = new JPanel(new BorderLayout(10, 10));
        row1.setBackground(inputPanel.getBackground());
        row1.add(new JLabel("Service Name:"), BorderLayout.WEST);
        row1.add(serviceField, BorderLayout.CENTER);
        row1.add(iconCombo, BorderLayout.EAST);

        // Row 2: Date and Cost inputs
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 0));
        row2.setBackground(inputPanel.getBackground());

        JPanel datePanel = new JPanel(new BorderLayout(5, 0));
        datePanel.setBackground(inputPanel.getBackground());
        datePanel.add(new JLabel("Date (YYYY-MM-DD):", JLabel.RIGHT), BorderLayout.WEST);
        datePanel.add(dateField, BorderLayout.CENTER);

        JPanel costPanel = new JPanel(new BorderLayout(5, 0));
        costPanel.setBackground(inputPanel.getBackground());
        costPanel.add(new JLabel("Cost ($):", JLabel.RIGHT), BorderLayout.WEST);
        costPanel.add(costField, BorderLayout.CENTER);

        Dimension fieldSize = new Dimension(200, 35);
        costField.setPreferredSize(fieldSize);
        dateField.setPreferredSize(fieldSize);

        row2.add(datePanel);
        row2.add(costPanel);

        // Row 3: Add, Remove, Edit buttons
        JPanel row3 = new JPanel(new GridLayout(1, 3, 10, 10));
        row3.setBackground(inputPanel.getBackground());
        row3.add(addBtn);
        row3.add(removeBtn);
        row3.add(editBtn);

        // Add rows to input panel
        inputPanel.add(row1);
        inputPanel.add(row2);
        inputPanel.add(row3);

        // Add subscription when button clicked
        addBtn.addActionListener(e -> addSubscription(serviceField, costField, dateField, iconCombo, table));

        // Remove selected subscription
        removeBtn.addActionListener(e -> removeSubscription(table));

        // Edit subscription cost/date
        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "⚠️ Please select a subscription to edit.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Get old values
            String oldService = model.getValueAt(row, 1).toString();
            String oldDate = model.getValueAt(row, 3).toString();

            // Get new inputs
            String newService = serviceField.getText().trim();
            String newCostText = costField.getText().trim();
            String newDate = dateField.getText().trim();
            String newIcon = (String) iconCombo.getSelectedItem();

            if (newService.isEmpty() || newCostText.isEmpty() || newDate.isEmpty()) {
                JOptionPane.showMessageDialog(this, "⚡ Please fill in all fields.", "Missing Input", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try (Connection conn = getConnection()) {
                conn.setAutoCommit(false);

                // Update subscriptions table
                PreparedStatement subStmt = conn.prepareStatement(
                        "UPDATE subscriptions SET service_name = ?, cost = ?, icon = ? WHERE user_id = ? AND service_name = ?"
                );
                subStmt.setString(1, newService);
                subStmt.setDouble(2, Double.parseDouble(newCostText));
                subStmt.setString(3, newIcon);
                subStmt.setInt(4, userId);
                subStmt.setString(5, oldService);
                subStmt.executeUpdate();

                // Update expenses table
                PreparedStatement expStmt = conn.prepareStatement(
                        "UPDATE expenses SET description = ?, amount = ?, expense_date = ?, month = DATE_FORMAT(?, '%M %Y') " +
                                "WHERE user_id = ? AND description = ? AND expense_date = ?"
                );
                expStmt.setString(1, newService);
                expStmt.setDouble(2, Double.parseDouble(newCostText));
                expStmt.setString(3, newDate);
                expStmt.setString(4, newDate);
                expStmt.setInt(5, userId);
                expStmt.setString(6, oldService);
                expStmt.setString(7, oldDate);
                expStmt.executeUpdate();

                conn.commit();

                // Reflect changes in table UI
                model.setValueAt(newIcon, row, 0);
                model.setValueAt(newService, row, 1);
                model.setValueAt(Double.parseDouble(newCostText), row, 2);
                model.setValueAt(newDate, row, 3);
                updateTotalCost();

                // Refresh records if applicable
                if (recordsPage != null) {
                    recordsPage.refreshTransactions();
                }

                // Clear inputs
                serviceField.setText("");
                costField.setText("");
                dateField.setText(java.time.LocalDate.now().toString());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        return inputPanel;
    }

    // Apply consistent styling to action buttons
    private void styleButton(JButton button) {
        button.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 27));
        button.setMargin(new Insets(10, 20, 10, 20));
        button.setBackground(new Color(50, 130, 70));
        button.setForeground(Color.WHITE);
    }

    // Add a new subscription and insert into database
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

            // Insert into subscriptions table
            PreparedStatement subStmt = conn.prepareStatement(
                    "INSERT INTO subscriptions (user_id, service_name, cost, icon) VALUES (?, ?, ?, ?)"
            );
            subStmt.setInt(1, userId);
            subStmt.setString(2, serviceName);
            subStmt.setDouble(3, Double.parseDouble(costText));
            subStmt.setString(4, (String) iconCombo.getSelectedItem());
            subStmt.executeUpdate();

            // Insert into expenses table
            PreparedStatement expStmt = conn.prepareStatement(
                    "INSERT INTO expenses (user_id, account_id, category_id, expense_date, month, amount, type, description) " +
                            "VALUES (?, ?, ?, ?, DATE_FORMAT(?, '%M %Y'), ?, 'expense', ?)"
            );
            expStmt.setInt(1, userId);
            expStmt.setInt(2, 2); // Example account ID
            expStmt.setInt(3, 10); // Example category ID
            expStmt.setString(4, dateText);
            expStmt.setString(5, dateText);
            expStmt.setDouble(6, Double.parseDouble(costText));
            expStmt.setString(7, serviceName);
            expStmt.executeUpdate();

            conn.commit();

            // Add new row to UI
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

    // Remove selected subscription from database and UI
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

            // Delete from expenses
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

    // Load all subscriptions from database
    private void loadSubscriptions() {
        model.setRowCount(0);
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT service_name, cost, icon FROM subscriptions WHERE user_id = ?"
            );
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("icon"),
                        rs.getString("service_name"),
                        rs.getDouble("cost"),
                        java.time.LocalDate.now().toString() // Default to current date
                });
            }
            updateTotalCost();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Recalculate total monthly subscription cost
    private void updateTotalCost() {
        double total = 0;
        for (int i = 0; i < model.getRowCount(); i++) {
            total += Double.parseDouble(model.getValueAt(i, 2).toString());
        }
        totalCostLabel.setText("Total Monthly Cost: $" + String.format("%.2f", total));
    }
}
