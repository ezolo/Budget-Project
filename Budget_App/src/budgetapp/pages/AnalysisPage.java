package budgetapp.pages;

import budgetapp.connection.DatabaseConnection;
import org.jfree.chart.*;
import org.jfree.chart.entity.*;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AnalysisPage extends BaseFrame {

    private final int userId;
    private Comparable<?> lastExplodedKey = null;
    private int hoveredBarRow = -1, hoveredBarColumn = -1;

    public AnalysisPage(int userId) {
        super("Analysis", userId);
        this.userId = userId;
        initUI();
    }

    @Override
    protected void initUI() {
        contentPanel.setLayout(new GridLayout(1, 3));

        ChartPanel pieChartPanel = new ChartPanel(createPieChart());
        addPieChartInteractivity(pieChartPanel);
        contentPanel.add(pieChartPanel);

        JPanel barChartContainer = new JPanel(new BorderLayout());
        ChartPanel barChartPanel = new ChartPanel(createBarChart()); // Removed argument
        addBarChartInteractivity(barChartPanel);
        barChartContainer.add(barChartPanel, BorderLayout.CENTER);
        contentPanel.add(barChartContainer);

        ChartPanel budgetVsExpensePanel = new ChartPanel(createBudgetVsExpenseChart());
        contentPanel.add(budgetVsExpensePanel);
    }

    private JFreeChart createPieChart() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        String sql = "SELECT c.name AS category, SUM(e.amount) AS total " +
                "FROM expenses e " +
                "JOIN categories c ON e.category_id = c.id " +
                "WHERE e.user_id = ? AND e.type = 'expense' " +
                "GROUP BY c.name";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                dataset.setValue(rs.getString("category"), rs.getDouble("total"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading pie chart data: " + e.getMessage());
        }

        JFreeChart chart = ChartFactory.createPieChart(
                "Expense Distribution", dataset, true, true, false);
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setLabelFont(new Font("SansSerif", Font.BOLD, 18));
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0}: {2}"));
        return chart;
    }

    private JFreeChart createBarChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        String sql = "SELECT c.name AS category, SUM(e.amount) AS total " +
                "FROM expenses e " +
                "JOIN categories c ON e.category_id = c.id " +
                "WHERE e.user_id = ? " +
                "GROUP BY c.name";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                dataset.addValue(rs.getDouble("total"), "Amount", rs.getString("category"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading bar chart data: " + e.getMessage());
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Category Breakdown", // Chart title
                "Category",           // X-axis label
                "Amount",             // Y-axis label
                dataset,              // Dataset
                PlotOrientation.VERTICAL, // Orientation
                true,                 // Include legend
                true,                 // Tooltips
                false                 // URLs
        );

        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        BarRenderer renderer = new BarRenderer() {
            private final Paint[] colors = new Paint[] {
                    Color.BLUE, Color.GREEN, Color.ORANGE, Color.RED, Color.MAGENTA, Color.CYAN
            };

            @Override
            public Paint getItemPaint(int row, int column) {
                return colors[column % colors.length];
            }
        };
        plot.setRenderer(renderer);

        return chart;
    }

    private JFreeChart createBudgetVsExpenseChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        String sql = "SELECT MONTH(e.expense_date) AS month, " +
                "COALESCE(SUM(CASE WHEN e.type = 'income' THEN e.amount END), 0) AS total_income, " +
                "COALESCE(SUM(CASE WHEN e.type = 'expense' THEN e.amount END), 0) AS total_expense " +
                "FROM expenses e " +
                "WHERE e.user_id = ? " +
                "GROUP BY MONTH(e.expense_date)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String month = Month.of(rs.getInt("month")).name();
                dataset.addValue(rs.getDouble("total_income"), "Income", month);
                dataset.addValue(rs.getDouble("total_expense"), "Expense", month);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading budget vs expense data: " + e.getMessage());
        }

        return ChartFactory.createLineChart(
                "Budget vs Expense by Month", "Month", "Amount", dataset);
    }

    private void addPieChartInteractivity(ChartPanel pieChartPanel) {
        PiePlot plot = (PiePlot) pieChartPanel.getChart().getPlot();
        pieChartPanel.addChartMouseListener(new ChartMouseListener() {
            @Override
            public void chartMouseClicked(ChartMouseEvent event) {}

            @Override
            public void chartMouseMoved(ChartMouseEvent event) {
                for (Object key : plot.getDataset().getKeys()) {
                    plot.setExplodePercent((Comparable<?>) key, 0.0);
                }
                if (event.getEntity() instanceof PieSectionEntity) {
                    Comparable<?> key = ((PieSectionEntity) event.getEntity()).getSectionKey();
                    plot.setExplodePercent(key, 0.10);
                    lastExplodedKey = key;
                } else if (lastExplodedKey != null) {
                    plot.setExplodePercent(lastExplodedKey, 0.0);
                    lastExplodedKey = null;
                }
                pieChartPanel.repaint();
            }
        });
    }

    private void addBarChartInteractivity(ChartPanel barChartPanel) {
        barChartPanel.addChartMouseListener(new ChartMouseListener() {
            @Override
            public void chartMouseClicked(ChartMouseEvent event) {}

            @Override
            public void chartMouseMoved(ChartMouseEvent event) {
                hoveredBarRow = -1;
                hoveredBarColumn = -1;
                if (event.getEntity() instanceof CategoryItemEntity) {
                    CategoryItemEntity entity = (CategoryItemEntity) event.getEntity();
                    CategoryPlot plot = (CategoryPlot) barChartPanel.getChart().getPlot();
                    DefaultCategoryDataset dataset = (DefaultCategoryDataset) plot.getDataset();
                    hoveredBarRow = dataset.getRowIndex(entity.getRowKey());
                    hoveredBarColumn = dataset.getColumnIndex(entity.getColumnKey());
                }
                barChartPanel.repaint();
            }
        });
    }
}