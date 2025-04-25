package budgetapp.pages;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import budgetapp.auth.LoginApp;
import budgetapp.connection.DatabaseConnection;
import budgetapp.controller.MenuUtilities;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SubscriptionPage  {
	  
public static void showSubscriptionPanel(int userId) {
    JFrame frame = new JFrame("My Subscriptions");
    frame.setSize(600, 350);
    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    frame.setLayout(new BorderLayout());

    DefaultTableModel model = new DefaultTableModel(new String[]{"Service", "Cost"}, 0);
    JTable table = new JTable(model);
    frame.add(new JScrollPane(table), BorderLayout.CENTER);

    // Total cost label
    JLabel totalCostLabel = new JLabel("Total Monthly Cost: $0.00");
    frame.add(totalCostLabel, BorderLayout.NORTH);

    // Input + button panel
    JPanel inputPanel = new JPanel(new GridLayout(2, 3, 5, 5));
    JTextField serviceField = new JTextField();
    JTextField costField = new JTextField();
    JButton addBtn = new JButton("Add");
    JButton removeBtn = new JButton("Remove");
    JButton updateBtn = new JButton("Update Cost");
    inputPanel.add(new JLabel("Service:"));
    inputPanel.add(serviceField);
    inputPanel.add(addBtn);
    inputPanel.add(new JLabel("Cost:"));
    inputPanel.add(costField);
    inputPanel.add(updateBtn);
    inputPanel.add(new JLabel(""));
    inputPanel.add(removeBtn);
    frame.add(inputPanel, BorderLayout.SOUTH);

    // Function to refresh table and total
    Runnable loadSubscriptions = () -> {
        model.setRowCount(0);
        double total = 0;
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT service_name, cost FROM subscriptions WHERE user_id = ?");
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String service = rs.getString("service_name");
                double cost = rs.getDouble("cost");
                model.addRow(new Object[]{service, cost});
                total += cost;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        totalCostLabel.setText("Total Monthly Cost: $" + String.format("%.2f", total));
    };

    // Add
    addBtn.addActionListener(e -> {
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO subscriptions (user_id, service_name, cost) VALUES (?, ?, ?)");
            stmt.setInt(1, userId);
            stmt.setString(2, serviceField.getText());
            stmt.setDouble(3, Double.parseDouble(costField.getText()));
            stmt.executeUpdate();
            serviceField.setText("");
            costField.setText("");
            loadSubscriptions.run();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    });

    // Remove
    removeBtn.addActionListener(e -> {
        int row = table.getSelectedRow();
        if (row == -1) return;
        String service = model.getValueAt(row, 0).toString();
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM subscriptions WHERE user_id=? AND service_name=?");
            stmt.setInt(1, userId);
            stmt.setString(2, service);
            stmt.executeUpdate();
            loadSubscriptions.run();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    });

    // Update cost
    updateBtn.addActionListener(e -> {
        int row = table.getSelectedRow();
        if (row == -1) return;
        String service = model.getValueAt(row, 0).toString();
        double newCost = Double.parseDouble(costField.getText());

        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("UPDATE subscriptions SET cost=? WHERE user_id=? AND service_name=?");
            stmt.setDouble(1, newCost);
            stmt.setInt(2, userId);
            stmt.setString(3, service);
            stmt.executeUpdate();
            loadSubscriptions.run();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    });

    // Load table and total on startup
    loadSubscriptions.run();
    frame.add(MenuUtilities.createMenuPanel(frame, userId), BorderLayout.SOUTH);
    frame.setVisible(true);
    
}
   }

