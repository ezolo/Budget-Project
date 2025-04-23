package budgetapp.pages;

import budgetapp.connection.DatabaseConnection;
import budgetapp.controller.MenuUtilities;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
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
    
    private Map<String, String> essentialCategories;
    private Map<String, String> nonEssentialCategories;
    private Map<String, String> customCategories;

    private void loadCategories() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Load predefined categories (both essential and non-essential)
            essentialCategories = loadPredefinedCategories(conn, userId, true);
            nonEssentialCategories = loadPredefinedCategories(conn, userId, false);
            
            // Load only custom categories (user-added)
            customCategories = loadCustomCategories(conn, userId);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading categories: " + e.getMessage());
        }
    }

    private Map<String, String> loadPredefinedCategories(Connection conn, int userId, boolean isEssential) throws SQLException {
        Map<String, String> categories = new HashMap<>();
        String sql = "SELECT name, image_path FROM categories WHERE user_id = ? AND is_predefined = ? AND is_custom_image = FALSE";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setBoolean(2, isEssential);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                categories.put(rs.getString("name"), rs.getString("image_path"));
            }
        }
        return categories;
    }

    private Map<String, String> loadCustomCategories(Connection conn, int userId) throws SQLException {
        Map<String, String> categories = new HashMap<>();
        String sql = "SELECT name, image_path FROM categories WHERE user_id = ? AND is_custom_image = TRUE";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                categories.put(rs.getString("name"), rs.getString("image_path"));
            }
        }
        return categories;
    }


    @Override
    protected void initUI() {
        loadCategories(); // Load categories first
        
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
        mainContent.add(createFullWidthSection("Essential Expenses", true));
        mainContent.add(Box.createRigidArea(new Dimension(0, 15)));
        mainContent.add(createFullWidthSection("Non-Essential Expenses", false));
        
        // Add Custom Categories section only if there are custom categories
        if (customCategories != null && !customCategories.isEmpty()) {
            mainContent.add(Box.createRigidArea(new Dimension(0, 15)));
            mainContent.add(createCustomCategoriesSection());
        }
        
        // Add Category button
        JButton addButton = createAddButton();
        mainContent.add(Box.createRigidArea(new Dimension(0, 40)));
        mainContent.add(addButton);
        
        // Add components to frame
        contentPanel.add(titleBarPanel, BorderLayout.NORTH);
        contentPanel.add(new JScrollPane(mainContent), BorderLayout.CENTER);
    }
    
    private JPanel createCustomCategoriesSection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBackground(new Color(242, 243, 247));
        
        // Section header
        JPanel header = new JPanel();
        header.setBackground(Color.WHITE);
        header.setLayout(new BoxLayout(header, BoxLayout.X_AXIS));
        header.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        
        JLabel titleLabel = new JLabel("Custom Categories", SwingConstants.LEFT);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setForeground(new Color(80, 80, 80));
        header.add(titleLabel);
        header.add(Box.createHorizontalGlue());
        
        // Category grid
        JPanel grid = new JPanel(new GridLayout(0, 3, 15, 15));
        grid.setBackground(new Color(242, 243, 247));
        
        // Add all custom categories with their custom images
        for (Map.Entry<String, String> entry : customCategories.entrySet()) {
            grid.add(createCategoryCard(entry.getKey(), entry.getValue(), true));
        }
        
        section.add(header);
        section.add(grid);
        return section;
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
        
        // Modified action listener - removes popup behavior
        button.addActionListener(e -> {
            new AddCategoryPage(userId);  // Opens as full page
            dispose();  // Closes current CategoriesPage
        });
        
        return button;
    }

    private JPanel createFullWidthSection(String title, boolean isEssential) {
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
        
        Map<String, String> categories = isEssential ? essentialCategories : nonEssentialCategories;
        
        // Use default categories if none in DB
        String[] defaultCategories = isEssential ? 
            new String[]{"Housing", "Transportation", "Utilities", "Healthcare", "Groceries", "Loans"} :
            new String[]{"Entertainment", "Shopping", "Travel", "Subscriptions"};
        
        for (String category : defaultCategories) {
            // For predefined categories, always use the default images
            grid.add(createCategoryCard(category, null, false));
        }
        
        section.add(header);
        section.add(grid);
        return section;
    }
    
    private String getImagePathFromDatabase(String categoryName) {
        String sql = "SELECT image_path FROM categories WHERE user_id = ? AND name = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            stmt.setString(2, categoryName);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("image_path");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Return null if no image path found
    }

    private JPanel createCategoryCard(String categoryName, String imagePath, boolean isCustom) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(242, 243, 247));
        
        // Image container
        JPanel imageContainer = new JPanel(new BorderLayout());
        imageContainer.setBackground(new Color(242, 243, 247));
        imageContainer.setPreferredSize(new Dimension(120, 120));
        
        ImageIcon icon;
        try {
            if (isCustom && imagePath != null && !imagePath.isEmpty()) {
                // Load custom image for custom categories
                File imageFile = new File(imagePath);
                if (imageFile.exists()) {
                    icon = new ImageIcon(ImageIO.read(imageFile)
                        .getScaledInstance(64, 64, Image.SCALE_SMOOTH));
                } else {
                    icon = loadCategoryIcon1("default");
                }
            } else {
                // For predefined categories, always use default images
                icon = loadCategoryIcon1(categoryName.toLowerCase());
            }
        } catch (Exception e) {
            icon = loadCategoryIcon1("default");
        }
        
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        iconLabel.setBackground(new Color(242, 243, 247));
        iconLabel.setOpaque(true);
        
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
    
    private ImageIcon loadCustomCategoryIcon(String imagePath) {
        try {
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                Image image = ImageIO.read(imageFile);
                return new ImageIcon(image.getScaledInstance(64, 64, Image.SCALE_SMOOTH));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return loadCategoryIcon1("default"); // Fallback to default image
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