package de.binaerebauten.gleichklang.core.utils.pdf.template;

import com.lowagie.text.*;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.*;

/**
 * Page Event Helper for styling page background of Gleichklang documents
 */
public class GleichklangPageEvent extends AdvancedPdfPageEventHelper {

    private final Color backgroundColor = new Color(238, 246, 226);
    private DesignTemplate designTemplate = null;
    private String headerText = null;

    public GleichklangPageEvent() {
        this.designTemplate = null;
        this.headerText = null;
    }

    public GleichklangPageEvent(DesignTemplate designTemplate) {
        this.designTemplate = designTemplate;
    }


    @Override
    public void onEndPage(PdfWriter writer, Document document) {
//        document.setHeaderText(new HeaderFooter(getHeaderText(headerText), false));
//        Rectangle background = new Rectangle(document.getPageSize());
//        background.setBackgroundColor(backgroundColor);
//        PdfContentByte canvas = writer.getDirectContentUnder();
//        canvas.rectangle(background);
//        canvas.fill();


        PdfContentByte cb = writer.getDirectContentUnder();

        // Adding Document header
        if (headerText != null && document.getPageNumber() > 1) {
            Phrase headerPhrase = new Phrase(headerText, ((GleichklangUserProfileTemplate)designTemplate).getPageHeaderFont());
            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, headerPhrase, document.leftMargin(), document.top() + 30, 0);
        }

    }

    private Phrase getHeader(String headerText) {

        Phrase header = new Phrase(headerText);
        return header;
    }

    @Override
    public void setDesignTemplate(DesignTemplate template) {
        this.designTemplate = designTemplate;
    }

    @Override
    public DesignTemplate getDesignTemplate() {
        return this.designTemplate;
    }

    @Override
    public void setHeaderText(String headerText) {
        this.headerText = headerText;
    }

    public String getHeaderText() {
        return headerText;
    }
}
