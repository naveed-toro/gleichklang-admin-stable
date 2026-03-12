package de.binaerebauten.gleichklang.core.view.component.filter;

import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import de.binaerebauten.gleichklang.core.model.filter.AbstractFilter;
import de.binaerebauten.gleichklang.core.model.filter.ActivatedProlongationsFilter;
import de.binaerebauten.gleichklang.core.model.filter.DeactivatedProlongationsFilter_;
import de.binaerebauten.gleichklang.core.view.component.ComponentGroup;
import de.binaerebauten.gleichklang.core.view.component.FilterComponent;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;

public class AtivatedProlongationsFilterComponent extends CustomComponent implements FilterComponent
{
    private final ComponentGroup<ActivatedProlongationsFilter> activatedProlongationsFilterComponentGroup;

    public AtivatedProlongationsFilterComponent(ActivatedProlongationsFilter filter, FilterComponentHandler handler)
    {
        activatedProlongationsFilterComponentGroup = new ComponentGroup<>(ActivatedProlongationsFilter.class, filter);

        final HorizontalLayout layout = new HorizontalLayout();
        final VerticalLayout verticalLayout = new VerticalLayout();

        layout.setSpacing(true);
        layout.setStyleName(CssStyle.AGE_FILTER_COMPONENT.getStyleName());

        layout.addComponent(activatedProlongationsFilterComponentGroup.buildAndBind(true, "Start", DeactivatedProlongationsFilter_.startDate));
        layout.addComponent(activatedProlongationsFilterComponentGroup.buildAndBind(true, "End", DeactivatedProlongationsFilter_.endDate));
        verticalLayout.addComponent(layout);
        verticalLayout.addComponent(new Label("\n"));
        //verticalLayout.addComponent(activatedProlongationsFilterComponentGroup.buildAndBind(false, "By Users only", com.vaadin.ui.CheckBox.class,DeactivatedProlongationsFilter_.value));

        setCompositionRoot(verticalLayout);
    }

    @Override
    public void commit() throws FieldGroup.CommitException
    {
        activatedProlongationsFilterComponentGroup.commit();
    }

    @Override
    public AbstractFilter getFilter()
    {
        return activatedProlongationsFilterComponentGroup.getItemDataSource().getBean();
    }
}