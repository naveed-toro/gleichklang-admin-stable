package de.binaerebauten.gleichklang.core.utils;

import com.vaadin.ui.UI;
import de.binaerebauten.gleichklang.core.model.I18NEntity.BaseName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * TODO Diese Util-Klasse wird von der Relationship verwendet um das Gender zu bestimmen. Wenn es hierbei keine Probleme gibt, sollte diese Klasse der Deduplizierung und Konsistenz wegen auch von der LocalizedEntity verwendet werden.
 */
public class DatabaseResourceBundleUtil
{
	private static final Logger LOG = LoggerFactory.getLogger(DatabaseResourceBundleUtil.class);
	
	/**
	 * The UTF-8 resource bundle control singleton.
	 */
	private static final ResourceBundle.Control UTF8_CONTROL = new UTF8Control();
	private static final ResourceBundle.Control DATABASE_CONTROL = new DatabaseResourceBundleControl();
	
	private DatabaseResourceBundleUtil()
	{
	}
	
	/**
	 * Returns the localized message with the given arguments.
	 *
	 * @param args the message arguments
	 * @return the localized message
	 */
	public static String msg(String key, BaseName baseName, Object... args)
	{
		String msg = "!" + key;
		try
		{
			final ResourceBundle bundle = getDatabaseBundle(baseName.name());
			
			if (!hasKey(key, baseName))
			{
				LOG.warn("Translation for {} key {} wasn't found", baseName, key);
				
			}
			else
			{
				msg = bundle.getString(key);
				msg = MessageFormat.format(msg, args);
			}
			
		}
		catch (MissingResourceException ex)
		{
			LOG.error("Missing resource exception for key {} (Bundle doesn't exists): {}", key, ex.getLocalizedMessage
					());
		}
		catch (Exception ex)
		{
			LOG.error("Unexpected exception for translation ({})", key, ex);
		}
		return msg;
	}
	
	private static boolean hasKey(String key, BaseName baseName)
	{
		final ResourceBundle bundle = getDatabaseBundle(baseName.name());
		return bundle.containsKey(key);
	}
	
	private static ResourceBundle getDatabaseBundle(String baseName)
	{
		Objects.requireNonNull(baseName, "baseName == null");
		return ResourceBundle.getBundle(baseName, getLocale(), DATABASE_CONTROL);
	}
	
	public static Locale getLocale()
	{
		// note: Locale.Root returns an empty string!
		return UI.getCurrent() != null ? UI.getCurrent().getLocale() : Locale.ROOT;
	}
}
