package budgetapp.pages;

import budgetapp.connection.DatabaseConnection;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EditCategoryPage extends JFrame {
    private final int userId;
    private final String categoryName;
    private final Runnable refreshCallback;
    private JLabel imagePreviewLabel;
    private File selectedImageFile;
    private JTextArea descriptionField;
    private JTextField nameField;

    public EditCategoryPage(int userId, String categoryName, Runnable refreshCallback) {
        this.userId = userId;
        this.categoryName = categoryName;
        this.refreshCallback = refreshCallback;
        initializeUI();
        loadCategoryData();
        setVisible(true);
    }

    private void initializeUI() {
        setTitle("Edit Category: " + categoryName);
        setSize(400, 400);
        setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(4, 2, 5, 5));

        nameField = new JTextField();
        descriptionField = new JTextArea(3, 20);
        JScrollPane descriptionScroll = new JScrollPane(descriptionField);

        imagePreviewLabel = new JLabel("No image selected", SwingConstants.CENTER);
        imagePreviewLabel.setPreferredSize(new Dimension(150, 150));

        JButton selectImageBtn = new JButton("Select Image");
        selectImageBtn.addActionListener(this::handleImageSelection);

        JButton saveBtn = new JButton("Save Changes");
        saveBtn.addActionListener(e -> saveChanges());

        formPanel.add(new JLabel("Category Name:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Description:"));
        formPanel.add(descriptionScroll);
        formPanel.add(new JLabel("Image:"));
        formPanel.add(selectImageBtn);
        formPanel.add(new JLabel("Preview:"));
        formPanel.add(imagePreviewLabel);

        add(formPanel, BorderLayout.CENTER);
        add(saveBtn, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
    }

    private void loadCategoryData() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT name, description, image_path FROM categories " +
                    "WHERE user_id = ? AND name = ? LIMIT 1";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                stmt.setString(2, categoryName);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    nameField.setText(rs.getString("name"));
                    descriptionField.setText(rs.getString("description"));

                    String imagePath = rs.getString("image_path");
                    if (imagePath != null && !imagePath.isEmpty()) {
                        try {
                            File imageFile = new File(imagePath);
                            if (imageFile.exists()) {
                                selectedImageFile = imageFile;
                                ImageIcon icon = new ImageIcon(ImageIO.read(imageFile)
                                        .getScaledInstance(150, 150, Image.SCALE_SMOOTH));
                                imagePreviewLabel.setIcon(icon);
                                imagePreviewLabel.setText("");
                            }
                        } catch (IOException e) {
                            System.err.println("Error loading image: " + e.getMessage());
                        }
                    }
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading category data: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
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

    private void saveChanges() {
        String newName = nameField.getText().trim();
        if (newName.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a category name", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                if (!newName.equalsIgnoreCase(categoryName)) {
                    String checkSql = "SELECT id FROM categories WHERE user_id = ? AND LOWER(name) = LOWER(?)";
                    try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                        checkStmt.setInt(1, userId);
                        checkStmt.setString(2, newName);
                        ResultSet rs = checkStmt.executeQuery();
                        if (rs.next()) {
                            JOptionPane.showMessageDialog(this,
                                    "A category with this name already exists",
                                    "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }
                }

                String updateSql = "UPDATE categories SET name = ?, description = ?, image_path = ? " +
                        "WHERE user_id = ? AND name = ?";

                try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                    String imagePath = null;
                    if (selectedImageFile != null) {
                        imagePath = saveCategoryImage(selectedImageFile);
                    } else {
                        String sql = "SELECT image_path FROM categories WHERE user_id = ? AND name = ? LIMIT 1";
                        try (PreparedStatement imgStmt = conn.prepareStatement(sql)) {
                            imgStmt.setInt(1, userId);
                            imgStmt.setString(2, categoryName);
                            ResultSet rs = imgStmt.executeQuery();
                            if (rs.next()) {
                                imagePath = rs.getString("image_path");
                            }
                        }
                    }

                    stmt.setString(1, newName);
                    stmt.setString(2, descriptionField.getText());
                    stmt.setString(3, imagePath);
                    stmt.setInt(4, userId);
                    stmt.setString(5, categoryName);

                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected > 0) {
                        conn.commit();
                        JOptionPane.showMessageDialog(this, "Category updated successfully");
                        refreshCallback.run();
                        dispose();
                    } else {
                        conn.rollback();
                        JOptionPane.showMessageDialog(this,
                                "Failed to update category", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException | IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Error saving changes: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String saveCategoryImage(File imageFile) throws IOException {
        File directory = new File("custom_images");
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Failed to create directory: " + directory.getAbsolutePath());
        }

        String uniqueName = System.currentTimeMillis() + "_" + imageFile.getName();
        File destination = new File(directory, uniqueName);
        ImageIO.write(ImageIO.read(imageFile), "png", destination);
        return destination.getPath();
    }
}