package de.binaerebauten.gleichklang.core.repository.matching;

import de.binaerebauten.gleichklang.core.model.matching.AreaMatchStatisticEntry;
import de.binaerebauten.gleichklang.core.model.matching.AreaMatchStatisticEntry.MatchArea;
import de.binaerebauten.gleichklang.core.model.matching.MatchStatistic;
import de.binaerebauten.gleichklang.core.model.matching.MatchStatisticEntry;
import de.binaerebauten.gleichklang.core.model.matching.MatchStatisticEntry.LogArea;
import de.binaerebauten.gleichklang.core.repository.AbstractRepositoryTest;
import de.binaerebauten.gleichklang.core.utils.DefaultEntityFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalTime;
import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class MatchStatisticRepositoryTest extends AbstractRepositoryTest<MatchStatistic>
{
	@Autowired
	private MatchStatisticRepository matchStatisticRepository;
	
	@Autowired
	private DefaultEntityFactory defaultEntityFactory;
	
	@Override
	protected Collection<MatchStatistic> getPersistedEntities()
	{
		final MatchStatistic matchStatistic = defaultEntityFactory.persistDefaultMatchStatistic();
		return Collections.singleton(matchStatistic);
	}
	
	@Test
	public void testConcreteGet()
	{
		final MatchStatistic matchStatistic = matchStatisticRepository.findAll().iterator().next();
		
		assertThat(matchStatistic, notNullValue());
		assertThat(matchStatistic.getAreaMatchStatisticEntries().size(), equalTo(1));
		assertThat(matchStatistic.getAreaMatchStatisticEntryMap().size(), equalTo(1));
		assertThat(matchStatistic.getMatchStatisticEntries().size(), equalTo(1));
		assertThat(matchStatistic.getMatchStatisticEntryMap().size(), equalTo(1));
		
		final AreaMatchStatisticEntry areaMatchStatisticEntry = matchStatistic.getAreaMatchStatisticEntries().iterator().next();
		final MatchStatisticEntry matchStatisticEntry = matchStatistic.getMatchStatisticEntries().iterator().next();
		
		assertThat(areaMatchStatisticEntry.getLogMatchArea(), equalTo(MatchArea.AFFINITY));
		assertThat(areaMatchStatisticEntry.getDuration().getSeconds(), equalTo((long) LocalTime.of(0, 0, 10).toSecondOfDay()));
		assertThat(matchStatistic.getAreaMatchStatisticEntryMap().get(MatchArea.AFFINITY), equalTo(areaMatchStatisticEntry));
		
		assertThat(matchStatisticEntry.getLogArea(), equalTo(LogArea.DELETE_MATCH));
		assertThat(matchStatisticEntry.getDuration().getSeconds(), equalTo((long) LocalTime.of(0, 0, 8).toSecondOfDay()));
		assertThat(matchStatistic.getMatchStatisticEntryMap().get(LogArea.DELETE_MATCH), equalTo(matchStatisticEntry));
	}
	
	@Override
	protected JpaRepository<MatchStatistic, Long> getRepository()
	{
		return matchStatisticRepository;
	}
}
