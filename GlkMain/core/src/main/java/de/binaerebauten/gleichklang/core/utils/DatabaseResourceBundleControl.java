package de.binaerebauten.gleichklang.core.utils;

import de.binaerebauten.gleichklang.core.initializer.AppUI;
import de.binaerebauten.gleichklang.core.model.I18NEntity;
import de.binaerebauten.gleichklang.core.model.I18NEntity.Language;
import de.binaerebauten.gleichklang.core.repository.I18NRepository;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This class implements {@link java.util.ResourceBundle.Control} so that
 * messages are retrieved from the database.
 */
public class DatabaseResourceBundleControl extends ResourceBundle.Control
{
	/**
	 * Time to live for this bundle cache.
	 * TODO Länge überdenken, evtl. hochsetzen
	 */
	private static final long CACHE_TTL_MS = Duration.ofMinutes(5L).toMillis();
	
	private static final String DB_RESOURCEBUNDLE_FORMAT = "database.rb";
	
	private static final Set<String> BASE_NAMES = EnumSet.allOf(I18NEntity.BaseName.class)
			.stream().map(I18NEntity.BaseName::name).collect(Collectors.toSet());

	/* only for testing */
	private final I18NRepository i18NRepository;

	public DatabaseResourceBundleControl()
	{
		this(null);
	}

	/**
	 * Used for testing only.
	 *
	 * @param i18NRepository
	 */
	public DatabaseResourceBundleControl(I18NRepository i18NRepository)
	{
		this.i18NRepository = i18NRepository;
	}

	private I18NRepository getI18NRepository()
	{
		return i18NRepository == null ? AppUI.getApplicationContext().getBean(I18NRepository.class) : i18NRepository;
	}

	@Override
	public long getTimeToLive(String baseName, Locale locale)
	{
		return CACHE_TTL_MS;
	}

	@Override
	public ResourceBundle newBundle(String baseNameStr, Locale locale, String format, ClassLoader loader, boolean reload)
			throws IllegalAccessException, InstantiationException, IOException
	{
		if (DB_RESOURCEBUNDLE_FORMAT.equals(format))
		{
			Language language = Language.valueOf(locale);
			if (language == null)
			{
				language = Language.DE;
			}
			
			if (!BASE_NAMES.contains(baseNameStr)) return null;
			
			final I18NEntity.BaseName baseName = I18NEntity.BaseName.valueOf(baseNameStr);
			final List<I18NEntity> i18NEntities = getI18NRepository().findByBaseNameAndLanguage(baseName, language);
			
			if (i18NEntities.isEmpty()) return null;
			
			final Object[][] contentsArray = new Object[i18NEntities.size()][];
			for (int i = 0; i < i18NEntities.size(); i++)
			{
				final I18NEntity i18NEntity = i18NEntities.get(i);
				contentsArray[i] = new Object[] { i18NEntity.getKey(), i18NEntity.getValue() };
			}
			
			return new DatabaseResourceBundle(contentsArray);
			
		}
		throw new IllegalArgumentException("Unsupported format:" + format);
	}

	@Override
	public boolean needsReload(String baseNameStr, Locale locale, String format, ClassLoader loader, ResourceBundle bundle, long loadTime)
	{
		if (DB_RESOURCEBUNDLE_FORMAT.equals(format))
		{
			boolean needsReload = false;

			Language language = Language.valueOf(locale);
			if (language == null)
			{
				language = Language.DE;
			}

			if (BASE_NAMES.contains(baseNameStr))
			{

				final I18NEntity.BaseName baseName = I18NEntity.BaseName.valueOf(baseNameStr);
				final LocalDateTime lastChangeDate = getI18NRepository().getLastChangeDate(baseName, language);
				final LocalDateTime loadTimeAsLocalDateTime = LocalDateTime.ofInstant(new Date(loadTime).toInstant(), ZoneId.systemDefault());

				needsReload = lastChangeDate != null && lastChangeDate.isAfter(loadTimeAsLocalDateTime);
			}

			return needsReload;
		}
		throw new IllegalArgumentException("Unsupported format:" + format);
	}

	/**
	 * Returns the default formats and adds the {@link #DB_RESOURCEBUNDLE_FORMAT} as first format.
	 * This means that this control first tries to retrieve the messages from the database and then
	 * from the default formats.
	 *
	 * @param baseName the basename
	 * @return the list of supported formats
	 */
	@Override
	public List<String> getFormats(String baseName)
	{
		return Collections.singletonList(DB_RESOURCEBUNDLE_FORMAT);
	}

	private static class DatabaseResourceBundle extends ListResourceBundle
	{
		private final Object[][] contentsArray;

		public DatabaseResourceBundle(Object[][] contentsArray)
		{
			this.contentsArray = contentsArray;
		}

		@Override
		protected Object[][] getContents()
		{
			return contentsArray;
		}
	}
}
