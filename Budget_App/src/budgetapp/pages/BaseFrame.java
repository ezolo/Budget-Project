package budgetapp.pages;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.*;
import java.awt.*;

import budgetapp.controller.MenuUtilities;

public abstract class BaseFrame extends JFrame {
    protected JPanel contentPanel;
    protected int userId;
    
    public BaseFrame(String title, int userId) {
        this.userId = userId;
        setTitle("Budget App - " + title);
        setSize(800, 650);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        contentPanel = new JPanel();
        add(contentPanel, BorderLayout.CENTER);
        
        // Add standardized menu
        add(MenuUtilities.createMenuPanel(this, userId), BorderLayout.SOUTH);
        
        setVisible(true);
    }
    
    protected abstract void initUI();
}