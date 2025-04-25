package budgetapp.pages;

import javax.swing.JLabel;

public class BudgetPage extends BaseFrame {
    public BudgetPage(int userId) {
        super("budget", userId);
        initUI();
    }
    
    @Override
    protected void initUI() {
        contentPanel.add(new JLabel("Budget Content"));
        // Add analysis-specific components
    }
}