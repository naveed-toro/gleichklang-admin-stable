package de.binaerebauten.gleichklang.core.model.questionnaire;

import de.binaerebauten.gleichklang.core.model.BaseMigrationTest;
import org.junit.Ignore;

import java.util.Arrays;
import java.util.List;

/**
 * Created by michael on 08/06/15.
 */
@Ignore("tests disabled until migration test runs faster")
public class QuestionnairesMigrationTest extends BaseMigrationTest
{
	@Override
	protected List<Class<?>> getEntityClasses()
	{
		return Arrays.asList(Answer.class, ChoiceAnswer.class);
	}
}
