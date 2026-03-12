package de.binaerebauten.gleichklang.core.utils.pdf;

import com.lowagie.text.Font;
import de.binaerebauten.gleichklang.core.utils.pdf.template.DesignTemplate;

/**
 * Styled Text Paragraph.
 * NOT USED ANYMORE. NEEDS TO BE REMOVED.
 */
public class TextParagraph extends StyledParagraph {

    // Constructors

    public TextParagraph(String text, DesignTemplate template) {
        super();
        if (template != null) {
            template.style(this);
            isStyled = true;
        }
        add(text);
    }

    public TextParagraph(String text) {
        super(text);
    }

    public TextParagraph(String text, Font font) {
        super(text, font);
    }

    public TextParagraph(DesignTemplate template) {
        this("", template);
    }

    public TextParagraph() {
        super();
    }
}
