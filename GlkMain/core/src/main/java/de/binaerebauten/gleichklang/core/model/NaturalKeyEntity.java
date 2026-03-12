package de.binaerebauten.gleichklang.core.model;

import de.binaerebauten.gleichklang.core.model.locatable.Country;
import de.binaerebauten.gleichklang.core.model.questionnaire.*;

import java.io.Serializable;
import java.util.EnumSet;

public interface NaturalKeyEntity<ID extends Serializable> extends IdEntity<ID>
{
	String NATURAL_KEY = "i18nKey";
	String SEX_QUESTION = "stamm.sex";

	String getI18nKey();

	void setI18nKey(String i18nKey);
	
	default NaturalKey getNaturalKey(Class<? extends NaturalKeyEntity<?>> type)
	{
		return NaturalKey.getNaturalKey(getI18nKey(), type);
	}
	
	default void setNaturalKey(NaturalKey naturalKey)
	{
		setI18nKey(naturalKey != null ? naturalKey.naturalKey : null);
	}
	
	enum NaturalKey
	{
		SEX(SEX_QUESTION, ChoiceQuestion.class),
		SEX_W("sex_w", Choice.class),
		SEX_M("sex_m", Choice.class),
		SEX_I("sex_i", Choice.class),
		SEX_WI("sex_wi", Choice.class),
		SEX_MI("sex_mi", Choice.class),
		PERSON("person", Questionnaire.class),
		PETS("person.tiere", QuestionGroup.class),
		HOBBIES("hobby_v", Questionnaire.class),
		REGISTRATION("registration", Questionnaire.class),
		LOOK("aussehen", Questionnaire.class),
		EROTICISM_P("p_erotikundsex", QuestionGroup.class),
		EROTICISM_F("f_erotikundsex", QuestionGroup.class),
		FREE_TEXT_PARTNER("ptext", Questionnaire.class),
		FREE_TEXT_FRIENDSHIP("ftext", Questionnaire.class),
		AWARE_THROUGH("source", ChoiceQuestion.class),
		FREE_TEXT_GK("source_text", TextQuestion.class),
		PROFILE_GESELLSCHAFT("gesellschaft", Questionnaire.class),
        PROFILE_FREUNDSCHAFT("freundschaft", Questionnaire.class),
        PROFILE_PARTNERSCHAFT("partnerschaft", Questionnaire.class),
        PROFILE_PERSOENLICHKEIT("persoenlichkeit", Questionnaire.class),
		GERMANY("DE", Country.class),
		AUSTRIA("AT", Country.class);
		
		public final String naturalKey;
		public final Class<? extends NaturalKeyEntity<?>> naturalKeyClass;

		NaturalKey(String naturalKey, Class<? extends NaturalKeyEntity<?>> naturalKeyClass)
		{
			this.naturalKey = naturalKey;
			this.naturalKeyClass = naturalKeyClass;
		}

		public static NaturalKey getNaturalKey(String value, Class<? extends NaturalKeyEntity<?>> type)
		{
			for (NaturalKey naturalKey : NaturalKey.values())
			{
				if (naturalKey.naturalKey.equals(value) && naturalKey.naturalKeyClass.equals(type))
					return naturalKey;
			}
			return null;
		}
		
		public EnumSet<NaturalKey> asSet()
		{
			return EnumSet.of(this);
		}
	}
}
