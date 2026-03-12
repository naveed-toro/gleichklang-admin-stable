package de.binaerebauten.gleichklang.core.repository;

import de.binaerebauten.gleichklang.core.model.I18NEntity;
import de.binaerebauten.gleichklang.core.utils.DefaultEntityFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assert.assertThat;

/**
 * Unit test for {@link I18NRepository}.
 */
public class I18NRepositoryTest extends AbstractRepositoryTest<I18NEntity>
{
	private static final String QUESTION_KEY = "question";
	private static final String PRODUCT_DESCRIPTION_KEY = "product_description";
	private static final String PRODUCT_DESCRIPTION_KEY1 = "product_description1";

	@Autowired
	private I18NRepository i18NRepository;

	@Autowired
	private DefaultEntityFactory entityFactory;

	private I18NEntity germanQuestionI18nEntity;

	private I18NEntity englishQuestionI18nEntity;

	private I18NEntity germanProductDescriptionI18nEntity;

	private I18NEntity germanProductDescriptionI18nEntity1;

	private I18NEntity englishProductDescriptionI18nEntity;

	@Test
	public void testFindByBaseNameAndKey()
	{
		List<I18NEntity> i18NEntities = i18NRepository.findByBaseNameAndKey(I18NEntity.BaseName.PRODUCT_DESCRIPTION,
				PRODUCT_DESCRIPTION_KEY);

		assertThat(i18NEntities.size(), is(2));
		assertThat(i18NEntities, hasItems(englishProductDescriptionI18nEntity, germanProductDescriptionI18nEntity));

		i18NEntities = i18NRepository.findByBaseNameAndKey(I18NEntity.BaseName.QUESTION_NAME,
				PRODUCT_DESCRIPTION_KEY);

		assertThat(i18NEntities.isEmpty(), is(true));

		i18NEntities = i18NRepository.findByBaseNameAndKey(I18NEntity.BaseName.QUESTION_NAME,
				QUESTION_KEY);

		assertThat(i18NEntities.size(), is(2));
		assertThat(i18NEntities, hasItems(englishQuestionI18nEntity, germanQuestionI18nEntity));
	}

	@Test
	public void testFindByBaseNameAndLanguage()
	{
		List<I18NEntity> i18NEntities = i18NRepository.findByBaseNameAndLanguage(I18NEntity.BaseName.PRODUCT_DESCRIPTION,
				I18NEntity.Language.DE);

		assertThat(i18NEntities.size(), is(2));
		assertThat(i18NEntities, hasItems(germanProductDescriptionI18nEntity, germanProductDescriptionI18nEntity1));

		i18NEntities = i18NRepository.findByBaseNameAndLanguage(I18NEntity.BaseName.QUESTION_NAME,
				I18NEntity.Language.EN);

		assertThat(i18NEntities.size(), is(1));
		assertThat(i18NEntities, hasItems(englishQuestionI18nEntity));
	}

	@Test
	public void testGetLastChangeDate()
	{
		LocalDateTime lastChangeDate = i18NRepository.getLastChangeDate(I18NEntity.BaseName.PRODUCT_DESCRIPTION, I18NEntity.Language.DE);

		assertThat(lastChangeDate, nullValue());

		LocalDateTime changeDate = LocalDateTime.now();
		germanProductDescriptionI18nEntity.setChangeDate(changeDate);
		i18NRepository.save(germanProductDescriptionI18nEntity);

		englishProductDescriptionI18nEntity.setChangeDate(changeDate.minusDays(1));
		i18NRepository.save(germanProductDescriptionI18nEntity);

		lastChangeDate = i18NRepository.getLastChangeDate(I18NEntity.BaseName.PRODUCT_DESCRIPTION, I18NEntity.Language.DE);

		assertThat(lastChangeDate, greaterThanOrEqualTo(changeDate));
	}

	@Override
	protected Collection<I18NEntity> getPersistedEntities()
	{
		germanQuestionI18nEntity = entityFactory.persistDefaultI18NEntry(I18NEntity.BaseName.QUESTION_NAME,
				I18NEntity.Language.DE, QUESTION_KEY, "Dies ist eine Testfrage");
		englishQuestionI18nEntity = entityFactory.persistDefaultI18NEntry(I18NEntity.BaseName.QUESTION_NAME,
				I18NEntity.Language.EN, QUESTION_KEY, "This is a test question");

		germanProductDescriptionI18nEntity = entityFactory.persistDefaultI18NEntry(I18NEntity.BaseName.PRODUCT_DESCRIPTION,
				I18NEntity.Language.DE, PRODUCT_DESCRIPTION_KEY, "Dies ist eine Testproduktbeschreibung");
		germanProductDescriptionI18nEntity1 = entityFactory.persistDefaultI18NEntry(I18NEntity.BaseName.PRODUCT_DESCRIPTION,
				I18NEntity.Language.DE, PRODUCT_DESCRIPTION_KEY1, "Dies ist eine Testproduktbeschreibung nummer 1");

		englishProductDescriptionI18nEntity = entityFactory.persistDefaultI18NEntry(I18NEntity.BaseName.PRODUCT_DESCRIPTION,
				I18NEntity.Language.EN, PRODUCT_DESCRIPTION_KEY, "This is a test product description");

		return Arrays.asList(germanQuestionI18nEntity, englishQuestionI18nEntity,
				germanProductDescriptionI18nEntity, germanProductDescriptionI18nEntity1, englishProductDescriptionI18nEntity);
	}

	@Override
	protected JpaRepository<I18NEntity, Long> getRepository()
	{
		return i18NRepository;
	}
}
