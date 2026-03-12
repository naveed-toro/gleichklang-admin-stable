package de.binaerebauten.gleichklang.core.repository;

import de.binaerebauten.gleichklang.core.model.questionnaire.Answer;
import de.binaerebauten.gleichklang.core.model.questionnaire.Question;
import de.binaerebauten.gleichklang.core.model.questionnaire.Questionnaire;
import de.binaerebauten.gleichklang.core.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

/**
 * TODO Refactoring: Hier scheinen viele Methoden veraltet / falsch / doppelt oder unnötig zu sein!
 * Repository for {@link Answer} entities.
 */
@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long>
{
	String ANSWERED_NUMBER_ANSWER = "Type(a) = 'NumberAnswer' AND a.numberValue IS NOT NULL ";
	String VALID_NUMBER_ANSWER = "((a.numberValue >= a.question.minVal AND a.numberValue <= a.question.maxVal) OR (a.question.maxVal = 0 AND a.question.minVal = 0)) ";
	String ANSWERED_TEXT_ANSWER = "(TYPE(a) = 'TextAnswer' AND a.textValue IS NOT NULL AND a.textValue <> '') ";
	String ANSWERED_CHOICE_ANSWER = "(Type(a) = 'ChoiceAnswer' AND SIZE(a.choices) > 0) ";
	String ANSWERED_REGION_ANSWER = "(Type(a) IN ('RegionAnswer', 'PartnerRegionAnswer')) ";
	String ANSWERED_AND_VALID_ANSWERS = "(SELECT a FROM Answer a WHERE a.question = q AND a.user = :user "
			+ "AND ("
			+ ANSWERED_TEXT_ANSWER
			+ "OR "
			+ ANSWERED_NUMBER_ANSWER + "AND " + VALID_NUMBER_ANSWER
			+ "OR "
			+ ANSWERED_CHOICE_ANSWER
			+ "OR "
			+ ANSWERED_REGION_ANSWER
			+ "))";

	/**
	 * Finds the answer for the given user and user.
	 *
	 * @param user
	 * @param question
	 * @return the answer for the given user and question or null if it doesn't exist
	 */
	Answer findByUserAndQuestion(User user, Question question);

	/**
	 * Finds the answers for the given user and questions.
	 *
	 * @param user
	 * @param questions
	 * @return the answers sorted by their {@link Question#sortOrder} for the given user
	 *     and questions
	 */
	@Query("SELECT a FROM Answer a JOIN a.question q WHERE "
			+ "a.user = :user AND "
			+ "q IN :questions "
			+ "ORDER BY q.sortOrder")
	List<Answer> findAnswersForUser(@Param("user") User user,
			@Param("questions") List<? extends Question> questions);

	/**
	 * Counts the answers of the given user.
	 * @param user
	 * @return the number of answers of the given user
	 */
	int countByUser(User user);

	/**
	 * Counts the unanswered questions of the given user.
	 *
	 * @param user
	 * @param questions
	 * @return the number of unanswered questions
	 */
	@Query("SELECT COUNT(q) FROM Question q "
			+ "WHERE "
			+ "q IN (:questions) AND "
			+ "NOT EXISTS (SELECT a FROM Answer a JOIN a.question aq WHERE a.user = :user AND aq = q)")
	int countUnansweredQuestions(@Param("user") User user,
			@Param("questions") List<Question> questions);

	/**
	 * Returns the questionnaires with unanswered questions for the given user.
	 *
	 * @param user
	 * @param questions
	 * @return the ordered unanswered questionnaires for the given user and questions
	 */
	@Query("SELECT DISTINCT qn FROM Question q JOIN q.questionGroup qg JOIN qg.questionnaire qn "
			+ "WHERE "
			+ "q IN (:questions) AND "
			+ "NOT EXISTS (SELECT a FROM Answer a JOIN a.question aq WHERE a.user = :user AND aq = q) "
			+ "ORDER BY qn.sortOrder")
	List<Questionnaire> getUnansweredQuestionnaires(@Param("user") User user,
			@Param("questions") List<Question> questions);

	/**
	 * Returns the unanswered questions for the given user.
	 *
	 * @param user
	 * @param questions
	 * @return the unanswered questions of the given user
	 */
	@Query("SELECT q FROM Question q "
			+ "WHERE "
			+ "q IN (:questions) AND "
			+ "NOT EXISTS (SELECT a FROM Answer a JOIN a.question aq WHERE a.user = :user AND aq = q)")
	List<Question> getUnansweredQuestions(@Param("user") User user,
			@Param("questions") List<Question> questions);

	/**
	 * Counts the incomplete or invalid answers of the given user.
	 *
	 * @param user
	 * @param questions
	 * @return the number of incomplete or invalid answers of the given user
	 */
	@Query("SELECT COUNT(a) FROM Answer a JOIN a.question q JOIN q.questionGroup qg "
			+ "WHERE "
			+ "a.user = :user AND "
			+ "q IN (:questions) "+ "AND "
			+ "(NOT EXISTS " + ANSWERED_AND_VALID_ANSWERS + ")")
	int countIncompleteAndInvalidAnswers(@Param("user") User user,
			@Param("questions") List<Question> questions);
	
	/**
	 * Counts the complete and valid answers of the given user.
	 *
	 * @param userId
	 * @param questions
	 * @return the number of complete and valid answers of the given user
	 */
	@Query("SELECT COUNT(a) FROM Answer a WHERE "
			+ "a.question IN (:questions) AND a.user.id = :user "
			+ "AND ("
			+ ANSWERED_TEXT_ANSWER
			+ "OR "
			+ ANSWERED_NUMBER_ANSWER
			+ "OR "
			+ ANSWERED_CHOICE_ANSWER
			+ "OR "
			+ ANSWERED_REGION_ANSWER
			+ ")")
	int countCompleteAnswers(@Param("user") Long userId, @Param("questions") Collection<Question> questions);

	/**
	 * Returns the questionnaires with incomplete or invalid answers for the given user.
	 *
	 * @param user
	 * @param questions
	 * @return the ordered incompleted or invalid questionnaires for the given user and questions
	 */
	@Query("SELECT DISTINCT qn FROM Question q JOIN q.questionGroup qg JOIN qg.questionnaire qn "
			+ "WHERE "
			+ "q IN (:questions) "+ "AND "
			+ "(NOT EXISTS " + ANSWERED_AND_VALID_ANSWERS + ") "
			+ "ORDER BY qn.sortOrder")
	List<Questionnaire> getIncompleteAndInvalidQuestionnaires(@Param("user") User user,
			@Param("questions") List<Question> questions);

	/**
	 * Returns the incomplete or invalid answers of the given user.
	 * @param user
	 * @param questions
	 * @return the incomplete and invalid answers for the given user
	 */
	@Query("SELECT a FROM Answer a JOIN a.question q JOIN q.questionGroup qg "
			+ "WHERE "
			+ "a.user = :user AND "
			+ "q IN (:questions) "+ "AND "
			+ "(NOT EXISTS " + ANSWERED_AND_VALID_ANSWERS + ")")
	List<Answer> getIncompleteAndInvalidAnswers(@Param("user") User user,
			@Param("questions") List<Question> questions);

	/**
	 * Calculates the mean and population standard deviation over all users for a given questionnaire.
	 *
	 * Mean and population standard deviation are force casted to DECIMAL so Hibernate will convert this to {@link java.math.BigDecimal}.
	 * Otherwise, it would depend of the calculation.
	 *
	 * @param naturalKey predefined questionnaire key
	 * @return mean and population standard deviation of all users for each question group in the questionnaire
	 */
	@Query(
			value = "SELECT CAST(avg(sq.summe) AS DECIMAL(5,3)) AS mean , CAST(STDDEV_POP(sq.summe) AS DECIMAL(6,4)) AS standardDeviation , sq.kategorie as category " +
					"FROM( SELECT sum(CASE question.representation_type WHEN 'AFFINITY_INVERTED' THEN ((4 - (choice.sort_order)) + 1) ELSE (choice.sort_order + 1) END) AS summe , question_group.i18n_key AS kategorie , answer.user_id " +
					"FROM questionnaire " +
					"LEFT JOIN question_group ON question_group.questionnaire_id = questionnaire.id " +
					"LEFT JOIN question ON question.question_group_id = question_group.id AND question.deleted = false AND (representation_type = 'AFFINITY' OR representation_type = 'AFFINITY_INVERTED') " +
					"LEFT JOIN answer ON answer.question_id = question.id " +
					"LEFT JOIN choice_answer ON choice_answer.answer_id = answer.id " +
					"LEFT JOIN choice ON choice.id = choice_answer.choice_id " +
					"WHERE questionnaire.i18n_key = ?1 GROUP BY question_group.id , answer.user_id) sq GROUP BY sq.kategorie",
			nativeQuery = true)
	List<Object[]> getAggregatedAffinityResultsForKey(String naturalKey);

	/**
	 * Calculates the sum of sum choice questions for the users answers for a given questionnaire.
	 *
	 * @param userid the user id
	 * @param naturalKey predefined questionnaire key
	 * @return sum of users answers for each question group in the questionnaire
	 */
    @Query(
            value = "SELECT CAST(sum(CASE question.representation_type WHEN 'AFFINITY_INVERTED' THEN ((4 - (choice.sort_order)) + 1) ELSE (choice.sort_order + 1) END) AS DECIMAL(5,3)) AS summe, question_group.i18n_key AS kategorie " +
                    "FROM questionnaire " +
                    "LEFT JOIN question_group ON question_group.questionnaire_id = questionnaire.id " +
                    "LEFT JOIN question ON question.question_group_id = question_group.id AND question.deleted = false AND (representation_type = 'AFFINITY' OR representation_type = 'AFFINITY_INVERTED') " +
                    "LEFT JOIN answer ON answer.question_id = question.id " +
                    "LEFT JOIN choice_answer ON choice_answer.answer_id = answer.id " +
                    "LEFT JOIN choice ON choice.id = choice_answer.choice_id " +
                    "WHERE answer.user_id = ?1 AND questionnaire.i18n_key = ?2 GROUP BY question_group.id , answer.user_id",
            nativeQuery = true)
	List<Object[]> getAggregatedAffinityUserResultsForKey(Long userid, String naturalKey);
	
	List<Answer> findByUser(User user);

	@Query(value=" SELECT  Distinct c.i18n_key FROM answer a  " +
			" left outer join choice_answer ca on ca.answer_id=a.id " +
			" left outer join choice c on ca.choice_id=c.id " +
			" inner join question q on a.question_id=q.id " +
			" where q.DTYPE='ChoiceQuestion' and a.user_id = ?1 and a.question_id = ?2 and c.id = ?3 ",
			nativeQuery = true)

	Object getChoiceAnswersForUserDataExportFromAdmin(Long userid,Long qid,Long cid);
	@Query(value=" SELECT  Distinct CASE WHEN q.DTYPE='TextQuestion' THEN a.text_value WHEN q.DTYPE='NumberQuestion' THEN number_value ELSE q.i18n_key END FROM answer a  " +
			" inner join question q on a.question_id=q.id " +
			" where a.user_id = ?1 and a.question_id = ?2 ",
			nativeQuery = true)
	Object getAnswersForUserDataExportFromAdmin(Long userid,Long qid);

	@Query(value=" SELECT lt.i18n_key FROM locatable lt " +
			"inner join region_search_request_restriction rsrr on lt.id=rsrr.locatable_id " +
			"inner join region_search_request rsr on rsr.id=rsrr.region_search_request_id " +
			"inner join answer a on a.id=rsr.answer_id " +
			"inner join question q on a.question_id=q.id " +
			"where a.user_id=?1 and q.DTYPE='RegionQuestion' and q.id=?2",
			nativeQuery = true)

	List<Object> getRegionAnswersForUserDataExportFromAdmin(Long userid,Long qid);

	@Query(value=" SELECT DISTINCT CONCAT(IFNULL(i18.i18n_value,''),':',IFNULL(i181.i18n_value,''),':',IFNULL(lt1.region_name,'')) FROM answer a " +
			"INNER JOIN region_search_request rsr ON a.id = rsr.answer_id " +
			"INNER JOIN locatable lt ON rsr.continent_id = lt.id " +
			"INNER JOIN i18n i18 ON lt.i18n_key = i18.i18n_key " +
			"LEFT OUTER JOIN region_search_request_restriction rsrr ON rsr.id = rsrr.region_search_request_id " +
			"LEFT OUTER JOIN locatable lt1 ON rsrr.locatable_id = lt1.id " +
			"LEFT OUTER JOIN locatable lt2 ON rsr.country_id = lt2.id " +
			"LEFT OUTER JOIN i18n i181 ON lt2.i18n_key = i181.i18n_key " +
			"WHERE  a.user_id = ?1 AND a.DTYPE = 'RegionAnswer' AND i18.base_name = 'CONTINENT' AND i18.language=?2",
			nativeQuery = true)
	List<Object> getRegionsHeadersAnswersByUser(Long userid,String locale);

	@Query(value=" SELECT DISTINCT psr.distance,psr.restrict_country,lt.zip,psr.center_zip_id FROM answer a " +
				 "INNER JOIN proximity_search_request psr ON a.id = psr.answer_id " +
			     "INNER JOIN locatable lt on lt.id=psr.center_zip_id " +
				 "WHERE  a.user_id = ?1 AND a.DTYPE = 'RegionAnswer'",
				  nativeQuery = true)
	List<Object> getProximityHeadersAndAnswersByUser(Long userid);

	/*@Modifying
	@Transactional
	void deleteByUser(@Param("user") User user);*/
}
