package de.binaerebauten.gleichklang.memberweb.view.component;

import com.google.common.base.Strings;
import com.vaadin.server.Resource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;

/**
 * Generic View Header with Icon
 */
public class GenericViewHeader extends CustomComponent {

    private Image iconImage;
    private final Component iconLayout;
    private final Label captionLayout;
    private final Label descriptionLayout;

    public GenericViewHeader(String caption, String description, Resource icon) {
        final VerticalLayout rootLayout = new VerticalLayout();
        rootLayout.setStyleName("root-wrapper");
        iconLayout = createIconLayout();
        captionLayout = createCaptionLayout();
        descriptionLayout = createDescriptionLayout();

        setIcon(icon);
        setCaption(caption);
        setDescription(description);

        rootLayout.addComponents(iconLayout, captionLayout, descriptionLayout);

        setCompositionRoot(rootLayout);
        setStyleName(CssStyle.GENERIC_VIEW_HEADER.getStyleName());
    }

    public GenericViewHeader() {
        this(null, null, null);
    }

    public GenericViewHeader(String caption) {
        this(caption, null, null);
    }

    public GenericViewHeader(String caption, String description) {
        this(caption, description, null);
    }

    private Component createIconLayout() {
        final HorizontalLayout iconLayout = new HorizontalLayout();
        iconLayout.setStyleName(CssStyle.GENERIC_VIEW_HEADER_ICON_WRAPPER.getStyleName());
        iconLayout.setWidth(100, Unit.PERCENTAGE);


        final HorizontalLayout iconContainer = new HorizontalLayout();
        iconContainer.setSizeUndefined();
        iconLayout.addComponent(iconContainer);
        iconLayout.setComponentAlignment(iconContainer, Alignment.MIDDLE_CENTER);

        iconImage = new Image();
        iconImage.addStyleName(CssStyle.GENERIC_VIEW_HEADER_ICON.getStyleName());
        iconContainer.addComponent(iconImage);

        return iconLayout;
    }

    private Label createCaptionLayout() {
        final Label headerCaption = new Label();
        headerCaption.setStyleName(CssStyle.GENERIC_VIEW_HEADER_LABEL.getStyleName());
        headerCaption.setContentMode(ContentMode.HTML);

        return headerCaption;
    }

    private Label createDescriptionLayout() {
        final Label descriptionLabel = new Label();
        descriptionLabel.setStyleName(CssStyle.GENERIC_VIEW_HEADER_DESCRIPTION.getStyleName());
        descriptionLabel.setContentMode(ContentMode.HTML);

        return descriptionLabel;
    }

    /* === */

    @Override
    public void setCaption(String caption) {
        this.captionLayout.setValue(caption);
        this.captionLayout.setVisible(!Strings.isNullOrEmpty(caption));
    }

    @Override
    public void setDescription(String description) {
        this.descriptionLayout.setValue(description);
        this.descriptionLayout.setVisible(!Strings.isNullOrEmpty(description));
    }

    @Override
    public void setIcon(Resource icon) {
        iconImage.setSource(icon);
        iconLayout.setVisible(icon != null);
    }
}
