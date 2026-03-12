package de.binaerebauten.gleichklang.core.view.component.filter;

import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.ui.*;
import de.binaerebauten.gleichklang.core.model.filter.*;
import de.binaerebauten.gleichklang.core.view.component.FilterComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LayoutFilterComponent extends CustomComponent implements FilterComponent
{
	private AbstractOrderedLayout layout;

	private final List<FilterComponent> filterComponents = new ArrayList<>();
	private final AbstractFilter filter;

	/** DO NOT USE {@link #addFilterComponent(FilterComponent)} with this constructor **/
	public LayoutFilterComponent(AbstractFilter filter, FilterComponent filterComponent)
	{
		Objects.requireNonNull(filterComponent);

		this.filter = filter;
		filterComponents.add(filterComponent);
		setCompositionRoot(filterComponent);
	}

	public LayoutFilterComponent(UnaryOperatorFilter unaryOperatorFilter, boolean visibleNot)
	{
		this.filter = unaryOperatorFilter;

		switch (unaryOperatorFilter.getUnaryOperator())
		{
			case NOT:
				layout = new HorizontalLayout();
				    if(!showAudioFilterAgtion(unaryOperatorFilter))
				    {
						if (visibleNot) layout.addComponent(new Label("!"));
					}
				break;
		}

		layout.setSpacing(true);

		setCompositionRoot(layout);
	}

	/**
	 * Method for if we have to show neagtion label for audio partnership/friendship filter else flow will be as it is for all filters..
	 * @param unaryOperatorFilter
	 * @return boolean
	 */
	public boolean showAudioFilterAgtion(UnaryOperatorFilter unaryOperatorFilter)
	{
		boolean labelNegation=false;
		if(unaryOperatorFilter.getFilter()!=null ? (unaryOperatorFilter.getFilter().getClass().getName().contains("AudioPartnershipFilter"))
				||(unaryOperatorFilter.getFilter().getClass().getName().contains("AudioFriendshipFilter")):false)
		{
			labelNegation=false;
			if (unaryOperatorFilter.getFilter().getClass().getName().contains("AudioPartnershipFilter")) {
				labelNegation=((AudioPartnershipFilter) unaryOperatorFilter.getFilter()).isNegation();

				if(!(((AudioPartnershipFilter) unaryOperatorFilter.getFilter()).isValue()))
				{
					labelNegation=true;
				}
			}
			if (unaryOperatorFilter.getFilter().getClass().getName().contains("AudioFriendshipFilter")) {
				labelNegation=((AudioFriendshipFilter) unaryOperatorFilter.getFilter()).isNegation();

				if(!(((AudioFriendshipFilter) unaryOperatorFilter.getFilter()).isValue()))
				{
					labelNegation=true;
				}
			}

		}
		return labelNegation;
	}
	public LayoutFilterComponent(UnaryOperatorFilter unaryOperatorFilter)
	{
		this(unaryOperatorFilter, false);
	}

	public LayoutFilterComponent(BinaryOperatorFilter binaryOperatorFilter)
	{
		this.filter = binaryOperatorFilter;

		switch (binaryOperatorFilter.getBinaryOperator())
		{
			case INTERSECTION:
				layout = new VerticalLayout();
				break;
			case UNION:
				layout = new HorizontalLayout();
				break;
		}

		layout.setSpacing(true);

		setCompositionRoot(layout);
	}

	public void addFilterComponent(FilterComponent filterComponent)
	{
		Objects.requireNonNull(layout);
		layout.addComponent(filterComponent);
		filterComponents.add(filterComponent);
	}

	@Override
	public void commit() throws CommitException
	{
		for(FilterComponent filterComponent : filterComponents)
		{
			filterComponent.commit();
		}
	}

	@Override
	public AbstractFilter getFilter()
	{
		return filter;
	}
}
