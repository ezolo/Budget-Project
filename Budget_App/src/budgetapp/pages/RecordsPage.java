package budgetapp.pages;

import budgetapp.connection.DatabaseConnection;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class RecordsPage extends BaseFrame {
	private JPanel transactionsPanel;
	private JPanel headerPanel;

	public RecordsPage(int userId) {
		super("Records", userId);
		initUI();
	}

	@Override
	protected void initUI() {
		contentPanel.setLayout(new BorderLayout());

		// Header Section
		headerPanel = createHeaderPanel();
		contentPanel.add(headerPanel, BorderLayout.NORTH);

		// Transactions Section
		transactionsPanel = new JPanel();
		transactionsPanel.setLayout(new BoxLayout(transactionsPanel, BoxLayout.Y_AXIS));
		transactionsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		transactionsPanel.setBackground(Color.WHITE);

		loadTransactions();

		JScrollPane scrollPane = new JScrollPane(transactionsPanel);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		contentPanel.add(scrollPane, BorderLayout.CENTER);

		// Footer Section
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
		String sql = "SELECT SUM(amount) FROM expenses " +
				"WHERE user_id = ? AND type = ? " +
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

		JPanel footerPanel = new JPanel();
		footerPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		footerPanel.setBackground(Color.WHITE);
		footerPanel.add(addRecordButton);

		return footerPanel;
	}

	private void loadTransactions() {
		transactionsPanel.removeAll();
		Map<String, JPanel> datePanels = new LinkedHashMap<>();

		try (Connection conn = DatabaseConnection.getConnection()) {
			String sql = "SELECT e.id, e.expense_date, a.account_name, e.amount, e.description, c.name AS category_name, c.image_path " +
					"FROM expenses e " +
					"JOIN accounts a ON e.account_id = a.id " +
					"JOIN categories c ON e.category_id = c.id " +
					"WHERE e.user_id = ? " +
					"ORDER BY e.expense_date DESC";
			try (PreparedStatement stmt = conn.prepareStatement(sql)) {
				stmt.setInt(1, userId);
				ResultSet rs = stmt.executeQuery();
				while (rs.next()) {
					String date = rs.getString("expense_date");
					System.out.println("date" + date);
					JPanel datePanel = datePanels.computeIfAbsent(date, d -> createDatePanel(d));
					datePanel.add(createTransactionPanel(
							rs.getInt("id"),
							date,
							rs.getString("account_name"),
							rs.getString("amount"),
							rs.getString("description"),
							rs.getString("category_name"),
							rs.getString("image_path")
					));
				}
			}
		} catch (SQLException e) {
			System.out.println("Error loading transactions: " + e.getMessage());
			JOptionPane.showMessageDialog(this, "Error loading transactions: " + e.getMessage());
		}
		datePanels.values().forEach(transactionsPanel::add);
		transactionsPanel.revalidate();
		transactionsPanel.repaint();
	}

	private JPanel createDatePanel(String date) {
		JPanel datePanel = new JPanel();
		datePanel.setLayout(new BoxLayout(datePanel, BoxLayout.Y_AXIS));
		datePanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
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

		JLabel imageLabel = new JLabel();
		ImageIcon icon = loadCategoryIcon(categoryName.toLowerCase());
		if (icon != null) {
			imageLabel.setIcon(icon);
		} else {
			imageLabel.setText("No Image");
		}
		imageLabel.setPreferredSize(new Dimension(50, 50));
		panel.add(imageLabel, BorderLayout.WEST);

		JLabel detailsLabel = new JLabel("<html>" + date + " | " + account + " | " + categoryName + " | $" + amount + "<br>Description: " + description + "</html>");
		detailsLabel.setFont(new Font("Arial", Font.PLAIN, 14));
		panel.add(detailsLabel, BorderLayout.CENTER);

		JPanel buttonsPanel = createButtonsPanel(id);
		panel.add(buttonsPanel, BorderLayout.EAST);

		return panel;
	}

	private JPanel createButtonsPanel(int id) {
		JPanel buttonsPanel = new JPanel();
		buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
		buttonsPanel.setBackground(Color.WHITE);

		JButton editButton = createIconButton("/resources/records/edit.png", "Edit Record", e -> editTransaction(id));
		JButton deleteButton = createIconButton("/resources/records/delete.png", "Delete Record", e -> deleteTransaction(id));

		buttonsPanel.add(editButton);
		buttonsPanel.add(deleteButton);

		return buttonsPanel;
	}

	private JButton createIconButton(String iconPath, String tooltip, ActionListener actionListener) {
		ImageIcon scaledIcon = scaleIcon(new ImageIcon(getClass().getResource(iconPath)), 16, 16);
		JButton button = new JButton(scaledIcon);
		button.setToolTipText(tooltip);
		button.setFocusPainted(false);
		button.setContentAreaFilled(false);
		button.setBorderPainted(false);
		button.setCursor(new Cursor(Cursor.HAND_CURSOR));
		button.addActionListener(actionListener);
		return button;
	}

	private void editTransaction(int id) {
		new EditRecordPage(userId, id, this::refreshTransactions).setVisible(true);
	}

	private void deleteTransaction(int id) {
		int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this record?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
		if (confirm == JOptionPane.YES_OPTION) {
			try (Connection conn = DatabaseConnection.getConnection()) {
				String sql = "DELETE FROM expenses WHERE id = ?";
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
	private void refreshTransactions() {
		loadTransactions();
		refreshSummaryPanel();
	}

	private void refreshSummaryPanel() {
//		double totalExpenses = getMonthlyTotal("expenses");
//		double totalIncome = getMonthlyTotal("income");
//		double totalBalance = totalIncome - totalExpenses;


		contentPanel.remove(headerPanel); // Remove the old header panel
		headerPanel = createHeaderPanel();
		contentPanel.add(headerPanel, BorderLayout.NORTH); // Add the new header panel
		contentPanel.revalidate();
		contentPanel.repaint();
	}

	private ImageIcon loadCategoryIcon(String categoryName) {
		Map<String, String> categoryImages = new HashMap<>();
		categoryImages.put("housing", "house_image.JPG");
		categoryImages.put("utilities", "utilities_image.JPG");
		categoryImages.put("groceries", "groceries_image.JPG");
		categoryImages.put("transportation", "car_image.JPG");
		categoryImages.put("healthcare", "healthcare_image.JPG");
		categoryImages.put("loans", "loans_image.JPG");
		categoryImages.put("entertainment", "entertainment_image.JPG");
		categoryImages.put("travel", "travel_image.JPG");
		categoryImages.put("shopping", "shopping_image.JPG");
		categoryImages.put("subscriptions", "subscription_image.JPG");

		String imageFile = categoryImages.getOrDefault(categoryName, "default_image.JPG");
		String fullPath = "/resources/categories/" + imageFile;

		ImageIcon icon = tryLoadIcon(fullPath);
		if (icon == null) {
			return tryLoadIcon("/resources/categories/default_image.JPG");
		}
		return scaleIcon(icon, 50, 50);
	}

	private ImageIcon tryLoadIcon(String path) {
		try {
			URL imageUrl = getClass().getResource(path);
			if (imageUrl != null) {
				return new ImageIcon(imageUrl);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private ImageIcon scaleIcon(ImageIcon icon, int width, int height) {
		if (icon == null) {
			return null;
		}
		return new ImageIcon(icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH));
	}

}
