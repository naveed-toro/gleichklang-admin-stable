package de.binaerebauten.gleichklang.core.utils.pdf;

import de.binaerebauten.gleichklang.core.utils.pdf.template.DesignTemplate;

/**
 * Text Header Element.
 */
public class Header extends StyledParagraph {

    public enum HeaderType {
        H1,
        H2,
        H3
    }

    private HeaderType type;


    // Constructors

    public Header(String text, HeaderType type) {
        this(text, type, null);
    }

    public Header(String text) {
        this(text, HeaderType.H1, null);
    }

    public Header(HeaderType type) {
        this("", type, null);
    }

    public Header(String text, HeaderType type, DesignTemplate template) {
        super();
        this.type = type;
        if (template != null) {
            template.style(this);
            isStyled = true;
        }
        add(text);
    }

    public Header(String text, DesignTemplate template) {
        this(text, HeaderType.H1, null);
    }


    // Accessors

    public HeaderType getType() {
        return type;
    }

    public void setType(HeaderType type) {
        this.type = type;
    }
}
