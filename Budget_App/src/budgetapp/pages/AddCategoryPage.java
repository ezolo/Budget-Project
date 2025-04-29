//Adding necessary packages
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

//This class represents the UI and logic for adding a new category
public class AddCategoryPage extends JFrame
{
    //ID of the logged-in user
    private final int userId;
    //Callback to refresh the parent page
    private final Runnable refreshCallback;
    //Label to display the selected image preview
    private JLabel imagePreviewLabel;
    //File object for the selected image
    private File selectedImageFile;
    // Text area for category description
    private JTextArea descriptionField;

    //Constructor to initialize the page with user ID and refresh callback
    public AddCategoryPage(int userId, Runnable refreshCallback) {
        this.userId = userId;
        this.refreshCallback = refreshCallback;
        // Set up the UI components
        initializeUI();
    }

    //Method to initialize the UI components
    private void initializeUI() {
        //Set the window title
        setTitle("Add New Category");
        //Set the window size
        setSize(400, 400);
        //Use BorderLayout for the frame
        setLayout(new BorderLayout());

        //Panel to hold form fields
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 5, 5));

        //Input fields for category name and description
        JTextField nameField = new JTextField();
        descriptionField = new JTextArea(3, 20);
        JScrollPane descriptionScroll = new JScrollPane(descriptionField);

        //Label to preview the selected image
        imagePreviewLabel = new JLabel("No image selected", SwingConstants.CENTER);
        imagePreviewLabel.setPreferredSize(new Dimension(150, 150));

        //Button to select an image
        JButton selectImageBtn = new JButton("Select Image");
        selectImageBtn.addActionListener(this::handleImageSelection);

        //Button to save the category
        JButton saveBtn = new JButton("Save Category");
        saveBtn.addActionListener(e -> saveCategory(
                userId,
                nameField.getText(),
                selectedImageFile,
                refreshCallback
        ));

        //Add components to the form panel
        formPanel.add(new JLabel("Category Name:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Description:"));
        formPanel.add(descriptionScroll);
        formPanel.add(new JLabel("Image:"));
        formPanel.add(selectImageBtn);
        formPanel.add(new JLabel("Preview:"));
        formPanel.add(imagePreviewLabel);

        //Add the form panel and save button to the frame
        add(formPanel, BorderLayout.CENTER);
        add(saveBtn, BorderLayout.SOUTH);

        //Adjust the frame size to fit components
        pack();
        //Center the frame on the screen
        setLocationRelativeTo(null);
    }

    //Method to handle image selection
    private void handleImageSelection(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Image files", "jpg", "png", "gif"));

        //Open file chooser and handle the selected file
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedImageFile = fileChooser.getSelectedFile();
            try {
                //Display the selected image in the preview label
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
    //Method to save the category to the database
    private void saveCategory(int userId, String name, File imageFile, Runnable callback) {
        if (name == null || name.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a category name", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            //Save the category to the database
            saveCategoryToDatabase(userId, name, imageFile, descriptionField.getText());
            if (callback != null) {
                //Refresh the parent page
                callback.run();
            }
            //Close the current window
            dispose();
        } catch (SQLException | IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error saving category: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    //Method to save the category details to the database
    private void saveCategoryToDatabase(int userId, String name, File imageFile, String description)
            throws SQLException, IOException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            //Start a transaction
            conn.setAutoCommit(false);
            //Check if the category already exists
            try {
                String checkSql = "SELECT id FROM categories WHERE user_id = ? AND name = ?";
                boolean exists = false;
                try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                    checkStmt.setInt(1, userId);
                    checkStmt.setString(2, name);
                    ResultSet rs = checkStmt.executeQuery();
                    exists = rs.next();
                }
                //SQL query to insert or update the category
                String sql;
                if (exists) {
                    sql = "UPDATE categories SET image_path = ?, description = ? WHERE user_id = ? AND name = ?";
                } else {
                    sql = "INSERT INTO categories (user_id, name, is_predefined, image_path, is_custom_image, description) " +
                            "VALUES (?, ?, FALSE, ?, TRUE, ?)";
                }
                //Execute the query
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
                //Commit the transaction
                conn.commit();
            } catch (Exception e) {
                //Rollback on error
                conn.rollback();
                throw e;
            } finally {
                //Restore auto-commit mode
                conn.setAutoCommit(true);
            }
        }
    }

    //Method to save the selected image to a directory
    private String saveCategoryImage(File imageFile) throws IOException {
        File directory = new File("custom_images");
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Failed to create directory: " + directory.getAbsolutePath());
        }
        //Generate a unique name for the image file
        String uniqueName = System.currentTimeMillis() + "_" + imageFile.getName();
        File destination = new File(directory, uniqueName);
        ImageIO.write(ImageIO.read(imageFile), "png", destination);
        return destination.getPath();
    }
}