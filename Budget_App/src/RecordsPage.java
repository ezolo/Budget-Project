import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

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
String [] menuItems = {"Records", "Subscriptions", "Analysis", "Budget", "Accounts", "Categories", "Challenges"};

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


if (item.equals("Subscriptions")) {
   BudgetApp.showSubscriptionPanel(LoginApp.user_id);
}
else {contentPanel.add(new JLabel("This is the " + item + " page"), BorderLayout.CENTER);
}
contentPanel.revalidate();
contentPanel.repaint();

}
}