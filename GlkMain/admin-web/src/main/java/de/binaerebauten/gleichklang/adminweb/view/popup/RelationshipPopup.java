package de.binaerebauten.gleichklang.adminweb.view.popup;

import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.VerticalLayout;
import de.binaerebauten.gleichklang.core.model.matching.Relationship;
import de.binaerebauten.gleichklang.core.model.matching.Relationship_;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.model.user.User_;
import de.binaerebauten.gleichklang.core.service.validator.ValidationException;
import de.binaerebauten.gleichklang.core.utils.filter.FilterSpecificationBuilder;
import de.binaerebauten.gleichklang.core.view.component.ComponentGroup;
import de.binaerebauten.gleichklang.core.view.component.FilterControlComponent;
import de.binaerebauten.gleichklang.core.view.component.FilterControlComponent.FilterControlHandler;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanTable;
import de.binaerebauten.gleichklang.core.view.component.Popup;
import de.binaerebauten.gleichklang.core.view.component.validator.SaveHelper;

import java.util.Arrays;
import java.util.Objects;

public class RelationshipPopup extends Popup
{
	public interface CreateCallback
	{
		void create(Relationship relationship) throws ValidationException;
	}
	
	private final ComponentGroup<Relationship> relationshipComponentGroup;
	private User targetUser = null;
	
	public RelationshipPopup(FilterControlHandler filterControlHandler, FilterSpecificationBuilder filterSpecificationBuilder, User sourceUser, CreateCallback createCallback)
	{
		Objects.requireNonNull(filterControlHandler);
		Objects.requireNonNull(filterSpecificationBuilder);
		Objects.requireNonNull(sourceUser);
		Objects.requireNonNull(createCallback);
		
		final Relationship relationship = new Relationship();
		relationship.setSourceUserId(sourceUser.getId());
		
		relationshipComponentGroup = new ComponentGroup<>(Relationship.class, relationship);
		
		setCaption(I18N.RELATIONSHIP_POPUP_NEW.msg());
		
		final SaveHelper saveHelper = new SaveHelper(this::commit, () -> save(createCallback), true);
		saveHelper.getSaveButton().setCaption(I18N.RELATIONSHIP_POPUP_CREATE_BUTTON.msg());
		
		final VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);
		layout.setMargin(true);
		
		layout.addComponents(saveHelper.getValidationComponent(), createAddUser(filterControlHandler, filterSpecificationBuilder), createDialog(), saveHelper.getSaveButton());
		
		setContent(layout);
		
		saveHelper.addFields(relationshipComponentGroup);
	}
	
	private Component createAddUser(FilterControlHandler filterControlHandler, FilterSpecificationBuilder filterSpecificationBuilder)
	{
		final VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);
		
		final FilterControlComponent filterControlComponent = new FilterControlComponent(filterControlHandler, filterSpecificationBuilder);
		
		final Label usersLabel = new Label();
		
		final LazyBeanTable<User> table = new LazyBeanTable<>();
		table.setSelectable(true);
		table.setMultiSelect(false);
		table.setSizeFull();
		
		table.addContainerProperty(I18N.RELATIONSHIP_POPUP_ALIAS.msg(), User_.alias);
		table.addContainerProperty(I18N.RELATIONSHIP_POPUP_LASTNAME.msg(), User_.lastName);
		table.addContainerProperty(I18N.RELATIONSHIP_POPUP_FIRSTNAME.msg(), User_.firstName);
		
		table.setHandler(filterControlHandler::getUsers);
		filterControlComponent.addFilterChangedListener(table::setFilter);
		
		table.addValueChangeListener(values ->
		{
			targetUser = table.getValue();
			usersLabel.setValue(targetUser == null ? "" : targetUser.getAlias());
		});
		
		layout.addComponents(filterControlComponent, usersLabel, table);
		
		return layout;
	}
	
	private void commit() throws CommitException
	{
		if (targetUser == null)
			throw new CommitException(I18N.RELATIONSHIP_POPUP_VALIDATION_MISSING_TARGET_USER.msg());
		
		final Relationship bean = relationshipComponentGroup.getItemDataSource().getBean();
		bean.setTargetUserId(targetUser.getId());
	}
	
	private void save(CreateCallback createCallback) throws ValidationException
	{
		createCallback.create(relationshipComponentGroup.getItemDataSource().getBean());
		close();
	}
	
	private Component createDialog()
	{
		final VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);
		
		layout.addComponent(relationshipComponentGroup.buildAndBind(I18N.RELATIONSHIP_POPUP_AFFILIATION.msg(), Relationship_.affiliation));
		layout.addComponent(createCategoriesComponent());
		layout.addComponent(relationshipComponentGroup.buildAndBind(I18N.RELATIONSHIP_POPUP_DELETED.msg(), Relationship_.deleted));
		layout.addComponent(relationshipComponentGroup.buildAndBind(I18N.RELATIONSHIP_POPUP_MEMO.msg(), Relationship_.memo));
		layout.addComponent(relationshipComponentGroup.buildAndBind(I18N.RELATIONSHIP_POPUP_VIEWED.msg(), Relationship_.viewed));
		
		return layout;
	}
	
	private Component createCategoriesComponent()
	{
		final ListSelect listSelect = relationshipComponentGroup.buildAndBind(true, I18N.RELATIONSHIP_POPUP_CATEGORIES.msg(), ListSelect.class, Relationship_.categories);
		listSelect.setContainerDataSource(new BeanItemContainer<>(RecommendationCategory.class, Arrays.asList(RecommendationCategory.values())));
		
		return listSelect;
	}
}
