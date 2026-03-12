package de.binaerebauten.gleichklang.core.view.component.filter;

import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import de.binaerebauten.gleichklang.core.model.filter.AbstractFilter;
import de.binaerebauten.gleichklang.core.model.filter.ActiveSubscriptionFilter;
import de.binaerebauten.gleichklang.core.model.filter.LastLoginFilter;
import de.binaerebauten.gleichklang.core.model.filter.LastLoginFilter_;
import de.binaerebauten.gleichklang.core.view.component.ComponentGroup;
import de.binaerebauten.gleichklang.core.view.component.FilterComponent;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;

public class LastLoginDateRangeFilterComponent extends CustomComponent implements FilterComponent
{
    private final ComponentGroup<LastLoginFilter> lastLoginFilterComponentGroup;

    public LastLoginDateRangeFilterComponent(LastLoginFilter filter, FilterComponent.FilterComponentHandler handler)
    {
        lastLoginFilterComponentGroup = new ComponentGroup<>(LastLoginFilter.class, filter);

        final HorizontalLayout layout = new HorizontalLayout();
        layout.setSpacing(true);
        layout.setStyleName(CssStyle.AGE_FILTER_COMPONENT.getStyleName());

        layout.addComponent(lastLoginFilterComponentGroup.buildAndBind(true, "Start", LastLoginFilter_.startDate));
        layout.addComponent(lastLoginFilterComponentGroup.buildAndBind(true, "End", LastLoginFilter_.endDate));

        setCompositionRoot(layout);
    }

    @Override
    public void commit() throws FieldGroup.CommitException
    {
        lastLoginFilterComponentGroup.commit();
    }

    @Override
    public AbstractFilter getFilter()
    {
        return lastLoginFilterComponentGroup.getItemDataSource().getBean();
    }
}