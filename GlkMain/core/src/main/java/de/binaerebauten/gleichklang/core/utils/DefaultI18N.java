package de.binaerebauten.gleichklang.core.utils;

import de.binaerebauten.gleichklang.core.model.I18NEntity.Language;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * Defines the contract for localizable messages, this interface should be implemented by an enum that defines all
 * messages for a package.
 */
public interface DefaultI18N extends LocaleAware
{
	/**
	 * The UTF-8 resource bundle control singleton.
	 */
	ResourceBundle.Control UTF8_CONTROL = new UTF8Control();

	/**
	 * Property-name for vaadin to the {@link #getName()}-Method
	 */
	String NAME = "name";

	/**
	 * Returns the localized message with the given arguments.
	 *
	 * @param args the message arguments
	 * @return the localized message
	 */
	default String msg(Object... args)
	{
		ResourceBundle bundle = getBundle();
		String msg = bundle.getString(getKey());

		return MessageFormat.format(msg, args);
	}

	default String msgInLanguage(Language language, Object... args)
	{
		final ResourceBundle bundle = ResourceBundle.getBundle(getBaseName(), language.toLocale(), getControl());
		final String msg = bundle.getString(getKey());

		return MessageFormat.format(msg, args);
	}

	default ResourceBundle getBundle(String baseName)
	{
		Objects.requireNonNull(baseName, "baseName == null");
		ResourceBundle resourceBundle=null;

		if(getLocale() !=null && !getLocale().getDisplayName().equals("") && getControl()!=null) {
			try {
				resourceBundle = ResourceBundle.getBundle(baseName, getLocale(), getControl());
			}
			catch (Exception ex){
				ex.printStackTrace();
			}
		}
		else resourceBundle = ResourceBundle.getBundle(baseName, Locale.GERMAN, getControl());
		return resourceBundle;
	}

	default ResourceBundle getBundle()
	{
		String baseName = getBaseName();
		return getBundle(baseName);
	}

	default String getBaseName()
	{
		return this.getClass().getName();
	}

	/**
	 * Returns the control used for creating resource bundles.
	 * This defaults to {@link DatabaseResourceBundleControl} so that
	 * resource bundles can be loaded from the database.
	 *
	 * @return the control
	 */
	default ResourceBundle.Control getControl()
	{
		return UTF8_CONTROL;
	}

	/**
	 * Returns the key name of this message.
	 *
	 * @return the key name
	 */
	default String getKey()
	{
		return toString();
	}

	default String getName() { return msg(); }
}
