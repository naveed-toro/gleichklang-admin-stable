package de.binaerebauten.gleichklang.memberweb.presenter;

import com.google.common.base.Strings;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.ui.UI;
import de.binaerebauten.gleichklang.core.model.I18NEntity.Language;
import de.binaerebauten.gleichklang.core.model.NaturalKeyEntity.NaturalKey;
import de.binaerebauten.gleichklang.core.model.payment.PaymentMethod;
import de.binaerebauten.gleichklang.core.model.questionnaire.Answer;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.model.user.UserPaymentSettings;
import de.binaerebauten.gleichklang.core.model.user.UserSettings;
import de.binaerebauten.gleichklang.core.navigation.ManualNavigator;
import de.binaerebauten.gleichklang.core.presenter.ManualNavigatePresenter;
import de.binaerebauten.gleichklang.core.repository.user.UserRepository;
import de.binaerebauten.gleichklang.core.service.AnswerService;
import de.binaerebauten.gleichklang.core.service.LocatableService;
import de.binaerebauten.gleichklang.core.service.UserService;
import de.binaerebauten.gleichklang.core.service.validator.UniqueValidationException;
import de.binaerebauten.gleichklang.core.utils.ParametersHolder;
import de.binaerebauten.gleichklang.core.utils.ParametersHolder.ParameterKey;
import de.binaerebauten.gleichklang.core.view.component.MessageBox;
import de.binaerebauten.gleichklang.memberweb.navigation.ManualNavigatorFactory.ManualNavigationItem;
import de.binaerebauten.gleichklang.memberweb.service.LanguageService;
import de.binaerebauten.gleichklang.memberweb.view.PreregistrationView;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.Objects;

public class PreregistrationPresenter extends ManualNavigatePresenter implements PreregistrationView.PreregistrationViewListener
{
	private final UserRepository userRepository;
	private final PreregistrationView view;
	private final AnswerService answerService;
	private final UserService userService;
	private final LocatableService locatableService;
	private final ManualNavigator navigator;
	private final LanguageService languageService;
	
	private final String serverBaseUrl;
	
	public PreregistrationPresenter(ApplicationContext ctx, PreregistrationView preregistrationView, ManualNavigator navigator)
	{
		super(preregistrationView);
		
		this.view = preregistrationView;
		this.navigator = Objects.requireNonNull(navigator);
		
		this.userRepository = ctx.getBean(UserRepository.class);
		
		this.answerService = ctx.getBean(AnswerService.class);
		this.userService = ctx.getBean(UserService.class);
		this.locatableService = ctx.getBean(LocatableService.class);
		this.languageService = ctx.getBean(LanguageService.class);
		
		final Environment environment = ctx.getEnvironment();
		
		this.serverBaseUrl = environment.getProperty("server.base_url", "");
		
		this.view.setListener(this);
	}
	
	@Override
	public void enter(ParametersHolder parameterMap)
	{
		User user = new User();
		
		Answer sexAnswer = answerService.getAnswers(user, NaturalKey.SEX, true).iterator().next();
		
		UserPaymentSettings userPaymentSettings = new UserPaymentSettings();
		userPaymentSettings.setUser(user);
		
		// Set the first allowed payment method pre-selected
		Arrays.stream(PaymentMethod.values())
				.findFirst()
				.ifPresent(userPaymentSettings::setPaymentMethod);
		
		UserSettings userSettings = new UserSettings();
		user.setUserSettings(userSettings);
		userSettings.setUser(user);
		final Language language = Language.valueOf(UI.getCurrent().getLocale());
		userSettings.setLanguage(language != null ? language : Language.DE);
		
		view.startRegistration(user, userPaymentSettings, sexAnswer, locatableService);
	}
	
	@Override
	public void preregister(User user, boolean sendConfirmationEmail, UserPaymentSettings userPaymentSettings, Answer sexAnswer) throws UniqueValidationException
	{
		try
		{
			user = userService.preregister(user, sendConfirmationEmail, userPaymentSettings, sexAnswer);
		}
		catch (UniqueValidationException ex)
		{
			MessageBox.show(I18N.PREREGISTRATION_VALIDATION_EMAILALREADYUSED.msg(serverBaseUrl));
			return;
		}
		
		final ParametersHolder parameters = new ParametersHolder();
		parameters.addParameter(ParameterKey.EMAIL, user.getEmail());
		callPreregistrationSucceed(parameters);
	}
	
	@Override
	public void callPreregistrationSucceed(ParametersHolder parameters)
	{
		parameters.addParameter(ParameterKey.MESSAGE, de.binaerebauten.gleichklang.core.view.I18N.REGISTRATIONVIEW_REGISTRATION_SUCCESS.msg());
		
		navigator.navigateTo(ManualNavigationItem.LOGIN, parameters);
	}
	
	@Override
	public void validateAlias(String alias) throws InvalidValueException
	{
		if (Strings.isNullOrEmpty(alias)) return;
		
		if (this.userRepository.findByAlias(alias) != null)
		{
			throw new InvalidValueException(de.binaerebauten.gleichklang.memberweb.view.I18N.USERDATAVIEW_VALIDATION_ALIASALREADYUSED.msg());
		}
		if(invalidAlias(alias)){
			throw new InvalidValueException(de.binaerebauten.gleichklang.memberweb.view.I18N.INVALID_ALIAS.msg());
		}
	}
	
	@Override
	public void changeLanguage(Language language)
	{
		if (languageService.changeLanguage(language))
		{
			UI.getCurrent().getPage().reload();
		}
	}

	public static boolean invalidAlias(String alias){
		if(alias.contains("@") || alias.contains("google") || alias.contains("yahoo") || alias.contains("gmail") || alias.contains("hotmail") ||
				alias.contains("gmx") || alias.contains("outlook") || alias.contains("(") || alias.contains(")") || alias.contains("{") || alias.contains("}"
		) || alias.contains("[") || alias.contains("]") ||  alias.contains(" at ") ||alias.contains("-at-") || alias.contains("+at*")
				|| countSpaces(alias)>2 || coutSpecialChar(alias)>2 || alias.endsWith(".com") || alias.endsWith(".de") || alias.endsWith(".net") ||
				alias.endsWith(" com") || alias.endsWith(" de") || alias.endsWith(" net") || alias.endsWith("-com") || alias.endsWith("-de") ||
				alias.endsWith("-net") || alias.endsWith("*de") || alias.endsWith("*com") || alias.endsWith("*net"))
		{
			return true;
		}
		return false;
	}

	 static long countSpaces(String alias){
		long spaceCount = alias.chars().filter(c -> c == (int)' ').count();
		return spaceCount;
	}

	public static int coutSpecialChar(String alias){
		 final String FINAL_CHAR_REGEX = "[!@#$%^&*()[\\\\]|;',./{}\\\\\\\\:\\\"<>?]";
		int specialCharCount = alias.split(FINAL_CHAR_REGEX, -1).length - 1;
		return specialCharCount;
   }

}
