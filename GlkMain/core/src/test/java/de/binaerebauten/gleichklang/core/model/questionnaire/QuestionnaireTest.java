package de.binaerebauten.gleichklang.core.model.questionnaire;

import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import org.junit.Test;

import java.util.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for {@link Questionnaire}.
 */
public class QuestionnaireTest
{
	@Test
	public void testCompareByCategory()
	{
		List<Questionnaire> sortedQuestionnaires = new ArrayList<>();

		List<RecommendationCategory> categories = Arrays.asList(null, RecommendationCategory.PARTNERSHIP, RecommendationCategory.FRIENDSHIP);

		for (RecommendationCategory category : categories)
		{
			for (int sortOrder = 0; sortOrder < 3; sortOrder++)
			{
				Questionnaire questionnaire = createQuestionnaire(category, sortOrder);
				sortedQuestionnaires.add(questionnaire);
			}
		}

		List<Questionnaire> questionnairesSortedWithComparator = new ArrayList<>(sortedQuestionnaires);
		Collections.shuffle(questionnairesSortedWithComparator);

		Collections.sort(questionnairesSortedWithComparator, Questionnaire.COMPARE_BY_CATEGORY_AND_SORTORDER);

		assertThat(sortedQuestionnaires, equalTo(questionnairesSortedWithComparator));
	}

	private Questionnaire createQuestionnaire(RecommendationCategory category, int sortOrder)
	{
		final Questionnaire questionnaire = new Questionnaire();
		questionnaire.setRecommendationCategory(category);
		questionnaire.setSortOrder(sortOrder);

		return questionnaire;
	}
}
