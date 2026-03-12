package de.binaerebauten.gleichklang.core.utils.pdf.modify;

import com.lowagie.text.Chunk;
import com.lowagie.text.Element;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import de.binaerebauten.gleichklang.core.utils.pdf.HTMLElement;

import java.util.Objects;

/**
 * Created by Domi on 01.02.2017.
 */
public class EmojiSizeModifier implements HTMLElement.HTMLStyleModifier {

    private final Float height;

    public EmojiSizeModifier(Float height) {
        this.height = height;
    }

    @Override
    public Element modify(Element element) {

        for (Object item : element.getChunks()) {
            Chunk chunk = (Chunk)item;

            if (chunk.hasAttributes()) {
                if (chunk.getAttributes().containsKey("IMAGE")) {
//                    Image image = (Image)chunk.getContent();

                }
            }
        }


        return element;
    }
}
