package de.binaerebauten.gleichklang.core.view.component.filter;

import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import de.binaerebauten.gleichklang.core.model.filter.AbstractFilter;
import de.binaerebauten.gleichklang.core.model.filter.AgeFilter;
import de.binaerebauten.gleichklang.core.model.filter.AgeFilter_;
import de.binaerebauten.gleichklang.core.view.component.ComponentGroup;
import de.binaerebauten.gleichklang.core.view.component.FilterComponent;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;

public class AgeFilterComponent extends CustomComponent implements FilterComponent
{
	private final ComponentGroup<AgeFilter> ageFilterComponentGroup;

	public AgeFilterComponent(AgeFilter filter, FilterComponentHandler handler)
	{
		ageFilterComponentGroup = new ComponentGroup<>(AgeFilter.class, filter);

		final HorizontalLayout layout = new HorizontalLayout();
		layout.setSpacing(true);
		layout.setStyleName(CssStyle.AGE_FILTER_COMPONENT.getStyleName());

		layout.addComponent(ageFilterComponentGroup.buildAndBind(true, "min", AgeFilter_.minAge));
		layout.addComponent(ageFilterComponentGroup.buildAndBind(true, "max", AgeFilter_.maxAge));

		ageFilterComponentGroup.getItemDataSource().getBean().setMinAge(18);
		ageFilterComponentGroup.getItemDataSource().getBean().setMaxAge(115);

		setCompositionRoot(layout);
	}

	@Override
	public void commit() throws CommitException
	{
		ageFilterComponentGroup.commit();
	}

	@Override
	public AbstractFilter getFilter()
	{
		return ageFilterComponentGroup.getItemDataSource().getBean();
	}
}
