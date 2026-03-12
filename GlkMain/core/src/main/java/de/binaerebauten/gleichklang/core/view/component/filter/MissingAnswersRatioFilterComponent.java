package de.binaerebauten.gleichklang.core.view.component.filter;

import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import de.binaerebauten.gleichklang.core.model.filter.*;
import de.binaerebauten.gleichklang.core.view.component.ComponentGroup;
import de.binaerebauten.gleichklang.core.view.component.FilterComponent;

public class MissingAnswersRatioFilterComponent extends CustomComponent implements FilterComponent
{
	private final ComponentGroup<MissingAnswersRatioFilter> missingAnswersRatioFilterComponentGroup;

	public MissingAnswersRatioFilterComponent(MissingAnswersRatioFilter filter)
	{
		missingAnswersRatioFilterComponentGroup = new ComponentGroup<>(MissingAnswersRatioFilter.class, filter);

		final HorizontalLayout layout = new HorizontalLayout();
		layout.setSpacing(true);

		layout.addComponent(missingAnswersRatioFilterComponentGroup.buildAndBind(true, MissingAnswersRatioFilter_.category));

		setCompositionRoot(layout);
	}

	@Override
	public void commit() throws CommitException
	{
		missingAnswersRatioFilterComponentGroup.commit();
	}

	@Override
	public AbstractFilter getFilter()
	{
		return missingAnswersRatioFilterComponentGroup.getItemDataSource().getBean();
	}
}
