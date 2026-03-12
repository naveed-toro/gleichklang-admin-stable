package de.binaerebauten.gleichklang.core.migration;

import de.binaerebauten.gleichklang.core.migration.queryinserter.QuestionnaireInserter;
import org.flywaydb.core.api.migration.spring.SpringJdbcMigration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.InputStream;

/**
 * Created by michael on 05/05/15.
 */
public class V01_00_05__Insert_Questionnaires implements SpringJdbcMigration
{
	
	private static final String QUESTIONNAIRES_PERSISTENCE_COMPONENTS_XML = "questionnaires/persistence_components.xml";
	
	@Override
	public void migrate(JdbcTemplate jdbcTemplate) throws Exception
	{
		Integer numberOfUsers = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM compuser", Integer.class);
		
		if (numberOfUsers == 0)
		{
		/* Only for performance in "mvn clean package" */
			return;
		}
		
		jdbcTemplate.execute("DELETE FROM choice");
		jdbcTemplate.execute("DELETE FROM question");
		jdbcTemplate.execute("DELETE FROM question_group");
		jdbcTemplate.execute("DELETE FROM choice_group");
		jdbcTemplate.execute("DELETE FROM questionnaire");
		
		insertBooleanChoices(jdbcTemplate);
		
		QuestionnaireInserter questionnaireInserter = new QuestionnaireInserter(jdbcTemplate);
		InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(QUESTIONNAIRES_PERSISTENCE_COMPONENTS_XML);
		questionnaireInserter.insertQuestionnaires(inputStream);
		
		//		jdbcTemplate.execute("UPDATE choice_group as cg join question as q on q.legacy_id = cg.legacy_id SET q.choice_group_id = cg.id");
		
		jdbcTemplate.execute("UPDATE choice_group AS cg JOIN question_choice_group_name AS qcgn "
				+ "ON qcgn.question = cg.legacy_id SET cg.legacy_name = cg.name, cg.name = qcgn.choice_group");
		
	}
	
	private void insertBooleanChoices(JdbcTemplate jdbcTemplate)
	{
		String groupName = "Ja/Nein";
		String insertBoolGroupQuery = String.format("INSERT INTO choice_group (legacy_id, create_date, name) "
				+ "VALUES ('%s', NOW(), '%s')", groupName, groupName);
		jdbcTemplate.execute(insertBoolGroupQuery);
		
		String yesOpt = "yes_opt";
		String noOpt = "no_opt";
		
		jdbcTemplate.execute(String.format("INSERT INTO choice(legacy_id, create_date, sort_order, i18n_key, "
				+ "choice_group_id) "
				+ "VALUES ('%s', NOW(), 0, '%s', (SELECT id FROM choice_group WHERE name = '%s'))", yesOpt, yesOpt, groupName));
		
		jdbcTemplate.execute(String.format("INSERT INTO choice(legacy_id, create_date, sort_order, i18n_key, "
				+ "choice_group_id) "
				+ "VALUES ('%s', NOW(), 1, '%s', (SELECT id FROM choice_group WHERE name = '%s'))", noOpt, noOpt, groupName));
		
	}
	
}
