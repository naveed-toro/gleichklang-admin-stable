package de.binaerebauten.gleichklang.adminweb.view.popup;

import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.*;
import de.binaerebauten.gleichklang.core.model.filter.AbstractFilter;
import de.binaerebauten.gleichklang.core.model.filter.TemplateContext;
import de.binaerebauten.gleichklang.core.model.filter.TemplateFilter;
import de.binaerebauten.gleichklang.core.model.filter.TemplateFilter_;
import de.binaerebauten.gleichklang.core.utils.filter.FilterSpecificationBuilder;
import de.binaerebauten.gleichklang.core.view.component.ComponentGroup;
import de.binaerebauten.gleichklang.core.view.component.FilterControlComponent;
import de.binaerebauten.gleichklang.core.view.component.FilterControlComponent.FilterControlHandler;
import de.binaerebauten.gleichklang.core.view.component.Popup;

import java.util.Arrays;
import java.util.Objects;

public class TemplateFilterPopup extends Popup
{
	public interface SaveListener
	{
		void save(TemplateFilter filter);
	}

	private final FilterControlComponent filterControlComponent;
	private final SaveListener saveListener;
	private final ComponentGroup<TemplateFilter> templateFilterComponentGroup;

	public TemplateFilterPopup(FilterControlHandler filterControlHandler,
			FilterSpecificationBuilder filterSpecificationBuilder, SaveListener saveListener)
	{
		this(filterControlHandler, filterSpecificationBuilder, new TemplateFilter(), saveListener);
	}

	public TemplateFilterPopup(FilterControlHandler filterControlHandler, FilterSpecificationBuilder filterSpecificationBuilder,
			TemplateFilter templateFilter, SaveListener saveListener)
	{
		Objects.requireNonNull(templateFilter);
		Objects.requireNonNull(saveListener);

		this.saveListener = saveListener;
		this.templateFilterComponentGroup = new ComponentGroup<>(TemplateFilter.class);

		filterControlComponent = new FilterControlComponent(templateFilter.getFilter(),
				filterControlHandler, filterSpecificationBuilder);

		final VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);
		layout.setMargin(true);

		layout.addComponent(templateFilterComponentGroup.buildAndBind(true, I18N.TEMPLATE_FILTER_POPUP_NAME.msg(), TemplateFilter_.name));
		layout.addComponent(createTemplateContextComponent());
		layout.addComponent(filterControlComponent);
		layout.addComponent(createSaveButton());

		this.templateFilterComponentGroup.setItemDataSource(templateFilter);

		setContent(layout);
	}

	private Component createTemplateContextComponent()
	{
		final TwinColSelect twinColSelect = templateFilterComponentGroup.buildAndBind(I18N.TEMPLATE_FILTER_POPUP_CONTEXT.msg(), TwinColSelect.class, TemplateFilter_.templateContexts);
		twinColSelect.setContainerDataSource(new BeanItemContainer<>(TemplateContext.class, Arrays.asList(TemplateContext.values())));

		return twinColSelect;
	}

	private Button createSaveButton()
	{
		final Button button = new Button(I18N.TEMPLATE_FILTER_POPUP_SAVE.msg());

		button.addClickListener(event -> createFilter());

		return button;
	}

	private void createFilter()
	{
		try
		{
			final AbstractFilter filter = filterControlComponent.getFilter();
			if (filter == null)
				throw new CommitException(I18N.TEMPLATE_FILTER_POPUP_COMMIT_XCPTN.msg());

			templateFilterComponentGroup.commit();
			final TemplateFilter templateFilter = templateFilterComponentGroup.getItemDataSource().getBean();
			templateFilter.setFilter(filter);

			saveListener.save(templateFilter);

			close();
		}
		catch (CommitException e)
		{
			Notification.show(de.binaerebauten.gleichklang.adminweb.view.popup.I18N.TEMPLATE_FILTER_POPUP_VALUE_XCPTN.msg());
		}
	}
}
