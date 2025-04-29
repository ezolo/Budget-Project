//Importing necessary packages
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

//Define the LoginApp class, which extendsJFrame to create a GUI window
	public class LoginApp extends JFrame 
	{
		//Declare UI components and variables
        //Custom text field for username
		private LineTextField usernameField;
        //Custom password field
		private LinePasswordField passwordField;
        //Label to display username error messages
		private JLabel usernameError;
        //Label to display password error messages
	    private JLabel passwordError;
	    //Static variable to store the logged-in user's ID
        public static int user_id;
	    //Gets the user_id
	    public static int getUserId()
        {
	       return user_id;
	    }
//Constructor to set up the login page
  public LoginApp() 
  {
  	//Setting window properties
      setTitle("Login");
      setSize(350, 400);
      setDefaultCloseOperation(EXIT_ON_CLOSE);
      //Center the window
      setLocationRelativeTo(null);
      //Use absolute positioning
      setLayout(null);
      
      //Set background color
      Container cp = getContentPane();
		cp.setBackground(Color.WHITE);

        //Initialize and add the username field
        usernameField = new LineTextField("Username");
      usernameField.setBounds(50, 50, 250, 40);
      add(usernameField);

      //Initialize and add the password field
      passwordField = new LinePasswordField("Password");
      passwordField.setBounds(50, 110, 250, 40);
      add(passwordField);
    
      //Add an eye toggle button to show/hide the password
      JButton eyeToggle = new JButton("👁");
      eyeToggle.setBounds(285, 112, 40, 40);
      eyeToggle.setBackground(Color.WHITE);
      eyeToggle.setOpaque(true);   
      eyeToggle.setFocusable(false);
      eyeToggle.setBorderPainted(false); 
      eyeToggle.setBorder(BorderFactory.createEmptyBorder()); 

   //Add action listener to toggle password visibility
      eyeToggle.addActionListener(e ->
      {
          if (passwordField.getEchoChar() == '•')
          {
              //Show Password
              passwordField.setEchoChar((char) 0);
              //Lock Icon
              eyeToggle.setText("👁");
          }
          else
          {
              //Hide Password
              passwordField.setEchoChar('•');
              eyeToggle.setText("👁");
          }
      });
      
      //Add eye toggle button to the frame
      add(passwordField);
      add(eyeToggle);

      //Force UI refresh
      revalidate();
      repaint(); 

      //Add a login button
      JButton loginButton = new JButton("Login");
      loginButton.setBounds(50, 190, 250, 35);
      loginButton.setBackground(new Color(0, 127, 255));
      loginButton.setForeground(Color.WHITE);
      add(loginButton);

      //Add a register button to navigate to the registration page
      JButton registerButton = new JButton("<HTML><U>Create an  Account</U></HTML>");
      registerButton.setHorizontalAlignment(SwingConstants.LEFT);
      registerButton.setBorderPainted(false);
      registerButton.setOpaque(false);
      registerButton.setForeground(Color.BLACK);
      registerButton.setBackground(Color.WHITE);
      registerButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
      registerButton.setBounds(105, 220, 250, 35);
      add(registerButton);

      //Add labels for error messages
      usernameError = new JLabel(" ");
      usernameError.setForeground(Color.RED);
      usernameError.setBounds(50, 90, 250, 15);
      add(usernameError);

      passwordError = new JLabel(" ");
      passwordError.setForeground(Color.RED);
      passwordError.setBounds(50, 150, 250, 15);
      add(passwordError);

      //Add action listeners for login and register buttons
      loginButton.addActionListener(e -> Login());
      registerButton.addActionListener(e -> 
      {
          //Close the login window
          dispose();
          //Open the registration page
          new RegisterPage();
      });
      //Make the frame visible
      setVisible(true);
  }

  //Method to handle login logic
  private void Login()
  {
	    // Clear previous error messages
	    usernameError.setText("");
	    passwordError.setText("");

	    //Get the user inputs
	    String username = usernameField.getText().trim();
	    String password = String.valueOf(passwordField.getPassword()).trim();

	    //Validate input fields
	    boolean hasError = false;
	    
	 //Check if username is empty or placeholder is active
	    if (username.isEmpty() || usernameField.isPlaceholderActive())
        {
	        usernameError.setText("Please enter your username.");
	        hasError = true;
	    }
	    
	 //Check if password is empty or placeholder is active
	    if (username.isEmpty() || usernameField.isPlaceholderActive())
        {
	        passwordError.setText("Please enter your password.");
	        hasError = true;
	    }

        //Stop execution if there are validation errors
	    if (hasError) return;

        //Attempt to connect to the database and validate credentials
	    try (Connection conn = DatabaseConnection.getConnection())
        {
	        //Check if the username exists in the database
	    	PreparedStatement userStmt = conn.prepareStatement("SELECT * FROM users WHERE BINARY username=?");
	        userStmt.setString(1, username);
	        ResultSet userRs = userStmt.executeQuery();

            //If username is not found, show an error
	        if (!userRs.next())
            {
	            usernameError.setText("Username not found.");
	            usernameField.requestFocus();
	            return;
	        }

	        //Verify the password
	        String dbPassword = userRs.getString("password");
	        if (!dbPassword.equals(password))
            {
	            passwordError.setText("Invalid password.");
	            passwordField.setText("");
	            passwordField.requestFocus();
	            return;
	        }

	        //If both checks pass, retrieve the user ID and proceed
	        int userId = userRs.getInt("id");
	        JOptionPane.showMessageDialog(this, "Login successful!");
	        //Close login window
            dispose();

	        //Open the records page for the logged-in user
	        SwingUtilities.invokeLater(() ->
            {
	        	 new RecordsPage(userId);
	        });
	        
	    }
        catch (Exception e)
        {
            //Handle database errors
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