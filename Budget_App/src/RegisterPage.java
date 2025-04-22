import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;



	//This section will allow user to create a new account for application
class RegisterPage extends JFrame 
{
    private LineTextField nameField, usernameField, emailField;
    private LinePasswordField passwordField;
    private JLabel nameError, usernameError, emailError, passwordError;

    public RegisterPage() {
        // Frame setup
        setTitle("Sign Up");
        setSize(350, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);
        
        // Color scheme
        Container cp = getContentPane();
        cp.setBackground(Color.WHITE);

        // Modern form fields (using your custom styled components)
        nameField = new LineTextField("Full Name");
        nameField.setBounds(50, 50, 250, 40);
        add(nameField);

        usernameField = new LineTextField("Username");
        usernameField.setBounds(50, 120, 250, 40);
        add(usernameField);

        emailField = new LineTextField("Email");
        emailField.setBounds(50, 190, 250, 40);
        add(emailField);

        passwordField = new LinePasswordField("Password");
        passwordField.setBounds(50, 260, 250, 40);
        add(passwordField);

        // Error labels
        nameError = new JLabel(" ");
        nameError.setForeground(Color.RED);
        nameError.setBounds(50, 90, 250, 15);
        add(nameError);

        usernameError = new JLabel(" ");
        usernameError.setForeground(Color.RED);
        usernameError.setBounds(50, 160, 250, 15);
        add(usernameError);

        emailError = new JLabel(" ");
        emailError.setForeground(Color.RED);
        emailError.setBounds(50, 230, 250, 15);
        add(emailError);

        passwordError = new JLabel(" ");
        passwordError.setForeground(Color.RED);
        passwordError.setBounds(50, 300, 250, 15);
        add(passwordError);

        // Continue button (styled like login button)
        JButton continueButton = new JButton("Continue");
        continueButton.setBounds(50, 350, 250, 35);
        continueButton.setBackground(new Color(0, 127, 255));
        continueButton.setForeground(Color.WHITE);
        continueButton.setFocusPainted(false);
        continueButton.addActionListener(e -> Register());
        add(continueButton);

        // Login redirect (styled as link)
        JButton loginRedirect = new JButton("<HTML><U>Already have an account? Login</U></HTML>");
        loginRedirect.setBounds(50, 400, 250, 30);
        loginRedirect.setHorizontalAlignment(SwingConstants.CENTER);
        loginRedirect.setBorderPainted(false);
        loginRedirect.setContentAreaFilled(false);
        loginRedirect.setForeground(Color.BLACK);
        loginRedirect.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginRedirect.addActionListener(e -> {
            dispose();
            new LoginApp();
        });
        add(loginRedirect);

        setVisible(true);
    }

    private void Register() {
        // Clear previous errors
        nameError.setText("");
        usernameError.setText("");
        emailError.setText("");
        passwordError.setText("");

        // Validate fields
        boolean hasError = false;
        
        if (nameField.getText().trim().isEmpty()) {
            nameError.setText("Please enter your name");
            hasError = true;
        }
        
        if (usernameField.getText().trim().isEmpty()) {
            usernameError.setText("Please enter a username");
            hasError = true;
        }
        
        if (emailField.getText().trim().isEmpty() || !emailField.getText().contains("@")) {
            emailError.setText("Please enter a valid email");
            hasError = true;
        }
        
        if (passwordField.getPassword().length == 0) {
            passwordError.setText("Please enter a password");
            hasError = true;
        }

        if (hasError) return;

        // Database operation
        try (Connection conn = BudgetApp.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO users (name, username, email, password) VALUES (?, ?, ?, ?)");
            
            stmt.setString(1, nameField.getText().trim());
            stmt.setString(2, usernameField.getText().trim());
            stmt.setString(3, emailField.getText().trim());
            stmt.setString(4, String.valueOf(passwordField.getPassword()));
            
            stmt.executeUpdate();
            
            JOptionPane.showMessageDialog(this, "Account created successfully!");
            dispose();
            new LoginApp();
            
        } catch (SQLException e) {
            if (e.getMessage().contains("username")) {
                usernameError.setText("Username already exists");
            } else if (e.getMessage().contains("email")) {
                emailError.setText("Email already registered");
            } else {
                JOptionPane.showMessageDialog(this, "Registration failed: " + e.getMessage());
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
}