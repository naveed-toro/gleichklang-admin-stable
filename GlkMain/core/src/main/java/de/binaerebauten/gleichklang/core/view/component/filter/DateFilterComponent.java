package de.binaerebauten.gleichklang.core.view.component.filter;

import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import de.binaerebauten.gleichklang.core.model.filter.*;
import de.binaerebauten.gleichklang.core.view.component.ComponentGroup;
import de.binaerebauten.gleichklang.core.view.component.FilterComponent;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;

public class DateFilterComponent extends CustomComponent implements FilterComponent
{
    private final ComponentGroup<DateFilter> dateFilterComponentGroup;

    public DateFilterComponent(DateFilter filter, FilterComponent.FilterComponentHandler handler)
    {
        dateFilterComponentGroup = new ComponentGroup<>(DateFilter.class, filter);

        final HorizontalLayout layout = new HorizontalLayout();
        layout.setSpacing(false);
        layout.setStyleName(CssStyle.AGE_FILTER_COMPONENT.getStyleName());

        layout.addComponent(dateFilterComponentGroup.buildAndBind(true, "start", DateFilter_.startDate));
        layout.addComponent(dateFilterComponentGroup.buildAndBind(true, "end", DateFilter_.endDate));
        layout.addComponent(dateFilterComponentGroup.buildAndBind("Type", DateFilter_.productType));
        layout.addComponent(dateFilterComponentGroup.buildAndBind("Payment State", DateFilter_.paymentState));
        layout.addComponent(dateFilterComponentGroup.buildAndBind("Greater than", DateFilter_.value));
        layout.addComponent(dateFilterComponentGroup.buildAndBind("Less than", DateFilter_.value1));
        setCompositionRoot(layout);

    }
    @Override
    public void commit() throws FieldGroup.CommitException
    {
        dateFilterComponentGroup.commit();
    }

    @Override
    public AbstractFilter getFilter()
    {
        return dateFilterComponentGroup.getItemDataSource().getBean();
    }
}