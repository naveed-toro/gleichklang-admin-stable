package de.binaerebauten.gleichklang.adminweb.service.matching;

import de.binaerebauten.gleichklang.adminweb.service.matching.MatchCacheService.MatchCache;
import de.binaerebauten.gleichklang.core.model.matching.AreaMatchStatisticEntry.MatchArea;
import de.binaerebauten.gleichklang.core.model.matching.Match;
import de.binaerebauten.gleichklang.core.model.matching.MatchStatisticEntry.LogArea;

import java.time.Duration;

public interface MatchingLog
{
	default void add(MatchArea matchArea, Duration between)
	{
	}
	
	default void add(LogArea logArea, Duration duration)
	{
	}
	
	default void requiredMissingAnswers(Match match, MatchCache matchCache)
	{
	}
	
	default void addDebugMessage(String msg)
	{
	}
}
