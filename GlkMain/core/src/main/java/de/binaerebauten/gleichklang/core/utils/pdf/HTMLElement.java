package de.binaerebauten.gleichklang.core.utils.pdf;

import com.lowagie.text.Chunk;
import com.lowagie.text.Element;
import com.lowagie.text.html.simpleparser.HTMLWorker;
import com.lowagie.text.html.simpleparser.StyleSheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * HTML Text Element.
 * HTML Elements get parsed to Pdf Elements.
 */
public class HTMLElement {

    public interface HTMLContentModifier {
        String modify(String content);
    }

    public interface HTMLStyleModifier {
        Element modify(Element element);
    }

    private final static Logger LOG = LoggerFactory.getLogger(HTMLElement.class);

    private final Set<HTMLContentModifier> contentModifiers = new LinkedHashSet<>();
    private final Set<HTMLStyleModifier> styleModifiers = new LinkedHashSet<>();
    private StyleSheet styleSheet;

    private String htmlContent = "";

    public HTMLElement() {
        this("");
    }

    public HTMLElement(StyleSheet styleSheet) {
        this("", styleSheet);
    }

    public HTMLElement(String content) {
        this(content, null);
    }

    public HTMLElement(String content, StyleSheet styleSheet) {
        this.htmlContent = content;
        this.styleSheet = styleSheet;
    }

    /**
     * Add HTML elements.
     *
     * @param newContent HTML content
     */
    public void addContent(String newContent) {
        htmlContent = htmlContent.concat(newContent);
    }

    /**
     * Get the HTML elements as pdf text elements.
     *
     * @return List of parsed pdf text elements.
     */
    public List<Element> getAsPdfElements() {

        List<Element> pdfElements;
        String content = htmlContent;
        for (HTMLContentModifier converter : contentModifiers) {
            content = converter.modify(content);
        }

        try {
            pdfElements = HTMLWorker.parseToList(new StringReader(content), this.styleSheet);
        } catch (IOException e) {
            pdfElements = new ArrayList<>();
            LOG.error(e.getLocalizedMessage());
            return pdfElements;
        }

        return pdfElements;
    }


    public void addContentModifier(HTMLContentModifier modifier) {
        contentModifiers.add(modifier);
    }

    public void removeContentModifier(HTMLContentModifier modifier) {
        contentModifiers.remove(modifier);
    }

    public void addStyleModifier(HTMLStyleModifier modifier) {
        styleModifiers.add(modifier);
    }

    public void removeStyleModifier(HTMLStyleModifier modifier) {
        styleModifiers.remove(modifier);
    }

    public Set<HTMLStyleModifier> getStyleModifiers() {
        return styleModifiers;
    }
}
