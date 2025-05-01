package budgetapp.pages;

import budgetapp.connection.DatabaseConnection;
import budgetapp.models.Category;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
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
import javax.imageio.ImageIO;

public class CategoriesPage extends BaseFrame {
    private int userId;
    private JPanel titleBarPanel;
    private JPanel footerPanel;

    // Constructor to initialize the CategoriesPage
    public CategoriesPage(int userId) {
        super("categories", userId);
        this.userId = userId;
        initUI();
        setVisible(true);
    }
    // Set layout and background for the content panel
    @Override
    protected void initUI() {
        contentPanel.setLayout(new BorderLayout());
        contentPanel.setBackground(new Color(242, 243, 247));

        initTitleBar();
        initFooter();
        loadCategories();
    }

    // Initialize the title bar with a label
    private void initTitleBar() {
        titleBarPanel = new JPanel(new BorderLayout());
        titleBarPanel.setBackground(new Color(0, 150, 0));
        titleBarPanel.setPreferredSize(new Dimension(getWidth(), 60));

        JLabel titleLabel = new JLabel("CATEGORIES", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        titleBarPanel.add(titleLabel, BorderLayout.CENTER);

        contentPanel.add(titleBarPanel, BorderLayout.NORTH);
    }

    private void initFooter() {
        footerPanel = new JPanel();
        footerPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        footerPanel.setBackground(Color.LIGHT_GRAY);

        JButton addButton = createAddButton();
        styleButton(addButton, new Color(0, 150, 0));

        JButton editButton = createEditButton();
        styleButton(editButton, new Color(70, 130, 180));

        JButton deleteButton = createDeleteButton();
        styleButton(deleteButton, new Color(255, 69, 58));

        footerPanel.add(addButton);
        footerPanel.add(editButton);
        footerPanel.add(deleteButton);

        contentPanel.add(footerPanel, BorderLayout.SOUTH);
    }

    private void loadCategories() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Solution 1: Single query with CASE statement to determine category type
            String sql = "SELECT c.name, c.description, " +
                    "CASE " +
                    "  WHEN c.name IN ('Housing', 'Utilities', 'Groceries', 'Transportation', 'Healthcare', 'Loans') THEN 'Essential' " +
                    "  WHEN c.name IN ('Entertainment', 'Travel', 'Shopping', 'Subscriptions') THEN 'NonEssential' " +
                    "  ELSE 'Other' " +
                    "END as category_type " +
                    "FROM categories c " +
                    "WHERE c.user_id = ? " +
                    "GROUP BY c.name, c.description";

            Map<String, List<Category>> categorized = new HashMap<>();

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    String type = rs.getString("category_type");
                    Category cat = new Category(rs.getString("name"), rs.getString("description"));
                    categorized.computeIfAbsent(type, k -> new ArrayList<>()).add(cat);
                }
            }

            JPanel mainContent = new JPanel();
            mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
            mainContent.setBackground(new Color(242, 243, 247));

            if (categorized.containsKey("Essential")) {
                mainContent.add(createFullWidthSection("Essential Expenses", categorized.get("Essential")));
                mainContent.add(Box.createRigidArea(new Dimension(0, 15)));
            }

            if (categorized.containsKey("NonEssential")) {
                mainContent.add(createFullWidthSection("Non-Essential Expenses", categorized.get("NonEssential")));
                mainContent.add(Box.createRigidArea(new Dimension(0, 15)));
            }

            if (categorized.containsKey("Other")) {
                mainContent.add(createFullWidthSection("Other Expenses", categorized.get("Other")));
            }

            Component center = ((BorderLayout)contentPanel.getLayout()).getLayoutComponent(BorderLayout.CENTER);
            if (center != null) {
                contentPanel.remove(center);
            }

            contentPanel.add(new JScrollPane(mainContent), BorderLayout.CENTER);
            contentPanel.revalidate();
            contentPanel.repaint();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading categories: " + e.getMessage());
        }
    }

    private JButton createAddButton() {
        JButton button = new JButton("Add New Category");
        button.addActionListener(e -> new AddCategoryPage(userId, this::loadCategories).setVisible(true));
        return button;
    }

    private JButton createEditButton() {
        JButton button = new JButton("Edit Category");
        button.addActionListener(e -> {
            String selectedCategory = showCategorySelectionDialog("Select Category to Edit");
            if (selectedCategory != null) {
                new EditCategoryPage(userId, selectedCategory, this::loadCategories).setVisible(true);
            }
        });
        return button;
    }

    private JButton createDeleteButton() {
        JButton button = new JButton("Delete Category");
        button.addActionListener(e -> {
            String selectedCategory = showCategorySelectionDialog("Select Category to Delete");
            if (selectedCategory != null) {
                int confirm = JOptionPane.showConfirmDialog(
                        this,
                        "Are you sure you want to delete '" + selectedCategory + "'?",
                        "Confirm Delete",
                        JOptionPane.YES_NO_OPTION
                );

                if (confirm == JOptionPane.YES_OPTION) {
                    deleteCategory(selectedCategory);
                }
            }
        });
        return button;
    }

    private String showCategorySelectionDialog(String title) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            List<String> categories = new ArrayList<>();
            String sql = "SELECT DISTINCT name FROM categories WHERE user_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    categories.add(rs.getString("name"));
                }
            }

            if (categories.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No categories found to edit/delete");
                return null;
            }

            return (String) JOptionPane.showInputDialog(
                    this,
                    "Select category:",
                    title,
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    categories.toArray(),
                    categories.get(0));
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading categories: " + e.getMessage());
            return null;
        }
    }

    private void deleteCategory(String categoryName) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "DELETE FROM categories WHERE user_id = ? AND name = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                stmt.setString(2, categoryName);
                int rowsAffected = stmt.executeUpdate();

                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Category deleted successfully");
                    loadCategories();
                } else {
                    JOptionPane.showMessageDialog(this, "Category not found or could not be deleted");
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error deleting category: " + e.getMessage());
        }
    }

    private JPanel createFullWidthSection(String title, List<Category> categories) {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBackground(new Color(242, 243, 247));

        JPanel header = new JPanel();
        header.setBackground(Color.WHITE);
        header.setLayout(new BoxLayout(header, BoxLayout.X_AXIS));
        header.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setForeground(new Color(80, 80, 80));
        header.add(titleLabel);
        header.add(Box.createHorizontalGlue());

        JPanel grid = new JPanel(new GridLayout(0, 3, 15, 15));
        grid.setBackground(new Color(242, 243, 247));

        for (Category category : categories) {
            grid.add(createCategoryCard(category.getName(), category.getDescription()));
        }

        section.add(header);
        section.add(grid);
        return section;
    }

    private JPanel createCategoryCard(String categoryName, String description) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(242, 243, 247));

        JPanel imageContainer = new JPanel(new BorderLayout());
        imageContainer.setBackground(new Color(242, 243, 247));
        imageContainer.setPreferredSize(new Dimension(120, 120));

        ImageIcon icon = loadCategoryIcon(categoryName.toLowerCase());
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);

        String tooltipText = description != null && !description.isEmpty() ?
                "<html><div style='width:200px;'>" + description + "</div></html>" :
                "No description available";
        iconLabel.setToolTipText(tooltipText);

        imageContainer.add(iconLabel, BorderLayout.CENTER);
        card.add(imageContainer);

        JLabel nameLabel = new JLabel(categoryName, SwingConstants.CENTER);
        nameLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        nameLabel.setForeground(new Color(80, 80, 80));
        nameLabel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        nameLabel.setToolTipText(tooltipText);
        card.add(nameLabel);

        return card;
    }

    private ImageIcon loadCategoryIcon(String categoryName) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT image_path FROM categories WHERE LOWER(name) = LOWER(?) AND user_id = ? ORDER BY id DESC LIMIT 1";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, categoryName);
                stmt.setInt(2, userId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    String imagePath = rs.getString("image_path");
                    if (imagePath != null && !imagePath.isEmpty()) {
                        try {
                            File imageFile = new File(imagePath);
                            if (imageFile.exists()) {
                                BufferedImage img = ImageIO.read(imageFile);
                                if (img != null) {
                                    return new ImageIcon(img.getScaledInstance(64, 64, Image.SCALE_SMOOTH));
                                }
                            }
                        } catch (IOException e) {
                            System.err.println("Error loading custom image: " + e.getMessage());
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error loading image: " + e.getMessage());
        }

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

        String imageFile = categoryImages.getOrDefault(categoryName.toLowerCase(), "default_image.JPG");
        String fullPath = "/resources/categories/" + imageFile;

        ImageIcon icon = tryLoadIcon(fullPath);
        return icon != null ? scaleIcon(icon, 64, 64) : scaleIcon(null, 64, 64);
    }

    private ImageIcon tryLoadIcon(String path) {
        try {
            URL imageUrl = getClass().getResource(path);
            if (imageUrl != null) {
                BufferedImage img = ImageIO.read(imageUrl);
                if (img != null) {
                    return new ImageIcon(img);
                }
            }

            File imageFile = new File(path);
            if (imageFile.exists()) {
                BufferedImage img = ImageIO.read(imageFile);
                if (img != null) {
                    return new ImageIcon(img);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading image from " + path + ": " + e.getMessage());
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

    private void styleButton(JButton button, Color backgroundColor) {
        button.setFont(new Font("SansSerif", Font.PLAIN, 12));
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 25, 8, 25));
        button.setPreferredSize(new Dimension(150, 40));
    }

    public static void showCategoriesPanel(int userId) {
        new CategoriesPage(userId);
    }
}