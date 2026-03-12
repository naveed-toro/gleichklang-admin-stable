package de.binaerebauten.gleichklang.core.utils.pdf.chart;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;

/**
 * Pie chart for adding to a styled PDF document
 */
public class PieChart extends AbstractChart {

    private DefaultPieDataset dataset;

    public PieChart() {
        dataset = new DefaultPieDataset();
    }

    public PieChart(DefaultPieDataset dataset) {
        this.dataset = dataset;
    }


    public void setValue(Comparable key, Number value) {
        dataset.setValue(key, value);
    }

    @Override
    public JFreeChart getChart() {
        if (chart == null) {
            chart = ChartFactory.createPieChart(chartTitle, dataset, showLegend, showTooltips, false);
        }
        return chart;
    }
}
