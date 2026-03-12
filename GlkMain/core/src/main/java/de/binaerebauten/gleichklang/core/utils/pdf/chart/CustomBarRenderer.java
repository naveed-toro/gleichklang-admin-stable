package de.binaerebauten.gleichklang.core.utils.pdf.chart;

import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.CategoryDataset;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom Bar Renderer for Bars and three colors format
 */
public class CustomBarRenderer extends BarRenderer {

    public static final String MAX_COLOR_KEY = "higher";
    public static final String AVERAGE_COLOR_KEY = "default";
    public static final String MIN_COLOR_KEY = "lower";

    private Map<String, Paint> colors = new HashMap<>();


    public CustomBarRenderer() {
        colors.put(MAX_COLOR_KEY, new Color(222, 118, 97));
        colors.put(AVERAGE_COLOR_KEY, new Color(149, 214, 73));
        colors.put(MIN_COLOR_KEY, new Color(100, 153, 182));

        setBarPainter(new StandardBarPainter());
        setShadowVisible(false);
    }

    @Override
    public Paint getItemPaint(int row, int column) {
        CategoryDataset dataset = getPlot().getDataset();
        Number value = dataset.getValue(row, column);
        if (value.doubleValue() < 0) {
            return colors.get(MIN_COLOR_KEY);
        } else if (value.doubleValue() >= 1 ) {
            return colors.get(MAX_COLOR_KEY);
        }
        return colors.get(AVERAGE_COLOR_KEY);
    }

    public void setColor(String key, Color color) {
        this.colors.put(key, color);
    }
}
