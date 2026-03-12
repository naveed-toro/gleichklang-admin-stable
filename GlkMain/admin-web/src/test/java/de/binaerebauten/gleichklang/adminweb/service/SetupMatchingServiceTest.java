package de.binaerebauten.gleichklang.adminweb.service;

import de.binaerebauten.gleichklang.adminweb.config.AdminTestConfig;
import de.binaerebauten.gleichklang.core.model.matching.*;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.repository.*;
import de.binaerebauten.gleichklang.core.service.validator.UniqueValidationException;
import de.binaerebauten.gleichklang.core.service.validator.ValidationException;
import de.binaerebauten.gleichklang.core.utils.DefaultEntityFactory;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = { AdminTestConfig.class })
public class SetupMatchingServiceTest extends BasePersistenceTest
{
	@Rule
	public final ExpectedException thrown = ExpectedException.none();

	@Autowired
	private DefaultEntityFactory entityFactory;

	@Autowired
	private SetupMatchingService setupMatchingService;

	@After
	public void teardown()
	{
		entityFactory.reset();
	}

	@Test
	public void testSaveQuestionsMapping_UniqueValidationException() throws Exception
	{
		final AbstractQuestionsMapping questionMapping = entityFactory.persistDefaultAffinityMapping();
		questionMapping.setNaturalKey("natural_key");
		setupMatchingService.saveQuestionsMapping(questionMapping);

		final AbstractQuestionsMapping questionMapping2 = entityFactory.persistDefaultAffinityMapping();
		questionMapping2.setNaturalKey("natural_key");

		thrown.expect(UniqueValidationException.class);

		setupMatchingService.saveQuestionsMapping(questionMapping2);
	}

	@Test
	public void testSaveQuestionsMapping_ValidationException() throws Exception
	{
		final AbstractQuestionsMapping questionMapping = entityFactory.persistDefaultAffinityMapping();
		questionMapping.setNaturalKey("natural_key");
		setupMatchingService.saveQuestionsMapping(questionMapping);
		questionMapping.setNaturalKey(null);

		thrown.expect(ValidationException.class);

		setupMatchingService.saveQuestionsMapping(questionMapping);
	}

	@Test
	public void testSaveMatchingMatrix_UniqueValidationException() throws Exception
	{
		MatchingMatrix matchingMatrix1 = entityFactory.persistDefaultMatchingMatrix();
		matchingMatrix1.setName("Matrix");
		setupMatchingService.saveMatrix(matchingMatrix1);

		MatchingMatrix matchingMatrix2 = entityFactory.persistDefaultMatchingMatrix();
		matchingMatrix2.setName(matchingMatrix1.getName());

		thrown.expect(UniqueValidationException.class);
		setupMatchingService.saveMatrix(matchingMatrix2);
	}

	@Test
	public void testSaveMatchingMatrix_ValidationException() throws Exception
	{
		MatchingMatrix matchingMatrix = entityFactory.persistDefaultMatchingMatrix();
		matchingMatrix.setName("01234567890123456789012345678901");

		thrown.expect(ValidationException.class);
		setupMatchingService.saveMatrix(matchingMatrix);
	}

	@Test
	public void testDeleteMatchingMatrix() throws ValidationException
	{
		MatchingMatrix matchingMatrix = entityFactory.persistDefaultMatchingMatrix();

		setupMatchingService.deleteMatrix(matchingMatrix);
	}

	@Test
	public void testDeleteMatchingMatrix_ValidationException()
			throws ValidationException
	{
		ChoiceQuestionsMapping choiceQuestionsMapping = entityFactory.persistDefaultChoiceQuestionsMapping(RecommendationCategory.FRIENDSHIP);

		thrown.expect(ValidationException.class);
		setupMatchingService.deleteMatrix(choiceQuestionsMapping.getMatrix());
	}

	@Test
	public void testSaveActivator_UniqueValidationException()
			throws ValidationException
	{
		QuestionActivator questionActivator1 = entityFactory.persistDefaultQuestionActivator();
		questionActivator1.setNaturalKey("Activator");
		setupMatchingService.saveActivator(questionActivator1);

		QuestionActivator questionActivator2 = entityFactory.persistDefaultQuestionActivator();
		questionActivator2.setNaturalKey(questionActivator1.getNaturalKey());

		thrown.expect(UniqueValidationException.class);
		setupMatchingService.saveActivator(questionActivator2);
	}

	@Test
	public void testSaveActivator_ValidationException()
			throws ValidationException
	{
		QuestionActivator questionActivator = entityFactory.persistDefaultQuestionActivator();
		questionActivator.setNaturalKey(null);

		thrown.expect(ValidationException.class);
		setupMatchingService.saveActivator(questionActivator);
	}
}
