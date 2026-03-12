package de.binaerebauten.gleichklang.memberweb.initializer;

import com.google.common.base.Strings;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.UI;
import de.binaerebauten.gleichklang.core.initializer.AppUI;
import de.binaerebauten.gleichklang.core.model.I18NEntity.Language;
import de.binaerebauten.gleichklang.core.model.user.SignableUser;
import de.binaerebauten.gleichklang.core.model.user.SignableUser.UserType;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.navigation.ManualNavigator;
import de.binaerebauten.gleichklang.core.repository.JsIncludePostfixRepository;
import de.binaerebauten.gleichklang.core.service.UserService;
import de.binaerebauten.gleichklang.core.utils.ParametersHolder;
import de.binaerebauten.gleichklang.core.utils.ParametersHolder.ParameterKey;
import de.binaerebauten.gleichklang.memberweb.navigation.ManualNavigatorFactory;
import de.binaerebauten.gleichklang.memberweb.navigation.ManualNavigatorFactory.ManualNavigationItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import javax.servlet.http.Cookie;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.regex.Pattern;

@SuppressWarnings("serial")
public class MemberUI extends AppUI
{
	private static final Logger LOG = LoggerFactory.getLogger(MemberUI.class);

	private static final String PAGE_ROOT = "Gleichklang";
	private static final boolean productionMode;
	private JsIncludePostfixRepository jsIncludePostfixRepository;

	static
	{
		productionMode = VaadinService.getCurrent().getDeploymentConfiguration().isProductionMode();
	}

	private boolean enableDebugFeatures = false;

	public static boolean isEnableDebugFeatures()
	{
		if (productionMode)
		{
			return false;
		}
		else
		{
			final UI currentUI = UI.getCurrent();
			return currentUI instanceof MemberUI && ((MemberUI) currentUI).enableDebugFeatures;
		}
	}

	@Override
	protected String getTitle()
	{
		return PAGE_ROOT;
	}

	@Override
	protected void initView(VaadinRequest request)
	{
		readCookiesToSession();
		if(!rootContext.getEnvironment().getProperty("language.selection.activated", Boolean.class, false))
		{
			setLanguageToSession(Language.DE);
		}

		final ParametersHolder parameterMap = new ParametersHolder(request.getParameterMap());
		enableDebugFeatures = parameterMap.hasParameter(ParameterKey.DEBUG);

		final CssLayout mainContainer = new CssLayout();
		mainContainer.setWidth(100, Unit.PERCENTAGE);
		setContent(mainContainer);

		final ManualNavigatorFactory manualNavigatorFactory = new ManualNavigatorFactory(rootContext);
		final ManualNavigator manualNavigator = manualNavigatorFactory.buildNavigator(mainContainer);

		final User currentUser = getCurrentUser();
		if (currentUser == null)
		{
			if (parameterMap.hasParameter(ParameterKey.REGISTER))
			{
				manualNavigator.navigateTo(ManualNavigationItem.REGISTER, parameterMap);
			}
			else if (parameterMap.hasParameter(ParameterKey.FORGOT_PASSWORD))
			{
				manualNavigator.navigateTo(ManualNavigationItem.FORGOT_PASSWORD, parameterMap);
			}
			else
			{
				manualNavigator.navigateTo(ManualNavigationItem.LOGIN, parameterMap);
			}
		}
		else
		{
			manualNavigator.navigateTo(ManualNavigationItem.LOGIN, parameterMap);
		}

		// including custom JS file
		final String customJsPath = rootContext.getBean(Environment.class).getProperty("custom_js_file", String.class, "");
		if (!customJsPath.isEmpty())
		{
			jsIncludePostfixRepository = AppUI.getApplicationContext().getBean(JsIncludePostfixRepository.class);
            String js=null;
			if(jsIncludePostfixRepository.findLastRecord()!=null && jsIncludePostfixRepository.findLastRecord().getKeyName()!=null) {
                String keyName = jsIncludePostfixRepository.findLastRecord().getKeyName();
				//String[] jsPath=customJsPath.replaceAll(Pattern.quote(separator), "\\\\").split(".js");
				String[] jsPath = customJsPath.split("\\.js");
                js = jsPath[0]+keyName+".js"+jsPath[1];
            }
            else{
			    js=customJsPath;
            }
			final String customJsInjectionCode = "var scriptNode=document.createElement('script');scriptNode.src='" + js  + "';scriptNode.type='text/javascript';document.body.appendChild(scriptNode);";
			UI.getCurrent().getPage().getJavaScript().execute(customJsInjectionCode);
		}
	}

	private User getCurrentUser()
	{
		return rootContext.getBean(UserService.class).getCurrentUser();
	}

	private void readCookiesToSession()
	{
		final Cookie[] cookies = VaadinService.getCurrentRequest().getCookies();
		if (cookies != null && cookies.length > 0) {
			for (Cookie cookie : cookies)
			{
				if (Language.COOKIE_NAME.equals(cookie.getName()) && !Strings.isNullOrEmpty(cookie.getValue()))
				{
					try
					{
						final Language language = Language.valueOf(cookie.getValue());
						setLanguageToSession(language);

						return;
					}
					catch (Exception ex)
					{
						LOG.error("can't read language from cookie", ex);
					}
				}
			}
		}
	}

	private void setLanguageToSession(Language language)
	{
		final Locale locale = language.toLocale();

		UI.getCurrent().setLocale(locale); // Call to affect this current UI. Workaround for bug: http://dev.vaadin.com/ticket/12350
		VaadinSession.getCurrent().setLocale(locale); // Affects only future UI instances, not current one because of bug. See workaround in line above.
	}

	@Override
	protected UserType getUserType()
	{
		return SignableUser.UserType.MEMBER;
	}

}