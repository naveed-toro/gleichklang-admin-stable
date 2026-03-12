package de.binaerebauten.gleichklang.memberweb.view;

import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.service.validator.ValidationException;
import de.binaerebauten.gleichklang.core.view.AbstractNavigateView;
import de.binaerebauten.gleichklang.core.view.component.FooterCommandBar;
import de.binaerebauten.gleichklang.core.view.component.FormPanel;
import de.binaerebauten.gleichklang.core.view.component.validator.SaveHelper;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;
import de.binaerebauten.gleichklang.memberweb.navigation.DefaultNavigatorFactory.MemberMenuItem;
import de.binaerebauten.gleichklang.memberweb.view.SubscriptionView.SubscriptionTab;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.stream.Collectors;

/**
 * Created by rgoerner on 19.09.16.
 */
public class ActiveCategoriesViewImpl extends AbstractNavigateView<ActiveCategoriesView.ActiveCategoriesViewListener> implements ActiveCategoriesView
{
	private final VerticalLayout layout;
	
	public ActiveCategoriesViewImpl()
	{
		layout = new VerticalLayout();
		layout.setSizeFull();
		layout.setSpacing(true);
		
		setCompositionRoot(layout);
	}
	
	private void saveCategories(Set<RecommendationCategory> recommendationCategories) throws ValidationException
	{
		getListener().saveCategories(recommendationCategories);
	}
	
	@Override
	public void setCategories(SortedMap<RecommendationCategory, Boolean> categories)
	{
		final FormPanel formPanel = new FormPanel(I18N.ACCOUNT_TAB_CATGEGORY.msg());
		formPanel.setDescription(I18N.ACCOUNT_TAB_CATEGORY_DESCRIPTION.msg());
		formPanel.addStyleName(CssStyle.GK_PANEL.getStyleName());
		
		final List<RecommendationCategory> selectedCategories = categories.entrySet().stream().filter(Map.Entry::getValue).map(Map.Entry::getKey).collect(Collectors.toList());
		final String optionGroupCaption = selectedCategories.size() == RecommendationCategory.values().length ? I18N.ACCOUNT_CAPTION_ACTIVATE_CATEGORIES_ALL.msg() : I18N.ACCOUNT_CAPTION_ACTIVATE_CATEGORIES.msg();
		final OptionGroup optionGroup = new OptionGroup(optionGroupCaption, categories.keySet());
		optionGroup.setMultiSelect(true);
		optionGroup.setValue(selectedCategories);
		optionGroup.setRequired(true);
		formPanel.addFormElement(optionGroup);
		
		if (categories.size() < RecommendationCategory.values().length)
		{
			final Label label = new Label(I18N.ACCOUNT_CAPTION_MORECATEGORIES.msg());
			final Button button = new Button(I18N.ACCOUNT_ACTION_TOUPGRADEOFFERS.msg());
			button.addClickListener(event -> getListener().navigateTo(MemberMenuItem.SUBSCRIPTION, SubscriptionTab.UPGRADEOFFERS));
			button.setIcon(FontAwesome.CHEVRON_RIGHT);
			button.addStyleName(ValoTheme.BUTTON_ICON_ALIGN_RIGHT);
			
			formPanel.addComponent(label);
			formPanel.addComponent(button);
		}
		
		
		final SaveHelper saveHelper = new SaveHelper(() -> saveCategories((Set<RecommendationCategory>) optionGroup.getValue()));
		final FooterCommandBar commandBar = new FooterCommandBar(saveHelper.getSaveButton());
		
		layout.addComponent(saveHelper.getValidationComponent());
		layout.addComponent(formPanel);
		layout.addComponent(commandBar);
	}
}
