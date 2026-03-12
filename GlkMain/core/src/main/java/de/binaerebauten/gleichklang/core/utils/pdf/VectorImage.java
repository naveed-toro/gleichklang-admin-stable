package de.binaerebauten.gleichklang.core.utils.pdf;

import com.lowagie.text.BadElementException;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Vector image for including in pdf documents
 */
public class VectorImage {

    private final Logger LOG = LoggerFactory.getLogger(VectorImage.class);

    public enum Alignment {
        DEFAULT(0),
        LEFT(0),
        RIGHT(2),
        CENTER(1),
        TEXTWRAP(4),
        UNDERLYING(8);

        private int value;

        private Alignment(int value) {
            this.value = value;
        }

        public int intValue() {
            return value;
        }
    }

    private String filename;
    private float width = 0;
    private float height = 0;
    private Alignment alignment = Alignment.DEFAULT;

    private VectorImage() {}

    public VectorImage(String filename) {
        this.filename = filename;
    }

    /**
     * Creates PDF image object from the vector image.
     *
     * @param writer pdf writer of the document
     * @return Pdf image object
     */
    public Image getImage(PdfWriter writer) {
        PdfReader reader = null;
        try {
            reader = new PdfReader(filename);
        } catch (IOException e) {
            LOG.error(e.getLocalizedMessage());
            return null;
        }
        PdfImportedPage page = writer.getImportedPage(reader, 1);
        Image image = null;
        try {
            image = Image.getInstance(page);
        } catch (BadElementException e) {
            LOG.error(e.getLocalizedMessage());
            return null;
        }


        image.scaleToFit(width, height);
        image.setAlignment(alignment.intValue());

        return image;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public Alignment getAlignment() {
        return alignment;
    }

    public void setAlignment(Alignment alignment) {
        this.alignment = alignment;
    }
}
