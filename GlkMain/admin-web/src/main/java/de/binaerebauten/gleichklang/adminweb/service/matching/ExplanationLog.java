package de.binaerebauten.gleichklang.adminweb.service.matching;

import com.google.common.base.Strings;
import de.binaerebauten.gleichklang.adminweb.service.matching.MatchCacheService.MatchCache;
import de.binaerebauten.gleichklang.core.model.matching.Match;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

public class ExplanationLog implements MatchingLog
{
	private static final Logger LOG = LoggerFactory.getLogger(ExplanationLog.class);
	
	private final Collection<String> messageList = new ArrayList<>();
	
	private void print()
	{
		messageList.forEach(LOG::info);
	}
	
	@Override
	public void addDebugMessage(String message)
	{
		if (Strings.isNullOrEmpty(message)) return;
		
		messageList.add(message);
	}
	
	public void finish()
	{
		print();
	}
	
	@Override
	public void requiredMissingAnswers(Match match, MatchCache matchCache)
	{
		if (matchCache.getContent(match.getSourceUserId()).isRequiredAnswersMissing(match.getCategory()))
			addDebugMessage("userId " + match.getSourceUserId() + " required answers missing for category " + match.getCategory());
		
		if (matchCache.getContent(match.getTargetUserId()).isRequiredAnswersMissing(match.getCategory()))
			addDebugMessage("userId " + match.getTargetUserId() + " required answers missing for category " + match.getCategory());
	}
	
	public String getMessage()
	{
		return messageList.stream().collect(Collectors.joining("\n"));
	}
}
