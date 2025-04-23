package budgetapp.pages;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import budgetapp.connection.DatabaseConnection;

public class AddCategoryPage extends BaseFrame {
    private final int userId;
    private JLabel imagePreviewLabel;
    private File selectedImageFile;

    public AddCategoryPage(int userId) {  // Only takes userId now
        super("add_category", userId);    // BaseFrame parameters
        this.userId = userId;
        initUI();
        setVisible(true);
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
        
     // Create a centered panel for the title
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(0, -60, 0, 0)); // Adjust -60 for perfect centering
        
        JLabel titleLabel = new JLabel("ADD NEW CATEGORY");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        centerPanel.add(titleLabel);

        titleBarPanel.add(centerPanel, BorderLayout.CENTER);
        
        // Back button
        JButton backButton = new JButton("<html>&#8592; Back</html>");
        backButton.setFont(new Font("SansSerif", Font.PLAIN, 14));
        backButton.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        backButton.setBackground(new Color(0, 120, 0)); // Darker green to match title bar
        backButton.setForeground(Color.WHITE);
        backButton.setFocusPainted(false);
        backButton.setContentAreaFilled(false); // Makes the button transparent
        backButton.setOpaque(true); // Allows background color to show
        backButton.addActionListener(e -> {
            new CategoriesPage(userId);
            dispose();
        });
        titleBarPanel.add(backButton, BorderLayout.WEST);
        
        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(242, 243, 247));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JTextField nameField = new JTextField(20);
        imagePreviewLabel = new JLabel("No image selected", SwingConstants.CENTER);
        imagePreviewLabel.setPreferredSize(new Dimension(150, 150));
        
        JButton selectImageBtn = new JButton("Select Image");
        selectImageBtn.addActionListener(this::handleImageSelection);
        
        JButton saveBtn = new JButton("Save Category");
        saveBtn.addActionListener(e -> saveCategory(nameField.getText(), selectedImageFile));

        // Add components to form
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Category Name:"), gbc);
        
        gbc.gridx = 1;
        formPanel.add(nameField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Image:"), gbc);
        
        gbc.gridx = 1;
        formPanel.add(selectImageBtn, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.CENTER;
        formPanel.add(imagePreviewLabel, gbc);
        
        gbc.gridy = 3;
        formPanel.add(saveBtn, gbc);
        
        // Add components to frame
        contentPanel.add(titleBarPanel, BorderLayout.NORTH);
        contentPanel.add(formPanel, BorderLayout.CENTER);
    }

    private void handleImageSelection(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "Image files", "jpg", "png", "gif"));
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedImageFile = fileChooser.getSelectedFile();
            try {
                ImageIcon icon = new ImageIcon(ImageIO.read(selectedImageFile)
                    .getScaledInstance(150, 150, Image.SCALE_SMOOTH));
                imagePreviewLabel.setIcon(icon);
                imagePreviewLabel.setText("");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, 
                    "Error loading image", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void saveCategory(String name, File imageFile) {
        if (name == null || name.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please enter a category name", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            saveCategoryToDatabase(userId, name, imageFile);
            JOptionPane.showMessageDialog(this, 
                "Category added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            new CategoriesPage(userId);
            dispose();
        } catch (SQLException | IOException ex) {
            JOptionPane.showMessageDialog(this, 
                "Error saving category: " + ex.getMessage(), 
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveCategoryToDatabase(int userId, String name, File imageFile) 
    	    throws SQLException, IOException {
    	    
    	    try (Connection conn = DatabaseConnection.getConnection()) {
    	        conn.setAutoCommit(false);
    	        try {
    	            String sql = "INSERT INTO categories (user_id, name, is_predefined, image_path, is_custom_image) " +
    	                        "VALUES (?, ?, FALSE, ?, TRUE)";
    	            
    	            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
    	                String imagePath = saveCategoryImage(imageFile);
    	                stmt.setInt(1, userId);
    	                stmt.setString(2, name);
    	                stmt.setString(3, imagePath);
    	                stmt.executeUpdate();
    	            }
    	            conn.commit();
    	        } catch (Exception e) {
    	            conn.rollback();
    	            throw e;
    	        }
    	    }
    	}
    
    private String saveCategoryImage(File imageFile) throws IOException {
        // Create directory if it doesn't exist
        File dir = new File("user_images/" + userId);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        // Copy file to user directory
        String newFileName = System.currentTimeMillis() + "_" + imageFile.getName();
        File dest = new File(dir, newFileName);
        
        Files.copy(imageFile.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
        
        return dest.getAbsolutePath();
    
    }
}