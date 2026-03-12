package de.binaerebauten.gleichklang.adminweb.view;

import de.binaerebauten.gleichklang.adminweb.view.MatchingView.MatchingViewListener;
import de.binaerebauten.gleichklang.core.model.matching.*;
import de.binaerebauten.gleichklang.core.presenter.filter.DefaultFilterControlHandler;
import de.binaerebauten.gleichklang.core.utils.filter.FilterSpecificationBuilder;
import de.binaerebauten.gleichklang.core.view.NavigateView;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanItemContainer.LazyBeanFilteredItemsHandler;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanItemContainer.LazyBeanItemsHandler;

import java.util.Collection;

public interface MatchingView extends NavigateView<MatchingViewListener>
{
	enum MatchingTab
	{
		MATRIX,
		QUESTION_MAPPER,
		ACTIVATOR,
		MATCH,
		MATCH_STATISTIC,
		MATCHING_DEBUGGING
	}
	
	interface AliasGetter
	{
		String getAlias(Long userId);
	}
	
	interface MatchingViewListener extends NavigateView.NavigateViewListener
	{
		void newMatrix();

		void editMatrix(MatchingMatrix matchingMatrix);

		void deleteMatrix(MatchingMatrix matchingMatrix);

		void newQuestionMapping();

		void editQuestionMapping(AbstractQuestionsMapping questionsMapping);

		void deleteQuestionMapping(AbstractQuestionsMapping questionMapping);

		void newActivator();

		void editActivator(Activator activator);

		void deleteActivator(Activator activator);

		void startMatching();

		void generateSuggestion();

		void openMatchStatistic(MatchStatistic matchStatistic);
		
		void onTabSelected(MatchingTab selectedTab);
		
		void startDebugMatching(Collection<String> userList);
		
		long getCountRelationships(MatchStatistic matchStatistic);
		
		long getCountAffectedUsers(MatchStatistic matchStatistic);
	}

	void setMatricesHandler(LazyBeanItemsHandler<MatchingMatrix> handler);

	void setQuestionMappingHandler(LazyBeanItemsHandler<AbstractQuestionsMapping> handler);

	void setActivatorHandler(LazyBeanItemsHandler<Activator> handler);

	void setMatchHandler(LazyBeanFilteredItemsHandler<Match> handler);

	void setMatchStatisticHandler(LazyBeanFilteredItemsHandler<MatchStatistic> handler);
	
	void disableMatching();

	void enableMatching();
	
	MatchingTab getSelectedMatchingTab();
	
	void setAliasGetter(AliasGetter aliasGetter);
	
	void setMatchingDebugResult(String result);
	
	void setMatchFilterHandlerAndBuilder(DefaultFilterControlHandler matchFilterControlHandler, FilterSpecificationBuilder filterSpecificationBuilder);
	
	void updateWaitingInfo(String text);
	
	void setAutoMatchingEnabled(boolean autoMatchingEnabled);
}
