package budgetapp.pages;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.JPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.entity.PieSectionEntity;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

public class AnalysisPage extends BaseFrame {

    private Comparable<?> lastExplodedKey = null;
    private int hoveredBarRow = -1, hoveredBarColumn = -1;

    public AnalysisPage(int userId) {
        super("analysis", userId);
        initUI();
    }

    @Override
    protected void initUI() {
        contentPanel.setLayout(new GridLayout(1, 3));

        ChartPanel pieChartPanel = new ChartPanel(createPieChart());
        addPieChartInteractivity(pieChartPanel);
        contentPanel.add(pieChartPanel);

        ChartPanel barChartPanel = new ChartPanel(createBarChart());
        addBarChartInteractivity(barChartPanel);
        contentPanel.add(barChartPanel);

        ChartPanel budgetVsExpensePanel = new ChartPanel(createBudgetVsExpenseChart());
        contentPanel.add(budgetVsExpensePanel);
    }

    private JFreeChart createPieChart() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("Housing", 40);
        dataset.setValue("Groceries", 20);
        dataset.setValue("Transportation", 15);
        dataset.setValue("Entertainment", 10);
        dataset.setValue("Savings", 15);

        JFreeChart chart = ChartFactory.createPieChart(
                "Expense Distribution", dataset, true, true, false);
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setSectionPaint("Housing", new Color(255, 102, 102));
        plot.setSectionPaint("Groceries", new Color(102, 178, 255));
        plot.setSectionPaint("Transportation", new Color(102, 255, 178));
        plot.setSectionPaint("Entertainment", new Color(255, 178, 102));
        plot.setSectionPaint("Savings", new Color(178, 102, 255));
        plot.setLabelFont(new Font("SansSerif", Font.BOLD, 18));
        // Display category text and percentage on the pie chart
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0}: {2}"));
        plot.setLabelPaint(Color.WHITE);
        plot.setLabelBackgroundPaint(Color.BLACK);
        plot.setLabelOutlinePaint(Color.DARK_GRAY);
        return chart;
    }

    private JFreeChart createBarChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(40, "Expense", "Housing");
        dataset.addValue(20, "Expense", "Groceries");
        dataset.addValue(15, "Expense", "Transportation");
        dataset.addValue(10, "Expense", "Entertainment");
        dataset.addValue(15, "Expense", "Savings");

        JFreeChart chart = ChartFactory.createBarChart("Expenses Breakdown", "Category", "Percentage", dataset);
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setRenderer(new BarRenderer() {
            @Override
            public Color getItemPaint(int row, int column) {
                Color baseColor = getColorForCategory(dataset.getColumnKey(column));
                return (row == hoveredBarRow && column == hoveredBarColumn) ? baseColor.brighter() : baseColor;
            }
        });
        return chart;
    }

    private JFreeChart createBudgetVsExpenseChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1000, "Budget", "January");
        dataset.addValue(800, "Expense", "January");
        dataset.addValue(1200, "Budget", "February");
        dataset.addValue(900, "Expense", "February");
        dataset.addValue(1100, "Budget", "March");
        dataset.addValue(950, "Expense", "March");
        dataset.addValue(1300, "Budget", "April");
        dataset.addValue(1100, "Expense", "April");
        dataset.addValue(1400, "Budget", "May");
        dataset.addValue(1200, "Expense", "May");
        dataset.addValue(1500, "Budget", "June");
        dataset.addValue(1350, "Expense", "June");

        JFreeChart chart = ChartFactory.createLineChart("Budget vs Expense by Month", "Month", "Amount", dataset);
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.getRenderer().setSeriesPaint(0, new Color(102, 178, 255));
        plot.getRenderer().setSeriesPaint(1, new Color(255, 102, 102));
        return chart;
    }

    private Color getColorForCategory(Comparable<?> category) {
        String cat = category.toString();
        switch (cat) {
            case "Housing":
                return new Color(255, 102, 102);
            case "Groceries":
                return new Color(102, 178, 255);
            case "Transportation":
                return new Color(102, 255, 178);
            case "Entertainment":
                return new Color(255, 178, 102);
            case "Savings":
                return new Color(178, 102, 255);
            default:
                return Color.gray;
        }
    }

    private void addPieChartInteractivity(ChartPanel pieChartPanel) {
        PiePlot plot = (PiePlot) pieChartPanel.getChart().getPlot();
        pieChartPanel.addChartMouseListener(new ChartMouseListener() {
            @Override
            public void chartMouseClicked(ChartMouseEvent event) { }
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
            public void chartMouseClicked(ChartMouseEvent event) { }
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