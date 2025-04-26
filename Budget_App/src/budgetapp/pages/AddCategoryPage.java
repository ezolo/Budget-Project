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

public class AddCategoryPage extends JFrame {
    private final int userId;
    private final Runnable refreshCallback;
    private JLabel imagePreviewLabel;
    private File selectedImageFile;
    private JTextArea descriptionField;

    public AddCategoryPage(int userId, Runnable refreshCallback) {
        this.userId = userId;
        this.refreshCallback = refreshCallback;
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Add New Category");
        setSize(400, 400);
        setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(4, 2, 5, 5));

        JTextField nameField = new JTextField();
        descriptionField = new JTextArea(3, 20);
        JScrollPane descriptionScroll = new JScrollPane(descriptionField);

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
            saveCategoryToDatabase(userId, name, imageFile, descriptionField.getText());
            if (callback != null) {
                callback.run();
            }
            dispose();
        } catch (SQLException | IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error saving category: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveCategoryToDatabase(int userId, String name, File imageFile, String description)
            throws SQLException, IOException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String checkSql = "SELECT id FROM categories WHERE user_id = ? AND name = ?";
                boolean exists = false;
                try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                    checkStmt.setInt(1, userId);
                    checkStmt.setString(2, name);
                    ResultSet rs = checkStmt.executeQuery();
                    exists = rs.next();
                }

                String sql;
                if (exists) {
                    sql = "UPDATE categories SET image_path = ?, description = ? WHERE user_id = ? AND name = ?";
                } else {
                    sql = "INSERT INTO categories (user_id, name, is_predefined, image_path, is_custom_image, description) " +
                            "VALUES (?, ?, FALSE, ?, TRUE, ?)";
                }

                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    String imagePath = null;
                    if (imageFile != null) {
                        imagePath = saveCategoryImage(imageFile);
                    }

                    if (exists) {
                        stmt.setString(1, imagePath);
                        stmt.setString(2, description);
                        stmt.setInt(3, userId);
                        stmt.setString(4, name);
                    } else {
                        stmt.setInt(1, userId);
                        stmt.setString(2, name);
                        stmt.setString(3, imagePath);
                        stmt.setString(4, description);
                    }
                    stmt.executeUpdate();
                }

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
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