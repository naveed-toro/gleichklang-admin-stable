package de.binaerebauten.gleichklang.core.migration;

import com.google.common.base.Strings;
import org.flywaydb.core.api.migration.spring.SpringJdbcMigration;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class V03_74__Migrate_Cancel_Reason_After_Cancel_Source implements SpringJdbcMigration
{
	private static final int BATCH_SIZE = 50;
	
	private class InsertArguments<T, U>
	{
		private final T arg1;
		private final U arg2;
		
		public InsertArguments(T arg1, U arg2)
		{
			this.arg1 = arg1;
			this.arg2 = arg2;
		}
	}
	
	@Override
	public void migrate(JdbcTemplate jdbcTemplate) throws Exception
	{
		migrateSourcePage(jdbcTemplate);
		migrateCancelReason(jdbcTemplate);
		migrateAfterCancel(jdbcTemplate);
	}
	
	private void migrateAfterCancel(JdbcTemplate jdbcTemplate)
	{
		final List<InsertArguments<String, Long>> insertArguments = new ArrayList<>(BATCH_SIZE);
		
		final Map<String, String> afterCancelMap = getAfterCancelMap();
		
		final SqlRowSet rowSet = jdbcTemplate.queryForRowSet("SELECT ca.signoff_followups, u.id FROM compuseractivity ca JOIN user_ u ON ca.user = u.legacy_id WHERE signoff_followups IS NOT NULL");
		while (rowSet.next())
		{
			final Long userId = rowSet.getLong("id");
			final String followups = rowSet.getString("signoff_followups");
			
				for (String followup : followups.split(";"))
				{
					if(!Strings.isNullOrEmpty(followup))
					{
						final String afterCancel = afterCancelMap.get(followup);
						
						insertArguments.add(new InsertArguments<>(afterCancel, userId));
						if(insertArguments.size() >= BATCH_SIZE) insertAfterCancels(jdbcTemplate, insertArguments);
					}
				}
		}
		
		if(insertArguments.size() > 0) insertAfterCancels(jdbcTemplate, insertArguments);
	}
	
	private void migrateCancelReason(JdbcTemplate jdbcTemplate)
	{
		final List<InsertArguments<String, Long>> insertArguments = new ArrayList<>(BATCH_SIZE);
		
		final Map<String, String> cancelReasonMap = getCancelReasonsMap();
		
		final SqlRowSet rowSet = jdbcTemplate.queryForRowSet("SELECT ca.signoff_reason, u.id FROM compuseractivity ca JOIN user_ u ON ca.user = u.legacy_id WHERE signoff_reason IS NOT NULL");
		while (rowSet.next())
		{
			final Long userId = rowSet.getLong("id");
			final String reasons = rowSet.getString("signoff_reason");
			
			for (String reason : reasons.split(";"))
			{
				if(!Strings.isNullOrEmpty(reason))
				{
					final String cancelReason = cancelReasonMap.get(reason);
					
					insertArguments.add(new InsertArguments<>(cancelReason, userId));
					if(insertArguments.size() >= BATCH_SIZE) insertCancelReasons(jdbcTemplate, insertArguments);
				}
			}
		}
		
		if(insertArguments.size() > 0) insertCancelReasons(jdbcTemplate, insertArguments);
	}
	
	private void migrateSourcePage(JdbcTemplate jdbcTemplate)
	{
		final List<InsertArguments<Long, Long>> insertArguments = new ArrayList<>(BATCH_SIZE);
		
		jdbcTemplate.execute("CALL ADD_COLUMN('user_settings', 'source_page', 'VARCHAR(255)');");
		
		createAnswers(jdbcTemplate);
		
		final Map<Long, Long> answerMap = getAnswers(jdbcTemplate);
		final Map<String, Long> choicesMap = getChoices(jdbcTemplate);
		
		final SqlRowSet rowSet = jdbcTemplate.queryForRowSet("SELECT source_page, user_id FROM user_settings WHERE source_page IS NOT NULL");
		while (rowSet.next())
		{
			final Long user_id = rowSet.getLong("user_id");
			final String source_page = rowSet.getString("source_page");
			
			for (String source : source_page.split(";"))
				{
					if(!Strings.isNullOrEmpty(source))
					{
						final Long choiceId = choicesMap.get(source.toLowerCase());
						
						insertArguments.add(new InsertArguments<>(answerMap.get(user_id), choiceId));
						if(insertArguments.size() >= BATCH_SIZE) insertChoiceAnswers(jdbcTemplate, insertArguments);
					}
				}
		}
		
		if(insertArguments.size() > 0) insertChoiceAnswers(jdbcTemplate, insertArguments);
		
		jdbcTemplate.execute("CALL DROP_COLUMN('user_settings', 'source_page');");
	}
	
	private void insertCancelReasons(JdbcTemplate jdbcTemplate, List<InsertArguments<String, Long>> insertArguments)
	{
		if(insertArguments.isEmpty()) return;
		
		final String insertQuery = "INSERT IGNORE INTO cancel_reason (cancelReasons, user_id) VALUES (?, ?)";
		jdbcTemplate.batchUpdate(insertQuery, createStringLongBatchSetter(insertArguments));
		insertArguments.clear();
	}
	
	private void insertAfterCancels(JdbcTemplate jdbcTemplate, List<InsertArguments<String, Long>> insertArguments)
	{
		if(insertArguments.isEmpty()) return;
		
		final String insertQuery = "INSERT IGNORE INTO after_cancel (afterCancel, user_id) VALUES (?, ?)";
		jdbcTemplate.batchUpdate(insertQuery, createStringLongBatchSetter(insertArguments));
		insertArguments.clear();
	}
	
	private void insertChoiceAnswers(JdbcTemplate jdbcTemplate, List<InsertArguments<Long, Long>> insertArguments)
	{
		if(insertArguments.isEmpty()) return;
		
		final String insertQuery = "INSERT IGNORE INTO choice_answer (answer_id, choice_id) VALUES (?, ?)";
		jdbcTemplate.batchUpdate(insertQuery, createLongLongBatchSetter(insertArguments));
		insertArguments.clear();
	}
	
	private BatchPreparedStatementSetter createLongLongBatchSetter(List<InsertArguments<Long, Long>> insertArguments)
	{
		return new BatchPreparedStatementSetter()
		{
			@Override
			public void setValues(PreparedStatement ps, int i)
					throws SQLException
			{
				ps.setLong(1, insertArguments.get(i).arg1);
				ps.setLong(2, insertArguments.get(i).arg2);
			}
			
			@Override
			public int getBatchSize()
			{
				return insertArguments.size();
			}
		};
	}
	
	private BatchPreparedStatementSetter createStringLongBatchSetter(List<InsertArguments<String, Long>> insertArguments)
	{
		return new BatchPreparedStatementSetter()
		{
			@Override
			public void setValues(PreparedStatement ps, int i)
					throws SQLException
			{
				ps.setString(1, insertArguments.get(i).arg1);
				ps.setLong(2, insertArguments.get(i).arg2);
			}
			
			@Override
			public int getBatchSize()
			{
				return insertArguments.size();
			}
		};
	}
	
	private Map<String, Long> getChoices(JdbcTemplate jdbcTemplate)
	{
		final Map<String, Long> choicesMap = new HashMap<>();
		
		final SqlRowSet choices = jdbcTemplate.queryForRowSet("SELECT id, i18n_key FROM choice WHERE choice_group_id = (SELECT choice_group_id FROM question WHERE i18n_key = 'source');");
		while (choices.next())
		{
			final Long id = choices.getLong("id");
			final String i18n_key = choices.getString("i18n_key").split("quellen_")[1].toLowerCase();
			
			choicesMap.put(i18n_key, id);
		}
		
		return choicesMap;
	}
	
	private Map<Long, Long> getAnswers(JdbcTemplate jdbcTemplate)
	{
		final Map<Long, Long> answerMap = new HashMap<>();
		
		final SqlRowSet answers = jdbcTemplate.queryForRowSet("SELECT a.id, a.user_id FROM answer a LEFT JOIN question q ON q.id = a.question_id WHERE q.i18n_key = 'source_text';");
		while (answers.next())
		{
			final Long id = answers.getLong("id");
			final Long userId = answers.getLong("user_id");
			
			answerMap.put(userId, id);
		}
		
		return answerMap;
	}
	
	private void createAnswers(JdbcTemplate jdbcTemplate)
	{
		String insertQuery = "";
		insertQuery += "INSERT IGNORE INTO answer (DTYPE, create_date, question_id, user_id) ";
		insertQuery += "SELECT ";
		insertQuery += "		'ChoiceAnswer', ";
		insertQuery += "		NOW(), ";
		insertQuery += "		q.id, ";
		insertQuery += "		us.user_id ";
		insertQuery += "FROM ";
		insertQuery += "user_settings us LEFT JOIN question q ON q.i18n_key = 'source_text';";
		
		jdbcTemplate.execute(insertQuery);
	}
	
	private Map<String, String> getCancelReasonsMap()
	{
		final Map<String, String> cancelReasonsMap = new HashMap<>();
		cancelReasonsMap.put("other", "OTHER");
		cancelReasonsMap.put("matches_size", "TOO_LESS_RECOMMENDATIONS");
		cancelReasonsMap.put("matches_compatibility", "UNSUITABLE_RECOMMENDATIONS");
		cancelReasonsMap.put("service", "UNHAPPY_WITH_SERVICE");
		cancelReasonsMap.put("service_found_external", "SUCCESS_ELSEWHERE");
		cancelReasonsMap.put("service_no_time", "NO_TIME");
		cancelReasonsMap.put("service_found_interal", "SUCCESS_THROW_GK");
		
		return cancelReasonsMap;
	}
	
	private Map<String, String> getAfterCancelMap()
	{
		final Map<String, String> afterCancelMap = new HashMap<>();
		afterCancelMap.put("research", "ALLOWED_TO_CONTACT");
		afterCancelMap.put("publication", "WANT_TO_REPORT");
		afterCancelMap.put("information", "SUBSCRIBE_TO_NEWSLETTER");
		
		return afterCancelMap;
	}
}
