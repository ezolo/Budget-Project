package budgetapp.pages;

import budgetapp.connection.DatabaseConnection;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.border.EmptyBorder;
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

		// Header Section
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
		summaryPanel.add(createSummaryBox("Expenses", "$1,000.00", Color.RED));
		summaryPanel.add(createSummaryBox("Income", "$5,000.00", new Color(34, 139, 34)));
		summaryPanel.add(createSummaryBox("Total", "$2,000.00", Color.BLACK));
		headerPanel.add(summaryPanel);

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
		JButton addRecordButton = new JButton("+ Add Record");
		addRecordButton.setFont(new Font("SansSerif", Font.BOLD, 16));
		addRecordButton.setBackground(new Color(0, 150, 0));
		addRecordButton.setForeground(Color.WHITE);
		addRecordButton.setFocusPainted(false);
		addRecordButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
		addRecordButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
		addRecordButton.addActionListener(e -> {
			new AddRecordPage(userId, this::refreshTransactions).setVisible(true);
		});

		JPanel footerPanel = new JPanel();
		footerPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		footerPanel.setBackground(Color.WHITE);
		footerPanel.add(addRecordButton);
		contentPanel.add(footerPanel, BorderLayout.SOUTH);
	}

	private void loadTransactions() {
		transactionsPanel.removeAll();
		try (Connection conn = DatabaseConnection.getConnection()) {
			String sql = "SELECT e.id, e.expense_date, e.account_id, e.amount, e.description, c.name AS category_name, c.image_path " +
					"FROM expenses e " +
					"JOIN categories c ON e.category_id = c.id " +
					"WHERE e.user_id = ?";
			try (PreparedStatement stmt = conn.prepareStatement(sql)) {
				stmt.setInt(1, userId);
				ResultSet rs = stmt.executeQuery();
				while (rs.next()) {
					int id = rs.getInt("id");
					String date = rs.getString("expense_date");
					String account = rs.getString("account_id");
					String amount = rs.getString("amount");
					String description = rs.getString("description");
					String categoryName = rs.getString("category_name");
					String imagePath = rs.getString("image_path");
					transactionsPanel.add(createTransactionPanel(id, date, account, amount, description, categoryName, imagePath));
				}
			}
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(this, "Error loading transactions: " + e.getMessage());
		}
		transactionsPanel.revalidate();
		transactionsPanel.repaint();
	}

	private JPanel createTransactionPanel(int id, String date, String account, String amount, String description, String categoryName, String imagePath) {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
				new EmptyBorder(10, 10, 10, 10)
		));
		panel.setBackground(Color.WHITE);

		// Load the category image
		JLabel imageLabel = new JLabel();
		ImageIcon icon = loadCategoryIcon1(categoryName.toLowerCase());
		if (icon != null) {
			imageLabel.setIcon(icon);
		} else {
			imageLabel.setText("No Image");
		}
		imageLabel.setPreferredSize(new Dimension(50, 50));
		panel.add(imageLabel, BorderLayout.WEST);

		// Transaction details
		JLabel detailsLabel = new JLabel("<html>" + date + " | " + account + " | " + categoryName + " | $" + amount + "<br>Description: " + description + "</html>");
		detailsLabel.setFont(new Font("Arial", Font.PLAIN, 14));
		panel.add(detailsLabel, BorderLayout.CENTER);

		// Delete button
		JButton deleteButton = new JButton("Delete");
		deleteButton.setFont(new Font("SansSerif", Font.BOLD, 12));
		deleteButton.setBackground(Color.RED);
		deleteButton.setForeground(Color.WHITE);
		deleteButton.setFocusPainted(false);
		deleteButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
		deleteButton.addActionListener(e -> deleteTransaction(id));
		panel.add(deleteButton, BorderLayout.EAST);

		return panel;
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

	private void refreshTransactions() {
		loadTransactions();
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

	private ImageIcon loadCategoryIcon1(String categoryName) {
		Map<String, String> categoryImages = new HashMap<>();

		// Essential Expenses
		categoryImages.put("housing", "house_image.JPG");
		categoryImages.put("utilities", "utilities_image.JPG");
		categoryImages.put("groceries", "groceries_image.JPG");
		categoryImages.put("transportation", "car_image.JPG");
		categoryImages.put("healthcare", "healthcare_image.JPG");
		categoryImages.put("loans", "loans_image.JPG");

		// Non-Essential Expenses
		categoryImages.put("entertainment", "entertainment_image.JPG");
		categoryImages.put("travel", "travel_image.JPG");
		categoryImages.put("shopping", "shopping_image.JPG");
		categoryImages.put("subscriptions", "subscription_image.JPG");

		String normalizedName = categoryName.toLowerCase().trim();
		String imageFile = categoryImages.getOrDefault(normalizedName, "default_image.JPG");
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
			BufferedImage blankImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			return new ImageIcon(blankImage);
		}
		return new ImageIcon(icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH));
	}
}