package de.binaerebauten.gleichklang.core.view.component.filter;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import de.binaerebauten.gleichklang.core.model.filter.AbstractFilter;
import de.binaerebauten.gleichklang.core.view.component.FilterComponent;

public class EmptyFilterComponent extends CustomComponent implements FilterComponent
{
	public EmptyFilterComponent()
	{
		setCompositionRoot(new Label());
	}

	@Override
	public void commit()
	{
	}

	@Override
	public AbstractFilter getFilter()
	{
		return null;
	}
}
