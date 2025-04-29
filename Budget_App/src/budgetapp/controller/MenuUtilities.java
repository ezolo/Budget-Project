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

public class MenuUtilities {
    public static JPanel createMenuPanel(JFrame parentFrame, int userId) {
        JPanel menuPanel = new JPanel(new GridLayout(1, 7));
        menuPanel.setBackground(new Color(220, 220, 220));
        menuPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 200, 200)));
        
        String[] menuItems = {"Records", "Subscriptions", "Analysis", "Budget", "Accounts", "Categories", "Challenges"};

        for (String item : menuItems) {
            JButton button = createMenuButton(item, parentFrame, userId);
            menuPanel.add(button);
        }
        return menuPanel;
    }

    private static JButton createMenuButton(String item, JFrame parentFrame, int userId) {
        JButton button = new JButton(item);
        button.setFont(new Font("SansSerif", Font.PLAIN, 12));
        button.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
        button.setBackground(new Color(220, 220, 220));
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        
        // Highlight current page
        if (item.equalsIgnoreCase(parentFrame.getTitle().replace("Budget App - ", ""))) {
            button.setForeground(new Color(70, 130, 180));
            button.setFont(new Font("SansSerif", Font.BOLD, 12));
        }
        
        button.addActionListener(e -> navigateToPage(item, parentFrame, userId));
        return button;
    }

    private static void navigateToPage(String item, JFrame currentFrame, int userId) {
        currentFrame.dispose(); // Close current window
        
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
                new AnalysisPage(userId); // Make sure this class exists
                break;
            case "budget":
                new BudgetPage(userId); // Make sure this class exists
                break;
            case "accounts":
                new AccountsPage(userId); // Make sure this class exists
                break;
            case "challenges":
                new ChallengesPage(userId); // Make sure this class exists
                break;
        }
    }
}