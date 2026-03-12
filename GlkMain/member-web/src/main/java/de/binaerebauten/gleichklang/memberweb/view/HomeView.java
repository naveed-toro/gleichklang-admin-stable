package de.binaerebauten.gleichklang.memberweb.view;

import com.google.common.collect.Multimap;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.Component;
import de.binaerebauten.gleichklang.core.model.matching.Relationship;
import de.binaerebauten.gleichklang.core.model.news.UserNews;
import de.binaerebauten.gleichklang.core.model.questionnaire.Question.Requirement;
import de.binaerebauten.gleichklang.core.model.questionnaire.Questionnaire;
import de.binaerebauten.gleichklang.core.model.user.Address;
import de.binaerebauten.gleichklang.core.model.user.ClientInformation;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.navigation.DefaultNavigator;
import de.binaerebauten.gleichklang.core.service.UserService;
import de.binaerebauten.gleichklang.core.service.content.AdvertisementContentTemplate;
import de.binaerebauten.gleichklang.core.view.NavigateView;
import de.binaerebauten.gleichklang.memberweb.view.HomeView.HomeViewListener;
import de.binaerebauten.gleichklang.memberweb.view.component.MyAccountComponent.GotoActivateCategoryHandler;
import de.binaerebauten.gleichklang.memberweb.view.component.MyAccountComponent.GotoRecommendationBreakHandler;

import java.util.*;

public interface HomeView extends NavigateView<HomeViewListener>
{
	interface HomeViewListener extends NavigateView.NavigateViewListener, GotoActivateCategoryHandler, GotoRecommendationBreakHandler
	{
		void saveNews(UserNews news);

		void showIncompleteQuestions(Questionnaire incompleteQuestionnaire, Collection<Requirement> requirements);

		void showAvatar(RecommendationCategory category);

		void showMessages(RecommendationCategory category);

		void showMatches(RecommendationCategory category);

		void showRelationship(Relationship relation, ClientInformation.Device device);

		void showIncompleteAddress(Address address);

//		void showAudio(RecommendationCategory category);

		void showFullNews(UserNews userNews);

		void showAdminMessages();

		void showSubscription();
	}

	/**
	 * Sets the incomplete questionnaires grouped by question.required for the given user.
	 * @param incompleteQuestionnaires
	 */
	void setIncompleteQuestionnaires(Multimap<Questionnaire, Requirement> incompleteQuestionnaires);

	void setNewFootprints(List<Relationship> relationships);

	void setNewsList(List<UserNews> newsList);

	void setCurrentUser(User user);

	void setUserReportDownloader(Map<String, StreamResource> sourceList);

	void setMissingAvatars(List<RecommendationCategory> recommendationCategories);

	void setMissingAudio(Set<RecommendationCategory> recommendationCategories);

	void setNewUserMessages(HashMap<RecommendationCategory, Long> messages);

	void setNewSuggestions(HashMap<RecommendationCategory, Long> suggestions);

	void setNewUserMessagesUser(HashMap<String, Long> messages);

	void setNewSuggestionsUser(HashMap<String, Long> suggestions);

	void setUserReccats(Set<RecommendationCategory> reccats);

    void setVisibleCategories(Set<RecommendationCategory> visibleCategories);

	void setNewUserAdminMessages(Long nrOfMessages);
	
	void setDisabledCategories(Collection<RecommendationCategory> deactivatedCategories, Collection<RecommendationCategory> recommendationBreaks);
	
	void setSubscriptionInfo(String infoText, String buttonText);
	
	void setInvalidEmail(boolean isBlocked, String email, DefaultNavigator navigator, UserService userService, User currentUser);
	
	void checkPanelContent();
	
	void setStaticContent(Map<AdvertisementContentTemplate, Component> staticContent);

}
