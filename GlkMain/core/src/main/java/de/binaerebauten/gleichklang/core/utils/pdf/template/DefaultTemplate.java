package de.binaerebauten.gleichklang.core.utils.pdf.template;

import com.lowagie.text.Font;

/**
 * Default Template Values for a Styled PDF document
 */
public abstract class DefaultTemplate {

    protected final float marginTop = 36.0f;
    protected final float marginBottom = 36.0f;
    protected final float marginLeft = 36.0f;
    protected final float marginRight = 36.0f;

    protected final float baseFontSize = 12.0f;
    protected final float headerH1FontSize = 16.0f;
    protected final float headerH2FontSize = 14.0f;
    protected final float headerH3FontSize = baseFontSize;

    protected abstract Font getHeaderFont();
    protected abstract Font getBaseFont();
}
