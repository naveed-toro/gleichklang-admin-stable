package de.binaerebauten.gleichklang.core.model.user;

import de.binaerebauten.gleichklang.core.model.BaseEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Entity
@Table(name = "recommendation_break")
public class RecommendationBreak extends BaseEntity
{
	@ManyToOne
	@NotNull
	private User user;

	@Enumerated(EnumType.STRING)
	@NotNull
	private RecommendationCategory category;

	@Column(name = "end_date")
	private LocalDate endDate;

	public User getUser()
	{
		return user;
	}

	public void setUser(User user)
	{
		this.user = user;
	}

	public RecommendationCategory getCategory()
	{
		return category;
	}

	public void setCategory(RecommendationCategory category)
	{
		this.category = category;
	}

	public LocalDate getEndDate()
	{
		return endDate;
	}

	public void setEndDate(LocalDate endDate)
	{
		this.endDate = endDate;
	}
}
