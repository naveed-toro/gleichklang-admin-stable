package de.binaerebauten.gleichklang.core.service;

import de.binaerebauten.gleichklang.core.model.filter.AbstractFilter;
import de.binaerebauten.gleichklang.core.model.filter.AliasFilter;
import de.binaerebauten.gleichklang.core.model.filter.BinaryOperatorFilter;
import de.binaerebauten.gleichklang.core.model.filter.TemplateFilter;
import de.binaerebauten.gleichklang.core.repository.BasePersistenceTest;
import de.binaerebauten.gleichklang.core.repository.FilterRepository;
import de.binaerebauten.gleichklang.core.repository.TemplateFilterRepository;
import de.binaerebauten.gleichklang.core.utils.DefaultEntityFactory;
import de.binaerebauten.gleichklang.core.utils.FilterEntityFactory;
import de.binaerebauten.gleichklang.core.utils.filter.FilterBuilder;
import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class FilterServiceTest extends BasePersistenceTest
{
	@Autowired
	private FilterService filterService;

	@Autowired
	private DefaultEntityFactory defaultEntityFactory;

	@Autowired
	private FilterEntityFactory filterEntityFactory;

	@Autowired
	private TemplateFilterRepository templateFilterRepository;

	@Autowired
	private FilterRepository filterRepository;

	final int expectedOrs = 5;
	final int expectedAnds = 4;

	@After
	public void tearDown() throws Exception
	{
		defaultEntityFactory.reset();
		filterEntityFactory.reset();
	}

	@Test
	public void getFilterListTest()
	{
		final List<AbstractFilter> expectedFilters = createTestFilters();
		final TemplateFilter templateFilter = createTestTemplateFilter(expectedFilters);

		final List<AbstractFilter> actualList = filterService.getFilterList(templateFilter);
		int actualOrs = 0;
		int actualAnds = 0;

		final Iterator<AbstractFilter> expectedIterator = expectedFilters.iterator();
		for (AbstractFilter actualFilter : actualList)
		{
			if (actualFilter instanceof BinaryOperatorFilter)
			{
				final BinaryOperatorFilter binaryOperatorFilter = (BinaryOperatorFilter) actualFilter;
				switch (binaryOperatorFilter.getBinaryOperator())
				{
					case INTERSECTION:
						actualAnds++;
						break;
					case UNION:
						actualOrs++;
						break;
				}
			}
			else
			{
				final AbstractFilter expectedFilter = expectedIterator.next();
				assertThat(actualFilter, equalTo(expectedFilter));
			}
		}

		assertThat(actualAnds, equalTo(expectedAnds));
		assertThat(actualOrs, equalTo(expectedOrs));
	}

	@Test
	public void testNewTemplate()
	{
		final List<AbstractFilter> expectedFilters = createTestFilters();
		final TemplateFilter templateFilter = createTestTemplateFilter(expectedFilters);
		final long oneTemplateFilter = 1L;

		assertThat(templateFilterRepository.count(), equalTo(0L));
		assertThat(filterRepository.count(), equalTo(0L));
		filterService.saveFilter(templateFilter, Collections.emptyList());
		assertThat(templateFilterRepository.count(), equalTo(oneTemplateFilter));
		assertThat(filterRepository.count(), equalTo(expectedOrs + expectedAnds + expectedFilters.size() + oneTemplateFilter));

		final TemplateFilter actualTemplateFilter = templateFilterRepository.findAll().iterator().next();
		assertThat(actualTemplateFilter, equalTo(templateFilter));
	}

	@Test
	public void testEditTemplate()
	{
		final List<AbstractFilter> expectedFilters = createTestFilters();
		final List<AbstractFilter> newFilters = createTestFilters();
		final TemplateFilter templateFilter = createTestTemplateFilter(expectedFilters);
		final long oneTemplateFilter = 1L;
		final long originalSize = expectedOrs + expectedAnds + expectedFilters.size() + oneTemplateFilter;

		filterService.saveFilter(templateFilter, Collections.emptyList());
		assertThat(filterRepository.count(), equalTo(originalSize));

		assertRemoving(templateFilter, originalSize - 2L, expectedFilters.get(5));
		assertRemoving(templateFilter, originalSize - 2L - 4L, expectedFilters.get(1), expectedFilters.get(2));
		assertRemoving(templateFilter, originalSize - 2L - 4L - 4L, expectedFilters.get(3), expectedFilters.get(4));
		assertRemoving(templateFilter, originalSize - 2L - 4L - 4L - 2L, expectedFilters.get(0));

		assertAdd(templateFilter, originalSize - 2L - 4L - 4L, newFilters.get(0));
		assertAdd(templateFilter, originalSize - 2L, newFilters.get(1), newFilters.get(2), newFilters.get(3), newFilters.get(4));
		assertAdd(templateFilter, originalSize, newFilters.get(5));
	}

	private void assertRemoving(TemplateFilter templateFilter, long expectedSize, AbstractFilter... removeFilters)
	{
		final FilterBuilder filterBuilder = new FilterBuilder(templateFilter.getFilter());

		final List<AbstractFilter> oldFilters = filterService.getFilterList(templateFilter);

		for(AbstractFilter removeFilter : removeFilters)
		{
			filterBuilder.removeFilter(removeFilter);
		}
		templateFilter.setFilter(filterBuilder.getFilter());

		filterService.saveFilter(templateFilter, oldFilters);
		assertThat(filterRepository.count(), equalTo(expectedSize));

		final List<AbstractFilter> filters = filterRepository.findAll();
		assertThat(filters, hasItem(templateFilter));
		for(AbstractFilter removeFilter : removeFilters)
		{
			assertThat(filters, not(hasItem(removeFilter)));
		}
	}

	private void assertAdd(TemplateFilter templateFilter, long expectedSize, AbstractFilter... addFilters)
	{
		final FilterBuilder filterBuilder = new FilterBuilder(templateFilter.getFilter());

		final List<AbstractFilter> oldFilters = filterService.getFilterList(templateFilter);

		filterBuilder.addFilter(addFilters);
		templateFilter.setFilter(filterBuilder.getFilter());

		filterService.saveFilter(templateFilter, oldFilters);
		assertThat(filterRepository.count(), equalTo(expectedSize));

		final List<AbstractFilter> filters = filterRepository.findAll();
		assertThat(filters, hasItem(templateFilter));
		for(AbstractFilter addFilter : addFilters)
		{
			assertThat(filters, hasItem(addFilter));
		}
	}

	private TemplateFilter createTestTemplateFilter(List<AbstractFilter> filters)
	{
		final FilterBuilder filterBuilder = new FilterBuilder();

		filterBuilder.addFilter(filters.get(0));
		filterBuilder.addFilter(filters.get(1), filters.get(2), filters.get(3), filters.get(4));
		filterBuilder.addFilter(filters.get(5));
		filterBuilder.addFilter(filters.get(6), filters.get(7));
		filterBuilder.addFilter(filters.get(8), filters.get(9));

		final TemplateFilter templateFilter = new TemplateFilter();
		templateFilter.setFilter(filterBuilder.getFilter());
		templateFilter.setName("template");
		templateFilter.setDeleted(false);
		return templateFilter;
	}

	private List<AbstractFilter> createTestFilters()
	{
		final List<AbstractFilter> expectedFilters = new ArrayList<>();
		for (int i = 0; i < 10; i++)
		{
			final AliasFilter aliasFilter = new AliasFilter();
			aliasFilter.setValue("test" + i);
			expectedFilters.add(aliasFilter);
		}
		return expectedFilters;
	}
}