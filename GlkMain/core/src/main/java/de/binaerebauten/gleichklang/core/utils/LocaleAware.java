package de.binaerebauten.gleichklang.core.utils;

import com.vaadin.ui.UI;

import java.util.Locale;

/**
 * This interface encapsulates access to the user locale via a default method
 * delegating to vaadins locale support.
 */
public interface LocaleAware
{
	/**
	 * Returns the current users locale.
	 *
	 * @return the locale
	 */
	default Locale getLocale()
	{
		// note: Locale.Root returns an empty string!
		return UI.getCurrent() != null ? UI.getCurrent().getLocale() : Locale.ROOT;
	}
}
