package de.binaerebauten.gleichklang.core.utils.pdf.chart;

import com.lowagie.text.BadElementException;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.DefaultFontMapper;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import org.jfree.chart.ChartTheme;
import org.jfree.chart.JFreeChart;

import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * Chart Object for adding to a styled PDF document
 */
public abstract class AbstractChart {

    protected String chartTitle = null;
    protected float width = 400.0f;
    protected float height = 400.0f;
    protected boolean showLegend = false;
    protected boolean showTooltips = false;
    protected JFreeChart chart = null;
    protected ChartTheme chartTheme = null;

    public String getChartTitle() {
        return chartTitle;
    }

    public void setChartTitle(String chartTitle) {
        this.chartTitle = chartTitle;
        if (chart != null) {
            chart.setTitle(chartTitle);
        }
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public boolean isShowLegend() {
        return showLegend;
    }

    public void setShowLegend(boolean showLegend) {
        this.showLegend = showLegend;
    }

    public boolean isShowTooltips() {
        return showTooltips;
    }

    public void setShowTooltips(boolean showTooltips) {
        this.showTooltips = showTooltips;
    }

    public void setChart(JFreeChart chart) {
        this.chart = chart;
    }

    public ChartTheme getChartTheme() {
        return chartTheme;
    }

    public void setChartTheme(ChartTheme chartTheme) {
        this.chartTheme = chartTheme;
    }

    /**
     * Creates a PDF image object frome the Chart object.
     *
     * @param writer Pdf writer of the document
     * @return PDF image of the chart
     */
    public Image getImage(PdfWriter writer) {
        JFreeChart chart = getChart();
        PdfTemplate template = PdfTemplate.createTemplate(writer, width, height);
        Graphics2D graphics2D = template.createGraphics(width, height, new DefaultFontMapper());
        Rectangle2D rectangle2D = new Rectangle2D.Float(0, 0, width, height);
        chart.draw(graphics2D, rectangle2D);
        graphics2D.dispose();
        try {
            return Image.getInstance(template);
        } catch (BadElementException e) {
            return null;
        }
    }

    /**
     * Creates a JFreeChart object with the stored parameters.
     *
     * @return formatted chart object
     */
    public abstract JFreeChart getChart();
}
