package de.binaerebauten.gleichklang.core.view.component;

import com.vaadin.ui.Component;
import de.binaerebauten.gleichklang.core.model.filter.AbstractFilter;
import de.binaerebauten.gleichklang.core.model.filter.UserFilter.UserFilterType;
import de.binaerebauten.gleichklang.core.model.locatable.LocatableEntity;
import de.binaerebauten.gleichklang.core.model.questionnaire.Choice;
import de.binaerebauten.gleichklang.core.model.questionnaire.ChoiceQuestion;
import de.binaerebauten.gleichklang.core.model.questionnaire.NumberQuestion;
import de.binaerebauten.gleichklang.core.model.questionnaire.TextQuestion;

import java.util.List;

/**
 * FilterComponent combines a {@link AbstractFilter} and a Component, so that
 * actual state of the component can be committed to the filter. Also it is
 * necessary for removing a component to remove the filter too.
 */
public interface FilterComponent extends Committable, Component
{
	interface FilterComponentHandler
	{
		/**
		 * Necessary for {@link UserFilterType#REGION_FILTER}
		 *
		 * @param type
		 * @param parent
		 * @param <T>
		 * @return
		 */
		<T extends LocatableEntity> List<T> getLocatableEntities(Class<T> type, LocatableEntity parent);

		/**
		 * Necessary for {@link UserFilterType#CHOICE_QUESTION_FILTER}
		 *
		 * @return
		 */
		List<ChoiceQuestion> getChoiceQuestions();

		List<TextQuestion> getTextQuestions();
		
		List<NumberQuestion> getNumberQuestions();

		List<Choice> getSexChoiceQuestionChoices();
	}

	AbstractFilter getFilter();
}
