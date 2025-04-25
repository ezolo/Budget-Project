package budgetapp.pages;

import budgetapp.connection.DatabaseConnection;
import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
/*import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;*/
import javax.swing.border.EmptyBorder;
import budgetapp.pages.AddRecordPage;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


public class RecordsPage extends BaseFrame {
	private JPanel transactionsPanel;

	public RecordsPage(int userId) {
		super("records", userId);
		initUI();
	}

	@Override
	protected void initUI() {
		contentPanel.setLayout(new BorderLayout());

		// Header Section with Month and Totals
		JPanel headerPanel = new JPanel();
		headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
		headerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		headerPanel.setBackground(Color.WHITE);

		// Get current month and year dynamically
		LocalDate currentDate = LocalDate.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM, yyyy");
		String currentMonthYear = currentDate.format(formatter);

		JLabel monthLabel = new JLabel(currentMonthYear, SwingConstants.CENTER);
		monthLabel.setFont(new Font("Arial", Font.BOLD, 18));
		monthLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		headerPanel.add(monthLabel);

		JPanel summaryPanel = new JPanel(new GridLayout(1, 3, 10, 10));
		summaryPanel.setBorder(new EmptyBorder(10, 0, 10, 0));
		summaryPanel.add(createSummaryBox("Expenses", "$1,000.00", Color.RED));
		summaryPanel.add(createSummaryBox("Income", "$5,000.00", new Color(34, 139, 34)));
		summaryPanel.add(createSummaryBox("Total", "$2,000.00", Color.BLACK));
		headerPanel.add(summaryPanel);

		contentPanel.add(headerPanel, BorderLayout.NORTH);

		// Transaction Section
		transactionsPanel = new JPanel();
		transactionsPanel.setLayout(new BoxLayout(transactionsPanel, BoxLayout.Y_AXIS));
		transactionsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

		loadTransactions();

		JScrollPane scrollPane = new JScrollPane(transactionsPanel);
		contentPanel.add(scrollPane, BorderLayout.CENTER);

		// Add Record Button
		JButton addRecordButton = new JButton("+ Add Record");
		addRecordButton.setFont(new Font("SansSerif", Font.BOLD, 14));
		addRecordButton.setBackground(new Color(0, 150, 0));
		addRecordButton.setForeground(Color.WHITE);
		addRecordButton.setFocusPainted(false);
		addRecordButton.addActionListener(e -> new AddRecordPage(userId, this::refreshTransactions).setVisible(true));

		JPanel footerPanel = new JPanel();
		footerPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		footerPanel.add(addRecordButton);
		contentPanel.add(footerPanel, BorderLayout.SOUTH);
	}

	private void loadTransactions() {
		transactionsPanel.removeAll();
		try (Connection conn = DatabaseConnection.getConnection()) {
			String sql = "SELECT id, expense_date, month, account_id, amount, category_id FROM expenses WHERE user_id = ?";
			try (PreparedStatement stmt = conn.prepareStatement(sql)) {
				stmt.setInt(1, userId);
				ResultSet rs = stmt.executeQuery();
				while (rs.next()) {
					int id = rs.getInt("id");
					String date = rs.getString("date");
					String account = rs.getString("account");
					String amount = rs.getString("amount");
					String type = rs.getString("type");
					String category = rs.getString("category");
					transactionsPanel.add(createTransactionPanel(id, date, account, amount, type, category));
				}
			}
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(this, "Error loading transactions: " + e.getMessage());
		}
		transactionsPanel.revalidate();
		transactionsPanel.repaint();
	}

	private JPanel createTransactionPanel(int id, String date, String account, String amount, String type, String category) {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBorder(new EmptyBorder(5, 0, 5, 0));

		JLabel detailsLabel = new JLabel(date + " | " + account + " | " + category + " | " + type + " | $" + amount);
		detailsLabel.setFont(new Font("Arial", Font.PLAIN, 12));
		panel.add(detailsLabel, BorderLayout.CENTER);

		JButton deleteButton = new JButton("Delete");
		deleteButton.setFont(new Font("SansSerif", Font.BOLD, 12));
		deleteButton.setBackground(Color.RED);
		deleteButton.setForeground(Color.WHITE);
		deleteButton.addActionListener(e -> deleteTransaction(id));
		panel.add(deleteButton, BorderLayout.EAST);

		return panel;
	}

	private void deleteTransaction(int id) {
		int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this record?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
		if (confirm == JOptionPane.YES_OPTION) {
			try (Connection conn = DatabaseConnection.getConnection()) {
				String sql = "DELETE FROM records WHERE id = ?";
				try (PreparedStatement stmt = conn.prepareStatement(sql)) {
					stmt.setInt(1, id);
					stmt.executeUpdate();
				}
				JOptionPane.showMessageDialog(this, "Record deleted successfully!");
				refreshTransactions();
			} catch (SQLException e) {
				JOptionPane.showMessageDialog(this, "Error deleting record: " + e.getMessage());
			}
		}
	}

	private void refreshTransactions() {
		loadTransactions();
	}

	private JPanel createSummaryBox(String title, String value, Color color) {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBackground(Color.LIGHT_GRAY);
		panel.setBorder(new EmptyBorder(5, 5, 5, 5));

		JLabel titleLabel = new JLabel(title);
		JLabel valueLabel = new JLabel(value);
		valueLabel.setForeground(color);
		valueLabel.setFont(new Font("Arial", Font.BOLD, 14));

		panel.add(titleLabel);
		panel.add(valueLabel);
		return panel;
	}
};