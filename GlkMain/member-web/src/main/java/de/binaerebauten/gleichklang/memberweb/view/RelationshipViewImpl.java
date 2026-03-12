package de.binaerebauten.gleichklang.memberweb.view;

import com.vaadin.server.FontAwesome;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.*;
import com.vaadin.ui.ComboBox.ItemStyleGenerator;
import de.binaerebauten.gleichklang.core.model.BaseEntity_;
import de.binaerebauten.gleichklang.core.model.matching.Relationship;
import de.binaerebauten.gleichklang.core.model.matching.Relationship.Affiliation;
import de.binaerebauten.gleichklang.core.model.matching.Relationship_;
import de.binaerebauten.gleichklang.core.model.user.ClientInformation.Device;
import de.binaerebauten.gleichklang.core.model.user.MemberStatus;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.model.user.User_;
import de.binaerebauten.gleichklang.core.utils.ComboBoxUtils;
import de.binaerebauten.gleichklang.core.utils.filter.FilterSpecificationBuilder;
import de.binaerebauten.gleichklang.core.view.AbstractNavigateView;
import de.binaerebauten.gleichklang.core.view.component.ComponentFactory;
import de.binaerebauten.gleichklang.core.view.component.ComponentReplacer;
import de.binaerebauten.gleichklang.core.view.component.ComponentReplacer.SimpleReplacer;
import de.binaerebauten.gleichklang.core.view.component.FilterControlComponent;
import de.binaerebauten.gleichklang.core.view.component.FilterControlComponent.FilterControlHandler;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanItemContainer.LazyBeanFilteredItemsHandler;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;
import de.binaerebauten.gleichklang.core.view.filter.AbstractCategoryFilter;
import de.binaerebauten.gleichklang.core.view.filter.SimpleAttributeFilter;
import de.binaerebauten.gleichklang.core.view.filter.SimpleUserFilter.SimpleUserIdFilter;
import de.binaerebauten.gleichklang.memberweb.view.RelationshipView.RelationshipViewListener;
import de.binaerebauten.gleichklang.memberweb.view.component.RelationshipTable;
import de.binaerebauten.gleichklang.memberweb.view.component.RelationshipTable.RelationshipTableHandler;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import java.util.Collection;
import java.util.Objects;

public class RelationshipViewImpl extends AbstractNavigateView<RelationshipViewListener> implements RelationshipView
{
	private static class CategoryFilter extends AbstractCategoryFilter<Relationship>
	{
		CategoryFilter(boolean initialState, Collection<RecommendationCategory> visibleCategories)
		{
			super(initialState, visibleCategories);
		}

		@Override
		protected Specification<Relationship> createSingleFilter(RecommendationCategory category)
		{
			return (root, query, cb) -> cb.isMember(category, root.get(Relationship_.categories));
		}
	}

	private static class ViewedFilter implements Specification<Relationship>
	{
		private final boolean viewed;

		ViewedFilter(boolean viewed)
		{
			this.viewed = viewed;
		}

		@Override
		public Predicate toPredicate(Root<Relationship> root, CriteriaQuery<?> query, CriteriaBuilder cb)
		{
			return cb.equal(root.get(Relationship_.viewed), viewed);
		}
	}

	private static class FootprintFilter implements Specification<Relationship>
	{
		@Override
		public Predicate toPredicate(Root<Relationship> root, CriteriaQuery<?> query, CriteriaBuilder cb)
		{
			return cb.isNotNull(root.get(Relationship_.footprint));
		}
	}

	private static class CanceledFilter implements Specification<Relationship>
	{
		@Override
		public Predicate toPredicate(Root<Relationship> root, CriteriaQuery<?> query, CriteriaBuilder cb)
		{
			return cb.and(
					cb.equal(root.get(Relationship_.targetUser).get(User_.memberStatus), MemberStatus.REGISTERED),
					//cb.equal(root.get(Relationship_.targetUser).get(User_.isBlocked), false),
					cb.or(

							cb.isMember(RecommendationCategory.FRIENDSHIP, root.get(Relationship_.targetUser).get(User_.categories)),
							cb.isMember(RecommendationCategory.PARTNERSHIP, root.get(Relationship_.targetUser).get(User_.categories))
					)
			);
		}
	}

	private static class AffiliationFilter implements Specification<Relationship>
	{
		@Override
		public Predicate toPredicate(Root<Relationship> root, CriteriaQuery<?> query, CriteriaBuilder cb)
		{
			final Subquery<Relationship> subquery = query.subquery(Relationship.class);
			final Root<Relationship> root2 = subquery.from(Relationship.class);
			subquery.select(root2);

			final Predicate predicate = cb.and
					(
							cb.equal(root.get(Relationship_.sourceUser), root2.get(Relationship_.targetUser)),
							cb.equal(root.get(Relationship_.targetUser), root2.get(Relationship_.sourceUser)),
							cb.equal(root2.get(Relationship_.affiliation), Affiliation.POSITIVE)
					);

			subquery.where(predicate);

			return cb.exists(subquery);
		}
	}

	private static class ReceivedFootprintFilter implements Specification<Relationship>
	{
		@Override
		public Predicate toPredicate(Root<Relationship> root, CriteriaQuery<?> query, CriteriaBuilder cb)
		{
			final Subquery<Relationship> subquery = query.subquery(Relationship.class);
			final Root<Relationship> root2 = subquery.from(Relationship.class);
			subquery.select(root2);

			final Predicate predicate = cb.and
					(
							cb.equal(root.get(Relationship_.sourceUser), root2.get(Relationship_.targetUser)),
							cb.equal(root.get(Relationship_.targetUser), root2.get(Relationship_.sourceUser)),
							cb.isNotNull(root2.get(Relationship_.footprint))
					);

			subquery.where(predicate);

			return cb.exists(subquery);
		}
	}

	private final RelationshipTable relationshipTable;
	private final SimpleReplacer categoryFilterControlComponent;
	private final ComponentReplacer<FilterControlComponent> filterComponent;
	private final SimpleUserIdFilter<Relationship> relationshipFilter;

	private final ComponentContainer canceledFilterContainer = new CssLayout();

	private CategoryFilter categoryFilter = null;
	private ComboBox categoryComboBox;

	public RelationshipViewImpl(Device device)
	{
		relationshipTable = createRelationshipTable(device);

		categoryFilterControlComponent = new SimpleReplacer();

		filterComponent = new ComponentReplacer<>();
		filterComponent.addStyleName(CssStyle.USER_FILTER_COMBOBOX.getStyleName());
		relationshipFilter = new SimpleUserIdFilter<>(Relationship_.targetUserId);
		relationshipFilter.setItemComponent(relationshipTable.getPagingComponent());

		final Button focusPlaceholder = new Button();
		focusPlaceholder.addStyleName(CssStyle.FOCUS_BTN.getStyleName());

		final Button goToTopButton = new Button();
		goToTopButton.addStyleName(CssStyle.GO_TO_TOP.getStyleName());
		goToTopButton.addStyleName(CssStyle.GREEN.getStyleName());
		goToTopButton.setIcon(FontAwesome.CHEVRON_UP);
		goToTopButton.setCaption(I18N.GOTOTOP.msg());
		goToTopButton.addClickListener(event -> focusPlaceholder.focus());

		final VerticalLayout layout = new VerticalLayout();
		layout.setStyleName(CssStyle.RELATIONSHIP_VIEW_WRAPPER.getStyleName());

		layout.addComponent(focusPlaceholder);
		layout.addComponent(createHeader());
		layout.addComponent(categoryFilterControlComponent);
		layout.addComponent(createAffiliateFilterControl());
		layout.addComponent(filterComponent);
		layout.addComponent(createAdditionalFilters());
		layout.addComponent(relationshipTable);
		layout.addComponent(goToTopButton);

		initView(device);

		setCompositionRoot(layout);

		focusPlaceholder.focus();
	}

	private void refreshFilter(CheckBox checkBox, Specification<Relationship> filter)
	{
		relationshipTable.setCancelCheckBoxTrue(checkBox.getValue());
		if (checkBox.getValue())
		{
			checkBox.removeStyleName(CssStyle.UNCHECKED.getStyleName());
			relationshipTable.getPagingComponent().addFilter(filter);
		}
		else
		{
			checkBox.addStyleName(CssStyle.UNCHECKED.getStyleName());
			relationshipTable.getPagingComponent().removeFilter(filter);
		}
	}

	private Component createAdditionalFilters()
	{
		final HorizontalLayout layout = new HorizontalLayout();
		layout.setStyleName(CssStyle.RELATIONSHIP_FILTER_CHECKBOX_WRAPPER.getStyleName());
		layout.setSizeFull();

		final ViewedFilter unviewedFilter = new ViewedFilter(false);
		final AffiliationFilter affiliationFilter = new AffiliationFilter();
		final ReceivedFootprintFilter receivedFootprintFilter = new ReceivedFootprintFilter();
		final FootprintFilter footprintFilter = new FootprintFilter();
		final CanceledFilter canceledFilter = new CanceledFilter();

		final CheckBox unviewedCheckBox = ComponentFactory.getInstance().createField(CheckBox.class, I18N.RELATIONSHIP_FILTER_UNVIEWED.msg());
		final CheckBox affiliationCheckBox = ComponentFactory.getInstance().createField(CheckBox.class, I18N.RELATIONSHIP_FILTER_AFFILIATIONSHORT.msg());
		final CheckBox receivedFootprintCheckBox = ComponentFactory.getInstance().createField(CheckBox.class, I18N.RELATIONSHIP_FILTER_RECEIVEDFOOTPRINT.msg());
		final CheckBox sentFootprintCheckBox = ComponentFactory.getInstance().createField(CheckBox.class, I18N.RELATIONSHIP_FILTER_FOOTPRINT.msg());
		final CheckBox canceledCheckBox = ComponentFactory.getInstance().createField(CheckBox.class, I18N.RELATIONSHIP_FILTER_CANCELED.msg());

		unviewedCheckBox.setDescription(I18N.RELATIONSHIP_FILTER_UNVIEWED.msg());
		affiliationCheckBox.setDescription(I18N.RELATIONSHIP_FILTER_AFFILIATIONLONG.msg());
		receivedFootprintCheckBox.setDescription(I18N.RELATIONSHIP_FILTER_RECEIVEDFOOTPRINT.msg());
		sentFootprintCheckBox.setDescription(I18N.RELATIONSHIP_FILTER_FOOTPRINT.msg());
		canceledCheckBox.setDescription(I18N.RELATIONSHIP_FILTER_CANCELED.msg());


		final CssLayout spacer1 = new CssLayout();
		final CssLayout spacer2 = new CssLayout();
		final CssLayout spacer3 = new CssLayout();
		final CssLayout spacer4 = new CssLayout();
		spacer1.setPrimaryStyleName(CssStyle.CHECKBOX_SPACER.getStyleName());
		spacer2.setPrimaryStyleName(CssStyle.CHECKBOX_SPACER.getStyleName());
		spacer3.setPrimaryStyleName(CssStyle.CHECKBOX_SPACER.getStyleName());
		spacer4.setPrimaryStyleName(CssStyle.CHECKBOX_SPACER.getStyleName());

		final Image iconAffiliation = new Image();
		iconAffiliation.setSource(new ThemeResource("img/icon_affiliation.png"));

		final Image iconFootprintReceived = new Image();
		iconFootprintReceived.setSource(new ThemeResource("img/icon_foot_bekommen.svg"));

		final Image iconFootprintSent = new Image();
		iconFootprintSent.setSource(new ThemeResource("img/icon_foot_vergeben.svg"));

		final Image iconCanceled = new Image();
		iconCanceled.setSource(new ThemeResource("img/canceled_avatar_filter.svg"));

		final CssLayout unviewedWrapper = createAdditionalFilter(unviewedCheckBox, new Label(I18N.RELATIONSHIP_NEW.msg()));
		final CssLayout affiliationWrapper = createAdditionalFilter(affiliationCheckBox, iconAffiliation);
		final CssLayout receivedWrapper = createAdditionalFilter(receivedFootprintCheckBox, iconFootprintReceived);
		final CssLayout sentWrapper = createAdditionalFilter(sentFootprintCheckBox, iconFootprintSent);
		final CssLayout canceledWrapper = createAdditionalFilter(canceledCheckBox, iconCanceled);

		unviewedCheckBox.addValueChangeListener(event -> refreshFilter(unviewedCheckBox, unviewedFilter));
		affiliationCheckBox.addValueChangeListener(event -> refreshFilter(affiliationCheckBox, affiliationFilter));
		receivedFootprintCheckBox.addValueChangeListener(event -> refreshFilter(receivedFootprintCheckBox, receivedFootprintFilter));
		sentFootprintCheckBox.addValueChangeListener(event -> refreshFilter(sentFootprintCheckBox, footprintFilter));
		canceledCheckBox.addValueChangeListener(event -> refreshFilter(canceledCheckBox, canceledFilter));
		canceledCheckBox.setValue(true);

		unviewedCheckBox.addStyleName(CssStyle.UNCHECKED.getStyleName());
		affiliationCheckBox.addStyleName(CssStyle.UNCHECKED.getStyleName());
		receivedFootprintCheckBox.addStyleName(CssStyle.UNCHECKED.getStyleName());
		sentFootprintCheckBox.addStyleName(CssStyle.UNCHECKED.getStyleName());

		canceledFilterContainer.addComponent(canceledWrapper);

		layout.addComponents(unviewedWrapper, spacer1, affiliationWrapper, spacer2, receivedWrapper, spacer3, sentWrapper, spacer4, canceledFilterContainer);
		layout.setExpandRatio(unviewedWrapper, 0.24f);
		layout.setExpandRatio(spacer1, 0.01f);
		layout.setExpandRatio(affiliationWrapper, 0.24f);
		layout.setExpandRatio(spacer2, 0.01f);
		layout.setExpandRatio(receivedWrapper, 0.24f);
		layout.setExpandRatio(spacer3, 0.01f);
		layout.setExpandRatio(sentWrapper, 0.24f);
		layout.setExpandRatio(spacer4, 0.01f);
		layout.setExpandRatio(canceledFilterContainer, 0.24f);

		return layout;
	}

	private CssLayout createAdditionalFilter(Component checkBox, Component icon)
	{
		Objects.requireNonNull(checkBox);

		final CssLayout layout = new CssLayout();
		layout.setWidthUndefined();

		final Label label = new Label(checkBox.getCaption());
		label.setPrimaryStyleName(CssStyle.FILTER_LABEL.getStyleName());

		checkBox.setCaption("");

		layout.addComponent(checkBox);
		layout.addComponent(label);
		if(icon != null) layout.addComponent(icon);

		return layout;
	}

	private Component createHeader()
	{
		final HorizontalLayout layout = new HorizontalLayout();
		layout.setSizeUndefined();
		final Label label = new Label(I18N.RELATIONSHIPVIEW_HEADER.getName());
		layout.setStyleName(CssStyle.VIEW_HEADER.getStyleName());
		layout.addComponent(label);

		return layout;
	}

	private Component createCategoryFilterControl(Collection<RecommendationCategory> visibleCategories, ComboBox categoryComboBox)
	{
		final HorizontalLayout layout = new HorizontalLayout();
		layout.setSizeFull();
		layout.addStyleName(CssStyle.RELATIONSHIP_CATEGORY_FILTER_CONTROL.getStyleName());
		layout.setVisible(visibleCategories.size() > 1);

		layout.addComponent(categoryComboBox);

		return layout;
	}

	private ComboBox createCategoryFilterComboBox(Collection<RecommendationCategory> visibleCategories,
												  String preselectedCategory, CategoryFilter categoryFilter)
	{
		final ComboBox recommendationCategoryComboBox = ComponentFactory.getInstance().createField(ComboBox.class);

		ComboBoxUtils.addEmptyEntry(recommendationCategoryComboBox, I18N.RELATIONSHIPVIEW_CAPTION_ALLCATEGORIES.msg());
		for (RecommendationCategory category : visibleCategories)
		{
			ComboBoxUtils.addEntry(recommendationCategoryComboBox, category, category.getIcon());
		}

		recommendationCategoryComboBox.setTextInputAllowed(false);
		recommendationCategoryComboBox.setItemStyleGenerator((ItemStyleGenerator) (source, itemId) ->
				CssStyle.TABSHEET_DROPDOWN_POPUP_ITEMS.name());
		recommendationCategoryComboBox.select(ComboBoxUtils.emptyEntry);

		recommendationCategoryComboBox.addValueChangeListener(event ->
		{
			final RecommendationCategory selectedCategory =
					ComboBoxUtils.getValue(recommendationCategoryComboBox, RecommendationCategory.class);
			if (Objects.isNull(selectedCategory))
			{
				categoryFilter.reset();
			}
			else
			{
				categoryFilter.setCategoryEnabled(selectedCategory);
			}

			relationshipTable.getPagingComponent().refresh();
		});

		if (!org.apache.commons.lang.StringUtils.isBlank(preselectedCategory))
		{
			RecommendationCategory category = RecommendationCategory.valueOf(preselectedCategory);
			recommendationCategoryComboBox.select(category);
		}

		if(visibleCategories.size() == 1)
		{
			recommendationCategoryComboBox.select(visibleCategories.iterator().next());
		}

		return recommendationCategoryComboBox;
	}

	private Component createAffiliateFilterControl()
	{
		final SimpleAttributeFilter<Relationship, Affiliation> affiliationFilter = new SimpleAttributeFilter<>(Relationship_.affiliation);
		affiliationFilter.setItemComponent(relationshipTable.getPagingComponent());

		final HorizontalLayout layout = new HorizontalLayout();
		layout.setSizeFull();
		layout.addStyleName(CssStyle.TABSHEET_DROPDOWN_LIGHTGREEN.getStyleName());

		final ComboBox comboBox = ComponentFactory.getInstance().createField(ComboBox.class);

		ComboBoxUtils.addEmptyEntry(comboBox, I18N.RELATIONSHIPVIEW_CAPTION_ALLAFFILIATES.msg());
		for (Affiliation affiliation : Affiliation.values())
		{
			ComboBoxUtils.addEntry(comboBox, affiliation, affiliation.getIcon());
		}

		comboBox.setTextInputAllowed(false);
		comboBox.setItemStyleGenerator((ItemStyleGenerator) (source, itemId) ->
				CssStyle.TABSHEET_DROPDOWN_POPUP_ITEMS.name());
		comboBox.select(ComboBoxUtils.emptyEntry);

		comboBox.addValueChangeListener(event -> affiliationFilter.setValue(
				ComboBoxUtils.getValue(comboBox, Affiliation.class)));

		layout.addComponent(comboBox);

		return layout;
	}

	private RelationshipTable createRelationshipTable(Device device)
	{
		final RelationshipTable relationshipTable = new RelationshipTable(device);

		relationshipTable.setSortPropertyId(false, BaseEntity_.createDate);

		return relationshipTable;
	}

	@Override
	public void updateItem(Relationship relationship)
	{
		relationshipTable.refreshItem(relationship);
	}

	@Override
	public void setRelationshipHandler(LazyBeanFilteredItemsHandler<Relationship> handler, RelationshipTableHandler relationshipTableHandler)
	{
		relationshipTable.setRelationshipHandler(handler, relationshipTableHandler);
	}

	@Override
	public void setFilterHandlerAndBuilder(FilterControlHandler filterControlHandler, FilterSpecificationBuilder filterSpecificationBuilder)
	{
		relationshipFilter.setValue(null);
		filterComponent.setComponent(null);

		if (filterControlHandler != null)
		{
			final FilterControlComponent filterControlComponent = new FilterControlComponent(null,
					filterControlHandler, filterSpecificationBuilder);
			filterControlComponent.addFilterChangedListener(relationshipFilter::setValue);
			filterComponent.setComponent(filterControlComponent);
		}
	}

	@Override
	public void setActiveRecommendationCategories(Collection<RecommendationCategory> categories, String category)
	{
		Objects.requireNonNull(categories);

		final CategoryFilter newFilter = new CategoryFilter(true, categories);
		relationshipTable.getPagingComponent().replaceFilter(categoryFilter, newFilter);
		this.categoryFilter = newFilter;

		categoryComboBox = createCategoryFilterComboBox(categories, category, categoryFilter);
		categoryFilterControlComponent.setComponent(createCategoryFilterControl(categories, categoryComboBox));
	}

	@Override
	public void onDeviceChanged(Device device)
	{
		super.onDeviceChanged(device);

		relationshipTable.onDeviceChanged(device);
	}

	@Override
	public void initMobileView()
	{
		canceledFilterContainer.setVisible(false);
		filterComponent.setVisible(false);
	}

	@Override
	public void initDesktopView()
	{
		canceledFilterContainer.setVisible(true);
		filterComponent.setVisible(true);
	}

	@Override
	public RecommendationCategory getSelectedCategory()
	{
		return ComboBoxUtils.getValue(categoryComboBox, RecommendationCategory.class);
	}
}
