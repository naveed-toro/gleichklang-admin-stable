package de.binaerebauten.gleichklang.core.launcher;

import de.binaerebauten.gleichklang.core.model.I18NEntity.Language;
import de.binaerebauten.gleichklang.core.utils.DefaultI18N;
import de.binaerebauten.gleichklang.core.utils.UTF8Control;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;

import java.io.IOException;
import java.util.*;

public class CheckI18N
{
	public static void main(String[] args) throws IOException
	{
		final ArrayList<Class<?>> i18nClasses = getI18NClasses();
		final Map<Language, Long> countMissingBundles = new HashMap<>();
		
		for (Class<?> i18nClass : i18nClasses)
		{
			if (!i18nClass.isEnum()) continue;
			
			final Collection<DefaultI18N> enumValues = new HashSet<>();
			
			for (Object enumValue : i18nClass.getEnumConstants())
			{
				final DefaultI18N i18nEnum = (DefaultI18N) enumValue;
				enumValues.add(i18nEnum);
			}
			
			final String baseName = enumValues.iterator().next().getBaseName();
			final ResourceBundle rootResourceBundle = ResourceBundle.getBundle(baseName, Locale.ROOT, new UTF8Control());
			
			for (Language language : Language.values())
			{
				final ResourceBundle resourceBundle = ResourceBundle.getBundle(baseName, language.toLocale(), new UTF8Control());
				if (!resourceBundle.getLocale().equals(language.toLocale()))
				{
					if (language != Language.DE || !Locale.ROOT.equals(resourceBundle.getLocale()))
					{
						final Long countMissing = countMissingBundles.getOrDefault(language, 0L);
						countMissingBundles.put(language, countMissing + 1);
						continue;
					}
				}
				
				final Collection<String> bundleKeys = Collections.list(resourceBundle.getKeys());
				
				for (String bundleKey : bundleKeys)
				{
					if (enumValues.stream().map(DefaultI18N::getKey).noneMatch(bundleKey::equals))
					{
						System.out.println("Bundle-Key " + bundleKey + " is unused");
					}
				}
				
				for (DefaultI18N enumValue : enumValues)
				{
					if (!resourceBundle.containsKey(enumValue.getKey()))
					{
						System.out.println("i18n key " + enumValue.getKey() + " is missing in Bundle");
						continue;
					}
					
					final String value = resourceBundle.getString(enumValue.getKey());
					if(!resourceBundle.getLocale().equals(Locale.ROOT) && value.equals(rootResourceBundle.getString(enumValue.getKey())))
					{
						System.out.println("Key " + enumValue.getKey() + " in " + resourceBundle.getBaseBundleName() + " are identical with parent: " + value);
					}
				}
			}
		}
		
		for (Map.Entry<Language, Long> countMissingBundle : countMissingBundles.entrySet())
		{
			System.out.println("Bundle for " + countMissingBundle.getKey() + " is " + countMissingBundle.getValue() + " times missing!");
		}
	}
	
	private static ArrayList<Class<?>> getI18NClasses()
	{
		final ArrayList<Class<?>> result = new ArrayList<>();
		
		final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		final MetadataReaderFactory readerFactory = new CachingMetadataReaderFactory(resolver);
		final String basePath = ClassUtils.convertClassNameToResourcePath("de.binaerebauten");
		final Resource[] resources;
		
		try
		{
			resources = resolver.getResources("classpath*:" + basePath + "/**/*.class");
		}
		catch (IOException e)
		{
			throw new AssertionError(e);
		}
		
		for (Resource resource : resources)
		{
			final MetadataReader reader;
			
			try
			{
				reader = readerFactory.getMetadataReader(resource);
			}
			catch (IOException e)
			{
				throw new AssertionError(e);
			}
			
			final String className = reader.getClassMetadata().getClassName();
			if (className.endsWith("I18N"))
			{
				try
				{
					result.add(Class.forName(className));
				}
				catch (ClassNotFoundException e)
				{
					e.printStackTrace();
				}
			}
		}
		
		return result;
	}
}
