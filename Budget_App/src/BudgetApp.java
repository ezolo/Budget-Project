// Importing packages for application
import java.sql.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import javax.swing.border.*;

public class BudgetApp {

    public static Connection getConnection() throws SQLException {
        String dbURL = "jdbc:mysql://localhost:3306/budget_management";
        String username = "root";
        String password = "_Ali_9350303_";
        return DriverManager.getConnection(dbURL, username, password);
    }

    public static void showSubscriptionPanel(int userId) {
        JFrame frame = new JFrame("🌟 My Subscriptions 🌟");
        frame.setSize(900, 700);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout(10, 10));
        frame.getContentPane().setBackground(new Color(35, 128, 60)); // Dark greenish-blue

        // Table setup
        DefaultTableModel model = new DefaultTableModel(new String[]{"Icon", "Service", "Cost ($)"}, 0);
        JTable table = new JTable(model);

        table.setFont(new Font("Segoe UI", Font.PLAIN, 22));
        table.setRowHeight(80);
        table.setIntercellSpacing(new Dimension(10, 10));
        table.getColumnModel().getColumn(0).setPreferredWidth(120);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 20));

        table.setBackground(new Color(45, 138, 75)); // Slight greenish
        table.setForeground(Color.WHITE);
        table.setSelectionBackground(new Color(70, 170, 90)); // Greenish highlight
        table.setSelectionForeground(Color.WHITE);
        table.setGridColor(new Color(60, 140, 80));

        table.getTableHeader().setBackground(new Color(30, 118, 55));
        table.getTableHeader().setForeground(Color.WHITE);

        table.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                table.clearSelection();
                if (row > -1) {
                    table.addRowSelectionInterval(row, row);
                }
            }
        });

        // Icon column renderer
        table.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = new JLabel(value != null ? value.toString() : "", SwingConstants.CENTER);
                label.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
                label.setOpaque(true);
                if (isSelected) {
                    label.setBackground(table.getSelectionBackground());
                    label.setForeground(table.getSelectionForeground());
                } else {
                    label.setBackground(table.getBackground());
                    label.setForeground(table.getForeground());
                }
                return label;
            }
        });

        // Service and Cost column renderers
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        centerRenderer.setFont(new Font("Segoe UI", Font.PLAIN, 22));
        centerRenderer.setForeground(Color.WHITE);
        centerRenderer.setBackground(new Color(45, 138, 75));

        table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);

        JScrollPane scrollPane = new JScrollPane(table);
        frame.add(scrollPane, BorderLayout.CENTER);

        JLabel totalCostLabel = new JLabel("Total Monthly Cost: $0.00");
        totalCostLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        totalCostLabel.setForeground(Color.WHITE);
        totalCostLabel.setHorizontalAlignment(SwingConstants.CENTER);
        frame.add(totalCostLabel, BorderLayout.NORTH);

        // Input panel
        JPanel inputPanel = new JPanel(new GridLayout(3, 3, 10, 10));
        inputPanel.setBackground(new Color(40, 138, 70)); // Darker greenish panel
        inputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JTextField serviceField = new JTextField();
        JTextField costField = new JTextField();

        JButton addBtn = new JButton("➕ Add");
        JButton removeBtn = new JButton("🗑️ Remove");
        JButton updateBtn = new JButton("✏️ Update Cost");

        Font buttonFont = new Font("Segoe UI Emoji", Font.PLAIN, 27);
        Insets buttonMargin = new Insets(10, 20, 10, 20);

        addBtn.setFont(buttonFont);
        removeBtn.setFont(buttonFont);
        updateBtn.setFont(buttonFont);

        addBtn.setMargin(buttonMargin);
        removeBtn.setMargin(buttonMargin);
        updateBtn.setMargin(buttonMargin);

        // Button color styling
        Color buttonColor = new Color(50, 130, 70);
        addBtn.setBackground(buttonColor);
        removeBtn.setBackground(buttonColor);
        updateBtn.setBackground(buttonColor);

        addBtn.setForeground(Color.WHITE);
        removeBtn.setForeground(Color.WHITE);
        updateBtn.setForeground(Color.WHITE);

        JComboBox<String> iconCombo = new JComboBox<>(new String[]{
            "🎬", "🎵", "📚", "🎮", "📺", "💻", "🚗", "🍔", "🛒", "📱", "🏠", "✈️", "🛍️", "⚽", "🎉"
        });
        iconCombo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));

        inputPanel.add(new JLabel("Service Name:", JLabel.RIGHT)).setForeground(Color.WHITE);
        inputPanel.add(serviceField);
        inputPanel.add(iconCombo);

        inputPanel.add(new JLabel("Cost ($):", JLabel.RIGHT)).setForeground(Color.WHITE);
        inputPanel.add(costField);
        inputPanel.add(updateBtn);

        inputPanel.add(new JLabel(""));
        inputPanel.add(addBtn);
        inputPanel.add(removeBtn);

        frame.add(inputPanel, BorderLayout.SOUTH);

        // Runnable to update total cost
        Runnable updateTotalCost = () -> {
            double total = 0;
            for (int i = 0; i < model.getRowCount(); i++) {
                total += Double.parseDouble(model.getValueAt(i, 2).toString());
            }
            totalCostLabel.setText("Total Monthly Cost: $" + String.format("%.2f", total));
        };

        // Load subscriptions
        Runnable loadSubscriptions = () -> {
            model.setRowCount(0);
            try (Connection conn = getConnection()) {
                PreparedStatement stmt = conn.prepareStatement("SELECT service_name, cost, icon FROM subscriptions WHERE user_id = ?");
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    String service = rs.getString("service_name");
                    double cost = rs.getDouble("cost");
                    String icon = rs.getString("icon");
                    if (icon == null) icon = "📦";
                    model.addRow(new Object[]{icon, service, cost});
                }
                updateTotalCost.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        // ➕ Add Subscription
        addBtn.addActionListener(e -> {
            String serviceName = serviceField.getText().trim();
            String costText = costField.getText().trim();

            if (serviceName.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "⚡ Oops! You forgot to input Service Name.", "Missing Input", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (costText.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "⚡ Oops! You forgot to input Cost.", "Missing Input", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try (Connection conn = getConnection()) {
                PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO subscriptions (user_id, service_name, cost, icon) VALUES (?, ?, ?, ?)"
                );
                stmt.setInt(1, userId);
                stmt.setString(2, serviceName);
                stmt.setDouble(3, Double.parseDouble(costText));
                stmt.setString(4, (String) iconCombo.getSelectedItem());
                stmt.executeUpdate();

                model.addRow(new Object[]{
                    iconCombo.getSelectedItem(), serviceName, Double.parseDouble(costText)
                });

                serviceField.setText("");
                costField.setText("");
                updateTotalCost.run();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // 🗑️ Remove Subscription
        removeBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(frame, "⚠️ Please select a valid subscription to remove.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String service = model.getValueAt(row, 1).toString();
            try (Connection conn = getConnection()) {
                PreparedStatement stmt = conn.prepareStatement(
                    "DELETE FROM subscriptions WHERE user_id=? AND service_name=?"
                );
                stmt.setInt(1, userId);
                stmt.setString(2, service);
                stmt.executeUpdate();

                model.removeRow(row);
                updateTotalCost.run();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // ✏️ Update Subscription Cost
        updateBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(frame, "⚠️ Please select a valid subscription to update cost.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String costText = costField.getText().trim();
            if (costText.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "⚡ Oops! You forgot to input new Cost.", "Missing Input", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String service = model.getValueAt(row, 1).toString();
            double newCost = Double.parseDouble(costText);

            try (Connection conn = getConnection()) {
                PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE subscriptions SET cost=? WHERE user_id=? AND service_name=?"
                );
                stmt.setDouble(1, newCost);
                stmt.setInt(2, userId);
                stmt.setString(3, service);
                stmt.executeUpdate();

                model.setValueAt(newCost, row, 2);
                updateTotalCost.run();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        loadSubscriptions.run();
        frame.setVisible(true);
    }
}
