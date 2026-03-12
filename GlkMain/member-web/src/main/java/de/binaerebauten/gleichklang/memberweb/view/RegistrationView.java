package de.binaerebauten.gleichklang.memberweb.view;

import de.binaerebauten.gleichklang.core.model.questionnaire.Questionnaire;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.presenter.question.QuestionnairePresenter;
import de.binaerebauten.gleichklang.core.view.NavigateView;
import de.binaerebauten.gleichklang.core.view.QuestionnaireView;
import de.binaerebauten.gleichklang.memberweb.view.RegistrationReportingView.RegistrationReportingListener;
import de.binaerebauten.gleichklang.memberweb.view.component.RegistrationWizard;

import java.util.LinkedHashMap;
import java.util.Map;

public interface RegistrationView extends NavigateView<RegistrationView.RegistrationViewListener>
{
	interface RegistrationViewListener extends NavigateView.NavigateViewListener
	{
		void quickRegisterUser();
		
		void quickFill();
	}
	
	void setRegistrationStepViews(LinkedHashMap<Questionnaire, QuestionnairePresenter> questionnairePresenterMap, Map<RecommendationCategory, RegistrationReportingListener> reportingStepViewMap);
	
	/**
	 * Sets the subscription offer payment view. Must be called
	 * in the order of the registration steps.
	 *
	 * @param subscriptionOfferPaymentView
	 */
	void setSubscriptionOfferView(SubscriptionOfferPaymentView subscriptionOfferPaymentView);
	
	/**
	 * sets the external payment form view. Must be called
	 * in the order of the registration steps.
	 *
	 * @param externalPaymentFormView
	 */
	void setExternalPaymentFormView(ExternalPaymentFormView externalPaymentFormView);
	
	/**
	 * Sets the external payment result view. Must be called
	 * in the order of the registration steps.
	 *
	 * @param paymentResultView
	 */
	void setPaymentResultView(PaymentResultView paymentResultView);
	
	void setLastQuestionSteps(Map<Questionnaire, QuestionnaireView> questionnaireViewMap);
	
	void setPersonalDataStep(PersonalDataView personalDataView);
	
	void setPreConfigNotificationsView(PreConfigNotificationsView preConfigNotificationsView);
	
	void setFinalStep();
	
	void enableNextQuestionnaire(boolean show);
	
	RegistrationWizard getRegistrationWizard();
}
