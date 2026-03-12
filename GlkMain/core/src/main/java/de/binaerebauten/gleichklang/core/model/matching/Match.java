package de.binaerebauten.gleichklang.core.model.matching;

import de.binaerebauten.gleichklang.core.model.BaseEntity;
import de.binaerebauten.gleichklang.core.model.matching.MatrixValue.Strictness;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.model.user.User;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "match_")
public class Match extends BaseEntity
{
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "source_user_id", updatable = false, insertable = false)
	private User sourceUser;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "target_user_id", updatable = false, insertable = false)
	private User targetUser;
	
	@NotNull
	@Column(name = "source_user_id")
	private Long sourceUserId;

	@NotNull
	@Column(name = "target_user_id")
	private Long targetUserId;
	
	@Enumerated(EnumType.ORDINAL)
	private Strictness strictness;
	
	@Column(name = "count")
	private int number;
	
	@Enumerated(EnumType.STRING)
	private RecommendationCategory category;

	public Match(Long sourceUserId, Long targetUserId, RecommendationCategory recommendationCategory)
	{
		this.sourceUserId = sourceUserId < targetUserId ? sourceUserId : targetUserId;
		this.targetUserId = targetUserId > sourceUserId ? targetUserId : sourceUserId;
		setCategory(recommendationCategory);
	}

	public Match()
	{}
	
	public Match(User sourceUser, User targetUser, RecommendationCategory recommendationCategory)
	{
		this(sourceUser.getId(), targetUser.getId(), recommendationCategory);
		this.sourceUser = sourceUser.getId() < targetUser.getId() ? sourceUser : targetUser;
		this.targetUser = targetUser.getId() > sourceUser.getId() ? targetUser : sourceUser;
	}
	
	public Long getSourceUserId()
	{
		return sourceUserId;
	}

	public void setSourceUserId(Long sourceUserId)
	{
		this.sourceUserId = sourceUserId;
	}

	public Long getTargetUserId()
	{
		return targetUserId;
	}

	public void setTargetUserId(Long targetUserId)
	{
		this.targetUserId = targetUserId;
	}
	
	public User getSourceUser()
	{
		return sourceUser;
	}
	
	public void setSourceUser(User sourceUser)
	{
		this.sourceUser = sourceUser;
	}
	
	public User getTargetUser()
	{
		return targetUser;
	}
	
	public void setTargetUser(User targetUser)
	{
		this.targetUser = targetUser;
	}
	
	public Strictness getStrictness()
	{
		return strictness;
	}
	
	public void setStrictness(Strictness strictness)
	{
		this.strictness = strictness;
	}
	
	public int getNumber()
	{
		return number;
	}
	
	public void setNumber(int number)
	{
		this.number = number;
	}
	
	public RecommendationCategory getCategory()
	{
		return category;
	}
	
	public void setCategory(RecommendationCategory category)
	{
		this.category = category;
	}
}
