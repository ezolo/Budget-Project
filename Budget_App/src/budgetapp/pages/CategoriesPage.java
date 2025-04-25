package budgetapp.pages;

import budgetapp.connection.DatabaseConnection;
import budgetapp.controller.MenuUtilities;
import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class CategoriesPage extends BaseFrame {
    private int userId;
    
    public CategoriesPage(int userId) {
    	super("categories", userId);
    	loadCategories(); 
        this.userId = userId;
        initUI();
        setVisible(true);
    }
    
    private void loadCategories() {
        // Load both predefined and user-added categories from database
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Load essential categories
            List<String> essentialCategories = loadCategoryList(conn, userId, true);
            // Load non-essential categories
            List<String> nonEssentialCategories = loadCategoryList(conn, userId, false);
            
            // Update your UI components here with the loaded categories
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading categories: " + e.getMessage());
        }
    }
    
    private List<String> loadCategoryList(Connection conn, int userId, boolean ispredefined) throws SQLException {
        List<String> categories = new ArrayList<>();
        String sql = "SELECT name FROM categories WHERE user_id = ? AND is_predefined = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setBoolean(2, ispredefined);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                categories.add(rs.getString("name"));
            }
        }
        return categories;
    }

    @Override
    protected void initUI() {
        // Main content setup
        contentPanel.setLayout(new BorderLayout());
        contentPanel.setBackground(new Color(242, 243, 247));
        
        // Title bar
        JPanel titleBarPanel = new JPanel(new BorderLayout());
        titleBarPanel.setBackground(new Color(0, 150, 0));
        titleBarPanel.setPreferredSize(new Dimension(getWidth(), 60));
        
        JLabel titleLabel = new JLabel("CATEGORIES", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        titleBarPanel.add(titleLabel, BorderLayout.CENTER);
        
        // Main content container
        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setBackground(new Color(242, 243, 247));
        
        // Add sections
        mainContent.add(createFullWidthSection("Essential Expenses",
            new String[]{"Housing", "Transportation", "Utilities", "Healthcare", "Groceries", "Loans"}));
        
        mainContent.add(Box.createRigidArea(new Dimension(0, 15)));
        
        mainContent.add(createFullWidthSection("Non-Essential Expenses",
            new String[]{"Entertainment", "Shopping", "Travel", "Subscriptions"}));
        
        // Add Category button
        JButton addButton = createAddButton();
        mainContent.add(Box.createRigidArea(new Dimension(0, 40)));
        mainContent.add(addButton);
        
        // Add components to frame
        contentPanel.add(titleBarPanel, BorderLayout.NORTH);
        contentPanel.add(new JScrollPane(mainContent), BorderLayout.CENTER);
        
        // Menu is automatically added by BaseFrame
    }

    private JButton createAddButton() {
        JButton button = new JButton("+ Add New Category");
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
            new AddCategoryPage(userId, () -> {
                dispose();
                new CategoriesPage(userId);
            }).setVisible(true);
        });
        
        return button;
    }

    private JPanel createFullWidthSection(String title, String[] categories) {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBackground(new Color(242, 243, 247));
        
        // Section header
        JPanel header = new JPanel();
        header.setBackground(Color.WHITE);
        header.setLayout(new BoxLayout(header, BoxLayout.X_AXIS));
        header.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setForeground(new Color(80, 80, 80));
        header.add(titleLabel);
        header.add(Box.createHorizontalGlue());
        
        // Category grid
        JPanel grid = new JPanel(new GridLayout(0, 3, 15, 15));
        grid.setBackground(new Color(242, 243, 247));
        
        for (String category : categories) {
            grid.add(createCategoryCard(category));
        }
        
        section.add(header);
        section.add(grid);
        return section;
    }


    private JPanel createCategoryCard(String categoryName) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(242, 243, 247)); // Exact match
        
        // Image container with matched background
        JPanel imageContainer = new JPanel(new BorderLayout());
        imageContainer.setBackground(new Color(242, 243, 247)); // Exact match
        imageContainer.setPreferredSize(new Dimension(120, 120));
        
        ImageIcon icon = loadCategoryIcon1(categoryName.toLowerCase());
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        iconLabel.setBackground(new Color(242, 243, 247)); // Exact match
        iconLabel.setOpaque(true); // Important for background to show
        
        imageContainer.add(iconLabel, BorderLayout.CENTER);
        card.add(imageContainer);
        
        // Category name
        JLabel nameLabel = new JLabel(categoryName, SwingConstants.CENTER);
        nameLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        nameLabel.setForeground(new Color(80, 80, 80));
        nameLabel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        card.add(nameLabel);
        
        return card;
    }

    
    // [Keep your existing image loading methods]
 // [Keep your existing loadCategoryIcon1, tryLoadIcon, and scaleIcon methods]
    private ImageIcon loadCategoryIcon1(String categoryName) {
        // Create case-insensitive mapping (convert to lowercase for comparison)
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
        categoryImages.put("subscriptions", "subscription_image.JPG"); // Note filename difference
        
        // Normalize the category name (lowercase, trim)
        String normalizedName = categoryName.toLowerCase().trim();
        
        // Get the correct image filename
        String imageFile = categoryImages.getOrDefault(normalizedName, "default_image.JPG");
        String fullPath = "/resources/categories/" + imageFile;
        
        ImageIcon icon = tryLoadIcon(fullPath);
        
        if (icon == null) {
            return tryLoadIcon("/resources/categories/default_image.JPG");
        }
        
        return scaleIcon(icon, 64, 64);
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
            // Create a blank icon as ultimate fallback
            BufferedImage blankImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            return new ImageIcon(blankImage);
        }
        return new ImageIcon(icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH));
    }
    
    public static void showCategoriesPanel(int userId) {
        new CategoriesPage(userId);
    }
    
}