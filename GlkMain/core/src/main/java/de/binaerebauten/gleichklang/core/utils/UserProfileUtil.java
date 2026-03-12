package de.binaerebauten.gleichklang.core.utils;

import de.binaerebauten.gleichklang.core.model.NaturalKeyEntity.NaturalKey;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;

import java.util.Collection;
import java.util.EnumSet;

import static de.binaerebauten.gleichklang.core.model.NaturalKeyEntity.NaturalKey.*;

public class UserProfileUtil
{
	public interface NaturalKeyCallback<T>
	{
		T action(Collection<NaturalKey> naturalKeys, Collection<NaturalKey> excludedNaturalKeys);
	}
	
	public interface NaturalKeyCallbackWithParameter<P, T>
	{
		T action(P parameter, Collection<NaturalKey> naturalKeys, Collection<NaturalKey> excludedNaturalKeys);
	}
	
	public enum UserInfo implements DefaultEnumI18N
	{
		PERSON,
		HOBBIES,
		LOOK,
		SPECIALS;
		
		@Override
		public String toString()
		{
			return msg();
		}
	}
	
	public static <T> T getNaturalKeys(UserInfo userInfo, Collection<RecommendationCategory> recommendationCategories, NaturalKeyCallback<T> naturalKeyCallback)
	{
		return getNaturalKeys(userInfo, recommendationCategories, null, (parameter, naturalKeys, excludedNaturalKeys) -> naturalKeyCallback.action(naturalKeys, excludedNaturalKeys));
	}
	
	public static <P, T> T getNaturalKeys(UserInfo userInfo, Collection<RecommendationCategory> recommendationCategories, P parameter, NaturalKeyCallbackWithParameter<P, T> naturalKeyCallback)
	{
		final Collection<NaturalKey> naturalKeys;
		final Collection<NaturalKey> excludedNaturalKeys;
		
		switch (userInfo)
		{
			case PERSON:
				naturalKeys = PERSON.asSet();
				excludedNaturalKeys = PETS.asSet();
				break;
			case HOBBIES:
				naturalKeys = HOBBIES.asSet();
				excludedNaturalKeys = null;
				break;
			case LOOK:
				naturalKeys = LOOK.asSet();
				excludedNaturalKeys = null;
				break;
			case SPECIALS:
				naturalKeys = EnumSet.noneOf(NaturalKey.class);
				if (recommendationCategories != null)
				{
					if (recommendationCategories.contains(RecommendationCategory.FRIENDSHIP))
						naturalKeys.add(EROTICISM_F);
					if (recommendationCategories.contains(RecommendationCategory.PARTNERSHIP))
						naturalKeys.add(EROTICISM_P);
				}
				excludedNaturalKeys = null;
				break;
			default:
				naturalKeys = null;
				excludedNaturalKeys = null;
		}
		
		return naturalKeyCallback.action(parameter, naturalKeys, excludedNaturalKeys);
	}
}
