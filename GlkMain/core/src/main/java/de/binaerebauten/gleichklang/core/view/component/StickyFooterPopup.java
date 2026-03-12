package de.binaerebauten.gleichklang.core.view.component;

import com.vaadin.ui.*;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;

/**
 * Created by rgoerner on 04.11.16.
 * Use this popup for popups with a sticky footer. components added to footer will be aligned right.
 * Footer visibility can be set by setFooterVisible. To enable scrolling, content will be set into a panel.
 * If you adjust popup-width, you need to set footer width by setFooterWidth.
 */
public class StickyFooterPopup extends Popup
{
    private final Panel content;
    private final HorizontalLayout stickyFooter;
    private final VerticalLayout stickyHeader;

    public StickyFooterPopup()
    {
        this.setDraggable(false);
        this.setWidth("100%");
        this.setHeight("100%");
        this.setStyleName(CssStyle.STICKY_FOOTER_POPUP.getStyleName());

        final VerticalLayout wrapper = new VerticalLayout();
        wrapper.setStyleName(CssStyle.POPUP_WRAPPER.getStyleName());
        wrapper.setSizeFull();

        stickyHeader = new VerticalLayout();
        stickyHeader.setWidth(100, Unit.PERCENTAGE);
        stickyHeader.setVisible(false);

        content = new Panel();
        content.setStyleName(CssStyle.POPUP_CONTENT.getStyleName());
        content.setSizeFull();

        stickyFooter = new HorizontalLayout();
        stickyFooter.setStyleName(CssStyle.POPUP_FOOTER.getStyleName());
        stickyFooter.setWidth(100, Unit.PERCENTAGE);
        stickyFooter.setVisible(false);


        wrapper.addComponents(stickyHeader, content, stickyFooter);
        wrapper.setExpandRatio(stickyHeader, 0.0f);
        wrapper.setExpandRatio(content, 1.0f);
        wrapper.setExpandRatio(stickyFooter, 0.0f);

        setContent(wrapper);
    }

    public void setPopupContent(Component content)
    {
        this.content.setContent(content);
    }

    public void setFooter(Component component) {
        if (component == null) return;

        this.stickyFooter.removeAllComponents();
        this.stickyFooter.addComponent(component);
        this.stickyFooter.setVisible(this.stickyFooter.getComponentCount() > 0);
    }

    public void setHeader(Component component) {
        if (component == null) return;

        this.stickyHeader.removeAllComponents();
        this.stickyHeader.addComponent(component);
        this.stickyHeader.setVisible(this.stickyHeader.getComponentCount() > 0);
    }


    public void setFooterVisible(boolean visible)
    {
        this.stickyFooter.setVisible(visible);
    }

    public void setHeaderVisible(boolean visible) {
        this.stickyHeader.setVisible(visible);
    }

}
