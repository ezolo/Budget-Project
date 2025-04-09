//Importing packages for applications
import java.sql.*;
import javax.swing.*;
import java.awt.*;

//This sections establishes connection with our MySQL database
public class BudgetApp
{
	public static Connection getConnection() throws SQLException
	{
		String dbURL = "jdbc:mysql://localhost:3306/budget_management";
		String username = "root";
		String password = "password";
		Connection connection = DriverManager.getConnection(dbURL, username, password);
		return connection;
	}
}

	//This is where we set up the GUI & event handlers for Login page
 	class LoginApp extends JFrame 
 	{
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
        
        //Adding labels for username/password and adding buttons
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
        //username and password match records
        try (Connection conn = BudgetApp.getConnection()) 
        {
            PreparedStatement ab = conn.prepareStatement("SELECT * FROM users WHERE username=? AND password=?");
            ab.setString(1, username);
            ab.setString(2, password);
            ResultSet yz = ab.executeQuery();

            //If password and username match database, login message appears and it take user to first
            //page of application (Record Page)
            if (yz.next()) 
            {
                JOptionPane.showMessageDialog(this, "Login successful!");
                dispose();
                new RecordsPage();
            } 
            //If password and username don't match database, user will get "Invalid credentials"
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
 	//This is where we set up the GUI & event handlers for Records page
	class RecordsPage extends JFrame
	{
		JPanel contentPanel;
		
		public RecordsPage()
		{
			//Setting name, size, and layout of page
			setTitle("Records");
			setSize(400,300);
			setDefaultCloseOperation(EXIT_ON_CLOSE);
			setLayout (new BorderLayout());
			
			contentPanel = new JPanel();
			contentPanel.add(new JLabel("This is the Records Page"));
			add(contentPanel, BorderLayout.CENTER);
			
			add(createMenuPanel(), BorderLayout.SOUTH);
			setVisible(true);
		}
		//This is the menu panel, user selects this if they want to go to new page
		//of application
		private JPanel createMenuPanel()
		{
			JPanel menuPanel = new JPanel (new GridLayout(1,6));
			String [] menuItems = {"Records", "Analysis", "Budget", "Accounts", "Categories", "Challenges"};
			
			for (String item : menuItems)
			{
				JButton button = new JButton(item);
				button.addActionListener(e -> switchPage(item));
				menuPanel.add(button);
			}
			return menuPanel;
		}
		
		private void switchPage(String item)
		{
			contentPanel.removeAll();
			contentPanel.setLayout(new BorderLayout());
			contentPanel.add(new JLabel("This is the " + item + " page"), BorderLayout.CENTER); 
			contentPanel.revalidate();
			contentPanel.repaint();
		}
	}
	
//This section will allow user to create a new account for application
class RegisterPage extends JFrame
{
	JTextField nameField, usernameField, emailField;
	JPasswordField passwordField;

	//This is where we set up the GUI & event handlers for Registration page
	public RegisterPage()
	{
		//Setting name, size, and layout of page
		setTitle("Sign Up");
		setSize(300,200);
		setLayout(new GridLayout(6,1));
		
		//Setting name/username/email/password fields
		nameField = new JTextField();
		usernameField = new JTextField();
		emailField = new JTextField();
		passwordField = new JPasswordField();
		add(new JLabel("Name:")); add(nameField);
        add(new JLabel("Username:")); add(usernameField);
        add(new JLabel("Email:")); add(emailField);
        add(new JLabel("Password:")); add(passwordField);
        
        //Setting Continue/Redirect Buttons
        JButton continueButton = new JButton("Continue");
        JButton loginRedirect = new JButton("Already have an account? Login in");
        add(continueButton); add(loginRedirect);
        
        //Event handlers for login/register buttons, take user to new page (Register or Login page)
        continueButton.addActionListener(e -> Register());
        loginRedirect.addActionListener(e ->
        {
        	dispose();
        	new LoginApp();
        });
        setVisible(true);
	}
	
	private void Register()
	{
		//Once all information entered by user, information should be entered into database and should
		//show success message to user
		try (Connection abc = BudgetApp.getConnection())
		{
			PreparedStatement ab = abc.prepareStatement("INSERT INTO users (name, username, email, password) VALUES (?, ?, ?, ?)");
			ab.setString(1, nameField.getText());
			ab.setString(2, usernameField.getText());
			ab.setString(3, emailField.getText());
			ab.setString(4, String.valueOf(passwordField.getPassword()));
			
			ab.executeUpdate();
			JOptionPane.showMessageDialog(this, "Account Created!");
			dispose();
			new LoginApp();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			JOptionPane.showMessageDialog(this,  "Registration Failed");
		}
	}

}

