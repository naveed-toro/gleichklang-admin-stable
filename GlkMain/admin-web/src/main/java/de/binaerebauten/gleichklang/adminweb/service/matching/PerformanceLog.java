package de.binaerebauten.gleichklang.adminweb.service.matching;

import de.binaerebauten.gleichklang.core.model.matching.AreaMatchStatisticEntry;
import de.binaerebauten.gleichklang.core.model.matching.AreaMatchStatisticEntry.MatchArea;
import de.binaerebauten.gleichklang.core.model.matching.MatchStatistic;
import de.binaerebauten.gleichklang.core.model.matching.MatchStatistic.MatchingScope;
import de.binaerebauten.gleichklang.core.model.matching.MatchStatisticEntry;
import de.binaerebauten.gleichklang.core.model.matching.MatchStatisticEntry.LogArea;
import de.binaerebauten.gleichklang.core.repository.matching.MatchStatisticRepository;
import de.binaerebauten.gleichklang.core.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;

public class PerformanceLog implements MatchingLog
{
	private static final Logger LOG = LoggerFactory.getLogger(PerformanceLog.class);

	private final MatchStatisticRepository matchStatisticRepository;
	private final MatchStatistic matchStatistic;
	
	public PerformanceLog(MatchStatisticRepository matchStatisticRepository, MatchingScope matchingScope)
	{
		this.matchStatisticRepository = matchStatisticRepository;
		
		matchStatistic = new MatchStatistic(matchingScope, LocalDateTime.now());
		matchStatisticRepository.save(matchStatistic);
	}
	
	private void print()
	{
		for (AreaMatchStatisticEntry areaMatchStatisticEntry : matchStatistic.getAreaMatchStatisticEntryMap().values())
		{
			print(areaMatchStatisticEntry.getNumber(), areaMatchStatisticEntry.getDuration(), areaMatchStatisticEntry.getLogMatchArea().toString());
		}

		LOG.info("OTHER:");

		for (MatchStatisticEntry matchStatisticEntry : matchStatistic.getMatchStatisticEntryMap().values())
		{
			print(matchStatisticEntry.getNumber(), matchStatisticEntry.getDuration(), matchStatisticEntry.getLogArea().toString());
		}
		
		final Duration duration = matchStatistic.getDuration();
		LOG.info("{}-Duration: {}", matchStatistic.getMatchingScope().toString(), StringUtils.durationToString(duration));
	}

	private void print(Long number, Duration duration, String label)
	{
		final long s = duration.getSeconds();
		final long average = (long) (((double) s / (double) number) * 100000);
		LOG.info("{} \t number: {} duration: {} average (per 100k): {}",
				label,
				number,
				StringUtils.durationToString(s),
				StringUtils.durationToString(average));
	}

	@Override
	public void add(MatchArea matchArea, Duration duration)
	{
		matchStatistic.addDuration(matchArea, duration);
	}

	@Override
	public void add(LogArea logArea, Duration duration)
	{
		matchStatistic.addDuration(logArea, duration);
	}
	
	public void finish()
	{
		matchStatistic.setEndDate(LocalDateTime.now());
		matchStatisticRepository.save(matchStatistic);
		
		print();
	}
}
