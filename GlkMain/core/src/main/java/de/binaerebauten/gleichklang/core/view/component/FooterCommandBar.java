package de.binaerebauten.gleichklang.core.view.component;

import com.vaadin.ui.*;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;

/**
 * Created by Domi on 25.11.2016.
 */
public class FooterCommandBar extends CustomComponent {

    public enum Position {
        LEFT,
        MIDDLE,
        RIGHT
    }

    private final HorizontalLayout mainLayout;
    private final HorizontalLayout leftComponentGroup;
    private final HorizontalLayout middleComponentGroup;
    private final HorizontalLayout rightComponentGroup;

    public FooterCommandBar() {
        mainLayout = new HorizontalLayout();
        mainLayout.setStyleName(CssStyle.COMMAND_FOOTER_ROW.getStyleName());
        mainLayout.setWidth(100, Unit.PERCENTAGE);

        leftComponentGroup = new HorizontalLayout();
        leftComponentGroup.setSpacing(true);
        leftComponentGroup.setVisible(false);
        mainLayout.addComponent(leftComponentGroup);
        mainLayout.setComponentAlignment(leftComponentGroup, Alignment.MIDDLE_LEFT);

        middleComponentGroup = new HorizontalLayout();
        middleComponentGroup.setSpacing(true);
        middleComponentGroup.setVisible(false);
        mainLayout.addComponent(middleComponentGroup);
        mainLayout.setComponentAlignment(middleComponentGroup, Alignment.MIDDLE_CENTER);

        rightComponentGroup = new HorizontalLayout();
        rightComponentGroup.setSpacing(true);
        rightComponentGroup.setVisible(false);
        mainLayout.addComponent(rightComponentGroup);
        mainLayout.setComponentAlignment(rightComponentGroup, Alignment.MIDDLE_RIGHT);

        setCompositionRoot(mainLayout);
    }

    public FooterCommandBar(Button button) {
        this();
        addButton(button);
    }

    public void addButton(Button button) {
        addButton(button, Position.RIGHT);
    }

    public void addButton(Button button, Position position) {
        if (button == null) {
            return;
        }
        Layout componentGroup;

        switch (position) {
            case LEFT:
                componentGroup = leftComponentGroup;
                break;

            case MIDDLE:
                componentGroup = middleComponentGroup;
                break;

            default:
                componentGroup = rightComponentGroup;
        }

        componentGroup.addComponent(button);
        componentGroup.setVisible(componentGroup.getComponentCount() >= 0);
    }

    public HorizontalLayout getLeftComponentGroup() {
        return leftComponentGroup;
    }

    public HorizontalLayout getMiddleComponentGroup() {
        return middleComponentGroup;
    }

    public HorizontalLayout getRightComponentGroup() {
        return rightComponentGroup;
    }
}
