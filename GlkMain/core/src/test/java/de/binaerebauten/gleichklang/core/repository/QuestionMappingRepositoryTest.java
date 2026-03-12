package de.binaerebauten.gleichklang.core.repository;

import de.binaerebauten.gleichklang.core.model.matching.AbstractQuestionsMapping;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.utils.DefaultEntityFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Arrays;
import java.util.Collection;

public class QuestionMappingRepositoryTest extends AbstractRepositoryTest<AbstractQuestionsMapping>
{
	@Autowired
	private QuestionMappingRepository questionMappingRepository;

	@Autowired
	private DefaultEntityFactory entityFactory;

	public QuestionMappingRepositoryTest()
	{
	}

	@Override
	protected Collection<AbstractQuestionsMapping> getPersistedEntities()
	{
		return Arrays.asList(
				entityFactory.persistDefaultAffinityMapping(RecommendationCategory.PARTNERSHIP),
				entityFactory.persistDefaultChoiceQuestionsMapping(RecommendationCategory.PARTNERSHIP),
				entityFactory.persistDefaultNumberQuestionsMapping(RecommendationCategory.PARTNERSHIP),
				entityFactory.persistDefaultAgeQuestionMapping(RecommendationCategory.PARTNERSHIP),
				entityFactory.persistDefaultAvatarQuestionMapping(RecommendationCategory.PARTNERSHIP));
	}

	@Override
	protected JpaRepository<AbstractQuestionsMapping, Long> getRepository()
	{
		return questionMappingRepository;
	}
}
