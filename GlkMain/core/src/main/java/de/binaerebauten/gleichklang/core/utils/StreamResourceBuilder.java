package de.binaerebauten.gleichklang.core.utils;

import com.vaadin.server.StreamResource;
import de.binaerebauten.gleichklang.core.utils.pdf.StyledDocument;

import javax.validation.constraints.NotNull;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Project: gleichklang
 * Created by Domi on 15.04.2016.
 */
public class StreamResourceBuilder {

    /**
     * Returns a PDF Document as a Stream Resource
     *
     * @param document the styled Pdf Document
     * @param filename the file name
     * @return the document as Vaadin stream resource
     */
    public static StreamResource getPdfFileStreamResource(StyledDocument document, @NotNull String filename) {
        final StreamResource streamResource = new StreamResource(new StreamResource.StreamSource() {
            @Override
            public InputStream getStream() {
                return new ByteArrayInputStream(document.getOutputStream().toByteArray());
            }
        }, filename);
        streamResource.setMIMEType("application/pdf");

        return streamResource;
    }
}
