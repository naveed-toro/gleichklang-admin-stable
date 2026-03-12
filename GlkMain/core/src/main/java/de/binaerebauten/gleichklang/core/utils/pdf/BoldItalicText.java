package de.binaerebauten.gleichklang.core.utils.pdf;

import com.lowagie.text.Chunk;
import com.lowagie.text.Font;

/**
 * Bold-Italic Text Chunk.
 * Presets the font style to BOLD and ITALIC.
 */
public class BoldItalicText extends Chunk {

    public BoldItalicText(String text) {
        super(text);
        font.setStyle(Font.BOLDITALIC);
    }
}
