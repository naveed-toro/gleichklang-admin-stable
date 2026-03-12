package de.binaerebauten.gleichklang.core.service;

import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.model.user.UserActivityLog.UserActivity;
import de.binaerebauten.gleichklang.core.repository.user.UserActivityLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class UserActivityService
{
	@Autowired
	private UserActivityLogRepository userActivityLogRepository;

	public void createActivity(User user, UserActivity userActivity)
	{
		createActivity(user, userActivity, null);
	}
	
	public void createActivity(long userId, UserActivity userActivity, RecommendationCategory category)
	{
		Objects.requireNonNull(userActivity);
		
		if(category == null && userActivity.isWithCategory()) throw new IllegalArgumentException("category must not be null");
		if(category != null && !userActivity.isWithCategory()) throw new IllegalArgumentException("category must be null");
		
		userActivityLogRepository.insertUserActivityLog(userId, userActivity.name(), category != null ? category.name() : null);
	}

	public void createActivity(User user, UserActivity userActivity, RecommendationCategory category)
	{
		Objects.requireNonNull(user);
		
		createActivity(user.getId(), userActivity, category);
	}
}
