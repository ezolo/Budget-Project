package budgetapp.controller;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import budgetapp.pages.CategoriesPage;
import budgetapp.pages.RecordsPage;
import budgetapp.pages.SubscriptionPage;
import budgetapp.auth.LoginApp;  // Add this import
import budgetapp.pages.*;

public class MenuUtilities
{
    //Method to create a menu panel with navigationbuttons
    public static JPanel createMenuPanel(JFrame parentFrame, int userId)
    {
        //Create a panel with a grid layout for menu items
        JPanel menuPanel = new JPanel(new GridLayout(1, 7));
        //Set background color
        menuPanel.setBackground(new Color(220, 220, 220));
        menuPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 200, 200)));

        //Define menu itmes
        String[] menuItems = {"Records", "Subscriptions", "Analysis", "Budget", "Accounts", "Categories", "Challenges"};

        //Add buttons for each menu item
        for (String item : menuItems)
        {
            JButton button = createMenuButton(item, parentFrame, userId);
            menuPanel.add(button);
        }
        return menuPanel;
    }

    //Method to create a button for a specific menu item
    private static JButton createMenuButton(String item, JFrame parentFrame, int userId) {
        JButton button = new JButton(item);
        button.setFont(new Font("SansSerif", Font.PLAIN, 12));
        button.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
        button.setBackground(new Color(220, 220, 220));
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        
        // Highlight the button for the current page
        if (item.equalsIgnoreCase(parentFrame.getTitle().replace("Budget App - ", ""))) {
            //Set highlighted text color
            button.setForeground(new Color(70, 130, 180));
            //Set bold font
            button.setFont(new Font("SansSerif", Font.BOLD, 12));
        }
        //Add action listener to navigate to the corresponding page
        button.addActionListener(e -> navigateToPage(item, parentFrame, userId));
        return button;
    }

    //Method to navigate to a specific page based on the menu item
    private static void navigateToPage(String item, JFrame currentFrame, int userId) {
        // Close current window
        currentFrame.dispose();

        //Open the corresponding page on the menu item
        switch(item.toLowerCase()) {
            case "records":
                new RecordsPage(userId);
                break;
            case "subscriptions":
                new SubscriptionPage(userId, null);
                break;
            case "categories":
                new CategoriesPage(userId);
                break;
            case "analysis":
                new AnalysisPage(userId);
                break;
            case "budget":
                new BudgetPage(userId);
                break;
            case "accounts":
                new AccountsPage(userId);
                break;
            case "challenges":
                new ChallengesPage(userId);
                break;
        }
    }
}