package de.binaerebauten.gleichklang.core.repository;

import de.binaerebauten.gleichklang.core.model.IdKeyPairResult;
import de.binaerebauten.gleichklang.core.model.questionnaire.ChoiceQuestion;
import de.binaerebauten.gleichklang.core.model.questionnaire.Question;
import de.binaerebauten.gleichklang.core.model.questionnaire.Question.Requirement;
import de.binaerebauten.gleichklang.core.model.questionnaire.QuestionGroup;
import de.binaerebauten.gleichklang.core.model.questionnaire.Questionnaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Repository for {@link Question} entities.
 */
@Repository
public interface QuestionRepository extends JpaRepository<Question, Long>, SortRepository<Question, Long>, DeleteRepository<Question, Long>, JpaSpecificationExecutor<Question>, NaturalKeyRepository<Question, Long>
{
	String ACTIVE_QUESTIONS = "q.deleted = FALSE AND q.onlyAdminVisible = FALSE AND qg.deleted = FALSE ";
	
	@Query("SELECT q FROM Question q "
			+ "WHERE q.questionGroup IN :questionGroups "
			+ "AND q.deleted = FALSE "
			+ "ORDER BY q.sortOrder")
	List<Question> findActiveQuestionsForQuestionGroupsWithAdminVisible(@Param("questionGroups") Collection<QuestionGroup> questionGroups);
	
	@Query("SELECT q FROM Question q "
			+ "WHERE q.questionGroup IN :questionGroups "
			+ "AND q.deleted = FALSE "
			+ "AND q.onlyAdminVisible = FALSE "
			+ "ORDER BY q.sortOrder")
	List<Question> findActiveQuestionsForQuestionGroups(@Param("questionGroups") Collection<QuestionGroup> questionGroups);

	@Query("SELECT q FROM Question q "
			+ "WHERE q.questionGroup = ?1 "
			+ "AND q.deleted = FALSE "
			+ "AND q.onlyAdminVisible = FALSE "
			+ "ORDER BY q.sortOrder")
	List<Question> findActiveQuestionsForQuestionGroup(QuestionGroup questionGroups);
	
	default List<Question> findActiveQuestionsForQuestionnaire(Questionnaire questionnaire, Collection<Requirement> requirements)
	{
		return findActiveQuestionsForQuestionnaires(Collections.singletonList(questionnaire), requirements);
	}
	
	@Query("SELECT q FROM Question q JOIN q.questionGroup qg JOIN qg.questionnaire q3 "
			+ "WHERE q3 IN :questionnaires AND "
			+ ACTIVE_QUESTIONS
			+ " AND q.requirement IN :requirements "
			+ "ORDER BY q.sortOrder")
	List<Question> findActiveQuestionsForQuestionnaires(@Param("questionnaires") List<Questionnaire> questionnaires, @Param("requirements") Collection<Requirement> requirements);
	
	@Query("SELECT q FROM Question q JOIN q.questionGroup qg JOIN qg.questionnaire q3 "
			+ "WHERE q3 IN :questionnaires AND "
			+ ACTIVE_QUESTIONS
			+ "ORDER BY q.sortOrder")
	List<Question> findActiveQuestionsForQuestionnaires(@Param("questionnaires") List<Questionnaire> questionnaires);
	
	@Query("SELECT q FROM Question q WHERE TYPE(q) = :type AND q.deleted = false ORDER BY q.sortOrder")
	<T extends Question> List<T> findByType(@Param("type") Class<T> type);
	
	@Query("FROM Question q WHERE q.deleted = FALSE AND q.questionGroup.deleted = FALSE AND q.questionGroup.questionnaire.deleted = FALSE")
	List<Question> findAllNotDeleted();
	
	@Query("SELECT NEW de.binaerebauten.gleichklang.core.model.IdKeyPair(q.id, q.i18nKey) FROM Question q")
	List<IdKeyPairResult> getAllAsIdKeyPair();
	
	/**
	 * Returns max sort order for a given question group.
	 *
	 * @param questionGroup the quetion group
	 * @return max sort order or 0 for empty list
	 */
	@Query("SELECT COALESCE(MAX(q.sortOrder), 0) FROM Question q WHERE q.questionGroup = ?1")
	Integer getMaxSortOrderForQuestionGroup(QuestionGroup questionGroup);
	
	@Query("SELECT q FROM Question q WHERE q.questionGroup = ?1 AND q.sortOrder > 0 ORDER BY q.sortOrder ASC")
	List<Question> getAllForQuestionGroupWithPositiveSortOrder(QuestionGroup questionGroup);
	
	// TODO FH rename function or remove and filter in java
	@Query("SELECT q FROM Question q JOIN q.questionGroup qg JOIN qg.questionnaire q3 WHERE "
			+ "q.deleted = false AND qg.deleted = false AND q3.deleted = false AND "
			+ "q.onlyAdminVisible = false AND q.requirement IN ?1 "
			+ "ORDER BY q.sortOrder")
	List<Question> findAllQuestionsForRequirement(Collection<Requirement> requirements);
	
	@Query("SELECT q FROM ChoiceQuestion q WHERE q.deleted = false AND q.onlyAdminVisible = false ORDER BY q.sortOrder")
	List<ChoiceQuestion> findAllActivatingQuestions();
}
