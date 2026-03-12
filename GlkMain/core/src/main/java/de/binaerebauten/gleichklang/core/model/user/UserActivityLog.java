package de.binaerebauten.gleichklang.core.model.user;

import de.binaerebauten.gleichklang.core.model.BaseEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "user_activity_log")
public class UserActivityLog extends BaseEntity
{
	public enum UserActivity
	{
		QUESTIONNAIRE_COMPLETED(true),
		LOGIN(false),
		LOGIN_FAILED(false),
		NEW_MATCH(true),
		REGISTERED(false),
		RENEWAL_CANCELLED(false),
		RENEWAL_REACTIVATED(false);

		private final boolean withCategory;

		UserActivity(boolean withCategory)
		{
			this.withCategory = withCategory;
		}

		public boolean isWithCategory()
		{
			return withCategory;
		}
	}

	@ManyToOne
	@NotNull
	private User user;

	@Column(name = "user_activity")
	@Enumerated(EnumType.STRING)
	private UserActivity userActivity;

	@Enumerated(EnumType.STRING)
	private RecommendationCategory category;

	public User getUser()
	{
		return user;
	}

	public void setUser(User user)
	{
		this.user = user;
	}

	public UserActivity getUserActivity()
	{
		return userActivity;
	}

	public void setUserActivity(UserActivity userActivity)
	{
		this.userActivity = userActivity;
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
