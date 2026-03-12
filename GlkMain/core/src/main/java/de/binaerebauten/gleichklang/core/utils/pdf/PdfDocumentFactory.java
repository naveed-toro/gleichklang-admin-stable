package de.binaerebauten.gleichklang.core.utils.pdf;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfWriter;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.utils.pdf.template.DesignTemplate;
import de.binaerebauten.gleichklang.core.utils.pdf.template.GleichklangPageEvent;
import de.binaerebauten.gleichklang.core.utils.pdf.template.GleichklangPrintProfileTemplate;
import de.binaerebauten.gleichklang.core.utils.pdf.template.GleichklangUserProfileTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.io.ByteArrayOutputStream;
import java.util.Objects;

/**
 * Creates ready formatted PDF document objects
 */
public class PdfDocumentFactory {

    private final static Logger LOG = LoggerFactory.getLogger(PdfDocumentFactory.class);

    /**
     * Creates an PDF document object in Gleichklang design
     *
     * @return PDF document object in Gleichklang Design
     */
    public static StyledDocument userProfileReport() {
        return pdfReport(new GleichklangUserProfileTemplate());
    }

    public static StyledDocument userPrintProfile() {
        return pdfReport(new GleichklangPrintProfileTemplate());
    }

    /**
     * Create empty PDF Document object with given style template
     *
     * @param template Design template which styles the document elements
     * @return empty pdf document object with style information
     */
    public static StyledDocument pdfReport(@NotNull DesignTemplate template) {
        Objects.requireNonNull(template, "No design template was given");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        StyledDocument document = null;
        PdfWriter writer = null;
        try {
            document = new StyledDocument(template);
            writer = PdfWriter.getInstance(document, outputStream);
            if (template.getPageEventHelper() != null) {
                writer.setPageEvent(template.getPageEventHelper());
            }
        } catch (DocumentException e) {
            LOG.error(e.getLocalizedMessage());
            return null;
        }

        document.setOutputStream(outputStream);
        document.setWriter(writer);

        return document;
    }
}
