package de.binaerebauten.gleichklang.core.repository;

import de.binaerebauten.gleichklang.core.model.user.RecommendationBreak;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.utils.DefaultEntityFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class RecommendationBreakRepositoryTest extends AbstractRepositoryTest<RecommendationBreak>
{
	@Autowired
	private RecommendationBreakRepository recommendationBreakRepository;

	@Autowired
	private DefaultEntityFactory entityFactory;

	private User user;

	public RecommendationBreakRepositoryTest()
	{
	}

	@Override
	protected Collection<RecommendationBreak> getPersistedEntities()
	{
		user = entityFactory.persistDefaultUser("test");

		return Collections.singletonList(entityFactory.persistDefaultRecommendationBreak(user));
	}

	@Override
	protected JpaRepository<RecommendationBreak, Long> getRepository()
	{
		return recommendationBreakRepository;
	}

	@Test
	public void testFindByUser()
	{
		final List<RecommendationBreak> recommendationCategories = recommendationBreakRepository.findByUser(user);

		assertThat(recommendationCategories.size(), equalTo(1));
	}

	@Test
	public void testDeleteByUser()
	{
		assertThat(recommendationBreakRepository.findAll().size(), equalTo(1));

		recommendationBreakRepository.deleteByUser(user);

		assertThat(recommendationBreakRepository.findAll().size(), equalTo(0));
	}
}
