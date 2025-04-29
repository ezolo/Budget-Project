package budgetapp.pages;

import budgetapp.connection.DatabaseConnection;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class RecordsPage extends BaseFrame {
	private JPanel transactionsPanel;

	public RecordsPage(int userId) {
		super("Records", userId);
		initUI();
	}

	@Override
	protected void initUI() {
		contentPanel.setLayout(new BorderLayout());

		contentPanel.add(createHeaderPanel(), BorderLayout.NORTH);

		transactionsPanel = new JPanel();
		transactionsPanel.setLayout(new BoxLayout(transactionsPanel, BoxLayout.Y_AXIS));
		transactionsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		transactionsPanel.setBackground(Color.WHITE);

		loadTransactions();

		JScrollPane scrollPane = new JScrollPane(transactionsPanel);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		contentPanel.add(scrollPane, BorderLayout.CENTER);

		contentPanel.add(createFooterPanel(), BorderLayout.SOUTH);
	}

	private JPanel createHeaderPanel() {
		JPanel headerPanel = new JPanel();
		headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
		headerPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
		headerPanel.setBackground(new Color(240, 240, 240));

		LocalDate currentDate = LocalDate.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM, yyyy");
		String currentMonthYear = currentDate.format(formatter);

		JLabel monthLabel = new JLabel(currentMonthYear, SwingConstants.CENTER);
		monthLabel.setFont(new Font("Arial", Font.BOLD, 24));
		monthLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		headerPanel.add(monthLabel);

		JPanel summaryPanel = new JPanel(new GridLayout(1, 3, 10, 10));
		summaryPanel.setBorder(new EmptyBorder(10, 0, 10, 0));
		summaryPanel.setBackground(new Color(240, 240, 240));

		double totalExpenses = getMonthlyTotal("expenses");
		double totalIncome = getMonthlyTotal("income");
		double totalBalance = totalIncome - totalExpenses;

		summaryPanel.add(createSummaryBox("Expenses", String.format("$%.2f", totalExpenses), Color.RED));
		summaryPanel.add(createSummaryBox("Income", String.format("$%.2f", totalIncome), new Color(34, 139, 34)));
		summaryPanel.add(createSummaryBox("Total", String.format("$%.2f", totalBalance), Color.BLACK));
		headerPanel.add(summaryPanel);

		return headerPanel;
	}

	private double getMonthlyTotal(String type) {
		double total = 0.0;
		String sql = "SELECT SUM(amount) FROM expenses WHERE user_id = ? AND type = ? " +
				"AND MONTH(expense_date) = MONTH(CURRENT_DATE()) " +
				"AND YEAR(expense_date) = YEAR(CURRENT_DATE())";

		try (Connection conn = DatabaseConnection.getConnection();
			 PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, userId);
			stmt.setString(2, type.equals("expenses") ? "expense" : "income");
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				total = rs.getDouble(1);
			}
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(this, "Error fetching " + type + ": " + e.getMessage());
		}
		return total;
	}

	private JPanel createFooterPanel() {
		JButton addRecordButton = new JButton("+ Add Record");
		addRecordButton.setFont(new Font("SansSerif", Font.BOLD, 16));
		addRecordButton.setBackground(new Color(0, 150, 0));
		addRecordButton.setForeground(Color.WHITE);
		addRecordButton.setFocusPainted(false);
		addRecordButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
		addRecordButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
		addRecordButton.addActionListener(e -> new AddRecordPage(userId, this::refreshTransactions).setVisible(true));

		JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		footerPanel.setBackground(Color.WHITE);
		footerPanel.add(addRecordButton);

		return footerPanel;
	}

	private void loadTransactions() {
		transactionsPanel.removeAll();
		Map<String, JPanel> datePanels = new HashMap<>();

		String sql = "SELECT e.id, e.expense_date, a.account_name, e.amount, e.description, c.name AS category_name, c.image_path " +
				"FROM expenses e " +
				"JOIN accounts a ON e.account_id = a.id " +
				"JOIN categories c ON e.category_id = c.id " +
				"WHERE e.user_id = ? " +
				"ORDER BY e.expense_date DESC";

		try (Connection conn = DatabaseConnection.getConnection();
			 PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, userId);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				String date = rs.getString("expense_date");
				JPanel datePanel = datePanels.computeIfAbsent(date, d -> createDatePanel(d));
				datePanel.add(createTransactionPanel(
						rs.getInt("id"),
						rs.getString("expense_date"),
						rs.getString("account_name"),
						rs.getString("amount"),
						rs.getString("description"),
						rs.getString("category_name"),
						rs.getString("image_path")
				));
			}
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(this, "Error loading transactions: " + e.getMessage());
		}

		datePanels.values().forEach(transactionsPanel::add);
		transactionsPanel.revalidate();
		transactionsPanel.repaint();
	}

	private JPanel createDatePanel(String date) {
		JPanel datePanel = new JPanel();
		datePanel.setLayout(new BoxLayout(datePanel, BoxLayout.Y_AXIS));
		datePanel.setBorder(new EmptyBorder(10, 0, 10, 0));
		datePanel.setBackground(Color.LIGHT_GRAY);

		JLabel dateLabel = new JLabel(date);
		dateLabel.setFont(new Font("Arial", Font.BOLD, 16));
		dateLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		datePanel.add(dateLabel);

		return datePanel;
	}

	private JPanel createTransactionPanel(int id, String date, String account, String amount, String description, String categoryName, String imagePath) {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
				new EmptyBorder(10, 10, 10, 10)
		));
		panel.setBackground(Color.WHITE);

		ImageIcon icon = null;
		if (imagePath != null && !imagePath.isEmpty()) {
			File imageFile = new File(imagePath);
			if (imageFile.exists()) {
				icon = new ImageIcon(new ImageIcon(imageFile.getAbsolutePath()).getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH));
			}
		}
		JLabel imageLabel = new JLabel(icon);
		imageLabel.setPreferredSize(new Dimension(64, 64));
		panel.add(imageLabel, BorderLayout.WEST);

		JLabel detailsLabel = new JLabel("<html>" + date + " | " + account + " | " + categoryName + " | $" + amount +
				"<br>Description: " + description + "</html>");
		detailsLabel.setFont(new Font("Arial", Font.PLAIN, 14));
		panel.add(detailsLabel, BorderLayout.CENTER);

		return panel;
	}

	private JPanel createSummaryBox(String title, String value, Color color) {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBackground(Color.WHITE);
		panel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));

		JLabel titleLabel = new JLabel(title);
		titleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
		titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

		JLabel valueLabel = new JLabel(value);
		valueLabel.setForeground(color);
		valueLabel.setFont(new Font("Arial", Font.BOLD, 16));
		valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

		panel.add(titleLabel);
		panel.add(Box.createVerticalStrut(5));
		panel.add(valueLabel);
		return panel;
	}

	public void refreshTransactions() {
		loadTransactions();
	}
}
