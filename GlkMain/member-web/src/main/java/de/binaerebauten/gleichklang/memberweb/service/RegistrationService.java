package de.binaerebauten.gleichklang.memberweb.service;

import de.binaerebauten.gleichklang.core.model.questionnaire.Questionnaire;
import de.binaerebauten.gleichklang.core.model.user.*;
import de.binaerebauten.gleichklang.core.repository.user.UserRegistrationStateRepository;
import de.binaerebauten.gleichklang.core.service.UserActivityService;
import de.binaerebauten.gleichklang.memberweb.view.component.RegistrationWizard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Objects;

/**
 * This service handles registration process. e.g. step change routines,
 * registration states etc.
 */
@Service
public class RegistrationService
{
	public static final int MASTER_STEP_COUNT = 1;
	public static final int PAYMENT_STEP_COUNT = 1;

	@Autowired
	private UserRegistrationStateRepository userRegistrationStateRepository;

	@Autowired
	private UserActivityService userActivityService;

	/**
	 * Returns the name of the current step for the current user
	 *
	 * @param currentUser
	 * @return
	 */
	public String getCurrentStepId(User currentUser)
	{
		String currentStep = RegistrationWizard.FIRST_STEP;
		UserRegistrationState userRegistrationState = userRegistrationStateRepository.findByUser(currentUser);
		if (userRegistrationState != null)
		{

			switch (userRegistrationState.getRegistrationState())
			{
				case CATEGORY:
				case PERSONAL:
					currentStep = getNextStepForCategory(userRegistrationState, currentUser);
					break;
				case MEMBERADDRESS:
					currentStep = RegistrationWizard.MASTER_STEP;
					break;
				case CONFIGNOTIFICATION:
					currentStep = RegistrationWizard.CONFIG_NOTIFICATIONS_STEP;
					break;
				case PAYMENT:
					currentStep = RegistrationWizard.REGISTRATION_PAYMENT_STEP;
					break;
					
			}
		}

		return currentStep;
	}

	private String getNextStepForCategory(UserRegistrationState userRegistrationState, User currentUser)
	{
		RecommendationCategory recommendationCategory = userRegistrationState.getRecommendationCategory();
		Questionnaire questionnaire = userRegistrationState.getQuestionnaire();
		if(questionnaire != null){
			return questionnaire.getName();
		}
		else if (RegistrationState.PERSONAL.equals(userRegistrationState.getRegistrationState()))
		{
			return RegistrationWizard.PERSONAL_STEP + RegistrationWizard.PRE_TEMPLATE_SUFFIX;
		}
		else if(RegistrationState.CATEGORY.equals(userRegistrationState.getRegistrationState())){
			if (isFirstRecommendationCategory(currentUser, recommendationCategory))
			{
				return RegistrationWizard.FIRST_STEP;
			}
			else
			{
				return recommendationCategory.name().toLowerCase() + RegistrationWizard.PRE_TEMPLATE_SUFFIX;
			}
		}
		return RegistrationWizard.FIRST_STEP;
	}

	private boolean isFirstRecommendationCategory(User currentUser, RecommendationCategory recommendationCategory)
	{
		// recommendation categories ordered by enum order
		return currentUser.getOrderedCategories().iterator().next().equals(recommendationCategory);
	}
	
	public void updateQuestionnaireRegistrationState(User currentUser, Questionnaire questionnaire)
	{
		UserRegistrationState registrationState = this.userRegistrationStateRepository.findByUser(currentUser);
		if (Objects.isNull(registrationState))
		{
			registrationState = new UserRegistrationState();
			registrationState.setUser(currentUser);
		}

		RecommendationCategory category = questionnaire.getRecommendationCategory();
		if (category == null)
		{
			registrationState.setRegistrationState(RegistrationState.PERSONAL);
		}
		else
		{
			registrationState.setRegistrationState(RegistrationState.CATEGORY);
			registrationState.setRecommendationCategory(category);
		}
		registrationState.setQuestionnaire(questionnaire);
		if (!currentUser.getMemberStatus().equals(MemberStatus.REGISTERED))
			userRegistrationStateRepository.save(registrationState);
	}
	
	@Transactional
	public void updateRegistrationState(User currentUser, RegistrationState registrationState)
	{
		UserRegistrationState userRegistrationState = userRegistrationStateRepository.findByUser(currentUser);
		
		if (Objects.isNull(userRegistrationState))
		{
			userRegistrationState = new UserRegistrationState();
			userRegistrationState.setUser(currentUser);
		}
		
		userRegistrationState.setRegistrationState(registrationState);
		if (!currentUser.getMemberStatus().equals(MemberStatus.REGISTERED))
			userRegistrationStateRepository.save(userRegistrationState);
	}
}
