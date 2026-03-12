package de.binaerebauten.gleichklang.core.repository;

import de.binaerebauten.gleichklang.core.model.filter.AbstractFilter;
import de.binaerebauten.gleichklang.core.model.filter.TemplateContext;
import de.binaerebauten.gleichklang.core.model.filter.TemplateFilter;
import de.binaerebauten.gleichklang.core.utils.DefaultEntityFactory;
import de.binaerebauten.gleichklang.core.utils.FilterEntityFactory;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public class FilterRepositoryTest extends AbstractRepositoryTest<AbstractFilter>
{
	@Autowired
	private FilterRepository filterRepository;

	@Autowired
	private FilterEntityFactory filterEntityFactory;

	@Autowired
	private DefaultEntityFactory entityFactory;

	public FilterRepositoryTest()
	{
	}

	@Override
	protected Collection<AbstractFilter> getPersistedEntities()
	{
		final AbstractFilter referenceFilter = filterEntityFactory.persistDefaultMemberStatusFilter();

		return Arrays.asList(
				filterEntityFactory.persistDefaultBinaryOperatorFilter(referenceFilter, referenceFilter),
				filterEntityFactory.persistDefaultUnaryOperatorFilter(referenceFilter),
				filterEntityFactory.persistDefaultTemplateFilter(referenceFilter),
				filterEntityFactory.persistDefaultRegionFilter(entityFactory.persistDefaultCountry()),
				filterEntityFactory.persistDefaultProximityFilter(entityFactory.persistDefaultZip(entityFactory.persistDefaultCountry())),
				filterEntityFactory.persistDefaultRecommendationCategoryFilter(),
				filterEntityFactory.persistDefaultMemberStatusFilter(),
				filterEntityFactory.persistDefaultChocieQuestionFilter(entityFactory.persistDefaultChoiceQuestion(entityFactory.persistDefaultQuestionGroup(entityFactory.persistDefaultQuestionnaire()), entityFactory.persistDefaultI18NEntry())),
				filterEntityFactory.persistDefaultFirstNameFilter(),
				filterEntityFactory.persistDefaultLastNameFilter(),
				filterEntityFactory.persistDefaultAliasFilter(),
				filterEntityFactory.persistDefaultMailFilter(),
				filterEntityFactory.persistDefaultActionCodeFilter(),
				referenceFilter);
	}

	@Override
	protected JpaRepository<AbstractFilter, Long> getRepository()
	{
		return filterRepository;
	}

	@Override
	public void teardown()
	{
		filterEntityFactory.reset();
		super.teardown();
	}

	@Test
	public void findTemplateByContextTest()
	{
		Assert.assertThat(filterRepository.findTemplateByContext(TemplateContext.TEMPLATE).size(), CoreMatchers.equalTo(0));

		createTemplateFilter(Collections.singleton(TemplateContext.TEMPLATE));
		Assert.assertThat(filterRepository.findTemplateByContext(TemplateContext.TEMPLATE).size(), CoreMatchers.equalTo(1));

		createTemplateFilter(Collections.singleton(TemplateContext.NEWS));
		Assert.assertThat(filterRepository.findTemplateByContext(TemplateContext.TEMPLATE).size(), CoreMatchers.equalTo(1));

		createTemplateFilter(new HashSet<>(Arrays.asList(TemplateContext.TEMPLATE, TemplateContext.NEWS)));
		Assert.assertThat(filterRepository.findTemplateByContext(TemplateContext.TEMPLATE).size(), CoreMatchers.equalTo(2));
	}

	private void createTemplateFilter(Set<TemplateContext> templateContexts)
	{
		final TemplateFilter templateFilter = filterEntityFactory.persistDefaultTemplateFilter(filterEntityFactory.persistDefaultMemberStatusFilter());
		templateFilter.setTemplateContexts(templateContexts);
		filterRepository.save(templateFilter);
	}
}
