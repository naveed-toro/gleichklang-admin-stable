package de.binaerebauten.gleichklang.core.model;

import de.binaerebauten.gleichklang.core.utils.DefaultEnumI18N;
import de.binaerebauten.gleichklang.core.utils.XmlCDATAAdapter;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@XmlAccessorType(XmlAccessType.FIELD)
@Entity
@Table(name = "i18n")
public class I18NEntity extends BaseEntity
{
	public enum Language implements DefaultEnumI18N
	{
		DE(Locale.GERMAN),
		EN(Locale.ENGLISH);
//		KH(Locale.forLanguageTag("kh")),
//		FR(Locale.FRENCH),
//		ES(Locale.forLanguageTag("es")),
//		PT(Locale.forLanguageTag("pt")),
//		NL(Locale.forLanguageTag("nl"));
		
		public static final String COOKIE_NAME = "language";
		private static final Map<Locale, Language> reversMap = EnumSet.allOf(Language.class).stream().collect(Collectors.toMap(Language::toLocale, l -> l));

		private final Locale locale;

		Language(Locale locale)
		{
			this.locale = Objects.requireNonNull(locale);
		}

		/**
		 * Returns supported {@link Language} by {@link Locale}.
		 * If {@link Language} is not supported it returns null
		 *
		 * @param locale
		 * @return {@link Language} if it is supported by the application, null otherwise
		 */
		public static Language valueOf(Locale locale)
		{
			return reversMap.get(new Locale(locale.getLanguage()));
		}

		public Locale toLocale()
		{
			return locale;
		}

		public static Language getDefault(){
			return DE;
		}
		
		@Override
		public String toString()
		{
			return msg();
		}
	}

	public enum BaseName
	{
		NONE,
		QUESTIONNAIRE_NAME,
		QUESTIONNAIRE_DESCRIPTION,
		QUESTION_GROUP_NAME,
		QUESTION_GROUP_DESCRIPTION,
		QUESTION_NAME,
		QUESTION_DESCRIPTION,
		CHOICE_GROUP,
		CHOICE_VALUE,
		CONTINENT,
		COUNTRY,
		REGION,
		PRODUCT_DESCRIPTION
	}

    @XmlAttribute
	@Column
	@Enumerated(EnumType.STRING)
	private Language language;

    @XmlAttribute
	@Column(name = "i18n_key")
	private String key;

    @XmlValue
    @XmlJavaTypeAdapter(XmlCDATAAdapter.class)
	@Column(name = "i18n_value")
	private String value;


    @XmlAttribute
	@Column(name = "base_name")
	@Enumerated(EnumType.STRING)
	private BaseName baseName;

	public Language getLanguage()
	{
		return language;
	}

	public void setLanguage(Language language)
	{
		this.language = language;
	}

	public String getKey()
	{
		return key;
	}

	public void setKey(String key)
	{
		this.key = key;
	}

	public String getValue()
	{
		return value;
	}

	public void setValue(String value)
	{
		this.value = value;
	}

	public BaseName getBaseName()
	{
		return baseName;
	}

	public void setBaseName(BaseName baseName)
	{
		this.baseName = baseName;
	}
}
