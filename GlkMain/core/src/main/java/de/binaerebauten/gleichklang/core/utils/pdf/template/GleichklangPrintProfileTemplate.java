package de.binaerebauten.gleichklang.core.utils.pdf.template;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Header;
import com.lowagie.text.List;
import com.lowagie.text.pdf.BaseFont;
import de.binaerebauten.gleichklang.core.utils.pdf.*;
import de.binaerebauten.gleichklang.core.utils.pdf.chart.AbstractChart;

import java.awt.*;
import java.util.ArrayList;

/**
 * Created by Domi on 21.09.2016.
 */
public class GleichklangPrintProfileTemplate extends DefaultTemplate implements DesignTemplate {

    private final String fontName = "Yesteryear-Regular";
    private final Font headerFont;
    private final Font baseFont;

    private final float spacingBefore = 2;
    private final float spacingAfter = 5;

    private final Color linkColor = new Color(23, 146, 29);

    private final float baseFontSize = 10.0f;
    private final float headerH1FontSize = 12.0f;

    public GleichklangPrintProfileTemplate() {
        headerFont = FontFactory.getFont("/fonts/Roboto-Bold.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, headerH1FontSize);
        baseFont = FontFactory.getFont("/fonts/Roboto-Regular.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, baseFontSize);
    }

    @Override
    public void style(Element element) {
        if (element instanceof StyledParagraph) {
            if (((StyledParagraph) element).isStyled()) {
                return;
            }
        }

        if (element instanceof Paragraph) {
            styleParagraph((Paragraph)element);
        }

        if (element.getClass() == de.binaerebauten.gleichklang.core.utils.pdf.Header.class) {
            styleHeader((de.binaerebauten.gleichklang.core.utils.pdf.Header)element);
        }

        if (element instanceof List) {
            styleList((List)element);
        }

        if (element instanceof Anchor) {
            styleLink((Anchor)element);
        }
    }

    @Override
    public void style(Document document) {

    }

    @Override
    public void style(AbstractChart chart) {

    }

    @Override
    public void style(VectorImage image) {

    }

    @Override
    public Font getBaseFont() {
        return baseFont;
    }

    @Override
    protected Font getHeaderFont() {
        return headerFont;
    }

    /**
     * Style Header Elements.
     *
     * @param header pdf text header elements
     */
    private void styleHeader(de.binaerebauten.gleichklang.core.utils.pdf.Header header) {
        if (header.isStyled()) {
            return;
        }

        Font font = getHeaderFont();
        switch (header.getType()) {
            case H1:
                font.setSize(headerH1FontSize);
                break;
            case H2:
                font.setSize(headerH2FontSize);
                break;
            case H3:
                font.setSize(headerH3FontSize);
                break;
        }
        ArrayList<Chunk> content = header.getChunks();
        content.stream().forEach(chunk -> {
            Font chunkFont = chunk.getFont();
            if (chunkFont.isBold()) {
                Font newFont = new Font(font);
                newFont.setStyle("bold");
                chunk.setFont(newFont);
            } else {
                chunk.setFont(font);
            }
        });
    }

    /**
     * Style paragraphs.
     *
     * @param paragraph pdf paragraph
     */
    private void styleParagraph(Paragraph paragraph) {
        Font font = getBaseFont();
        ArrayList<Chunk> chunks = paragraph.getChunks();
        chunks.stream().forEach(chunk -> {
            styleChunk(chunk, font);
        });

        if (paragraph.getSpacingAfter() == 0) {
            paragraph.setSpacingAfter(this.spacingAfter);
        }
        if (paragraph.getSpacingBefore() == 0) {
            paragraph.setSpacingBefore(this.spacingBefore);
        }
    }

    /**
     * Styles Link elements in the pdf document.
     *
     * @param anchor Link object
     */
    private void styleLink(Anchor anchor) {
        Font linkFont = new Font(getBaseFont());
        linkFont.setColor(linkColor);
        linkFont.setStyle(Font.UNDERLINE);
        ArrayList<Chunk> chunks = anchor.getChunks();
        chunks.stream().forEach(chunk -> chunk.setFont(linkFont));
    }

    /**
     * Styles List elements and their list items in the pdf document.
     *
     * @param list the List object.
     */
    private void styleList(List list) {
        Font font = getBaseFont();
        ArrayList<Chunk> chunks = list.getChunks();
        chunks.stream().forEach(chunk -> {
            styleChunk(chunk, font);
        });
    }

    /**
     * Styles single chunks in the pdf document.
     * Used for setting the font by preserving the font style.
     *
     * @param chunk single text fragment
     * @param regularFont font the fragment get styled to
     */
    private void styleChunk(Chunk chunk, Font regularFont) {
        Font boldFont = new Font(regularFont);
        boldFont.setStyle(Font.BOLD);

        Font italicFont = new Font(regularFont);
        italicFont.setStyle(Font.ITALIC);

        Font boldItalicFont = new Font(regularFont);
        boldItalicFont.setStyle(Font.BOLDITALIC);

        Font chunkFont = chunk.getFont();

        chunk.setFont(regularFont);
        if (chunkFont.isBold() && chunkFont.isItalic()) {
            chunk.setFont(boldItalicFont);
        } else if (chunkFont.isBold()) {
            chunk.setFont(boldFont);
        } else if (chunkFont.isItalic()) {
            chunk.setFont(italicFont);
        }
    }
}
