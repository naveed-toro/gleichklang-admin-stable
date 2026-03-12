package de.binaerebauten.gleichklang.core.model.user;

import de.binaerebauten.gleichklang.core.model.BaseEntity;
import de.binaerebauten.gleichklang.core.model.questionnaire.Questionnaire;

import javax.persistence.*;

@Entity
@Table(name = "user_registration_state")
public class UserRegistrationState extends BaseEntity
{

	@Enumerated(EnumType.STRING)
	@Column(name = "registration_state")
	private RegistrationState registrationState;

	@Enumerated(EnumType.STRING)
	@Column(name = "recommendation_category")
	private RecommendationCategory recommendationCategory;

	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "user_id")
	private User user;

	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "questionnaire_id")
	private Questionnaire questionnaire;

	public RegistrationState getRegistrationState()
	{
		return registrationState;
	}

	public void setRegistrationState(RegistrationState registrationState)
	{
		this.registrationState = registrationState;
	}

	public RecommendationCategory getRecommendationCategory()
	{
		return recommendationCategory;
	}

	public void setRecommendationCategory(RecommendationCategory recommendationCategory)
	{
		this.recommendationCategory = recommendationCategory;
	}

	public User getUser()
	{
		return user;
	}

	public void setUser(User user)
	{
		this.user = user;
	}

	public Questionnaire getQuestionnaire()
	{
		return questionnaire;
	}

	public void setQuestionnaire(Questionnaire questionnaire)
	{
		this.questionnaire = questionnaire;
	}
}
