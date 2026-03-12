package de.binaerebauten.gleichklang.memberweb.service;

import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.UI;
import de.binaerebauten.gleichklang.core.model.I18NEntity.Language;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.service.UserService;
import de.binaerebauten.gleichklang.core.service.validator.UniqueValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import java.util.Locale;

@Service
public class LanguageService
{
	@Autowired
	private UserService userService;
	
	@Value("${language.selection.activated}")
	private boolean languageSelectionActivated;
	
	public boolean changeLanguage(Language language)
	{
		if (language == null || !languageSelectionActivated) return false;
		
		final User currentUser = userService.getCurrentUser();
		
		if (currentUser != null && !language.equals(currentUser.getUserSettings().getLanguage()))
		{
			currentUser.getUserSettings().setLanguage(language);
			try
			{
				userService.save(currentUser);
			}
			catch (UniqueValidationException ignore)
			{
			}
		}
		
		final Locale locale = language.toLocale();
		
		final Cookie languageCookie = new Cookie(Language.COOKIE_NAME, language.name());
		languageCookie.setMaxAge(365 * 24 * 60 * 60);
		languageCookie.setPath(VaadinService.getCurrentRequest().getContextPath());
		
		VaadinService.getCurrentResponse().addCookie(languageCookie);
		
		final Locale oldLocale = UI.getCurrent().getLocale();
		
		UI.getCurrent().setLocale(locale); // Call to affect this current UI. Workaround for bug: http://dev.vaadin.com/ticket/12350
		VaadinSession.getCurrent().setLocale(locale); // Affects only future UI instances, not current one because of bug. See workaround in line above.
		
		return !oldLocale.equals(locale);
	}
}
