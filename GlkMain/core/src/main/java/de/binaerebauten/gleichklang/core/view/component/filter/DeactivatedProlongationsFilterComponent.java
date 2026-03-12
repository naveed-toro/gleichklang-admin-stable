package de.binaerebauten.gleichklang.core.view.component.filter;

import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import de.binaerebauten.gleichklang.core.model.filter.*;
import de.binaerebauten.gleichklang.core.view.component.ComponentGroup;
import de.binaerebauten.gleichklang.core.view.component.FilterComponent;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;

public class DeactivatedProlongationsFilterComponent extends CustomComponent implements FilterComponent
{
    private final ComponentGroup<DeactivatedProlongationsFilter> deactivatedProlongationsFilterComponentGroup;

    public DeactivatedProlongationsFilterComponent(DeactivatedProlongationsFilter filter, FilterComponentHandler handler)
    {
        deactivatedProlongationsFilterComponentGroup = new ComponentGroup<>(DeactivatedProlongationsFilter.class, filter);

        final HorizontalLayout layout = new HorizontalLayout();
        final VerticalLayout  verticalLayout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setStyleName(CssStyle.AGE_FILTER_COMPONENT.getStyleName());

        layout.addComponent(deactivatedProlongationsFilterComponentGroup.buildAndBind(true, "Start", DeactivatedProlongationsFilter_.startDate));
        layout.addComponent(deactivatedProlongationsFilterComponentGroup.buildAndBind(true, "End", DeactivatedProlongationsFilter_.endDate));
        verticalLayout.addComponent(layout);
        verticalLayout.addComponent(new Label("\n"));
        //verticalLayout.addComponent(deactivatedProlongationsFilterComponentGroup.buildAndBind(false, "By Users only", com.vaadin.ui.CheckBox.class,DeactivatedProlongationsFilter_.value));

        setCompositionRoot(verticalLayout);
    }

    @Override
    public void commit() throws FieldGroup.CommitException
    {
        deactivatedProlongationsFilterComponentGroup.commit();
    }

    @Override
    public AbstractFilter getFilter()
    {
        return deactivatedProlongationsFilterComponentGroup.getItemDataSource().getBean();
    }
}