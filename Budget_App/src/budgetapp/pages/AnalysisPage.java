package budgetapp.pages;

import javax.swing.JLabel;

public class AnalysisPage extends BaseFrame {
    public AnalysisPage(int userId) {
        super("analysis", userId);
        initUI();
    }
    
    @Override
    protected void initUI() {
        contentPanel.add(new JLabel("Analysis Content"));
        // Add analysis-specific components
    }
}