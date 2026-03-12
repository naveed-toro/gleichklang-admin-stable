package de.binaerebauten.gleichklang.core.view.component.filter;

import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import de.binaerebauten.gleichklang.core.model.filter.*;
import de.binaerebauten.gleichklang.core.view.component.ComponentGroup;
import de.binaerebauten.gleichklang.core.view.component.FilterComponent;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;

public class SubscriptionEndDateRangeFilterComponent extends CustomComponent implements FilterComponent
{
    private final ComponentGroup<SubscriptionEndFilter> subscriptionEndFilterComponentGroup;

    public SubscriptionEndDateRangeFilterComponent(SubscriptionEndFilter filter, FilterComponent.FilterComponentHandler handler)
    {
        subscriptionEndFilterComponentGroup = new ComponentGroup<>(SubscriptionEndFilter.class, filter);

        final HorizontalLayout layout = new HorizontalLayout();
        layout.setSpacing(true);
        layout.setStyleName(CssStyle.AGE_FILTER_COMPONENT.getStyleName());

        layout.addComponent(subscriptionEndFilterComponentGroup.buildAndBind(true, "Start", SubscriptionEndFilter_.startDate));
        layout.addComponent(subscriptionEndFilterComponentGroup.buildAndBind(true, "End", SubscriptionEndFilter_.endDate));

        setCompositionRoot(layout);
    }

    @Override
    public void commit() throws FieldGroup.CommitException
    {
        subscriptionEndFilterComponentGroup.commit();
    }

    @Override
    public AbstractFilter getFilter()
    {
        return subscriptionEndFilterComponentGroup.getItemDataSource().getBean();
    }
}