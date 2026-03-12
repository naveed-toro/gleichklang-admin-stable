package de.binaerebauten.gleichklang.core.utils.pdf;

import com.lowagie.text.ListItem;


/**
 * Creates a ListItem from an HTML Element.
 */
public class TextListItem extends ListItem {

    public TextListItem(HTMLElement element) {
        super();
        addHTMLElement(element);
    }

    public void addHTMLElement(HTMLElement element) {
        element.getAsPdfElements().forEach(item -> add(item));
    }
}
