
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.PreparedStatement;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

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