package de.binaerebauten.gleichklang.memberweb.view;

import com.vaadin.data.Validator.InvalidValueException;
import de.binaerebauten.gleichklang.core.model.I18NEntity.Language;
import de.binaerebauten.gleichklang.core.model.questionnaire.Answer;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.model.user.UserPaymentSettings;
import de.binaerebauten.gleichklang.core.service.LocatableHandler;
import de.binaerebauten.gleichklang.core.service.validator.UniqueValidationException;
import de.binaerebauten.gleichklang.core.utils.ParametersHolder;
import de.binaerebauten.gleichklang.core.view.NavigateView;
import de.binaerebauten.gleichklang.memberweb.view.PreregistrationView.PreregistrationViewListener;

public interface PreregistrationView extends NavigateView<PreregistrationViewListener>
{
	void startRegistration(User user, UserPaymentSettings userPaymentSettings, Answer sexAnswer, LocatableHandler locatableHandler);

	interface PreregistrationViewListener extends NavigateView.NavigateViewListener
	{
		/**
		 * @see de.binaerebauten.gleichklang.core.service.UserService#preregister(User, boolean, UserPaymentSettings, Answer)
		 *
		 * @param user
		 * @param sendConfirmationEmail
		 * @param userPaymentSettings
		 * @param sexAnswer
		 */
		void preregister(User user, boolean sendConfirmationEmail, UserPaymentSettings userPaymentSettings, Answer sexAnswer) throws UniqueValidationException;

		void callPreregistrationSucceed(ParametersHolder parameters);

		void validateAlias(String alias) throws InvalidValueException;
		
		void changeLanguage(Language language);
	}
}
