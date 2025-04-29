//packages for project
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
    //Panel to hold the main content of the frame
    protected JPanel contentPanel;
    //ID of the logged-in user
    protected int userId;

    //Constructor to initialize the frame with a title and user ID
    public BaseFrame(String title, int userId) {
        //Store the user ID
        this.userId = userId;
        //Set the frame title
        setTitle("Budget App - " + title);
        //Set the frame size
        setSize(800, 650);
        //Set default close operation
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        //Center the frame on the screen
        setLocationRelativeTo(null);

        //Initialize the content panel and add it to the center of the frame
        contentPanel = new JPanel();
        add(contentPanel, BorderLayout.CENTER);

        //Add a standardized menu panel to the bottom of the frame
        add(MenuUtilities.createMenuPanel(this, userId), BorderLayout.SOUTH);

        //Make the frame visible
        setVisible(true);
    }

    //Abstract method to be implemented by subclasses to initialize the UI
    protected abstract void initUI();
}