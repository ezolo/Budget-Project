package budgetapp.pages;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import budgetapp.controller.MenuUtilities;

public class RecordsPage extends BaseFrame {
    
	 public RecordsPage(int userId) {
	        super("records", userId);
	        initUI();
	    }

	    @Override
	    protected void initUI() {
	        contentPanel.setLayout(new BorderLayout());
	        contentPanel.add(new JLabel("Records Page Content", JLabel.CENTER));
	        
	        // Add your specific records page components here
	        JButton sampleButton = new JButton("Sample Button");
	        contentPanel.add(sampleButton, BorderLayout.SOUTH);
	    }
	}