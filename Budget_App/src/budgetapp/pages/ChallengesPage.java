package budgetapp.pages;

import javax.swing.JLabel;

public class ChallengesPage extends BaseFrame {
    public ChallengesPage(int userId) {
        super("challenges", userId);
        initUI();
    }
    
    @Override
    protected void initUI() {
        contentPanel.add(new JLabel("Challenges Content"));
        // Add analysis-specific components
    }
}
