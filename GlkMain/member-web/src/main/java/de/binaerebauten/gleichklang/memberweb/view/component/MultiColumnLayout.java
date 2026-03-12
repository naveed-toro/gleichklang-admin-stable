package de.binaerebauten.gleichklang.memberweb.view.component;

import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.VerticalLayout;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Domi on 05.01.2017.
 */
public class MultiColumnLayout extends CssLayout {

    private List<VerticalLayout> columns = new ArrayList<>();

    public MultiColumnLayout() {
        this(1);
    }

    public MultiColumnLayout(int columns) {

        for (int i = 0; i < columns; i++) {
            final VerticalLayout layout = new VerticalLayout();
            layout.setStyleName(CssStyle.MULTICOLUMN_LAYOUT_COLUMN.getStyleName());

            this.columns.add(layout);
            super.addComponent(layout);
        }

        setPrimaryStyleName(CssStyle.MULTICOLUMN_LAYOUT.getStyleName());
    }

    public void addComponent(Component component) {
        addComponent(component, 0);
    }


    @Override
    public void addComponent(Component component, int column) {
        final int columnIndex = column >= this.columns.size() ? this.columns.size() - 1 : column;

        final VerticalLayout rowWrapper = new VerticalLayout();
        rowWrapper.addComponent(component);
        rowWrapper.setStyleName(CssStyle.MULTICOLUMN_LAYOUT_ROW.getStyleName());
        rowWrapper.setVisible(component.isVisible());

        this.columns.get(columnIndex).addComponent(rowWrapper);
    }

    public VerticalLayout getLayoutForComponent(Component component) {
        for (VerticalLayout column : this.columns) {
            for (Component row : column) {
                final VerticalLayout rowLayout = (VerticalLayout)row;
                for (Component item : rowLayout) {
                    if (item.equals(component)) {
                        return rowLayout;
                    }
                }
            }
        }

        return null;
    }
}
