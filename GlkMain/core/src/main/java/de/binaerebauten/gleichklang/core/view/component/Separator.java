package de.binaerebauten.gleichklang.core.view.component;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;

/**
 * Separator for FormPanels
 *
 * @author dwinkler
 */
public class Separator extends CustomComponent {

    public Separator() {
        addStyleName("separator");
        final VerticalLayout wrapper = new VerticalLayout();

        final HorizontalLayout separatorTop = new HorizontalLayout();
        separatorTop.setStyleName("separator-top");
        separatorTop.setWidth(100, Unit.PERCENTAGE);

        final HorizontalLayout separatorMiddle = new HorizontalLayout();
        separatorMiddle.setStyleName("separator-middle");
        separatorMiddle.setWidth(100, Unit.PERCENTAGE);

        final HorizontalLayout separatorBottom = new HorizontalLayout();
        separatorBottom.setStyleName("separator-bottom");
        separatorBottom.setWidth(100, Unit.PERCENTAGE);

        wrapper.addComponents(separatorTop, separatorMiddle, separatorBottom);
        setCompositionRoot(wrapper);
    }
}
