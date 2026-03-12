package de.binaerebauten.gleichklang.core.utils.pdf.template;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPageEventHelper;
import de.binaerebauten.gleichklang.core.utils.pdf.VectorImage;
import de.binaerebauten.gleichklang.core.utils.pdf.chart.AbstractChart;


/**
 * Interface for design template used in styling a Pdf document.
 */
public interface DesignTemplate {

    /**
     * Set style for a document element.
     *
     * @param element PDF document element
     */
    void style(Element element);

    /**
     * Set style for a pdf document.
     *
     * @param document PDF document
     */
    void style(Document document);

    /**
     * Set style for chart objects.
     *
     * @param chart Chart object
     */
    void style(AbstractChart chart);

    /**
     * Set style for vector image objects
     * @param image
     */
    void style(VectorImage image);

    /**
     * Page Event Helper for styling page backgrounds.
     * @return Page Handler
     */
    default AdvancedPdfPageEventHelper getPageEventHelper() {
        return null;
    }

    Font getBaseFont();
}
