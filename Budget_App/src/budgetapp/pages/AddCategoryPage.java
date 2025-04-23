package budgetapp.pages;
import javax.swing.*;

import budgetapp.connection.DatabaseConnection;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AddCategoryPage extends JFrame {
    private final int userId;
    private final Runnable refreshCallback;
    private JLabel imagePreviewLabel;
    private File selectedImageFile;

    public AddCategoryPage(int userId, Runnable refreshCallback) {
        this.userId = userId;
        this.refreshCallback = refreshCallback;
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Add New Category");
        setSize(400, 300);
        setLayout(new BorderLayout());
        
        // Form panel
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        
        JTextField nameField = new JTextField();
        imagePreviewLabel = new JLabel("No image selected", SwingConstants.CENTER);
        imagePreviewLabel.setPreferredSize(new Dimension(150, 150));
        
        JButton selectImageBtn = new JButton("Select Image");
        selectImageBtn.addActionListener(this::handleImageSelection);
        
        JButton saveBtn = new JButton("Save Category");
        saveBtn.addActionListener(e -> saveCategory(
            userId, 
            nameField.getText(), 
            selectedImageFile, 
            refreshCallback
        ));

        formPanel.add(new JLabel("Category Name:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Image:"));
        formPanel.add(selectImageBtn);
        formPanel.add(new JLabel("Preview:"));
        formPanel.add(imagePreviewLabel);
        
        add(formPanel, BorderLayout.CENTER);
        add(saveBtn, BorderLayout.SOUTH);
        
        pack();
        setLocationRelativeTo(null);
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

    private void saveCategory(int userId, String name, File imageFile, Runnable callback) {
        if (name == null || name.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please enter a category name", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // Save to database (implementation depends on your DB structure)
            saveCategoryToDatabase(userId, name, imageFile);
            callback.run(); // Refresh the categories list
            dispose(); // Close the form
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
                // 1. Save category info
                String sql = "INSERT INTO categories (user_id, name, is_predefined, image_path, is_custom_image) " +
                            "VALUES (?, ?, FALSE, ?, TRUE)";
                
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    String imagePath = null;
                    if (imageFile != null) {
                        imagePath = saveCategoryImage(imageFile); // Implement this method
                    }
                    
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
        // Implement your image saving logic here
        // Return the path where the image was saved
        return "custom_images/" + imageFile.getName();
    }
}