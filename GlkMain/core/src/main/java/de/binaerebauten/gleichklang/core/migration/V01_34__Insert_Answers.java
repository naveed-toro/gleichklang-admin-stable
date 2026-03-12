package de.binaerebauten.gleichklang.core.migration;

import com.google.common.base.Preconditions;
import de.binaerebauten.gleichklang.core.migration.model.MigrationAnswer;
import de.binaerebauten.gleichklang.core.migration.querybuilder.AnswersMigrationHelper;
import de.binaerebauten.gleichklang.core.migration.queryinserter.ChoiceInfo;
import de.binaerebauten.gleichklang.core.migration.queryinserter.QuestionInfo;
import org.flywaydb.core.api.migration.spring.SpringJdbcMigration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Created by michael on 11/06/15.
 */
public class V01_34__Insert_Answers implements SpringJdbcMigration
{

	private static final Logger LOG = LoggerFactory.getLogger(V01_34__Insert_Answers.class);
	private final Map<Integer, Integer> affinityChoices = new HashMap<>();
	private Map<String, Long> userIds;
	private String tableSchema;
	private int yesOptChoice;
	private int noOptChoice;

	@Override
	public void migrate(JdbcTemplate jdbcTemplate) throws Exception
	{

		this.userIds = AnswersMigrationHelper.getUserIds(jdbcTemplate);
		tableSchema = jdbcTemplate.queryForObject("SELECT DATABASE()", String.class);
		LOG.info("Selected schema {}", tableSchema);
		if (userIds.isEmpty())
		{
			return;
		}
		
		
		// Begin idempotence
		jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
		jdbcTemplate.execute("TRUNCATE TABLE choice_answer");
		jdbcTemplate.execute("TRUNCATE TABLE answer");
		jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");

		jdbcTemplate.update("UPDATE questionnaire SET legacy_id = NULL WHERE legacy_id IN ('partner.region_question', 'friend.region_question', 'registration', 'stammdaten')");
		jdbcTemplate.execute("CALL CREATE_INDEX('user_legacy_id', 'user_', 'legacy_id')");
		jdbcTemplate.execute("CALL CREATE_UNIQUE_INDEX('answer_question_user_idx', 'answer', 'question_id, user_id')");

		jdbcTemplate.execute("UPDATE question SET DTYPE = 'BooleanQuestion' WHERE legacy_id = 'bool_legacy'");
		jdbcTemplate.execute("UPDATE question SET DTYPE = 'NumberQuestion' WHERE legacy_id = 'affinity'");
		
		yesOptChoice = jdbcTemplate.queryForObject("SELECT id FROM choice WHERE i18n_key = 'yes_opt'", Integer.class);
		noOptChoice = jdbcTemplate.queryForObject("SELECT id FROM choice WHERE i18n_key = 'no_opt'", Integer.class);
		affinityChoices.put(1, jdbcTemplate.queryForObject("SELECT id FROM choice WHERE i18n_key = 'affinity_1'", Integer.class));
		affinityChoices.put(2, jdbcTemplate.queryForObject("SELECT id FROM choice WHERE i18n_key = 'affinity_2'", Integer.class));
		affinityChoices.put(3, jdbcTemplate.queryForObject("SELECT id FROM choice WHERE i18n_key = 'affinity_3'", Integer.class));
		affinityChoices.put(4, jdbcTemplate.queryForObject("SELECT id FROM choice WHERE i18n_key = 'affinity_4'", Integer.class));
		affinityChoices.put(5, jdbcTemplate.queryForObject("SELECT id FROM choice WHERE i18n_key = 'affinity_5'", Integer.class));

		long completeStart = System.currentTimeMillis();

		for (String questionnaireName : jdbcTemplate.queryForList("SELECT legacy_id FROM questionnaire WHERE legacy_id IS NOT NULL", String.class))
		{
			long questionnaireStart = System.currentTimeMillis();
			migrateAnswersForQuestionnaire(questionnaireName, jdbcTemplate);
			long questionnaireEnd = System.currentTimeMillis();
			LOG.info("Processing of questionnaire {} took {} ms", questionnaireName, questionnaireEnd - questionnaireStart);
		}

		long completeEnd = System.currentTimeMillis();
		LOG.info("Complete Answer migration took {} ms", completeEnd - completeStart);

		jdbcTemplate.execute("UPDATE question SET legacy_id = 'bool_legacy' WHERE DTYPE = 'BooleanQuestion'");
		jdbcTemplate.execute("UPDATE question SET DTYPE = 'ChoiceQuestion' WHERE legacy_id = 'affinity'");
	}

	private int migrateChoiceAnswersAndChoices(JdbcTemplate jdbcTemplate, QuestionInfo questionInfo, List<MigrationAnswer> migrationAnswers)
	{
		Preconditions.checkArgument(questionInfo.getDTYPE().equals("ChoiceQuestion"));

		long start = System.currentTimeMillis();
		migrateChoiceAnswers(jdbcTemplate, questionInfo);
		long end = System.currentTimeMillis();

		LOG.info("{} answers inserted. It took {} ms", migrationAnswers.size(), end - start);

		start = System.currentTimeMillis();

		String questionId = questionInfo.getId();

		String insertChoiceAnswersStatement = AnswersMigrationHelper.createInsertChoiceAnswerChoicesStatement();
		BatchPreparedStatementSetter insertChoiceAnswersBatchSetter =
				AnswersMigrationHelper.createInsertChoiceAnswerChoicesBatchSetter(jdbcTemplate, migrationAnswers, questionId);

		jdbcTemplate.batchUpdate(insertChoiceAnswersStatement, insertChoiceAnswersBatchSetter);

		end = System.currentTimeMillis();

		LOG.info("{} choices for {}.{} inserted. It took {} ms", insertChoiceAnswersBatchSetter.getBatchSize(),
				questionInfo.getQuestionnaireName(), questionInfo.getLabel(), end - start);

		return migrationAnswers.size();

	}

	private void commit(JdbcTemplate jdbcTemplate, Callable<Void> callable)
	{
		ConnectionCallback commitAction = c -> {
			try
			{
				return callable.call();
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}
			finally
			{
				c.commit();

			}
		};

		long start = System.currentTimeMillis();

		jdbcTemplate.execute(commitAction);
		long end = System.currentTimeMillis();

		LOG.info("Successfully commited transaction. It took {} ms", end - start);
	}

	private void migrateAnswersForQuestionnaire(String questionnaire, JdbcTemplate jdbcTemplate)
	{
		List<QuestionInfo> questionInfos = jdbcTemplate.query(AnswersMigrationHelper.getQuestionInfoQuery(null, questionnaire), new BeanPropertyRowMapper<>(QuestionInfo.class));
		String legacyTableName = "comp" + questionnaire;
		List<String> legacyColumnNames = AnswersMigrationHelper.getColumnNames(jdbcTemplate, legacyTableName, tableSchema);
		final int[] questionNumber = { 0 };
		for (QuestionInfo questionInfo : questionInfos)
		{

			String key = questionInfo.getLabel();

			if (legacyColumnNames.contains(key))
			{
				// this lambda is required to wrap the migration
				Callable<Void> doInTransaction = () -> {
					int numberOfInsertedRows = 0;
					LOG.info("Processing {} {}.{} ({}/{}):", questionInfo.getDTYPE(), questionnaire, key, questionNumber, questionInfos.size());
					long start = System.currentTimeMillis();
					switch (questionInfo.getDTYPE())
					{
						case "TextQuestion":
							numberOfInsertedRows = migrateTextAnswers(jdbcTemplate, questionInfo);
							break;
						case "NumberQuestion":
							if(isAffinity(questionInfo)){
								numberOfInsertedRows = migrateAffinityAnswers(jdbcTemplate, questionInfo);
							}
							else{
								numberOfInsertedRows = migrateNumberAnswers(jdbcTemplate, questionInfo);
							}
							break;
						case "BooleanQuestion":
							numberOfInsertedRows = migrateBooleanAnswers(jdbcTemplate, questionInfo);
							break;
						case "ChoiceQuestion":
							if (questionInfo.getSelectionType().equals("SINGLE"))
							{
								numberOfInsertedRows = migrateSingleChoiceAnswers(jdbcTemplate, questionInfo);
							}
							else
							{
								String query = String.format("SELECT no, owner, createdate, changedate, %s FROM comp%s AS t JOIN user_ AS u ON t.owner = u.legacy_id", key, questionnaire);

								final List<Map<String, Object>> rows = jdbcTemplate.queryForList(query);
								List<MigrationAnswer> migrationAnswerInfosForQuestion = new ArrayList<>();
								LOG.info("Found {} answers ", rows.size());
								Map<String, ChoiceInfo> questionChoices = AnswersMigrationHelper.getQuestionChoices(jdbcTemplate, questionInfo);

								for (Map<String, Object> row : rows)
								{
									MigrationAnswer migrationAnswer = createAnswerInfo(row, questionInfo, questionChoices);
									migrationAnswerInfosForQuestion.add(migrationAnswer);
								}

								numberOfInsertedRows = migrateChoiceAnswersAndChoices(jdbcTemplate, questionInfo, migrationAnswerInfosForQuestion);
							}

							break;
						default:
							throw new IllegalStateException("Invalid question DTYPE:" + questionInfo.getDTYPE());
					}
					long end = System.currentTimeMillis();
					String performanceQuery = String.format("CALL TRACK_ANSWER_QUERY('1.34', '%s', %s, '%s', %s, %s)", questionInfo.getDTYPE(), numberOfInsertedRows, questionInfo.getQuestionnaireName() + "\\." + questionInfo.getLabel(), end - start, questionInfo.getId());
					jdbcTemplate.execute(performanceQuery);

					return null;
				};
				commit(jdbcTemplate, doInTransaction);
			}
			else
			{
				LOG.warn("Column {} doesn't exists in table {}.{} ", key, tableSchema, legacyTableName);
			}
			questionNumber[0]++;

		}
	}

	private int migrateAffinityAnswers(JdbcTemplate jdbcTemplate, QuestionInfo questionInfo)
	{
		String questionId = questionInfo.getId();
		String query = String.format("INSERT INTO answer(DTYPE, legacy_id, question_id, user_id) SELECT 'ChoiceAnswer', c.no, %s, u.id FROM comp%s AS c JOIN user_ AS u ON c.owner = u.legacy_id ", questionId, questionInfo.getQuestionnaireName());
		int numberOfInsertedRows = jdbcTemplate.update(query);
		LOG.info("Processing affinity answer {}. Found {} affinity answers ", questionInfo.getLabel(), numberOfInsertedRows);

		for (int i = 1; i <= 5; i++)
		{
			Integer choiceId = affinityChoices.get(i);
			long start = System.currentTimeMillis();
			String choiceQuery = String.format("INSERT INTO choice_answer(answer_id, choice_id) SELECT a.id, %s FROM comp%s AS c JOIN answer AS a ON c.no = a.legacy_id WHERE c.%s = '%s' AND a.question_id = %s", choiceId, questionInfo.getQuestionnaireName(), questionInfo.getLabel(), i, questionId);
			int numberOfChoices = jdbcTemplate.update(choiceQuery);
			long end = System.currentTimeMillis();
			LOG.info("{} Choice answers for affinity choice {} inserted. It took {} ms", numberOfChoices, i, end - start);
		}

		return numberOfInsertedRows;
	}

	private int migrateSingleChoiceAnswers(JdbcTemplate jdbcTemplate, QuestionInfo questionInfo)
	{
		String questionId = questionInfo.getId();
		String query = String.format("INSERT INTO answer(DTYPE, legacy_id, question_id, user_id) SELECT 'ChoiceAnswer', c.no, %s, u.id FROM comp%s AS c JOIN user_ AS u ON c.owner = u.legacy_id ", questionId, questionInfo.getQuestionnaireName());
		int numberOfInsertedRows = jdbcTemplate.update(query);
		LOG.info("Found {} answers ", numberOfInsertedRows);

		Map<String, ChoiceInfo> questionChoices = AnswersMigrationHelper.getQuestionChoices(jdbcTemplate, questionInfo);
		for (String choice : questionChoices.keySet())
		{
			long start = System.currentTimeMillis();
			ChoiceInfo choiceInfo = questionChoices.get(choice);
			LOG.info("Inserting choice {} ", choiceInfo.getChoice());
			String choiceQuery = String.format("INSERT INTO choice_answer(answer_id, choice_id) SELECT a.id, %s FROM comp%s AS c JOIN answer AS a ON c.no = a.legacy_id WHERE c.%s = '%s' AND a.question_id = %s", choiceInfo.getId(), questionInfo.getQuestionnaireName(), questionInfo.getLabel(), choice, questionId);
			int numberOfChoices = jdbcTemplate.update(choiceQuery);
			long end = System.currentTimeMillis();
			LOG.info("{} Choice answers for choice {} inserted. It took {} ms", numberOfChoices, choice, end - start);
		}

		return numberOfInsertedRows;
	}

	private int migrateNumberAnswers(JdbcTemplate jdbcTemplate, QuestionInfo questionInfo)
	{
		String questionId = questionInfo.getId();
		String query = String.format("INSERT INTO answer(DTYPE, legacy_id, number_value, question_id, user_id) SELECT 'NumberAnswer', c.no, c.%s, %s, u.id FROM comp%s AS c JOIN user_ AS u ON c.owner = u.legacy_id ", questionInfo.getLabel(), questionId, questionInfo.getQuestionnaireName());
		return jdbcTemplate.update(query);
	}

	private int migrateTextAnswers(JdbcTemplate jdbcTemplate, QuestionInfo questionInfo)
	{
		String questionId = questionInfo.getId();
		String query = String.format("INSERT INTO answer(DTYPE, legacy_id, text_value, question_id, user_id) SELECT 'TextAnswer', c.no, c.%s, %s, u.id FROM comp%s AS c JOIN user_ AS u ON c.owner = u.legacy_id ", questionInfo.getLabel(), questionId, questionInfo.getQuestionnaireName());
		return jdbcTemplate.update(query);
	}

	private int migrateChoiceAnswers(JdbcTemplate jdbcTemplate, QuestionInfo questionInfo)
	{
		String questionId = questionInfo.getId();
		String query = String.format("INSERT INTO answer(DTYPE, legacy_id, change_date, create_date, question_id, user_id) SELECT 'ChoiceAnswer', c.no, c.createdate, c.changedate, %s, u.id FROM comp%s AS c JOIN user_ AS u ON c.owner = u.legacy_id ", questionId, questionInfo.getQuestionnaireName());
		return jdbcTemplate.update(query);
	}

	private int migrateBooleanAnswers(JdbcTemplate jdbcTemplate, QuestionInfo questionInfo)
	{
		String questionId = questionInfo.getId();
		int result = jdbcTemplate.update(String.format("INSERT INTO answer(DTYPE, legacy_id, question_id, user_id) SELECT 'ChoiceAnswer', c.no, %s, u.id FROM comp%s AS c JOIN user_ AS u ON c.owner = u.legacy_id ", questionId, questionInfo.getQuestionnaireName()));
		String questionInfoLabel = questionInfo.getLabel();

		jdbcTemplate.update(String.format("INSERT INTO choice_answer(answer_id, choice_id) SELECT a.id, %s FROM answer AS a JOIN comp%s AS c ON c.no = a.legacy_id WHERE a.question_id = %s AND c.%s = 1", yesOptChoice, questionInfo.getQuestionnaireName(), questionId, questionInfoLabel));
		jdbcTemplate.update(String.format("INSERT INTO choice_answer(answer_id, choice_id) SELECT a.id, %s FROM answer AS a JOIN comp%s AS c ON c.no = a.legacy_id WHERE a.question_id = %s AND c.%s = 0", noOptChoice, questionInfo.getQuestionnaireName(), questionId, questionInfoLabel));
		return result;
	}

	private MigrationAnswer createAnswerInfo(Map<String, Object> row, QuestionInfo questionInfo, Map<String, ChoiceInfo> questionChoices)
	{
		Long owner = userIds.get(row.get("owner"));

		MigrationAnswer migrationAnswer = new MigrationAnswer();
		migrationAnswer.setUserId(owner);
		migrationAnswer.setLegacyId((String) row.get("no"));
		migrationAnswer.setCreateDate((Timestamp) row.get("createdate"));
		migrationAnswer.setChangeDate((Timestamp) row.get("changedate"));
		migrationAnswer.setQuestionId(questionInfo.getId());
		migrationAnswer.setDTYPE(questionInfo.getDTYPE().replace("Question", "Answer"));

		Object value = row.get(questionInfo.getLabel());

		switch (migrationAnswer.getDTYPE())
		{
			case "ChoiceAnswer":
				migrationAnswer.setChoices(AnswersMigrationHelper.getChoices((String) value, questionChoices));
				break;
			default:
				throw new IllegalStateException("invalid question DTYPE:" + migrationAnswer.getDTYPE());
		}
		return migrationAnswer;
	}

	private boolean isAffinity(QuestionInfo questionInfo)
	{
		return questionInfo.getDTYPE().equals("NumberQuestion")
				&& !questionInfo.getLabel().contains("alter")
				&& !questionInfo.getLabel().contains("groesse")
				&& !questionInfo.getLabel().contains("kinder")
				&& !questionInfo.getLabel().contains("gewicht");
	}
}
