package de.binaerebauten.gleichklang.core.utils.pdf;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.PdfWriter;
import de.binaerebauten.gleichklang.core.utils.pdf.chart.AbstractChart;
import de.binaerebauten.gleichklang.core.utils.pdf.template.DesignTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 * Styled Pdf Document.
 *
 * Added Elements are styled by the used design template.
 */
public class StyledDocument extends Document {

    private final static Logger LOG = LoggerFactory.getLogger(StyledDocument.class);

    private PdfWriter writer;
    private ByteArrayOutputStream outputStream;
    private DesignTemplate template;

    private String headerText = null;
    private String footerText = null;

    public StyledDocument(DesignTemplate template) throws DocumentException {
        super();
        this.template = template;
        template.style(this);
    }


    public PdfWriter getWriter() {
        return writer;
    }

    public void setWriter(PdfWriter writer) {
        this.writer = writer;
    }

    public ByteArrayOutputStream getOutputStream() {
        return outputStream;
    }

    public void setOutputStream(ByteArrayOutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public boolean add(Element element) {
        template.style(element);
        try {
            super.add(element);
        } catch (DocumentException e) {
            LOG.error(e.getLocalizedMessage());
            return false;
        }
        return true;
    }

    public boolean add(AbstractChart chart) {
        template.style(chart);
        try {
            Image image = chart.getImage(writer);
            image.setAlignment(Element.ALIGN_CENTER);
            super.add(image);
        } catch (DocumentException e) {
            LOG.error(e.getLocalizedMessage());
        }
        return true;
    }

    public boolean add(VectorImage image) {
        template.style(image);
        add(image.getImage(writer));
        return true;
    }

    public boolean add(HTMLElement htmlElement) {
        List<Element> elements = htmlElement.getAsPdfElements();
        elements.forEach(element -> {
            for (HTMLElement.HTMLStyleModifier modifier : htmlElement.getStyleModifiers()) {
                modifier.modify(element);
            }
            add(element);
        });
        return true;
    }


    public DesignTemplate getTemplate() {
        return template;
    }

    public String getHeaderText() {
        return headerText;
    }

    public void setHeaderText(String headerText) {
        this.headerText = headerText;
        if (template.getPageEventHelper() != null) {
            template.getPageEventHelper().setHeaderText(headerText);
        }
    }

    public String getFooterText() {
        return footerText;
    }

    public void setFooterText(String footerText) {
        this.footerText = footerText;
    }
}
