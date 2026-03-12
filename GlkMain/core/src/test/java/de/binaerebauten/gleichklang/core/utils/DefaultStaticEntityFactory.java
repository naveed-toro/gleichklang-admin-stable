package de.binaerebauten.gleichklang.core.utils;

import de.binaerebauten.gleichklang.core.model.I18NEntity;
import de.binaerebauten.gleichklang.core.model.I18NEntity.BaseName;
import de.binaerebauten.gleichklang.core.model.I18NEntity.Language;
import de.binaerebauten.gleichklang.core.model.locatable.*;
import de.binaerebauten.gleichklang.core.model.questionnaire.QuestionGroup;
import de.binaerebauten.gleichklang.core.model.questionnaire.Questionnaire;
import de.binaerebauten.gleichklang.core.model.questionnaire.RegionQuestion;
import de.binaerebauten.gleichklang.core.model.user.*;

import java.time.LocalDate;
import java.util.*;

public class DefaultStaticEntityFactory
{
	public static final String DEFAULT_ALIAS = "ExampleUser";
	public static final String DEFAULT_EMAIL = "user@example.com";
	public static final String DEFAULT_EXTERNAL_ID = "ExternalId";

	private static int uniqueId = 0;

	private DefaultStaticEntityFactory()
	{
	}

	public static User createDefaultUser(String email, String alias, RecommendationCategory... recommendationCategories) {
		return createDefaultUser(email, alias, MemberStatus.REGISTERED, recommendationCategories);
	}

	public static User createDefaultUser(String email, String alias, MemberStatus memberStatus, RecommendationCategory... recommendationCategories)
	{
		final User user = new User();
		user.setEmail(email);
		user.setAlias(alias);
		user.setBirthDate(LocalDate.of(2013, 11, 2));

		user.setFirstName("Max");
		user.setLastName("Mustermann");
		user.setPassword("password");
		user.setMemberStatus(memberStatus);

		if(recommendationCategories != null){
			user.setCategories(new HashSet<>(Arrays.asList(recommendationCategories)));
		}

		final UserSettings userSettings = createDefaultUserSettings();
		userSettings.setUser(user);
		user.setUserSettings(userSettings);

		return user;
	}

	public static Address createDefaultAddress(Country defaultCountry)
	{
		Objects.requireNonNull(defaultCountry);
		Objects.requireNonNull(defaultCountry.getParent(), "continent is null");

		final Address address = new Address();
		address.setCity("Musterdorf");
		address.setCountry(defaultCountry);
		address.setContinent((Continent) defaultCountry.getParent());
		address.setStreetWithNumber("Musterstr 12");

		return address;
	}

	public static Address createDefaultAddress(Zip defaultZip)
	{
		Objects.requireNonNull(defaultZip);

		final Address address = createDefaultAddress((Country) defaultZip.getParent());
		address.setZip(defaultZip);
		address.setRegion(defaultZip.getRegion());
		return address;
	}

	private static UserSettings createDefaultUserSettings()
	{
		final UserSettings userSettings = new UserSettings();
		userSettings.setCancellationPolicyAccepted(true);
		userSettings.setCommunityRulesAccepted(true);
		userSettings.setDisableAds(true);
		userSettings.setDisableNewsNotifications(true);
		userSettings.setDisableRecommendationNotifications(false);
		return userSettings;
	}

	public static ProximitySearchRequest createDefaultProximitySearchRequest(Zip zip)
	{
		final ProximitySearchRequest proximitySearchRequest = new ProximitySearchRequest();
		proximitySearchRequest.setCenter(zip);
		proximitySearchRequest.setDistance(ProximitySearchRequest.ProximityDistance.LARGE);

		return proximitySearchRequest;
	}
	
	public static RegionSearchRequest createDefaultRegionSearchRequest(Region regionRestriction)
	{
		final Country country = regionRestriction.getParent();
		final Continent continent = country.getParent();
		
		final RegionSearchRequest regionSearchRequest = new RegionSearchRequest();
		regionSearchRequest.addRestriction(regionRestriction);
		regionSearchRequest.setCountry(country);
		regionSearchRequest.setContinent(continent);
		
		return regionSearchRequest;
	}

	private static Questionnaire createDefaultQuestionnaire()
	{
		final Questionnaire questionnaire = new Questionnaire();
		questionnaire.setI18nKey(createUniqueString());
		questionnaire.setSortOrder(createUniqueInt());
		return questionnaire;
	}

	static RegionQuestion createDefaultRegionQuestion(QuestionGroup questionGroup, I18NEntity i18NEntity)
	{
		final RegionQuestion regionQuestion = new RegionQuestion();
		regionQuestion.setI18nKey(i18NEntity.getKey());
		regionQuestion.setSortOrder(createUniqueInt());
		questionGroup.addQuestion(regionQuestion);
		return regionQuestion;
	}

	static I18NEntity createDefaultI18NEntry(String key, String value)
	{
		final I18NEntity i18NEntity = new I18NEntity();
		i18NEntity.setKey(key);
		i18NEntity.setBaseName(BaseName.NONE);
		i18NEntity.setLanguage(Language.DE);
		i18NEntity.setValue(value);

		return i18NEntity;
	}

	public static Region createDefaultRegion(Country country)
	{
		final Region region = new Region();
		region.setI18nKey(createUniqueString());
		region.setParent(country);
		return region;
	}

	public static Country createDefaultCountry(Continent continent)
	{
		final Country country = new Country();
		country.setI18nKey(createUniqueString());
		country.setParent(continent);
		return country;
	}

	public static Continent createDefaultContinent()
	{
		final Continent continent = new Continent();
		continent.setI18nKey(createUniqueString());
		return continent;
	}

	static Zip createDefaultZip(Country country, Region region)
	{
		final Zip zip = new Zip();
		zip.setZip("12345");
		zip.setLatitude(42.0);
		zip.setLongitude(42.0);
		zip.setRegion(region);
		zip.setParent(country);
		return zip;
	}

	static QuestionGroup createDefaultQuestionGroup(Questionnaire questionnaire, I18NEntity i18NKey)
	{
		final QuestionGroup questionGroup = new QuestionGroup();
		questionnaire.addQuestionGroup(questionGroup);
		questionGroup.setI18nKey(i18NKey.getKey());
		questionGroup.setSortOrder(createUniqueInt());
		return questionGroup;
	}

	static String createUniqueString()
	{
		return UUID.randomUUID().toString();
	}

	private static int createUniqueInt()
	{
		return uniqueId++;
	}

	public static Admin createDefaultAdmin(String email, String alias)
	{
		Admin admin = new Admin();
		admin.setEmail(email);
		admin.setAlias(alias);
		admin.setPassword("default");
		return admin;
	}
}
