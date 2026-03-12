package de.binaerebauten.gleichklang.core.model.matching;

import de.binaerebauten.gleichklang.core.model.BaseEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.Duration;

@Table(name = "area_match_statistic_entry")
@Entity
public class AreaMatchStatisticEntry extends BaseEntity
{
	/*
	 * Order of areas influences the performance of the matching algorithm.
	 *
	 * The best order can only determine in productive use. REGIONAL is slow, but the benefit of the greater count of exclusions weights here more.
	 */
	public enum MatchArea
	{
		REGIONAL,
		NUMBER,
		CHOICES,
		AFFINITY,
		AGE,
		AVATAR;
	}
	
	@Column(name = "log_match_area")
	@Enumerated(EnumType.STRING)
	@NotNull
	private MatchArea logMatchArea;
	
	@NotNull
	private Duration duration = Duration.ZERO;
	
	@NotNull
	private long number = 0;
	
	@ManyToOne//(fetch = FetchType.LAZY)
	@JoinColumn(name = "match_statistic_id")
	@NotNull
	private MatchStatistic matchStatistic;
	
	public AreaMatchStatisticEntry()
	{
	}
	
	public AreaMatchStatisticEntry(MatchArea logMatchArea, MatchStatistic matchStatistic)
	{
		this.logMatchArea = logMatchArea;
		this.matchStatistic = matchStatistic;
	}
	
	public MatchArea getLogMatchArea()
	{
		return logMatchArea;
	}
	
	public void setLogMatchArea(MatchArea logMatchArea)
	{
		this.logMatchArea = logMatchArea;
	}
	
	public Duration getDuration()
	{
		return duration;
	}
	
	public void setDuration(Duration duration)
	{
		this.duration = duration;
	}
	
	public void addDuration(Duration duration)
	{
		this.duration = this.duration.plus(duration);
		this.number++;
	}
	
	public long getNumber()
	{
		return number;
	}
	
	public void setNumber(long number)
	{
		this.number = number;
	}
	
	public MatchStatistic getMatchStatistic()
	{
		return matchStatistic;
	}
	
	public void setMatchStatistic(MatchStatistic matchStatistic)
	{
		this.matchStatistic = matchStatistic;
	}
}
