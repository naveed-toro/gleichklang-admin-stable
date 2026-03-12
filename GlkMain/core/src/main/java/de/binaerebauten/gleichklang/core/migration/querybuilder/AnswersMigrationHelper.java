package de.binaerebauten.gleichklang.core.migration.querybuilder;

import com.google.common.base.Strings;
import de.binaerebauten.gleichklang.core.migration.model.MigrationAnswer;
import de.binaerebauten.gleichklang.core.migration.model.MigrationQuestion;
import de.binaerebauten.gleichklang.core.migration.queryinserter.ChoiceInfo;
import de.binaerebauten.gleichklang.core.migration.queryinserter.QuestionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by michael on 08/06/15.
 */
public class AnswersMigrationHelper
{

	private static final Logger LOG = LoggerFactory.getLogger(AnswersMigrationHelper.class);

	public static String getQuestionInfoQuery(MigrationQuestion.QUESTION_DTYPE dtype, String questionnaireName)
	{
		String dtypeString = "";
		if (dtype != null)
		{
			dtypeString = String.format("AND q1.DTYPE = '%s'", dtype.name());
		}

		return String.format(
				"SELECT q1.id, q1.label, q1.DTYPE, q1.selection_type, q1.questionnaire_id, q2.legacy_id AS questionnaire_name, q1.required "
						+ "FROM question q1 JOIN questionnaire q2 ON q1.questionnaire_id = q2.id "
						+ "WHERE q1.i18n_key NOT REGEXP '_plz_|_radius_|_land_|_region$' %s AND q2.legacy_id = '%s'"
						+ "ORDER BY questionnaire_id",
				dtypeString, questionnaireName);
	}


	/**
	 * Returns the answer value column for the given question.
	 */
	private static String getAnswerValueColumn(QuestionInfo questionInfo)
	{
		String dtype = questionInfo.getDTYPE();
		switch (dtype)
		{
			case "NumberQuestion":
				return "number_value";
			case "TextQuestion":
				return "text_value";
			default:
				throw new IllegalStateException("Can't handle question dtype '" + dtype + "'");
		}
	}


	public static String createInsertChoiceAnswerChoicesStatement()
	{
		return "INSERT INTO choice_answer (answer_id, choice_id) " +
				"VALUES (?, ?)";
	}

	public static BatchPreparedStatementSetter createInsertChoiceAnswerChoicesBatchSetter(JdbcTemplate jdbcTemplate, List<MigrationAnswer> migrationAnswers, String questionId)
	{
		setAnswerId(jdbcTemplate, migrationAnswers, questionId);

		List<Long[]> choices = migrationAnswers.stream()
				.flatMap(a -> a.getChoices().stream().map(c -> new Long[] { a.getId(), c }))
				.collect(Collectors.toList());

		LOG.info("Found {} choices", choices.size());

		return new BatchPreparedStatementSetter()
		{
			@Override
			public void setValues(PreparedStatement ps, int i)
					throws SQLException
			{
				ps.setLong(1, choices.get(i)[0]);
				ps.setLong(2, choices.get(i)[1]);
			}

			@Override
			public int getBatchSize()
			{
				return choices.size();
			}
		};
	}

	public static void setAnswerId(JdbcTemplate jdbcTemplate, List<MigrationAnswer> migrationAnswers, String questionId)
	{
		String query = "SELECT id, user_id  FROM answer WHERE question_id = " + questionId;
		List<Map<String, Object>> queryResult = jdbcTemplate.queryForList(query);

		LOG.info("Found {} answers ", queryResult.size());

		Map<Long, MigrationAnswer> userIdToAnswerId = migrationAnswers.stream()
				.collect(Collectors.toMap(MigrationAnswer::getUserId, Function.identity()));
		queryResult.forEach(row -> userIdToAnswerId.get(row.get("user_id")).setId((Long) row.get("id")));
	}

	public static List<String> getColumnNames(JdbcTemplate jdbcTemplate, String tableName, String tableSchema)
	{
		final String query = String.format("SELECT COLUMN_NAME FROM information_schema.COLUMNS "
				+ "WHERE TABLE_SCHEMA = '%s' "
				+ "  AND TABLE_NAME = '%s'", tableSchema, tableName);
		return jdbcTemplate.queryForList(query, String.class);
	}

	public static Map<String, Long> getUserIds(JdbcTemplate jdbcTemplate)
	{
		String query = "SELECT legacy_id, id FROM user_";
		Map<String, Long> resultMap = new HashMap<>();
		final List<Map<String, Object>> resultList = jdbcTemplate.queryForList(query);
		for (Map<String, Object> result : resultList)
		{
			resultMap.put((String) result.get("legacy_id"), (Long)result.get("id"));
		}
		return resultMap;
	}

	public static List<Long> getChoices(String value, Map<String, ChoiceInfo> questionChoices)
	{
		List<Long> choices = new ArrayList<>();
		if (value != null)
		{
			for (String choice : value.split(";"))
			{
				if (!Strings.isNullOrEmpty(choice))
				{
					ChoiceInfo choiceInfo = questionChoices.get(choice);
					if (choiceInfo != null)
					{
						choices.add(choiceInfo.getId());
					}
				}
			}
		}
		return choices;
	}

	public static Map<String, ChoiceInfo> getQuestionChoices(JdbcTemplate jdbcTemplate, QuestionInfo questionInfo)
	{
		String query = "SELECT c.id, c.choice FROM choice AS c JOIN choice_group AS cg ON c.choice_group_id = cg.id JOIN question AS q ON cg.id = q.choice_group_id WHERE q.id =  " + questionInfo.getId();
		final List<ChoiceInfo> choices = jdbcTemplate.query(query, new BeanPropertyRowMapper<>(ChoiceInfo.class));

		return choices.stream().collect(Collectors.toMap(ChoiceInfo::getChoice, Function.identity()));
	}

}
