package de.binaerebauten.gleichklang.core.utils.pdf.chart;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

/**
 * Bar chart for adding to a styled PDF documrnz
 */
public class BarChart extends AbstractChart {

    private DefaultCategoryDataset dataset;
    private String categoryTitle = "";
    private String valuesTitle = "";
    private PlotOrientation orientation = PlotOrientation.VERTICAL;
    private BarRenderer renderer;

    public BarChart() {
        this.dataset = new DefaultCategoryDataset();
    }

    public BarChart(DefaultCategoryDataset dataset) {
        this.dataset = dataset;
    }

    public String getCategoryTitle() {
        return categoryTitle;
    }

    public void setCategoryTitle(String categoryTitle) {
        this.categoryTitle = categoryTitle;
    }

    public String getValuesTitle() {
        return valuesTitle;
    }

    public void setValuesTitle(String valuesTitle) {
        this.valuesTitle = valuesTitle;
    }

    public void addValue(Number value, String series, String category) {
        this.dataset.addValue(value, series, category);
    }

    public PlotOrientation getOrientation() {
        return orientation;
    }

    public void setOrientation(PlotOrientation orientation) {
        this.orientation = orientation;
    }

    public BarRenderer getRenderer() {
        return renderer;
    }

    public void setRenderer(BarRenderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public JFreeChart getChart() {
        if (chart == null) {
            chart = ChartFactory.createBarChart(chartTitle, categoryTitle, valuesTitle, dataset, orientation, showLegend, showTooltips, false);
        }
        return chart;
    }
}
