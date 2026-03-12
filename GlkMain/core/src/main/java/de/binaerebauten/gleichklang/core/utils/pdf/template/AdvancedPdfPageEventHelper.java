package de.binaerebauten.gleichklang.core.utils.pdf.template;

import com.lowagie.text.pdf.PdfPageEventHelper;

/**
 * Created by Domi on 15.09.2016.
 */
public abstract class AdvancedPdfPageEventHelper extends PdfPageEventHelper {

    public abstract void setDesignTemplate(DesignTemplate template);
    public abstract DesignTemplate getDesignTemplate();

    public abstract void setHeaderText(String headerText);
}
