package de.binaerebauten.gleichklang.core.view.component.filter;

import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.ui.CustomComponent;
import de.binaerebauten.gleichklang.core.model.filter.AbstractFilter;
import de.binaerebauten.gleichklang.core.model.filter.BooleanFilter;
import de.binaerebauten.gleichklang.core.model.filter.BooleanFilter_;
import de.binaerebauten.gleichklang.core.view.component.ComponentGroup;
import de.binaerebauten.gleichklang.core.view.component.FilterComponent;

public class BooleanFilterComponent extends CustomComponent implements FilterComponent
{
	private final ComponentGroup<BooleanFilter> booleanFilterComponentGroup;

	public BooleanFilterComponent(BooleanFilter filter)
	{
		booleanFilterComponentGroup = new ComponentGroup<>(BooleanFilter.class, filter);

		setCompositionRoot(booleanFilterComponentGroup.buildAndBind(BooleanFilter_.value));
	}

	@Override
	public void commit() throws CommitException
	{
		booleanFilterComponentGroup.commit();
	}

	@Override
	public AbstractFilter getFilter()
	{
		return booleanFilterComponentGroup.getItemDataSource().getBean();
	}
}
