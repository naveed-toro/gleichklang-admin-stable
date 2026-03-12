package de.binaerebauten.gleichklang.core.model.user;

import de.binaerebauten.gleichklang.core.model.BaseEntity;
import de.binaerebauten.gleichklang.core.model.matching.AreaMatchStatisticEntry;
import de.binaerebauten.gleichklang.core.model.matching.AreaMatchStatisticEntry.MatchArea;
import de.binaerebauten.gleichklang.core.model.matching.MatchStatisticEntry;
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

@Table(name = "user_statistic")
@Entity
public class UserStatistic extends BaseEntity
{
	@Column(name = "start_date")
	@NotNull
	private LocalDateTime startDate;

	@Column(name = "end_date")
	private LocalDateTime endDate;

	@Column(name = "status")
	private String Status;

	public UserStatistic()
	{
	}

	public UserStatistic(LocalDateTime startDate)
	{
		this.startDate = startDate;
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

	public String getStatus() {
		return Status;
	}

	public void setStatus(String status) {
		Status = status;
	}

	public Duration getDuration()
	{
		if (startDate == null || endDate == null) return null;
		
		return between(startDate, endDate);
	}
	
	}
