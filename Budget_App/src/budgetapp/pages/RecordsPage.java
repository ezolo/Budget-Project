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
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RecordsPage extends BaseFrame {
	private JPanel transactionsPanel;
	private JPanel headerPanel;
	private JComboBox<String> monthComboBox;
	private JComboBox<Integer> yearComboBox;
	private JComboBox<String> categoryComboBox;

	public RecordsPage(int userId) {
		super("Records", userId);
		initUI();
	}

	protected void initUI() {
		contentPanel.setLayout(new BorderLayout());

		// Create a container for the banner, filter, and summary panels
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BorderLayout());

		// Add green banner
		JPanel bannerPanel = new JPanel(new BorderLayout());
		bannerPanel.setBackground(new Color(0, 128, 0));
		JLabel bannerLabel = new JLabel("Expenses");
		bannerLabel.setForeground(Color.WHITE);
		bannerLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
		bannerLabel.setHorizontalAlignment(SwingConstants.CENTER);
		bannerPanel.add(bannerLabel, BorderLayout.CENTER);
		topPanel.add(bannerPanel, BorderLayout.NORTH);

		// Add filter panel
		JPanel filterPanel = createFilterPanel();
		topPanel.add(filterPanel, BorderLayout.CENTER);

		// Add summary panel
		headerPanel = createHeaderPanel(LocalDate.now().getMonth().name(), LocalDate.now().getYear(), "All Categories");
		topPanel.add(headerPanel, BorderLayout.SOUTH);

		// Add the combined top panel to the content panel
		contentPanel.add(topPanel, BorderLayout.NORTH);

		// Transactions Section
		transactionsPanel = new JPanel();
		transactionsPanel.setLayout(new BoxLayout(transactionsPanel, BoxLayout.Y_AXIS));
		transactionsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		transactionsPanel.setBackground(Color.WHITE);

		loadTransactions(LocalDate.now().getMonth().name(), LocalDate.now().getYear(), "All Categories");

		JScrollPane scrollPane = new JScrollPane(transactionsPanel);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		contentPanel.add(scrollPane, BorderLayout.CENTER);

		// Footer Section
		contentPanel.add(createFooterPanel(), BorderLayout.SOUTH);
	}

	private JPanel createFilterPanel() {
		JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
		filterPanel.setBackground(new Color(220, 220, 220));

		// Month dropdown
		String[] months = new String[12];
		for (int i = 0; i < 12; i++) {
			months[i] = Month.of(i + 1).getDisplayName(TextStyle.FULL, Locale.getDefault());
		}
		monthComboBox = new JComboBox<>(months);
		monthComboBox.setSelectedIndex(LocalDate.now().getMonthValue() - 1);

		// Year dropdown
		int currentYear = LocalDate.now().getYear();
		Integer[] years = {currentYear - 2, currentYear - 1, currentYear};
		yearComboBox = new JComboBox<>(years);
		yearComboBox.setSelectedItem(currentYear);

		// Category dropdown
		List<String> categories = new ArrayList<>();
		categories.add("All Categories");
		try (Connection conn = DatabaseConnection.getConnection()) {
			String sql = "SELECT name FROM categories ORDER BY name";
			try (PreparedStatement stmt = conn.prepareStatement(sql)) {
				ResultSet rs = stmt.executeQuery();
				while (rs.next()) {
					categories.add(rs.getString("name"));
				}
			}
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(this, "Error loading categories: " + e.getMessage());
		}
		categoryComboBox = new JComboBox<>(categories.toArray(new String[0]));
		categoryComboBox.setSelectedItem("All Categories");

		// Load button
		JButton loadButton = new JButton("Load");
		loadButton.addActionListener(e -> {
			String selectedMonth = (String) monthComboBox.getSelectedItem();
			int selectedYear = (Integer) yearComboBox.getSelectedItem();
			String selectedCategory = (String) categoryComboBox.getSelectedItem();
			loadTransactions(selectedMonth, selectedYear, selectedCategory);
			refreshSummaryPanel(selectedMonth, selectedYear, selectedCategory);
		});

		filterPanel.add(new JLabel("Select Month:"));
		filterPanel.add(monthComboBox);
		filterPanel.add(new JLabel("Year:"));
		filterPanel.add(yearComboBox);
		filterPanel.add(new JLabel("Category:"));
		filterPanel.add(categoryComboBox);
		filterPanel.add(loadButton);

		return filterPanel;
	}

	private JPanel createHeaderPanel(String selectedMonth, int selectedYear, String selectedCategory) {
		JPanel headerPanel = new JPanel();
		headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
		headerPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
		headerPanel.setBackground(new Color(240, 240, 240));

		JPanel summaryPanel = new JPanel(new GridLayout(1, 3, 10, 10));
		summaryPanel.setBorder(new EmptyBorder(10, 0, 10, 0));
		summaryPanel.setBackground(new Color(240, 240, 240));

		double totalExpenses = getMonthlyTotal("expenses", selectedMonth, selectedYear, selectedCategory);
		double totalIncome = getMonthlyTotal("income", selectedMonth, selectedYear, selectedCategory);
		double totalBalance = totalIncome - totalExpenses;

		summaryPanel.add(createSummaryBox("Expenses", String.format("$%.2f", totalExpenses), Color.RED));
		summaryPanel.add(createSummaryBox("Income", String.format("$%.2f", totalIncome), new Color(34, 139, 34)));
		summaryPanel.add(createSummaryBox("Total", String.format("$%.2f", totalBalance), Color.BLACK));
		headerPanel.add(summaryPanel);

		return headerPanel;
	}

	private double getMonthlyTotal(String type, String selectedMonth, int selectedYear, String selectedCategory) {
		double total = 0.0;
		String sql = "SELECT SUM(e.amount) FROM expenses e " +
				"JOIN categories c ON e.category_id = c.id " +
				"WHERE e.user_id = ? AND e.type = ? " +
				"AND MONTH(e.expense_date) = ? AND YEAR(e.expense_date) = ?";
		if (!selectedCategory.equals("All Categories")) {
			sql += " AND c.name = ?";
		}

		try (Connection conn = DatabaseConnection.getConnection();
			 PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, userId);
			stmt.setString(2, type.equals("expenses") ? "expense" : "income");
			stmt.setInt(3, Month.valueOf(selectedMonth.toUpperCase()).getValue());
			stmt.setInt(4, selectedYear);
			if (!selectedCategory.equals("All Categories")) {
				stmt.setString(5, selectedCategory);
			}
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
	private void loadTransactions(String selectedMonth, int selectedYear, String selectedCategory) {
		transactionsPanel.removeAll();
		Map<String, JPanel> datePanels = new LinkedHashMap<>();

		try (Connection conn = DatabaseConnection.getConnection()) {
			String sql = "SELECT e.id, e.expense_date, a.account_name, e.amount, e.description, c.name AS category_name, c.image_path " +
					"FROM expenses e " +
					"JOIN accounts a ON e.account_id = a.id " +
					"JOIN categories c ON e.category_id = c.id " +
					"WHERE e.user_id = ? AND MONTH(e.expense_date) = ? AND YEAR(e.expense_date) = ?";
			if (!selectedCategory.equals("All Categories")) {
				sql += " AND c.name = ?";
			}
			sql += " ORDER BY e.expense_date DESC";

			try (PreparedStatement stmt = conn.prepareStatement(sql)) {
				stmt.setInt(1, userId);
				stmt.setInt(2, Month.valueOf(selectedMonth.toUpperCase()).getValue());
				stmt.setInt(3, selectedYear);
				if (!selectedCategory.equals("All Categories")) {
					stmt.setString(4, selectedCategory);
				}
				ResultSet rs = stmt.executeQuery();
				while (rs.next()) {
					String date = rs.getString("expense_date");
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
		String selectedMonth = (String) monthComboBox.getSelectedItem();
		int selectedYear = (Integer) yearComboBox.getSelectedItem();
		String selectedCategory = (String) categoryComboBox.getSelectedItem();
		loadTransactions(selectedMonth, selectedYear, selectedCategory);
		refreshSummaryPanel(selectedMonth, selectedYear, selectedCategory);
	}

	private void refreshSummaryPanel(String selectedMonth, int selectedYear, String selectedCategory) {
		if (headerPanel != null) {
			JPanel topPanel = (JPanel) headerPanel.getParent();
			topPanel.remove(headerPanel);
		}

		headerPanel = createHeaderPanel(selectedMonth, selectedYear, selectedCategory);
		JPanel topPanel = (JPanel) contentPanel.getComponent(0); // Get the topPanel
		topPanel.add(headerPanel, BorderLayout.SOUTH);
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