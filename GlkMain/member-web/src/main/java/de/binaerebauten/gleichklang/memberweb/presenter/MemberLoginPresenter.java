package de.binaerebauten.gleichklang.memberweb.presenter;

import com.vaadin.server.Page;
import com.vaadin.server.WebBrowser;
import com.vaadin.ui.UI;
import de.binaerebauten.gleichklang.core.model.I18NEntity.Language;
import de.binaerebauten.gleichklang.core.model.payment.AbstractPayment;
import de.binaerebauten.gleichklang.core.model.user.SignableUser.UserType;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.navigation.ManualNavigator;
import de.binaerebauten.gleichklang.core.presenter.LoginPresenter;
import de.binaerebauten.gleichklang.core.repository.PaymentRepository;
import de.binaerebauten.gleichklang.core.security.AuthenticationService;
import de.binaerebauten.gleichklang.core.security.AuthenticationService.RedirectGoal;
import de.binaerebauten.gleichklang.core.service.UserDataService;
import de.binaerebauten.gleichklang.core.service.UserService;
import de.binaerebauten.gleichklang.core.service.mail.EmailLinkService;
import de.binaerebauten.gleichklang.core.utils.AppUrlBuilder;
import de.binaerebauten.gleichklang.core.utils.ParametersHolder;
import de.binaerebauten.gleichklang.core.utils.ParametersHolder.ParameterKey;
import de.binaerebauten.gleichklang.core.utils.SecurityUtils;
import de.binaerebauten.gleichklang.core.view.I18N;
import de.binaerebauten.gleichklang.core.view.component.MessageBox;
import de.binaerebauten.gleichklang.memberweb.view.popup.AskForDeletePopup;
import de.binaerebauten.gleichklang.memberweb.view.popup.AskForDeletePopup.AskForDeletePopupListener;
import de.binaerebauten.gleichklang.memberweb.view.popup.PendingPaymentsPopup;
import de.binaerebauten.gleichklang.memberweb.navigation.ManualNavigatorFactory.ManualNavigationItem;
import de.binaerebauten.gleichklang.memberweb.service.LanguageService;
import de.binaerebauten.gleichklang.memberweb.view.MemberLoginView;
import de.binaerebauten.gleichklang.memberweb.view.MemberLoginView.MemberLoginViewListener;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class MemberLoginPresenter extends LoginPresenter<MemberLoginViewListener> implements MemberLoginViewListener, AskForDeletePopupListener
{
	private final ManualNavigator navigator;
	private final MemberLoginView view;
	private final UserService userService;
	private final Environment environment;
	private final PaymentRepository paymentRepository;
	private final UserDataService userDataService;
	private final AuthenticationService authenticationService;
	private final AppUrlBuilder appUrlBuilder;
	private final LanguageService languageService;
	
	public MemberLoginPresenter(ApplicationContext ctx, MemberLoginView loginView, ManualNavigator navigator)
	{
		super(ctx, loginView);
		
		this.navigator = Objects.requireNonNull(navigator, "navigator");
		this.view = Objects.requireNonNull(loginView, "loginView");
		
		userService = ctx.getBean(UserService.class);
		userDataService = ctx.getBean(UserDataService.class);
		environment = ctx.getBean(Environment.class);
		paymentRepository = ctx.getBean(PaymentRepository.class);
		authenticationService = ctx.getBean(AuthenticationService.class);
		appUrlBuilder = ctx.getBean(AppUrlBuilder.class);
		languageService = ctx.getBean(LanguageService.class);

		final Page currentPage = Page.getCurrent();
		final WebBrowser currentWebBrowser = currentPage.getWebBrowser();
		String version = currentWebBrowser.getBrowserApplication();
		if(currentWebBrowser.isIPhone()) {
			String ver = version.split("OS ")[1];
			String iosVersion = ver.split(" ")[0];
			int versionOne = 0;
			int versiontwo = 0;
			if (iosVersion.contains("_")) {
				versionOne = Integer.parseInt(iosVersion.split("_")[0]);
				versiontwo = Integer.parseInt(iosVersion.split("_")[1]);
			} else if (iosVersion.contains(".")) {
				versionOne = Integer.parseInt(iosVersion.split(".")[0]);
				versiontwo = Integer.parseInt(iosVersion.split(".")[1]);
			}
			if (versionOne <= 12 && versiontwo < 2) {
				MessageBox.show(I18N.SCROLL_ISSUE_ON_IPHONE.msg());
			}
		}
		this.view.setListener(this);
	}
	
	@Override
	public void enter(ParametersHolder parameterMap)
	{
		if (tryQuickLogin()) return;
		
		if (userService.getCurrentUser() != null)
		{
			onLoginSuccess();
			return;
		}
		
		this.view.setLogin(environment.getProperty("login.default.username", ""));
		this.view.setPassword(environment.getProperty("login.default.password", ""));
		
		final String message = getMessage(parameterMap);
		final String email = getEmail(parameterMap);
		
		if (email == null) return;
		
		view.setLogin(SecurityUtils.decodeString(email));
		view.setPassword(null);
		
		if (message != null)
		{
			view.setMessage(message);
			view.setLoginCaption(I18N.LOGINVIEW_ACTION_REGISTER.msg());
			view.setRegistrationVisible(false);
		}
	}
	
	private boolean tryQuickLogin()
	{
		final String fragment = Page.getCurrent().getUriFragment();
		if (fragment == null || !fragment.contains(EmailLinkService.QUICK_LOGIN_FRAGMENT))
			return false;
		
		Page.getCurrent().setUriFragment(null);
		
		final String[] fragmentSplit = fragment.split("\\?");
		if (fragmentSplit.length != 2) return false;
		
		final String[] parameterSplit = fragmentSplit[1].split("&");
		final Map<String, String> parameterMap = new HashMap<>();
		
		for (String parameter : parameterSplit)
		{
			final String[] keyValue = parameter.split("=");
			if (keyValue.length > 0)
			{
				final String key = keyValue[0];
				final String value = keyValue.length == 2 ? keyValue[1] : null;
				parameterMap.put(key, value);
			}
		}
		
		final String email = parameterMap.get(ParameterKey.EMAIL.getValue());
		final String password = parameterMap.get(ParameterKey.PASSWORD.getValue());
		
		if (email == null || password == null) return false;
		
		login(email, password, UserType.MEMBER);
		return true;
	}
	
	@Override
	public void startRegistration()
	{
		final ParametersHolder parametersHolder = new ParametersHolder();
		parametersHolder.addParameter(ParameterKey.REGISTER);
		Page.getCurrent().setLocation(appUrlBuilder.toAppPath(parametersHolder));
	}
	
	@Override
	public void forgotPassword()
	{
		navigator.navigateTo(ManualNavigationItem.FORGOT_PASSWORD);
	}
	
	@Override
	protected void onLoginSuccess()
	{
		final User user = userService.getCurrentUser();
		if (user == null) return;
		
		languageService.changeLanguage(user.getUserSettings().getLanguage());
		
		switch (user.getMemberStatus())
		{
			case REGISTRATION:
			case REGISTERED:
			case ADMIN_BLOCKED:
			case BLOCKED:
				navigator.navigateTo(ManualNavigationItem.MAIN);
				break;
			case CANCELED:
				onLoginAndCancelled(user);
				break;
			case ADMIN_CANCELED:
				onLoginAndCancelled(user);
				break;
			case DELETED:
				break;
			case ADMIN_DELETED:
				break;
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
	
	private void onLoginAndCancelled(User user)
	{
		final Set<AbstractPayment> pendingPayments = paymentRepository.findPendingPayments(user);
		if (!pendingPayments.isEmpty())
		{
			final PendingPaymentsPopup pendingPaymentsPopup = new PendingPaymentsPopup(pendingPayments);
			tryOpenPopup(pendingPaymentsPopup);
		}
		else
		{
			final AskForDeletePopup popup = new AskForDeletePopup(this, user);
			tryOpenPopup(popup);
		}
	}
	
	@Override
	public void removeUser(User user)
	{
		userDataService.deleteUserData(user);
		authenticationService.logout(RedirectGoal.PREREGISTER);
	}
	
	@Override
	public void reuseUser(User user)
	{
		userService.reuseUser(user);
		// Remove the suspended recommendation break of user
		// GR3-47
		userService.DeleteRecommendationBreak(user);
		onLoginSuccess();
	}
}
