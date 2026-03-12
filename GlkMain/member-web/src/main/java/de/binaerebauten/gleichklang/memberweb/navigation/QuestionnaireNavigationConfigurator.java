package de.binaerebauten.gleichklang.memberweb.navigation;

import de.binaerebauten.gleichklang.core.model.questionnaire.Questionnaire;
import de.binaerebauten.gleichklang.core.model.questionnaire.QuestionnaireActivation;
import de.binaerebauten.gleichklang.core.model.user.ClientInformation;
import de.binaerebauten.gleichklang.core.model.user.ClientInformation.Device;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.navigation.AbstractNavigator.DefaultViewItem;
import de.binaerebauten.gleichklang.core.navigation.DefaultNavigator;
import de.binaerebauten.gleichklang.core.presenter.NavigatePresenter;
import de.binaerebauten.gleichklang.core.presenter.question.QuestionnairePresenter;
import de.binaerebauten.gleichklang.core.presenter.question.QuestionnairePresenter.ActivatorLevel;
import de.binaerebauten.gleichklang.core.service.QuestionnaireService;
import de.binaerebauten.gleichklang.core.service.UserService;
import de.binaerebauten.gleichklang.core.view.QuestionnaireView;
import de.binaerebauten.gleichklang.core.view.QuestionnaireView.QuestionnaireActivationListener;
import de.binaerebauten.gleichklang.core.view.QuestionnaireViewImpl;
import de.binaerebauten.gleichklang.core.view.component.MenuItem;
import de.binaerebauten.gleichklang.core.view.component.question.QuestionGroupTabSheet;
import de.binaerebauten.gleichklang.memberweb.presenter.AudioPresenter;
import de.binaerebauten.gleichklang.memberweb.presenter.MediaPresenter;
import de.binaerebauten.gleichklang.memberweb.presenter.ProfilePresenter;
import de.binaerebauten.gleichklang.memberweb.view.AudioViewImpl;
import de.binaerebauten.gleichklang.memberweb.view.MediaViewImpl;
import de.binaerebauten.gleichklang.memberweb.view.ProfileViewImpl;
import org.springframework.context.ApplicationContext;

import java.util.Collections;
import java.util.List;

/**
 * Creates menu items for recommendation categories for current user
 */
public class QuestionnaireNavigationConfigurator implements QuestionnaireActivationListener
{
	private class ProfileViewItem extends DefaultViewItem
	{
		private final RecommendationCategory category;
		
		public ProfileViewItem(RecommendationCategory category)
		{
			super("PROFILE_" + category.name(), I18N.MEMBERNAVIGATOR_MENUITEM_PROFILE.msg(category));
			this.category = category;
		}
		
		@Override
		public NavigatePresenter getPresenter(Device device)
		{
			return new ProfilePresenter(ctx, category, new ProfileViewImpl(device==Device.MOBILE?Device.MOBILE:Device.getDefault()), activation);
		}
	}

	private class MediaViewItem extends DefaultViewItem
	{

		public MediaViewItem()
		{
			super("MEDIA" , I18N.MEMBERMENUITEM_ENUM_MEDIA.msg());
		}

		@Override
		public NavigatePresenter getPresenter(Device device)
		{
			return new MediaPresenter(ctx, new MediaViewImpl(), device);
		}
	}

	private class AudioViewItem extends DefaultViewItem
	{

		public AudioViewItem()
		{
			super("AUDIO" , "Audio");
		}

		@Override
		public NavigatePresenter getPresenter(Device device)
		{
			return new AudioPresenter(ctx, new AudioViewImpl(device), device);
		}
	}
	
	private class QuestionnaireViewItem extends DefaultViewItem
	{
		private final Questionnaire questionnaire;
		
		public QuestionnaireViewItem(Questionnaire questionnaire)
		{
			super(questionnaire.getI18nKey(), questionnaire.getName());
			this.questionnaire = questionnaire;
		}
		
		@Override
		public NavigatePresenter getPresenter(Device device)
		{
			final QuestionGroupTabSheet questionGroupTabSheet = new QuestionGroupTabSheet();
			final QuestionnaireView questionnaireView = new QuestionnaireViewImpl(questionnaire, questionGroupTabSheet);
			final QuestionnairePresenter questionnairePresenter = new QuestionnairePresenter(ctx, QuestionnaireNavigationConfigurator.this, activation, questionnaireView, false, ActivatorLevel.QUESTION);
			
			questionGroupTabSheet.addSelectedTabChangeListener(event ->
			{
				questionnaireView.setSelectedDropdownValue(questionGroupTabSheet.getSelectedTab());
			});
			
			return questionnairePresenter;
		}
	}
	
	protected final ApplicationContext ctx;
	private final DefaultNavigator navigator;
	private final QuestionnaireActivation activation;
	private final QuestionnaireService questionnaireService;
	private final User currentUser;
	private boolean disabled;
	
	public QuestionnaireNavigationConfigurator(ApplicationContext ctx, DefaultNavigator navigator)
	{
		this.ctx = ctx;
		this.navigator = navigator;
		this.questionnaireService = ctx.getBean(QuestionnaireService.class);
		currentUser = ctx.getBean(UserService.class).getCurrentUser();
		this.activation = questionnaireService.getActivation(currentUser);
	}
	
	public void createMenusForCategories(boolean disabled)
	{
		this.disabled = disabled;
		final MenuItem personalProfile = this.navigator.createParentMenuItem("QUESTIONNAIRES", I18N.MEMBERNAVIGATOR_MENUITEM_QUESTIONAIRES.msg());
		createQuestionnairyChildMenuItems(null);

		createMediaMenuItem(personalProfile);
		createAudioMenuItem(personalProfile);
		personalProfile.setDisabled(disabled);
		
		currentUser.getOrderedCategories().forEach(item -> createParentMenuItem(item, null));
	}
	
	private void createParentMenuItem(RecommendationCategory recommendationCategory, MenuItem parentItem)
	{
		final MenuItem questionnaireGroupMenu = this.navigator.createParentMenuItem(recommendationCategory.getName(), recommendationCategory.getName());
		
		questionnaireGroupMenu.setDisabled(disabled);
		
		createQuestionnairyChildMenuItems(recommendationCategory);
	}
	
	private void createQuestionnairyChildMenuItems(RecommendationCategory recommendationCategory)
	{
		createProfileMenuItem(recommendationCategory);

		final List<Questionnaire> questionnaires = questionnaireService.getQuestionnaires(Collections.singleton(recommendationCategory), false);
		questionnaires.forEach(this::createQuestionnaireView);
	}

	private void createMediaMenuItem(MenuItem parent)
	{
		MenuItem item = navigator.createMenuItem(new MediaViewItem(), parent);
		item.setDisabled(disabled);
	}

	private void createAudioMenuItem(MenuItem parent)
	{
		MenuItem item = navigator.createMenuItem(new AudioViewItem(), parent);
		item.setDisabled(disabled);
	}

	private void createProfileMenuItem(RecommendationCategory category)
	{
		if (category == null) return;

		MenuItem item = navigator.createMenuItem(new ProfileViewItem(category), getParentMenu(category));
		item.setDisabled(disabled);
	}
	
	private void createQuestionnaireView(Questionnaire questionnaire)
	{
		final MenuItem menuItem = this.navigator.createMenuItem(new QuestionnaireViewItem(questionnaire), getParentMenu(questionnaire));
		menuItem.setDisabled(disabled);
		
		activateQuestionnaire(questionnaire, activation.isEnabledQuestionnaire(questionnaire));
	}
	
	private MenuItem getParentMenu(Questionnaire questionnaire)
	{
		RecommendationCategory recommendationCategory = questionnaire.getRecommendationCategory();
		return getParentMenu(recommendationCategory);
	}
	
	private MenuItem getParentMenu(RecommendationCategory recommendationCategory)
	{
		String parentMenuKey = recommendationCategory != null ? recommendationCategory.msg() : "QUESTIONNAIRES";
		return this.navigator.getMenuByKey(parentMenuKey);
	}
	
	public QuestionnaireActivation getActivation()
	{
		return activation;
	}
	
	@Override
	public void activateQuestionnaire(Questionnaire questionnaire, boolean enabled)
	{
		this.navigator.setVisibleMenuItem(questionnaire.getI18nKey(), enabled);
	}
}
