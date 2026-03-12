package de.binaerebauten.gleichklang.core.view.component.filter;

import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import de.binaerebauten.gleichklang.core.model.filter.*;
import de.binaerebauten.gleichklang.core.view.component.ComponentGroup;
import de.binaerebauten.gleichklang.core.view.component.FilterComponent;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;

public class ActiveSubscriptionDateRangeFilterComponent extends CustomComponent implements FilterComponent
{
    private final ComponentGroup<ActiveSubscriptionFilter> activeSubscriptionFilterComponentGroup;

    public ActiveSubscriptionDateRangeFilterComponent(ActiveSubscriptionFilter filter, FilterComponent.FilterComponentHandler handler)
    {
        activeSubscriptionFilterComponentGroup = new ComponentGroup<>(ActiveSubscriptionFilter.class, filter);

        final HorizontalLayout layout = new HorizontalLayout();
        layout.setSpacing(true);
        layout.setStyleName(CssStyle.AGE_FILTER_COMPONENT.getStyleName());

        layout.addComponent(activeSubscriptionFilterComponentGroup.buildAndBind(true, "Start", ActiveSubscriptionFilter_.startDate));
        layout.addComponent(activeSubscriptionFilterComponentGroup.buildAndBind(true, "End", ActiveSubscriptionFilter_.endDate));

        setCompositionRoot(layout);
    }

    @Override
    public void commit() throws FieldGroup.CommitException
    {
        activeSubscriptionFilterComponentGroup.commit();
    }

    @Override
    public AbstractFilter getFilter()
    {
        return activeSubscriptionFilterComponentGroup.getItemDataSource().getBean();
    }
}