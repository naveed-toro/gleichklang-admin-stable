package de.binaerebauten.gleichklang.core.view.popup;

import com.vaadin.server.Resource;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.*;
import de.binaerebauten.gleichklang.core.view.component.Popup;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;

/**
 * Created by Domi on 14.12.2016.
 */
public class GenericPopup extends Popup {

    public enum FooterPosition {
        LEFT, MIDDLE, RIGHT
    }

    public enum BoxSize {
        SMALL("popup-small"),               // 40% x 40%,
        MEDIUM("popup-medium"),             // 55% x 55%
        LARGE("popup-large"),               // 55% x 70%
        XTRA_LARGE("popup-xtra-large"),     // 60% x 90%
        NARROW("popup-narrow"),             // 40% x auto
        WIDE("popup-wide"),                 // 60% x auto
        FULL("popup-full");                 // 100% x 100%

        private final String sizeClass;

        BoxSize(String sizeClass) {
            this.sizeClass = sizeClass;
        }

        public String getSizeClass() {
            return this.sizeClass;
        }
    }

    private final VerticalLayout content;
    private final HorizontalLayout footer;
    private final HorizontalLayout leftFooter;
    private final HorizontalLayout middleFooter;
    private final HorizontalLayout rightFooter;
    private final VerticalLayout wrapper;

    public GenericPopup() {
        content = new VerticalLayout();
        content.setStyleName(CssStyle.POPUP_CONTENT.getStyleName());
        content.setWidth(100, Unit.PERCENTAGE);

        footer = new HorizontalLayout();
        footer.setStyleName(CssStyle.POPUP_FOOTER.getStyleName());
        footer.setWidth(100, Sizeable.Unit.PERCENTAGE);
        footer.setVisible(false);

        leftFooter = new HorizontalLayout();
        leftFooter.setHeight(100, Unit.PERCENTAGE);
        leftFooter.setVisible(false);
        leftFooter.setSpacing(true);
        footer.addComponent(leftFooter);
        footer.setComponentAlignment(leftFooter, Alignment.MIDDLE_LEFT);

        middleFooter = new HorizontalLayout();
        middleFooter.setHeight(100, Unit.PERCENTAGE);
        middleFooter.setVisible(false);
        middleFooter.setSpacing(true);
        footer.addComponent(middleFooter);
        footer.setComponentAlignment(middleFooter, Alignment.MIDDLE_CENTER);

        rightFooter = new HorizontalLayout();
        rightFooter.setHeight(100, Unit.PERCENTAGE);
        rightFooter.setVisible(false);
        rightFooter.setSpacing(true);
        footer.addComponent(rightFooter);
        footer.setComponentAlignment(rightFooter, Alignment.MIDDLE_RIGHT);

        wrapper = new VerticalLayout();
        wrapper.setStyleName(CssStyle.POPUP_WRAPPER.getStyleName());
        wrapper.setWidth(100, Unit.PERCENTAGE);
        wrapper.addComponents(content, footer);

        setContent(wrapper);
        setStyleName(CssStyle.GENERIC_POPUP.getStyleName());
        setDraggable(false);
    }

    public GenericPopup(String caption) {
        this();

        setCaption(caption);
    }

    public GenericPopup(String caption, Resource icon) {
        this(caption);

        setIcon(icon);
    }


    public void setFooter(Component component) {
        this.footer.removeAllComponents();
        this.footer.addComponent(component);
        this.footer.setVisible(footer.getComponentCount() > 0);
    }

    public void setPopupContent(Component content) {
        this.content.removeAllComponents();
        this.content.addComponent(content);
    }

    public HorizontalLayout getFooter() {
        return this.footer;
    }

    public void setFooterVisible(Boolean visible) {
        this.footer.setVisible(visible);
    }

    /**
     * Adds a component to popup footer at given position.
     *
     * @param component component to add
     * @param position footer position
     */
    public void addFooterComponent(Component component, FooterPosition position) {
        if (component == null) {
            return;
        }

        ComponentContainer container;

        switch (position) {
            case LEFT:
                container = leftFooter;
                break;

            case RIGHT:
                container = rightFooter;
                break;

            default:
                container = middleFooter;
        }

        container.addComponent(component);
        container.setVisible(container.getComponentCount() > 0);

        footer.setVisible(leftFooter.isVisible() || middleFooter.isVisible() || rightFooter.isVisible());
    }

    /**
     * Adds a component to popup footer at center position.
     *
     * @param component component to add
     */
    public void addFooterComponent(Component component) {
        addFooterComponent(component, FooterPosition.MIDDLE);
    }

    /**
     * Sets the size of the description Box to a predefined size.
     *
     * @param size predefined size
     */
    public void setBoxSize(BoxSize size) {
        for (BoxSize boxSize : BoxSize.values()) {
            removeStyleName(boxSize.getSizeClass());
        }

        this.addStyleName(size.getSizeClass());
    }

}
