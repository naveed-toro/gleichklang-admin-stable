package de.binaerebauten.gleichklang.core.view.component.filter;

import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import de.binaerebauten.gleichklang.core.model.filter.AbstractFilter;
import de.binaerebauten.gleichklang.core.model.filter.RelationshipFilter;
import de.binaerebauten.gleichklang.core.model.filter.RelationshipFilter_;
import de.binaerebauten.gleichklang.core.view.component.ComponentGroup;
import de.binaerebauten.gleichklang.core.view.component.FilterComponent;

public class RelationshipFilterComponent extends CustomComponent implements FilterComponent
{
	private final ComponentGroup<RelationshipFilter> relationshipFilterComponentGroup;

	public RelationshipFilterComponent(RelationshipFilter filter)
	{
		relationshipFilterComponentGroup = new ComponentGroup<>(RelationshipFilter.class, filter);

		final HorizontalLayout layout = new HorizontalLayout();
		layout.setSpacing(true);

		layout.addComponent(relationshipFilterComponentGroup.buildAndBind(false, "from", RelationshipFilter_.from));
		layout.addComponent(relationshipFilterComponentGroup.buildAndBind(false, "to", RelationshipFilter_.to));
		layout.addComponent(relationshipFilterComponentGroup.buildAndBind(true, RelationshipFilter_.direction));
		layout.addComponent(relationshipFilterComponentGroup.buildAndBind(true, "threshold", RelationshipFilter_.threshold));

		setCompositionRoot(layout);
	}

	@Override
	public void commit() throws CommitException
	{
		relationshipFilterComponentGroup.commit();
	}

	@Override
	public AbstractFilter getFilter()
	{
		return relationshipFilterComponentGroup.getItemDataSource().getBean();
	}
}
