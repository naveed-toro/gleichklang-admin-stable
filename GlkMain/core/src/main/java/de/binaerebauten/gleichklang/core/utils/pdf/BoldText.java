package de.binaerebauten.gleichklang.core.utils.pdf;

import com.lowagie.text.Chunk;
import com.lowagie.text.Font;


/**
 * Bold Text Chunk.
 * Presets the font style to BOLD.
 */
public class BoldText extends Chunk {

    public BoldText(String text) {
        super(text);
        font.setStyle(Font.BOLD);
    }
}

