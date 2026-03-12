package de.binaerebauten.gleichklang.core.model.matching;

import de.binaerebauten.gleichklang.core.model.BaseEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.Duration;

@Table(name = "match_statistic_entry")
@Entity
public class MatchStatisticEntry extends BaseEntity
{
	public enum LogArea
	{
		DELETE_CORRUPTED,
		SAVE_UPDATE,
		SAVE_INSERT,
		GET_USERS,
		CREATE_MISSING,
		DELETE_UNNECESSARY,
		DELETE_MATCH,
		SAVE_RELATIONSHIP,
		GET_MATCHES,
		SEND_MAILS,
		CACHE
	}
	
	@Column(name = "log_area")
	@Enumerated(EnumType.STRING)
	@NotNull
	private LogArea logArea;
	
	@NotNull
	private Duration duration = Duration.ZERO;
	
	@NotNull
	private long number = 0;
	
	@ManyToOne//(fetch = FetchType.LAZY)
	@JoinColumn(name = "match_statistic_id")
	@NotNull
	private MatchStatistic matchStatistic;
	
	public MatchStatisticEntry()
	{
	}
	
	public MatchStatisticEntry(LogArea logArea, MatchStatistic matchStatistic)
	{
		this.logArea = logArea;
		this.matchStatistic = matchStatistic;
	}
	
	public LogArea getLogArea()
	{
		return logArea;
	}
	
	public void setLogArea(LogArea logArea)
	{
		this.logArea = logArea;
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
