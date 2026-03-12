package de.binaerebauten.gleichklang.core.view.component.filter;

import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import de.binaerebauten.gleichklang.core.model.filter.AbstractFilter;
import de.binaerebauten.gleichklang.core.model.filter.AudioFriendshipFilter;
import de.binaerebauten.gleichklang.core.model.filter.AudioPartnershipFilter;
import de.binaerebauten.gleichklang.core.view.component.FilterComponent;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class LabelFilterComponent extends CustomComponent implements FilterComponent
{
	public interface FilterRemoveListener
	{
		void removeClicked(AbstractFilter filter);
	}

	private final AbstractFilter filter;
	private final Button removeButton;
	private final List<FilterRemoveListener> filterRemoveListeners = new ArrayList<>();
    private boolean isNegation=false;
	public LabelFilterComponent(String text, AbstractFilter filter)
	{
		this(text, filter, null);
	}

	public LabelFilterComponent(String text, AbstractFilter filter, FilterRemoveListener filterRemoveListener)
	{
		this.filter = filter;
		this.removeButton = createRemoveButton();

		final HorizontalLayout layout = new HorizontalLayout();
		layout.setSpacing(true);

		if(filter.getClass().getName().contains("AudioPartnershipFilter"))
		{
			isNegation=((AudioPartnershipFilter) filter).isNegation();
		}
		if(filter.getClass().getName().contains("AudioFriendshipFilter"))
		{
			isNegation=((AudioFriendshipFilter) filter).isNegation();
		}
		if (isNegation)
		{
			layout.addComponent(new Label("!"));
		}
		layout.addComponent(new Label(text));
		layout.addComponent(removeButton);

		setCompositionRoot(layout);

		addFilterRemoveListener(filterRemoveListener);
	}

	public void addFilterRemoveListener(FilterRemoveListener filterRemoveListener)
	{
		if (filterRemoveListener == null) return;

		filterRemoveListeners.add(filterRemoveListener);
		removeButton.setVisible(true);
	}

	private Button createRemoveButton()
	{
		final Button removeButton = new Button();
		removeButton.setIcon(FontAwesome.TRASH_O);
		removeButton.addClickListener(event -> filterRemoveListeners.forEach(listener -> listener.removeClicked(getFilter())));
		removeButton.setVisible(false);

		return removeButton;
	}

	@Override
	public void commit()
	{
		// nothing to do, because label is readonly
	}

	@Override
	public AbstractFilter getFilter()
	{
		return filter;
	}
}
