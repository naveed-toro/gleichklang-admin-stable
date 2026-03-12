package de.binaerebauten.gleichklang.core.utils.pdf;

import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;

/**
 * Styled Paragraph
 */
public abstract class StyledParagraph extends Paragraph {

    public StyledParagraph() {
        super();
    }

    public StyledParagraph(String text, Font font) {
        super(text, font);
    }

    public StyledParagraph(String text) {
        super(text);
    }

    protected boolean isStyled = false;

    public boolean isStyled() {
        return isStyled;
    }

    public void setStyled(boolean styled) {
        isStyled = styled;
    }

}
