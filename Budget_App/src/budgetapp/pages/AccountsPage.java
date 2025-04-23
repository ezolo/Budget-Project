package budgetapp.pages;

import javax.swing.JLabel;

public class AccountsPage extends BaseFrame {
    public AccountsPage(int userId) {
        super("accounts", userId);
        initUI();
    }
    
    @Override
    protected void initUI() {
        contentPanel.add(new JLabel("Accounts Content"));
        // Add analysis-specific components
    }
}