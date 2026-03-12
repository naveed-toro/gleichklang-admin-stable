package de.binaerebauten.gleichklang.core.view.component;

import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.*;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.Notification.Type;
import de.binaerebauten.gleichklang.core.model.filter.*;
import de.binaerebauten.gleichklang.core.model.filter.AbstractFilter.FilterType;
import de.binaerebauten.gleichklang.core.model.filter.UnaryOperatorFilter.UnaryOperator;
import de.binaerebauten.gleichklang.core.model.filter.UserFilter.UserFilterType;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.model.user.User_;
import de.binaerebauten.gleichklang.core.utils.PropertyPathBuilder;
import de.binaerebauten.gleichklang.core.utils.filter.FilterBuilder;
import de.binaerebauten.gleichklang.core.utils.filter.FilterComponentBuilder;
import de.binaerebauten.gleichklang.core.utils.filter.FilterHelper;
import de.binaerebauten.gleichklang.core.utils.filter.FilterSpecificationBuilder;
import de.binaerebauten.gleichklang.core.view.component.ComponentReplacer.SimpleReplacer;
import de.binaerebauten.gleichklang.core.view.component.FilterComponent.FilterComponentHandler;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.*;

/**
 * Configurable filter component via {@link FilterControlHandler}.
 */
public class FilterControlComponent extends CustomComponent
{
	private  CheckBox exactCheckBox;
	public interface FilterChangedListener
	{
		void filterChanged(Specification<User> userSpecification);
	}

	public interface FilterControlHandler extends FilterComponentHandler
	{
		/**
		 * To activate or deactivate different features
		 *
		 * @return
		 */
		default EnumSet<FilterControlFeature> getActivatedFeatures()
		{
			return EnumSet.allOf(FilterControlFeature.class);
		}

		/**
		 * To activate or deactivate filters (TemplateFilter / UserFilter),
		 * concrete UserFilters could be activated or deactivated with {@link
		 * #getActivatedUserFilters()}
		 *
		 * @return
		 */
		default EnumSet<FilterType> getActivatedFilters()
		{
			return EnumSet.allOf(FilterType.class);
		}

		/**
		 * To activate or deactivate concrete UserFilters
		 *
		 * @return
		 */
		default EnumSet<UserFilterType> getActivatedUserFilters()
		{
			return EnumSet.allOf(UserFilterType.class);
		}

		default EnumSet<UserFilterType> getPinnedUserFilters()
		{
			return EnumSet.noneOf(UserFilterType.class);
		}

		/**
		 * Returns true if a singleUsage marked Filter should only be addable
		 * once
		 *
		 * @return
		 */
		default boolean considerSingleUsage()
		{
			return false;
		}

		/**
		 * Necessary for {@link FilterControlFeature#PREVIEW}
		 *
		 * @param specification
		 * @param pageable
		 * @return
		 */
		Page<User> getUsers(Specification<User> specification, Pageable pageable);

		/**
		 * Necessary for {@link FilterType#TEMPLATE_FILTER}
		 *
		 * @return
		 */
		Collection<TemplateFilter> getTemplateFilters();
	}

	public enum FilterControlFeature
	{
		NOT_LINKABLE,
		OR_LINKABLE,
		PREVIEW
	}

	/**
	 * This is the representation of one new filter. If multiple filters are
	 * ORed this component exists for every filter. The or- or and-button are
	 * not part of this component.
	 */
	private class FilterChoiceComponent extends CustomComponent implements FilterComponent
	{
		private final ComboBox templateFilterComponent;
		private final ComboBox userFilterComponent;
		private final CheckBox notCheckBox;

		private final AbstractOrderedLayout layout;

		private FilterComponent filterComponent = createFilterComponent();

		private FilterChoiceComponent()
		{
			layout = new HorizontalLayout();
			layout.setSpacing(true);

			final boolean multipleFilters = filterControlHandler.getActivatedFilters().size() > 1;
			final ComboBox filterComboBox = ComponentFactory.getInstance().createField(ComboBox.class);
			filterComboBox.addItems(filterControlHandler.getActivatedFilters());
			filterComboBox.addStyleName(CssStyle.FILTER_COMBOBOX.getStyleName());
			filterComboBox.setVisible(multipleFilters);
			filterComboBox.setTextInputAllowed(false);

			userFilterComponent = createUserFilterComponent();
			templateFilterComponent = createTemplateFilterComponent();
			notCheckBox = createNotCheckBox();

			filterComboBox.addValueChangeListener(event -> showFilterSelection((FilterType) filterComboBox.getValue()));

			if (!multipleFilters)
			{
				filterControlHandler.getActivatedFilters().forEach(this::showFilterSelection);
			}

			final Label label = new Label(I18N.FILTERCONTROL_CAPTION_NEWFILTER.getName());
			label.addStyleName(CssStyle.BOLD.getStyleName());

			layout.addComponent(label);
			layout.addComponent(filterComboBox);
			layout.addComponent(notCheckBox);
			layout.addComponent(templateFilterComponent);
			layout.addComponent(userFilterComponent);
			layout.addComponent(filterComponent);

			setCompositionRoot(layout);
		}

		private CheckBox createNotCheckBox()
		{
			final CheckBox checkBox = ComponentFactory.getInstance().createField(CheckBox.class, "!");
			checkBox.setVisible(filterControlHandler.getActivatedFeatures().contains(FilterControlFeature.NOT_LINKABLE));

			return checkBox;
		}

		private ComboBox createTemplateFilterComponent()
		{
			final ComboBox templateFilterComboBox = ComponentFactory.getInstance().createField(TemplateFilter.class, ComboBox.class);

			templateFilterComboBox.setVisible(false);
			templateFilterComboBox.setWidth("145px");
			templateFilterComboBox.setNullSelectionAllowed(true);
			templateFilterComboBox.setContainerDataSource(new BeanItemContainer<>(TemplateFilter.class, filterControlHandler.getTemplateFilters()));
			templateFilterComboBox.setItemCaptionMode(ItemCaptionMode.PROPERTY);
			templateFilterComboBox.setItemCaptionPropertyId(PropertyPathBuilder.getFieldName(TemplateFilter_.name));
			templateFilterComboBox.addValueChangeListener(event ->
			{
				templateFilterComboBox.removeStyleName(templateFilterComboBox.getValue() != null ? CssStyle.EMPTY_ANSWER.getStyleName() : CssStyle.ANSWERED.getStyleName());
				templateFilterComboBox.addStyleName(templateFilterComboBox.getValue() == null ? CssStyle.EMPTY_ANSWER.getStyleName() : CssStyle.ANSWERED.getStyleName());
				final FilterComponent filterComponent = createFilterComponent((TemplateFilter) templateFilterComboBox.getValue());
				replaceFilterComponent(filterComponent);
			});
			templateFilterComboBox.select(0);

			return templateFilterComboBox;
		}

		private ComboBox createUserFilterComponent()
		{
			final ComboBox filterComboBox = ComponentFactory.getInstance().createField(ComboBox.class);
			filterComboBox.setWidth("145px");

			filterComboBox.setVisible(false);
			filterComboBox.setTextInputAllowed(false);
			filterComboBox.setNullSelectionAllowed(true);
			filterComboBox.addItems(getUsableUserFilters());
			filterComboBox.addValueChangeListener((ValueChangeListener) event ->
			{
				filterComboBox.removeStyleName(filterComboBox.getValue() != null ? CssStyle.EMPTY_ANSWER.getStyleName() : CssStyle.ANSWERED.getStyleName());
				filterComboBox.addStyleName(filterComboBox.getValue() == null ? CssStyle.EMPTY_ANSWER.getStyleName() : CssStyle.ANSWERED.getStyleName());
				final FilterComponent filterComponent = createFilterComponent((UserFilterType) filterComboBox.getValue());
				replaceFilterComponent(filterComponent);
			});

			filterComboBox.select(0);

			return filterComboBox;
		}

		private EnumSet<UserFilterType> getUsableUserFilters()
		{
			final EnumSet<UserFilterType> usableUserFilters = EnumSet.copyOf(filterControlHandler.getActivatedUserFilters());
			if (filterControlHandler.considerSingleUsage())
			{
				FilterHelper.getUserFilterTypes(filterBuilder.getFilter()).stream().filter(UserFilterType::isOnlySingleUsage).forEach(usableUserFilters::remove);
			}
			return usableUserFilters;
		}

		private void replaceFilterComponent(FilterComponent filterComponent)
		{
			layout.replaceComponent(this.filterComponent, filterComponent);

			this.filterComponent = filterComponent;

			refreshAddButton();
		}

		private void showFilterSelection(FilterType filterType)
		{
			switch (filterType)
			{
				case TEMPLATE_FILTER:
					//reset state of combobox to avoid missing filter details in ui
					templateFilterComponent.select(null);
					templateFilterComponent.setVisible(true);
					userFilterComponent.setVisible(false);
					break;
				case USER_FILTER:
					//reset state of combobox to avoid missing filter details in ui
					userFilterComponent.select(null);
					userFilterComponent.setVisible(true);
					templateFilterComponent.setVisible(false);
					break;
			}

			replaceFilterComponent(createFilterComponent());
		}

		@Override
		public AbstractFilter getFilter()
		{
			AbstractFilter result = filterComponent.getFilter();

			boolean applyUnaryFilter=false;
			if(result!=null ? (result.getClass().getName().contains("AudioPartnershipFilter"))
					||(result.getClass().getName().contains("AudioFriendshipFilter")):false)
			{
				{
					if (result.getClass().getName().contains("AudioPartnershipFilter")) {
						((AudioPartnershipFilter) result).setNegation((notCheckBox.getValue()));
					}
					if (result.getClass().getName().contains("AudioFriendshipFilter")) {
						((AudioFriendshipFilter) result).setNegation((notCheckBox.getValue()));
					}
					if (!notCheckBox.getValue()) {
						applyUnaryFilter = true;
					}

				}
				if (result != null ? ((result.getClass().getName().contains("AudioPartnershipFilter") && ((((AudioPartnershipFilter) result).isValue())))
						|| (result.getClass().getName().contains("AudioFriendshipFilter") && ((((AudioFriendshipFilter) result).isValue())))) : false) {
					if (notCheckBox.getValue()) {
						applyUnaryFilter = true;
					}
					if (!notCheckBox.getValue()) {
						applyUnaryFilter = false;
					}
				}
			}
			else
			{
				applyUnaryFilter = notCheckBox.getValue() && result != null;
			}
			if (applyUnaryFilter)
			{
				final UnaryOperatorFilter notFilter = new UnaryOperatorFilter();
				notFilter.setAudioNotFrindShipPartnerFilter(true);
				notFilter.setUnaryOperator(UnaryOperator.NOT);
				notFilter.setFilter(result);

				result = notFilter;
			}

			return result;
		}

		@Override
		public void commit() throws CommitException
		{
			filterComponent.commit();
		}

		private FilterComponent createFilterComponent()
		{
			return createFilterComponent((AbstractFilter) null);
		}

		private FilterComponent createFilterComponent(UserFilterType userFilterType)
		{
			return createFilterComponent(userFilterType != null ? userFilterType.createFilter() : null);
		}

		private FilterComponent createFilterComponent(AbstractFilter filter)
		{
			return filterComponentBuilder.buildNewComponent(filter);
		}
	}

	private final FilterBuilder filterBuilder;
	private final FilterComponentBuilder filterComponentBuilder;
	private final FilterSpecificationBuilder filterSpecificationBuilder;
	private final List<FilterChoiceComponent> filterChoiceComponents = new ArrayList<>();
	private final FilterControlHandler filterControlHandler;
	private final List<FilterChangedListener> filterChangedListeners = new ArrayList<>();

	private final SimpleReplacer activatedFilterComponent;
	private final SimpleReplacer newFilterComponentControl;
	private final SimpleReplacer pinnedFilterComponentControl;
	private Button addFilterButton;

	public FilterControlComponent(FilterControlHandler filterControlHandler,
			FilterSpecificationBuilder filterSpecificationBuilder)
	{
		this(null, filterControlHandler, filterSpecificationBuilder);
	}

	public FilterControlComponent(AbstractFilter rootFilter,
			FilterControlHandler filterControlHandler,
			FilterSpecificationBuilder filterSpecificationBuilder)
	{
		Objects.requireNonNull(filterControlHandler);
		this.filterControlHandler = filterControlHandler;

		this.filterBuilder = new FilterBuilder(rootFilter);

		this.filterComponentBuilder = new FilterComponentBuilder(filterControlHandler);
		filterComponentBuilder.setRemoveHandler((filter) ->
		{
			filterBuilder.removeFilter(filter);
			onFilterChanged("");
			refreshNewFilter();
		});

		this.filterSpecificationBuilder = filterSpecificationBuilder;

		activatedFilterComponent = new SimpleReplacer();
		activatedFilterComponent.addStyleName(CssStyle.ACTIVATED_FILTER.getStyleName());
		newFilterComponentControl = new SimpleReplacer();
		pinnedFilterComponentControl = new SimpleReplacer();

		final Button removeAllFilterBtn = new Button(I18N.FILTERCONTROL_ACTION_REMOVEFILTER.msg());
		removeAllFilterBtn.setStyleName(CssStyle.REMOVE_ALL_FILTER_BTN.getStyleName());
		removeAllFilterBtn.addClickListener(event ->
		{
			filterBuilder.reset();
			onFilterChanged("");
			refreshNewFilter();
			refreshPinnedFilter();
		});

		final VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		layout.setSpacing(true);

		final boolean existsFilter = !filterControlHandler.getActivatedFilters().isEmpty();
		final boolean existsPinnedFilter = !filterControlHandler.getPinnedUserFilters().isEmpty();

		if (existsPinnedFilter && existsFilter)
		{
			final Accordion accordionLayout = new Accordion();
			layout.addComponent(accordionLayout);

			accordionLayout.addTab(newFilterComponentControl, I18N.FILTERCONTROL_CAPTION_NEWFILTERTAB.msg());
			accordionLayout.addTab(pinnedFilterComponentControl, I18N.FILTERCONTROL_CATPTION_PINNEDFILTERTAB.msg());
			accordionLayout.setSelectedTab(pinnedFilterComponentControl);
		}
		else if(existsFilter)
		{
			layout.addComponent(newFilterComponentControl);
		}
		else if(existsPinnedFilter)
		{
			layout.addComponentAsFirst(pinnedFilterComponentControl);
		}

		layout.addComponent(activatedFilterComponent);
		layout.addComponent(removeAllFilterBtn);

		if (filterControlHandler.getActivatedFeatures().contains(FilterControlFeature.PREVIEW))
		{
			final LazyBeanTable<User> userTable = createTable();
			userTable.setVisible(true);
			userTable.setHandler(filterControlHandler::getUsers);
			filterChangedListeners.add(userTable::setFilter);

			layout.addComponent(userTable);
		}

		onFilterChanged("");
		refreshNewFilter();
		refreshPinnedFilter();

		setCompositionRoot(layout);
	}

	private void onFilterChanged(String exact)
	{
		final AbstractFilter filter = filterBuilder.getFilter();

		final Specification<User> userSpecification = filterSpecificationBuilder.build(filter,exact);
		filterChangedListeners.forEach(filterChangedListener -> filterChangedListener.filterChanged(userSpecification));

		refreshActivatedFilter(filter);
	}

	private void refreshAddButton()
	{
		addFilterButton.setVisible(filterChoiceComponents.stream().map(FilterChoiceComponent::getFilter).allMatch(Objects::nonNull));
	}

	private void refreshActivatedFilter(AbstractFilter filter)
	{
		final Component newComponent = filterComponentBuilder.buildViewComponent(filter);
		activatedFilterComponent.setComponent(newComponent);

		if (filter == null)
			activatedFilterComponent.setVisible(false);
		else
			activatedFilterComponent.setVisible(true);
	}

	private void refreshNewFilter()
	{
		newFilterComponentControl.setComponent(createNewFilterComponentControl());
	}

	private void refreshPinnedFilter()
	{
		pinnedFilterComponentControl.setComponent(createPinnedFilterComponentControl());
	}

	private FilterChoiceComponent createFilterChoiceComponent()
	{
		final FilterChoiceComponent filterChoiceComponent = new FilterChoiceComponent();
		filterChoiceComponents.add(filterChoiceComponent);
		refreshAddButton();
		return filterChoiceComponent;
	}

	private FilterChoiceComponent removeLastFilterChoiceComponent()
	{
		if (filterChoiceComponents.size() < 2) return null;
		final FilterChoiceComponent filterChoiceComponent = filterChoiceComponents.remove(filterChoiceComponents.size() - 1);
		refreshAddButton();
		return filterChoiceComponent;
	}

	private Component createNewFilterComponentControl()
	{
		filterChoiceComponents.clear();

		final Panel panel = new Panel();
		panel.setSizeFull();

		final HorizontalLayout layout = new HorizontalLayout();
		layout.setSpacing(true);
		layout.setMargin(true);

		addFilterButton = new Button("", addEvent ->
		{
			try
			{
				for (FilterChoiceComponent filterChoiceComponent : filterChoiceComponents)
				{
					filterChoiceComponent.commit();
					filterChoiceComponent.setVisible(false);
				}
				filterBuilder.addFilter(filterChoiceComponents.stream().map(FilterChoiceComponent::getFilter).toArray(AbstractFilter[]::new));
				onFilterChanged("");
				refreshNewFilter();
			}
			catch (CommitException e)
			{
				Notification.show(I18N.FILTERCONTROL_VALIDATION_ERROR.msg(), Type.WARNING_MESSAGE);
			}
		});
		addFilterButton.setCaption(I18N.FILTERCONTROL_ACTION_ADDFILTER.msg());
		addFilterButton.setStyleName(CssStyle.BTN_ADD_FILTER.getStyleName());

		final Button orButton = new Button("v");
		orButton.addClickListener(orEvent -> layout.addComponent(createFilterChoiceComponent(), layout.getComponentCount() - 3));
		orButton.setVisible(filterControlHandler.getActivatedFeatures().contains(FilterControlFeature.OR_LINKABLE));
		orButton.addStyleName(CssStyle.FILTER_BTN_OR.getStyleName());

		final Button removeButton = new Button("x");
		removeButton.addClickListener(e -> layout.removeComponent(removeLastFilterChoiceComponent()));
		removeButton.setVisible(false);

		orButton.addClickListener(e -> removeButton.setVisible(true));
		removeButton.addClickListener(e -> removeButton.setVisible(filterChoiceComponents.size() > 1));

		final FilterChoiceComponent filterDropdown = createFilterChoiceComponent();
		filterDropdown.setStyleName(CssStyle.FILTER_COMBOBOX.getStyleName());

		layout.addComponents(filterDropdown, orButton, removeButton, addFilterButton);
		layout.setComponentAlignment(filterDropdown, Alignment.MIDDLE_CENTER);
		layout.setComponentAlignment(orButton, Alignment.MIDDLE_LEFT);
		layout.setComponentAlignment(addFilterButton, Alignment.MIDDLE_LEFT);
		layout.addStyleName(CssStyle.FILTER_ROW.getStyleName());

		panel.setContent(layout);

		return panel;
	}

	private Component createPinnedFilterComponentControl()
	{
		final List<FilterComponent> filterComponents = new ArrayList<>();

		final HorizontalLayout pinnedLayout = new HorizontalLayout();
		pinnedLayout.setSpacing(true);
		pinnedLayout.setMargin(true);

		for (UserFilterType pinnedFilter : filterControlHandler.getPinnedUserFilters())
		{
			final FilterComponent filterComponent = filterComponentBuilder.buildNewComponent(pinnedFilter.createFilter());
			filterComponent.setCaption(pinnedFilter.toString());
			filterComponents.add(filterComponent);
			pinnedLayout.addComponent(filterComponent);
		}

		final Button button = new Button(I18N.FILTERCONTROL_ACTION_APPLYPINNEDFILTER.msg());
        exactCheckBox=createExactCheckBox();
		button.addClickListener(event ->
		{
			for (FilterComponent f : filterComponents)
			{
				try
				{
					filterBuilder.removeFilter(f.getFilter());
					f.commit();
					filterBuilder.addFilter(f.getFilter());
				}
				catch (CommitException ignored)
				{

				}
			}
			onFilterChanged(String.valueOf(exactCheckBox.getValue()));
		});
		pinnedLayout.addComponent(exactCheckBox);
		pinnedLayout.setComponentAlignment(exactCheckBox, Alignment.BOTTOM_CENTER);
		pinnedLayout.addComponent(button);
		pinnedLayout.setComponentAlignment(button, Alignment.BOTTOM_CENTER);
		return pinnedLayout;
	}
	private CheckBox createExactCheckBox()
	{
		final CheckBox checkBox = ComponentFactory.getInstance().createField(CheckBox.class, "EXACT Search");
		checkBox.setValue(false);
		checkBox.setVisible(true);

		return checkBox;
	}
	private LazyBeanTable<User> createTable()
	{
		final LazyBeanTable<User> table = new LazyBeanTable<>();
		table.setSizeFull();
		table.setVisible(false);

		table.addContainerProperty("Alias", User_.alias);

		return table;
	}

	public AbstractFilter getFilter()
	{
		return filterBuilder.getFilter();
	}

	public void setFilter(AbstractFilter filter)
	{
		filterBuilder.reset();
		filterBuilder.addFilter(filter);
		onFilterChanged("");
	}

	public void addFilterChangedListener(FilterChangedListener filterChangedListener)
	{
		filterChangedListeners.add(filterChangedListener);
	}
}
