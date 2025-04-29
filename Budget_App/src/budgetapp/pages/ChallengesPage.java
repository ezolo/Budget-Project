package budgetapp.pages;

import budgetapp.connection.DatabaseConnection;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.imageio.ImageIO;


public class ChallengesPage extends BaseFrame {
    private List<Challenge> challenges;
    private int userId;
    private Clip currentSoundClip;
    private javax.swing.Timer refreshTimer;
    private Set<Integer> lastKnownSubscriptionIds = new HashSet<>();


    public ChallengesPage(int userId) {
        super("challenges", userId);
        this.userId = userId;
        initChallenges();
        updateSubscriptionCache(); // Initialize cache
        initUI();
    }

    private void setupAutoRefreshTimer() {
        refreshTimer = new javax.swing.Timer(5000, e -> {
            if (hasSubscriptionBeenDeleted()) {
                refreshChallenges();
            }
        });
        refreshTimer.start();

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentHidden(ComponentEvent e) {
                if (refreshTimer != null) {
                    refreshTimer.stop();
                }
            }
        });
    }


    private void updateSubscriptionCache() {
        lastKnownSubscriptionIds.clear();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT id FROM expenses WHERE user_id = ? AND category_id = 10";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    lastKnownSubscriptionIds.add(rs.getInt("id"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean hasSubscriptionBeenDeleted() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT id FROM expenses WHERE user_id = ? AND category_id = 10";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();

                // Check if any IDs from our cache are missing
                Set<Integer> currentIds = new HashSet<>();
                while (rs.next()) {
                    currentIds.add(rs.getInt("id"));
                }

                for (Integer id : lastKnownSubscriptionIds) {
                    if (!currentIds.contains(id)) {
                        return true; // Subscription was deleted
                    }
                }

                // Update our cache
                lastKnownSubscriptionIds = currentIds;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void refreshChallenges() {
        // Rebuild the UI
        contentPanel.removeAll();
        initChallenges();
        initUI();
        contentPanel.revalidate();
        contentPanel.repaint();

        // Update the cache
        updateSubscriptionCache();
    }



    private void initChallenges() {
        challenges = new ArrayList<>();

        // Define challenges
        challenges.add(new Challenge(
                "No Spend Day",
                "<html>Give your wallet a well-deserved day off!<br>" +
                        "In this challenge, all you have to do is not spend a single <br>" +
                        "penny for a whole day. No coffee runs, no impulse buys, not even a <br>" +
                        "sneaky vending machine snack! Let your wallet snuggle up, <br>" +
                        "take a nap, and dream of savings. Complete this challenge and <br>" +
                        "you'll unlock a special prize — because sometimes, doing <br>" +
                        "nothing is the most rewarding thing you can do!</html>",
                this::checkNoSpendDay
        ));

        // Load badge paths and user progress
        loadBadgePathsFromDatabase();
        loadUserChallengeProgress();
    }

    private void loadBadgePathsFromDatabase() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT challenge_name, badge_image_path FROM badges";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    String name = rs.getString("challenge_name");
                    String path = rs.getString("badge_image_path");

                    for (Challenge challenge : challenges) {
                        if (challenge.getName().equals(name)) {
                            challenge.setBadgePath(path);
                            break;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadUserChallengeProgress() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT challenge_name, status FROM user_challenges WHERE user_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    String challengeName = rs.getString("challenge_name");
                    String status = rs.getString("status");

                    for (Challenge challenge : challenges) {
                        if (challenge.getName().equals(challengeName)) {
                            challenge.setStatus(status);
                            break;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void initUI() {
        contentPanel.setLayout(new BorderLayout());
        JLabel titleLabel = new JLabel("Challenges", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        contentPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel challengesPanel = new JPanel(new GridLayout(0, 1, 10, 10));
        challengesPanel.setBackground(new Color(242, 243, 247));

        for (Challenge challenge : challenges) {
            challengesPanel.add(createChallengePanel(challenge));
        }

        JScrollPane scrollPane = new JScrollPane(challengesPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        setupAutoRefreshTimer();
    }


    private JPanel createChallengePanel(Challenge challenge) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        panel.setBackground(getStatusColor(challenge.getStatus()));

        JLabel nameLabel = new JLabel(challenge.getName());
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        nameLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

        JLabel descriptionLabel = new JLabel(challenge.getDescription());
        descriptionLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        descriptionLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        JButton startButton = new JButton("Start Challenge");
        startButton.setFont(new Font("SansSerif", Font.PLAIN, 12));
        startButton.setBackground(new Color(0, 122, 204));
        startButton.setForeground(Color.WHITE);
        startButton.setFocusPainted(false);
        startButton.setPreferredSize(new Dimension(120, 30));

        JButton claimButton = new JButton("Claim Prize");
        claimButton.setFont(new Font("SansSerif", Font.PLAIN, 12));
        claimButton.setBackground(new Color(0, 153, 76));
        claimButton.setForeground(Color.WHITE);
        claimButton.setFocusPainted(false);
        claimButton.setPreferredSize(new Dimension(120, 30));

        // Configure buttons based on status
        if ("claimed".equals(challenge.getStatus())) {
            startButton.setEnabled(false);
            claimButton.setEnabled(false);
            claimButton.setText("Claimed!");
        } else if (challenge.getCondition().run()) {
            startButton.setEnabled(false);
            claimButton.setEnabled(true);
        } else if ("in_progress".equals(challenge.getStatus())) {
            claimButton.setEnabled(false);
        } else {
            claimButton.setEnabled(false);
        }

        JLabel badgeLabel = new JLabel();
        badgeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        badgeLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        badgeLabel.setOpaque(true);
        badgeLabel.setBackground(new Color(255, 255, 153)); // Yellow background

        if ("claimed".equals(challenge.getStatus())) {
            ImageIcon icon = tryLoadIcon(challenge.getBadgePath());
            if (icon != null) {
                badgeLabel.setIcon(icon);
                badgeLabel.setVisible(true);
            }
        } else {
            badgeLabel.setVisible(false);
        }

        // Declare buttonPanel as final to use it in listeners
        final JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        buttonPanel.setBackground(panel.getBackground()); // Match initial section color
        buttonPanel.add(startButton);
        buttonPanel.add(claimButton);

        startButton.addActionListener(e -> {
            startChallenge(challenge, panel);
            buttonPanel.setBackground(panel.getBackground()); // Sync button panel color
        });

        claimButton.addActionListener(e -> {
            claimAward(challenge, badgeLabel, panel);
            buttonPanel.setBackground(panel.getBackground()); // Sync button panel color
        });

        panel.add(nameLabel, BorderLayout.NORTH);
        panel.add(descriptionLabel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        panel.add(badgeLabel, BorderLayout.EAST);

        return panel;
    }

    private Color getStatusColor(String status) {
        switch (status) {
            case "claimed":
                return new Color(255, 255, 200); // Light yellow
            case "completed":
                return new Color(220, 255, 220); // Light green
            case "in_progress":
                return new Color(220, 220, 255); // Light blue
            default:
                return Color.WHITE;
        }
    }

    private void startChallenge(Challenge challenge, JPanel panel) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO user_challenges (user_id, challenge_name, status) " +
                    "VALUES (?, ?, 'in_progress') " +
                    "ON DUPLICATE KEY UPDATE status = 'in_progress'";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                stmt.setString(2, challenge.getName());
                stmt.executeUpdate();
            }

            // Update UI
            panel.setBackground(getStatusColor("in_progress"));
            panel.revalidate();
            panel.repaint();

            JOptionPane.showMessageDialog(this,
                    "Challenge started! Complete the requirements to earn your badge.",
                    "Challenge Begun",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error starting challenge: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void completeChallenge(Challenge challenge, JPanel panel) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // In completeChallenge method
            String sql = "UPDATE user_challenges SET status = 'completed', " +
                    "completion_date = CURDATE() " +
                    "WHERE user_id = ? AND challenge_name = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                stmt.setString(2, challenge.getName());
                stmt.executeUpdate();
            }

            // Update UI
            panel.setBackground(getStatusColor("completed"));
            for (Component comp : panel.getComponents()) {
                if (comp instanceof JPanel) {
                    for (Component btn : ((JPanel) comp).getComponents()) {
                        if (btn instanceof JButton &&
                                ((JButton) btn).getText().equals("Claim Prize")) {
                            ((JButton) btn).setEnabled(true);
                            break;
                        }
                    }
                }
            }
            panel.revalidate();
            panel.repaint();

            JOptionPane.showMessageDialog(this,
                    "Challenge completed! Click 'Claim Prize' to get your badge.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error completing challenge: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void claimAward(Challenge challenge, JLabel badgeLabel, JPanel panel) {
        // First verify the challenge is actually completed
        if (!challenge.getCondition().run()) {
            JOptionPane.showMessageDialog(this,
                    "You haven't completed this challenge yet!",
                    "Not Completed",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Update database
            String sql = "UPDATE user_challenges SET status = 'claimed' " +
                    "WHERE user_id = ? AND challenge_name = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                stmt.setString(2, challenge.getName());
                stmt.executeUpdate();
            }

            // Load and display badge
            ImageIcon badgeIcon = tryLoadIcon(challenge.getBadgePath());
            if (badgeIcon != null) {
                // Play victory sound
                playSound("resources/win_song.wav");

                // In claimAward, before showing success message
                if (!challenge.getCondition().run()) {
                    panel.setBackground(new Color(255, 200, 200)); // Light red
                    JOptionPane.showMessageDialog(this,
                            "You haven't completed the challenge requirements yet!",
                            "Not Completed",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Update the badge label in the panel
                badgeLabel.setIcon(badgeIcon);
                badgeLabel.setVisible(true);

                // Update panel and buttons
                panel.setBackground(getStatusColor("claimed"));
                for (Component comp : panel.getComponents()) {
                    if (comp instanceof JPanel) {
                        for (Component btn : ((JPanel) comp).getComponents()) {
                            if (btn instanceof JButton &&
                                    ((JButton) btn).getText().equals("Claim Prize")) {
                                ((JButton) btn).setEnabled(false);
                                ((JButton) btn).setText("Claimed!");
                                break;
                            }
                        }
                    }
                }
                panel.revalidate();
                panel.repaint();

                // Show success message
                JOptionPane optionPane = new JOptionPane(
                        "<html><div style='width:200px;text-align:center'>" +
                                "<b>Badge Claimed!</b><br>" + challenge.getName() + "</div></html>",
                        JOptionPane.INFORMATION_MESSAGE,
                        JOptionPane.DEFAULT_OPTION,
                        badgeIcon);
                JDialog dialog = optionPane.createDialog(this, "Achievement Unlocked");

                // Stop sound when dialog closes
                dialog.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosed(WindowEvent e) {
                        if (currentSoundClip != null && currentSoundClip.isRunning()) {
                            currentSoundClip.stop();
                            currentSoundClip.close();
                            currentSoundClip = null;
                        }
                    }
                });

                dialog.setVisible(true);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error claiming badge: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private ImageIcon tryLoadIcon(String path) {
        if (path == null || path.isEmpty()) return null;

        try {
            // Try loading from resources
            URL imageUrl = getClass().getClassLoader().getResource(path);
            if (imageUrl != null) {
                BufferedImage img = ImageIO.read(imageUrl);
                if (img != null) return new ImageIcon(img);
            }

            // Try loading from file system
            File imageFile = new File("src/" + path);
            if (imageFile.exists()) {
                BufferedImage img = ImageIO.read(imageFile);
                if (img != null) return new ImageIcon(img);
            }
        } catch (IOException e) {
            System.err.println("Error loading image: " + path + " - " + e.getMessage());
        }
        return null;
    }

    // Challenge condition check methods (keep your existing implementations)
    private boolean checkNoSpendDay() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT DISTINCT DATE(expense_date) AS expense_date " +
                    "FROM expenses WHERE user_id = ? " +
                    "ORDER BY expense_date ASC";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();

                java.time.LocalDate previousDate = null;

                while (rs.next()) {
                    java.time.LocalDate currentDate = rs.getDate("expense_date").toLocalDate();

                    if (previousDate != null) {
                        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(previousDate, currentDate);
                        if (daysBetween > 1) {
                            return true; // Found a gap of at least one day
                        }
                    }
                    previousDate = currentDate;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // No gap of at least one day found
    }

    private boolean checkCancelSubscription() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Find subscriptions that were in snapshot but no longer exist
            String sql = "SELECT ss.expense_id " +
                    "FROM subscription_snapshots ss " +
                    "LEFT JOIN expenses e ON ss.expense_id = e.id " +
                    "WHERE ss.user_id = ? " +
                    "AND e.id IS NULL " +  // Record no longer exists
                    "LIMIT 1";              // Only need one to qualify

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();
                return rs.next();  // True if any subscriptions were deleted
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean checkPlanMonthlyBudget() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT COUNT(*) FROM budget WHERE user_id = ? AND month_year = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                stmt.setString(2, getCurrentMonthYear());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    private String getCurrentMonthYear() {
        return java.time.LocalDate.now().getMonth() + "-" + java.time.LocalDate.now().getYear();
    }

    private boolean checkTrackExpensesForWeek() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT COUNT(DISTINCT DATE(expense_date)) FROM expenses " +
                    "WHERE user_id = ? AND expense_date >= CURDATE() - INTERVAL 7 DAY";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getInt(1) == 7;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    private static class Challenge {
        private final String name;
        private String badgePath;
        private final String description;
        private final Condition condition;
        private String status = "not_started";

        public Challenge(String name, String description, Condition condition) {
            this.name = name;
            this.description = description;
            this.condition = condition;
        }

        public String getName() { return name; }
        public String getDescription() { return description; }
        public Condition getCondition() { return condition; }
        public String getBadgePath() { return badgePath; }
        public String getStatus() { return status; }

        public void setBadgePath(String path) { this.badgePath = path; }
        public void setStatus(String status) { this.status = status; }
    }
    // Then modify your playSound method:
    private void playSound(String soundFile) {
        try {
            // Stop any currently playing sound
            stopCurrentSound();

            // Get the sound file from resources
            URL soundUrl = getClass().getClassLoader().getResource(soundFile);
            if (soundUrl != null) {
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundUrl);
                currentSoundClip = AudioSystem.getClip();
                currentSoundClip.open(audioIn);

                // Add listener to clean up when sound finishes naturally
                currentSoundClip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        synchronized(this) {
                            if (currentSoundClip != null) {
                                currentSoundClip.close();
                                currentSoundClip = null;
                            }
                        }
                    }
                });

                currentSoundClip.start();
            }
        } catch (Exception e) {
            System.err.println("Error playing sound: " + e.getMessage());
        }
    }

    // Add this helper method
    private synchronized void stopCurrentSound() {
        if (currentSoundClip != null) {
            if (currentSoundClip.isRunning()) {
                currentSoundClip.stop();
            }
            currentSoundClip.close();
            currentSoundClip = null;
        }
    }
    @FunctionalInterface
    private interface Condition {
        boolean run();
    }
}