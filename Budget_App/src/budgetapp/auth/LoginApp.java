package budgetapp.auth;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import budgetapp.components.*;
import budgetapp.connection.DatabaseConnection;
import budgetapp.pages.RecordsPage;
import budgetapp.pages.RegisterPage;

import javax.swing.*;
import java.awt.*;


//This is where we set up the GUI & event handlers for Login page
	public class LoginApp extends JFrame 
	{
		//private JTextField usernameField;
		//private JPasswordField passwordField;
		private LineTextField usernameField;
		private LinePasswordField passwordField;
		private JLabel usernameError;
	    private JLabel passwordError;
	    public static int user_id;
	    
	    public static int getUserId() {
	       return user_id;
	    }

  public LoginApp() 
  {
  	//Setting name, size, and layout of page
      setTitle("Login");
      setSize(350, 400);
      setDefaultCloseOperation(EXIT_ON_CLOSE);
      //New next 2 lines
      setLocationRelativeTo(null);
      setLayout(null);
      //setLayout(new GridLayout(4, 1));
      
      //Color of Container (new next 2 lines)
      Container cp = getContentPane();
		cp.setBackground(Color.WHITE);

		usernameField = new LineTextField("Username");
      usernameField.setBounds(50, 50, 250, 40);
      add(usernameField);

      passwordField = new LinePasswordField("Password");
      passwordField.setBounds(50, 110, 250, 40);
      add(passwordField);
    
    
   // Replace your current eye toggle button code with this:
      JButton eyeToggle = new JButton("👁");
      eyeToggle.setBounds(285, 112, 40, 40); // Slightly larger for visibility
      eyeToggle.setBackground(Color.WHITE);  // Match your container's background
      eyeToggle.setOpaque(true);   
      eyeToggle.setFocusable(false);
      eyeToggle.setBorderPainted(false); 
      eyeToggle.setBorder(BorderFactory.createEmptyBorder()); 

   // Action Listener
      eyeToggle.addActionListener(e -> {
          if (passwordField.getEchoChar() == '•') {
              passwordField.setEchoChar((char) 0);  // Show password
              eyeToggle.setText("👁"); // Lock icon
          } else {
              passwordField.setEchoChar('•');       // Hide password
              eyeToggle.setText("👁"); // Eye icon
          }
      });
      
      // Add it to the frame RIGHT AFTER password field
      add(passwordField);
      add(eyeToggle);

      // Force UI refresh
      revalidate();
      repaint(); 
      
      JButton loginButton = new JButton("Login");
      loginButton.setBounds(50, 190, 250, 35);
      loginButton.setBackground(new Color(0, 127, 255));
      loginButton.setForeground(Color.WHITE);
      add(loginButton);
      
      JButton registerButton = new JButton("<HTML><U>Create an  Account</U></HTML>");
      registerButton.setHorizontalAlignment(SwingConstants.LEFT);
      registerButton.setBorderPainted(false);
      registerButton.setOpaque(false);
      registerButton.setForeground(Color.BLACK);
      registerButton.setBackground(Color.WHITE);
      registerButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
      registerButton.setBounds(105, 220, 250, 35);
      add(registerButton);

      usernameError = new JLabel(" ");
      usernameError.setForeground(Color.RED);
      usernameError.setBounds(50, 90, 250, 15);
      add(usernameError);

      passwordError = new JLabel(" ");
      passwordError.setForeground(Color.RED);
      passwordError.setBounds(50, 150, 250, 15);
      add(passwordError);
		
      loginButton.addActionListener(e -> Login());
      registerButton.addActionListener(e -> 
      {
          dispose();
          new RegisterPage();
      });
      setVisible(true);
  }
   
 

  //Setting up how login credentials are checked on DB end
  private void Login() {
	    // Clear previous error messages
	    usernameError.setText("");
	    passwordError.setText("");

	    // Get the user inputs
	    String username = usernameField.getText().trim();
	    String password = String.valueOf(passwordField.getPassword()).trim();

	    // Validate fields
	    boolean hasError = false;
	    
	 // To this (more robust check):
	    if (username.isEmpty() || usernameField.isPlaceholderActive()) {
	        usernameError.setText("Please enter your username.");
	        hasError = true;
	    }
	    
	 // To this (more robust check):
	    if (username.isEmpty() || usernameField.isPlaceholderActive()) {
	        passwordError.setText("Please enter your password.");
	        hasError = true;
	    }

	    if (hasError) return;
	    
	    try (Connection conn = DatabaseConnection.getConnection()) {
	        // FIRST: Check if username exists
	    	PreparedStatement userStmt = conn.prepareStatement("SELECT * FROM users WHERE BINARY username=?");
	        userStmt.setString(1, username);
	        ResultSet userRs = userStmt.executeQuery();

	        if (!userRs.next()) {
	            usernameError.setText("Username not found.");
	            usernameField.requestFocus();
	            return;
	        }

	        // SECOND: Verify password (in a real app, use password hashing!)
	        String dbPassword = userRs.getString("password");
	        if (!dbPassword.equals(password)) {
	            passwordError.setText("Invalid password.");
	            passwordField.setText("");
	            passwordField.requestFocus();
	            return;
	        }

	        // Only if both checks pass
	        int userId = userRs.getInt("id");
	        JOptionPane.showMessageDialog(this, "Login successful!");
	        dispose();
	       // new RecordsPage(user_id);
	        
	        SwingUtilities.invokeLater(() -> {
	        	 new RecordsPage(userId);  // Center on screen
	        });
	        
	    } catch (Exception e) {
	        e.printStackTrace();
	        JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage());
	    }
	}
  
  //Main Method!!! LoginApp is first page to appear when program is launched
  public static void main(String[] args) 
  {
      new LoginApp();
  }
}
	
