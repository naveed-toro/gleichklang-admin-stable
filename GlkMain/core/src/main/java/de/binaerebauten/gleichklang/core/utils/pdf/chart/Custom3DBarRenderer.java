package de.binaerebauten.gleichklang.core.utils.pdf.chart;

import org.jfree.chart.renderer.category.BarRenderer3D;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.CategoryDataset;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom Bar Renderer for 3D Bars and three colors format
 */
public class Custom3DBarRenderer extends BarRenderer3D {

    public static final String MAX_COLOR_KEY = "higher";
    public static final String AVERAGE_COLOR_KEY = "default";
    public static final String MIN_COLOR_KEY = "lower";

    private Map<String, Paint> colors = new HashMap<>();


    public Custom3DBarRenderer() {
        colors.put(MAX_COLOR_KEY, Color.red);
        colors.put(AVERAGE_COLOR_KEY, Color.green);
        colors.put(MIN_COLOR_KEY, Color.blue);

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
