package de.binaerebauten.gleichklang.adminweb.service.serialization;

import com.sun.xml.bind.marshaller.CharacterEscapeHandler;
import com.sun.xml.bind.marshaller.MinimumEscapeHandler;

import java.io.IOException;
import java.io.Writer;

/**
 * Custom Character Escape Handler for CDATA XML.
 *
 * Escapes XML content with default {@link MinimumEscapeHandler} except CDATA content.
 */
public class CDATACharacterEscapeHandler implements CharacterEscapeHandler {

    private final CharacterEscapeHandler defaultEscapeHandler;

    private static final String CDATA_TAG_BEGIN = "<![CDATA[";
    private static final String CDATA_TAG_END = "]]>";

    public CDATACharacterEscapeHandler() {
        this.defaultEscapeHandler = MinimumEscapeHandler.theInstance;
    }

    public CDATACharacterEscapeHandler(CharacterEscapeHandler defaultEscapeHandler) {
        this.defaultEscapeHandler = defaultEscapeHandler;
    }

    @Override
    public void escape(char[] chars, int i, int i1, boolean b, Writer writer) throws IOException {
        String content = new String(chars);
        if (content.startsWith(CDATA_TAG_BEGIN) && content.endsWith(CDATA_TAG_END)) {
            writer.write(chars, i, i1);
        } else {
            defaultEscapeHandler.escape(chars, i, i1, b, writer);
        }
    }
}
