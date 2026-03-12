package de.binaerebauten.gleichklang.core.model;

import de.binaerebauten.gleichklang.core.model.NaturalKeyEntity.NaturalKey;
import de.binaerebauten.gleichklang.core.model.locatable.Country;
import de.binaerebauten.gleichklang.core.model.questionnaire.*;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

public class NaturalKeyTest
{
	@Test
	public void testUniqueNaturalKey()
	{
		final Map<Class<? extends NaturalKeyEntity<?>>, Set<String>> naturalKeyCombinations = new HashMap<>();

		for(NaturalKey naturalKey : NaturalKey.values())
		{
			final Set<String> naturalKeys = naturalKeyCombinations.getOrDefault(naturalKey.naturalKeyClass, new HashSet<>());
			Assert.assertThat(naturalKeys, CoreMatchers.not(CoreMatchers.hasItem(naturalKey.naturalKey)));
			naturalKeys.add(naturalKey.naturalKey);
			naturalKeyCombinations.putIfAbsent(naturalKey.naturalKeyClass, naturalKeys);
		}
	}

	@Test
	public void testNaturalKeyIsOfCorrectDatatype()
	{
		final List <Class<? extends NaturalKeyEntity<?>>> allowedTypes = Arrays.asList(Questionnaire.class, QuestionGroup.class, TextQuestion.class, ChoiceQuestion.class, NumberQuestion.class, RegionQuestion.class, Choice.class, Country.class);
		for(NaturalKey naturalKey : NaturalKey.values())
		{
			Assert.assertThat("type of natural key is unexpected",  allowedTypes, CoreMatchers.hasItem(naturalKey.naturalKeyClass));
		}
	}
}
