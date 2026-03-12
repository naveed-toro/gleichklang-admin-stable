package de.binaerebauten.gleichklang.core.view.popup;

import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;

/**
 * Simple Box for showing descriptions and info texts.
 */
public class DescriptionBox extends GenericPopup {

    private static final int TEXT_LENGTH_SMALL = 255;
    private static final int TEXT_LENGTH_MEDIUM = 1000;

    private DescriptionBox(String description, String caption, BoxSize size) {
        setIcon(new ThemeResource("img/info-general.svg"));
        setCaption(caption);

        final VerticalLayout boxLayout = new VerticalLayout();

        final Label descriptionLabel = new Label(description);
        descriptionLabel.setContentMode(ContentMode.HTML);
        boxLayout.addComponent(descriptionLabel);
        boxLayout.setComponentAlignment(descriptionLabel, Alignment.TOP_CENTER);

        setPopupContent(boxLayout);

        // if size is given use size otherwise calculate based on text length
        if (size != null) {
            setBoxSize(size);
        } else {
            setBoxSize(calculateBoxSize(description));
        }

        addStyleName(CssStyle.MESSAGE_BOX.getStyleName());
    }


    /**
     * Calculates the size of the description box for a given text.
     * and returns a predefined size.
     *
     * @param text description text of the box
     * @return predefined size
     */
    private BoxSize calculateBoxSize(String text) {

        if (text.length() <= TEXT_LENGTH_SMALL) {
            return BoxSize.NARROW;
        }

        if (text.length() <= TEXT_LENGTH_MEDIUM) {
            return BoxSize.WIDE;
        }

        return BoxSize.XTRA_LARGE;
    }

    /* === static === */

    /**
     * Shows a description box for the given text.
     *
     * @param description text to display
     */
    public static void show(String description) {
        show(description, null, null);
    }

    /**
     * Shows a description box for a given text with a caption.
     *
     * @param description text to display
     * @param caption caption of the box
     */
    public static void show(String description, String caption) {
        show(description, caption, null);
    }

    /**
     * Shows a description box for a given text with a predefined size.
     *
     * @param description text to display
     * @param size predefined size
     */
    public static void show(String description, BoxSize size) {
        show(description, null, size);
    }

    /**
     * Shows a description box for a given text with a caption and predefined size.
     *
     * @param description text to display
     * @param caption caption of the box
     * @param size predefined size
     */
    public static void show(String description, String caption, BoxSize size) {
        new DescriptionBox(description, caption, size).show();
    }
}
