package de.binaerebauten.gleichklang.core.model.matching;

import de.binaerebauten.gleichklang.core.model.BaseEntity;
import de.binaerebauten.gleichklang.core.model.matching.AreaMatchStatisticEntry.MatchArea;
import de.binaerebauten.gleichklang.core.model.matching.MatchStatisticEntry.LogArea;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static java.time.Duration.between;

@Table(name = "match_statistic")
@Entity
public class MatchStatistic extends BaseEntity
{
	public enum MatchingScope
	{
		MATCHING,
		SUGGESTION
	}
	
	@Transient
	private final Map<LogArea, MatchStatisticEntry> matchStatisticEntryMap = new TreeMap<>();
	@Transient
	private final Map<MatchArea, AreaMatchStatisticEntry> areaMatchStatisticEntryMap = new TreeMap<>();
	
	@Column(name = "matching_scope")
	@Enumerated(EnumType.STRING)
	@NotNull
	private MatchingScope matchingScope;
	
	@Column(name = "start_date")
	@NotNull
	private LocalDateTime startDate;
	
	@Column(name = "end_date")
	private LocalDateTime endDate;
	
	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "matchStatistic")
	private Set<MatchStatisticEntry> matchStatisticEntries = new HashSet<>();
	
	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "matchStatistic")
	private Set<AreaMatchStatisticEntry> areaMatchStatisticEntries = new HashSet<>();
	
	public MatchStatistic()
	{
	}
	
	public MatchStatistic(MatchingScope matchingScope, LocalDateTime startDate)
	{
		this.matchingScope = matchingScope;
		this.startDate = startDate;
	}
	
	@PostLoad
	public void initTransients()
	{
		matchStatisticEntryMap.clear();
		if(matchStatisticEntries != null) matchStatisticEntries.forEach(e -> matchStatisticEntryMap.put(e.getLogArea(), e));
		
		areaMatchStatisticEntryMap.clear();
		if(areaMatchStatisticEntries != null) areaMatchStatisticEntries.forEach(e -> areaMatchStatisticEntryMap.put(e.getLogMatchArea(), e));
	}
	
	public MatchingScope getMatchingScope()
	{
		return matchingScope;
	}
	
	public void setMatchingScope(MatchingScope matchingScope)
	{
		this.matchingScope = matchingScope;
	}
	
	public LocalDateTime getStartDate()
	{
		return startDate;
	}
	
	public void setStartDate(LocalDateTime startDate)
	{
		this.startDate = startDate;
	}
	
	public LocalDateTime getEndDate()
	{
		return endDate;
	}
	
	public void setEndDate(LocalDateTime endDate)
	{
		this.endDate = endDate;
	}
	
	public Set<MatchStatisticEntry> getMatchStatisticEntries()
	{
		return matchStatisticEntries;
	}
	
	public void setMatchStatisticEntries(Set<MatchStatisticEntry> matchStatisticEntries)
	{
		this.matchStatisticEntries = matchStatisticEntries;
	}
	
	public Set<AreaMatchStatisticEntry> getAreaMatchStatisticEntries()
	{
		return areaMatchStatisticEntries;
	}
	
	public void setAreaMatchStatisticEntries(Set<AreaMatchStatisticEntry> areaMatchStatisticEntries)
	{
		this.areaMatchStatisticEntries = areaMatchStatisticEntries;
	}
	
	public Duration getDuration()
	{
		if (startDate == null || endDate == null) return null;
		
		return between(startDate, endDate);
	}
	
	public void addDuration(MatchArea matchArea, Duration duration)
	{
		AreaMatchStatisticEntry areaMatchStatisticEntry = areaMatchStatisticEntryMap.get(matchArea);
		if (areaMatchStatisticEntry == null)
		{
			areaMatchStatisticEntry = areaMatchStatisticEntries.stream().filter(e -> matchArea.equals(e.getLogMatchArea())).findFirst().orElse(null);
			if (areaMatchStatisticEntry == null)
			{
				areaMatchStatisticEntry = new AreaMatchStatisticEntry(matchArea, this);
				areaMatchStatisticEntries.add(areaMatchStatisticEntry);
			}
			areaMatchStatisticEntryMap.put(matchArea, areaMatchStatisticEntry);
		}
		areaMatchStatisticEntry.addDuration(duration);
	}
	
	public void addDuration(LogArea logArea, Duration duration)
	{
		MatchStatisticEntry matchStatisticEntry = matchStatisticEntryMap.get(logArea);
		if (matchStatisticEntry == null)
		{
			matchStatisticEntry = matchStatisticEntries.stream().filter(e -> logArea.equals(e.getLogArea())).findFirst().orElse(null);
			if (matchStatisticEntry == null)
			{
				matchStatisticEntry = new MatchStatisticEntry(logArea, this);
				matchStatisticEntries.add(matchStatisticEntry);
			}
			matchStatisticEntryMap.put(logArea, matchStatisticEntry);
		}
		matchStatisticEntry.addDuration(duration);
	}
	
	public Map<LogArea, MatchStatisticEntry> getMatchStatisticEntryMap()
	{
		return matchStatisticEntryMap;
	}
	
	public Map<MatchArea, AreaMatchStatisticEntry> getAreaMatchStatisticEntryMap()
	{
		return areaMatchStatisticEntryMap;
	}
}
