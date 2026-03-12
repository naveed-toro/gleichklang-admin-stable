package de.binaerebauten.gleichklang.core.view.component.filter;

import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import de.binaerebauten.gleichklang.core.model.filter.AbstractFilter;
import de.binaerebauten.gleichklang.core.model.filter.MessageFilter;
import de.binaerebauten.gleichklang.core.model.filter.MessageFilter_;
import de.binaerebauten.gleichklang.core.model.filter.RelationshipFilter_;
import de.binaerebauten.gleichklang.core.view.component.ComponentGroup;
import de.binaerebauten.gleichklang.core.view.component.FilterComponent;

public class MessageFilterComponent extends CustomComponent implements FilterComponent
{
	private final ComponentGroup<MessageFilter> messageFilterComponentGroup;

	public MessageFilterComponent(MessageFilter filter)
	{
		messageFilterComponentGroup = new ComponentGroup<>(MessageFilter.class, filter);

		final HorizontalLayout layout = new HorizontalLayout();
		layout.setSpacing(true);

		layout.addComponent(messageFilterComponentGroup.buildAndBind(true, MessageFilter_.directory));
		layout.addComponent(messageFilterComponentGroup.buildAndBind(true, MessageFilter_.direction));
		layout.addComponent(messageFilterComponentGroup.buildAndBind(true, "threshold", RelationshipFilter_.threshold));

		setCompositionRoot(layout);
	}

	@Override
	public void commit() throws CommitException
	{
		messageFilterComponentGroup.commit();
	}

	@Override
	public AbstractFilter getFilter()
	{
		return messageFilterComponentGroup.getItemDataSource().getBean();
	}
}
