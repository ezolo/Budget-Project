import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;


public class LoginApp extends JFrame
{
	//This is where we set up the GUI & event handlers for Login page
		  
	public static int user_id;
	  JTextField usernameField;
	  JPasswordField passwordField;

	    public LoginApp()
	    {
	    //Setting name, size, and layout of page
	        setTitle("Login");
	        setSize(300, 200);
	        setDefaultCloseOperation(EXIT_ON_CLOSE);
	        setLayout(new GridLayout(4, 1));
	       
	        //Setting new fields where user enters credentials
	        usernameField = new JTextField();
	        passwordField = new JPasswordField();

	        //Setting new buttons with text inside
	        JButton loginButton = new JButton("Login");
	        JButton registerButton = new JButton("Create Account");
	       
	        //Adding labels for user name/password and adding buttons
	        add(new JLabel("Username: "));
	        add(usernameField);
	        add(new JLabel("Password:"));
	        add(passwordField);
	        add(loginButton);
	        add(registerButton);
	       
	        //Event handlers for the buttons, once clicked they either
	        //log user in or take user to register page (depending if login button
	        //is clicked or if register button is clicked)
	        loginButton.addActionListener(e -> Login());
	        registerButton.addActionListener(e ->
	        {
	            dispose();
	            new RegisterPage();
	        });
	        setVisible(true);
	    }

	    //Setting up how login credentials are checked on DB end
	    private void Login()
	    {
	        String username = usernameField.getText();
	        String password = String.valueOf(passwordField.getPassword());
	       
	        //Setting prepared statement that goes into database to check if user entered
	        //user name and password match records
	        try (Connection conn = BudgetApp.getConnection())
	        {
	            PreparedStatement ab = conn.prepareStatement("SELECT * FROM users WHERE username=? AND password=?");
	            ab.setString(1, username);
	            ab.setString(2, password);
	            ResultSet yz = ab.executeQuery();

	            //If password and user name match database, login message appears and it take user to first
	            //page of application (Record Page)
	            if (yz.next())
	            {
	            	user_id = yz.getInt("id");
	                JOptionPane.showMessageDialog(this, "Login successful!");
	                dispose();
	                new RecordsPage();
	            }
	            //If password and user name don't match database, user will get "Invalid credentials"
	            //message and will not be take to any new page
	            else
	            {
	                JOptionPane.showMessageDialog(this, "Invalid credentials.");
	            }
	        }
	        catch (Exception e)
	        {
	            e.printStackTrace();
	        }
	    }
	    //Main Method!!! LoginApp is first page to appear when program is launched
	    public static void main(String[] args)
	    {
	        new LoginApp();
	    }
	}
