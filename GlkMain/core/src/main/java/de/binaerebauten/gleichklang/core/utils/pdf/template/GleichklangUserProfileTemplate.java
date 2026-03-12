package de.binaerebauten.gleichklang.core.utils.pdf.template;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.List;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPageEventHelper;
import de.binaerebauten.gleichklang.core.utils.pdf.Header;
import de.binaerebauten.gleichklang.core.utils.pdf.StyledParagraph;
import de.binaerebauten.gleichklang.core.utils.pdf.VectorImage;
import de.binaerebauten.gleichklang.core.utils.pdf.chart.AbstractChart;
import de.binaerebauten.gleichklang.core.utils.pdf.chart.BarChart;
import de.binaerebauten.gleichklang.core.utils.pdf.chart.Custom3DBarRenderer;
import de.binaerebauten.gleichklang.core.utils.pdf.chart.CustomBarRenderer;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.block.ColorBlock;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.title.TextTitle;
import org.jfree.ui.HorizontalAlignment;

import java.awt.*;
import java.util.ArrayList;

/**
 * Style Template for Gleichklang Profile documents
 */
public class GleichklangUserProfileTemplate extends DefaultTemplate implements DesignTemplate {

    // Constants
    private final String fontName = "Yesteryear-Regular";
    private final Font headerFont;
    private final Font baseFont;

    private final Color linkColor = new Color(23, 146, 29);

    private final float spacingBefore = 5;
    private final float spacingAfter = 10;

    private final float baseFontSize = 10.0f;
    private final float headerH1FontSize = 12.0f;

    private final Color plotBackground = new Color(206, 211, 198);
    private final Color plotGradientBackground = new Color(135, 137, 132);
    private final Color gridLineColor = new Color(226, 226, 226);

    private final StandardChartTheme chartTheme = (StandardChartTheme)StandardChartTheme.createJFreeTheme();
    private AdvancedPdfPageEventHelper pageEventHelper;

    public GleichklangUserProfileTemplate() {
        headerFont = FontFactory.getFont("/fonts/Roboto-Bold.ttf",BaseFont.IDENTITY_H, BaseFont.EMBEDDED, headerH1FontSize);
        baseFont = FontFactory.getFont("/fonts/Roboto-Regular.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, baseFontSize);

        java.awt.Font titleFont = new java.awt.Font(chartTheme.getExtraLargeFont().getName(), chartTheme.getExtraLargeFont().getStyle(), 12);
        chartTheme.setExtraLargeFont(titleFont);
        chartTheme.setChartBackgroundPaint(Color.BLACK);

        pageEventHelper = new GleichklangPageEvent(this);
    }

    // Interface implements

    /**
     * Style pdf document text elements.
     *
     * @param element PDF document element
     */
    public void style(Element element) {
        if (element instanceof StyledParagraph) {
            if (((StyledParagraph) element).isStyled()) {
                return;
            }
        }

        if (element instanceof Paragraph) {
            styleParagraph((Paragraph)element);
        }

        if (element.getClass() == Header.class) {
            styleHeader((Header)element);
        }

        if (element instanceof List) {
            styleList((List)element);
        }

        if (element instanceof Anchor) {
            styleLink((Anchor)element);
        }
    }


    /**
     * Style document
     *
     * @param document PDF document
     */
    public void style(Document document) {
        float marginTop = 60.0f;
        float marginBottom = 60.0f;
        float marginLeft = 60.0f;
        float marginRight = 60.0f;
        document.setMargins(marginLeft, marginRight, marginTop, marginBottom);
    }

    /**
     * Style charts.
     *
     * @param chart Chart object
     */
    public void style(AbstractChart chart) {
        if (chart == null) {
            return;
        }
        chart.setChartTheme(chartTheme);
        if (chart.getClass() == BarChart.class) {
            styleBarChart((BarChart)chart);
        }

    }


    /**
     * Style vector images in the pdf document.
     *
     * @param image vector image
     */
    public void style(VectorImage image) {

    }

    // Styling

    /**
     * Style bar chart elements in the pdf document.
     * Sets size and font of chart
     *
     * @param chart bar chart
     */
    private void styleBarChart(BarChart chart) {
        chart.getChart().setBackgroundImageAlpha(0.0f);

        final CategoryPlot plot = chart.getChart().getCategoryPlot();
        plot.setBackgroundPaint(plotBackground);
        chart.getChart().setBorderVisible(false);
        plot.setRenderer(new CustomBarRenderer());

        plot.setOutlineVisible(false);
//        plot.setRangeGridlineStroke();


        GradientPaint backgroundGradient = new GradientPaint(0, 0, plotGradientBackground, 0, 0, plotBackground);
        plot.setBackgroundPaint(backgroundGradient);
        plot.setRangeGridlineStroke(new BasicStroke(0.5f));
        plot.setRangeGridlinePaint(gridLineColor);

        plot.setDomainGridlinesVisible(true);
        plot.setDomainGridlinePaint(plotBackground);

        java.awt.Font axisLabelFont = new java.awt.Font("/fonts/Roboto-Bold.ttf", java.awt.Font.BOLD, 10);

        CategoryAxis categoryAxis = plot.getDomainAxis();
//        java.awt.Font categoryFont = categoryAxis.getTickLabelFont();
        java.awt.Font newCategoryFont = new java.awt.Font("/fonts/Roboto-Regular.ttf", java.awt.Font.PLAIN, 8);
        categoryAxis.setTickLabelFont(newCategoryFont);
        categoryAxis.setLabelFont(axisLabelFont);
        categoryAxis.setAxisLineVisible(false);
        categoryAxis.setTickMarksVisible(false);

        java.awt.Font numberTickFont = new java.awt.Font("/fonts/Roboto-Bold.ttf", java.awt.Font.BOLD, 8);
        NumberAxis numberAxis = (NumberAxis)plot.getRangeAxis();
        numberAxis.setLabelFont(axisLabelFont);
        numberAxis.setTickLabelFont(numberTickFont);
        numberAxis.setAxisLineVisible(false);
        numberAxis.setTickMarksVisible(false);
    }

    /**
     * Style Header Elements.
     *
     * @param header pdf text header elements
     */
    private void styleHeader(Header header) {
        if (header.isStyled()) {
            return;
        }

        Font font = getHeaderFont();
        switch (header.getType()) {
            case H1:
                font.setSize(headerH1FontSize);
                break;
            case H2:
                font.setSize(headerH2FontSize);
                break;
            case H3:
                font.setSize(headerH3FontSize);
                break;
        }
        ArrayList<Chunk> content = header.getChunks();
        content.stream().forEach(chunk -> {
            Font chunkFont = chunk.getFont();
            if (chunkFont.isBold()) {
                Font newFont = new Font(font);
                newFont.setStyle("bold");
                chunk.setFont(newFont);
            } else {
                chunk.setFont(font);
            }
        });
    }

    /**
     * Style paragraphs.
     *
     * @param paragraph pdf paragraph
     */
    private void styleParagraph(Paragraph paragraph) {
        Font font = getBaseFont();
        ArrayList<Chunk> chunks = paragraph.getChunks();
        chunks.stream().forEach(chunk -> {
            styleChunk(chunk, font);
        });

        if (paragraph.getSpacingAfter() == 0) {
            paragraph.setSpacingAfter(this.spacingAfter);
        }
        if (paragraph.getSpacingBefore() == 0) {
            paragraph.setSpacingBefore(this.spacingBefore);
        }
    }

    /**
     * Styles Link elements in the pdf document.
     *
     * @param anchor Link object
     */
    private void styleLink(Anchor anchor) {
        Font linkFont = new Font(getBaseFont());
        linkFont.setColor(linkColor);
        linkFont.setStyle(Font.UNDERLINE);
        ArrayList<Chunk> chunks = anchor.getChunks();
        chunks.stream().forEach(chunk -> chunk.setFont(linkFont));
    }

    /**
     * Styles List elements and their list items in the pdf document.
     *
     * @param list the List object.
     */
    private void styleList(List list) {
        Font font = getBaseFont();
        ArrayList<Chunk> chunks = list.getChunks();
        chunks.stream().forEach(chunk -> {
            styleChunk(chunk, font);
        });
    }

    /**
     * Styles single chunks in the pdf document.
     * Used for setting the font by preserving the font style.
     *
     * @param chunk single text fragment
     * @param regularFont font the fragment get styled to
     */
    private void styleChunk(Chunk chunk, Font regularFont) {
        Font boldFont = new Font(regularFont);
        boldFont.setStyle(Font.BOLD);

        Font italicFont = new Font(regularFont);
        italicFont.setStyle(Font.ITALIC);

        Font boldItalicFont = new Font(regularFont);
        boldItalicFont.setStyle(Font.BOLDITALIC);

        Font chunkFont = chunk.getFont();

        chunk.setFont(regularFont);
        if (chunkFont.isBold() && chunkFont.isItalic()) {
            chunk.setFont(boldItalicFont);
        } else if (chunkFont.isBold()) {
            chunk.setFont(boldFont);
        } else if (chunkFont.isItalic()) {
            chunk.setFont(italicFont);
        }
    }

    @Override
    public AdvancedPdfPageEventHelper getPageEventHelper() {
        return pageEventHelper;
    }

    public void setPageEventHelper(AdvancedPdfPageEventHelper pageEventHelper) {
        this.pageEventHelper = pageEventHelper;
        pageEventHelper.setDesignTemplate(this);
    }

    // Abstract implementation

    protected Font getHeaderFont() {
        return headerFont;
    }

    public Font getBaseFont() {
        return baseFont;
    }

    public Font getPageHeaderFont() {
        Font pageHeaderFont = new Font(getBaseFont());
        pageHeaderFont.setSize(8);
        pageHeaderFont.setStyle(Font.NORMAL);
        pageHeaderFont.setColor(Color.gray);

        return pageHeaderFont;
    }
}
