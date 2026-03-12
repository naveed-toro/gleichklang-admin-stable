package de.binaerebauten.gleichklang.core.repository;

import de.binaerebauten.gleichklang.core.model.IdKeyPairResult;
import de.binaerebauten.gleichklang.core.model.questionnaire.Question.Requirement;
import de.binaerebauten.gleichklang.core.model.questionnaire.QuestionGroup;
import de.binaerebauten.gleichklang.core.model.questionnaire.Questionnaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface QuestionGroupRepository extends JpaRepository<QuestionGroup, Long>, JpaSpecificationExecutor<QuestionGroup>, DeleteRepository<QuestionGroup, Long>, SortRepository<QuestionGroup, Long>, NaturalKeyRepository<QuestionGroup, Long>
{

	@Query("SELECT qg FROM QuestionGroup as qg JOIN qg.questionnaire as q WHERE q = ?1 AND qg.deleted = FALSE ORDER BY qg.sortOrder")
	List<QuestionGroup> findActiveByQuestionnaire(Questionnaire questionnaire);

    List<QuestionGroup> findByI18nKeyIn(Collection<String> keys);

	@Query("SELECT NEW de.binaerebauten.gleichklang.core.model.IdKeyPair(qg.id, qg.i18nKey) FROM QuestionGroup qg")
	List<IdKeyPairResult> getAllAsIdKeyPair();

	@Query("FROM QuestionGroup qg WHERE qg.deleted = FALSE AND qg.questionnaire.deleted = FALSE")
	List<QuestionGroup> findAllNotDeleted();

	/**
	 * Returns max sort order for a given questionnaire if not null. Otherwise 0.
	 *
	 * @param questionnaire the questionnaire
	 * @return max sort oder or 0.
	 */
	@Query("SELECT COALESCE(MAX(qg.sortOrder), 0) FROM QuestionGroup qg WHERE qg.questionnaire = ?1")
	Integer getMaxSortOrderForQuestionnaire(Questionnaire questionnaire);

	@Query("SELECT qg FROM QuestionGroup qg WHERE qg.questionnaire = ?1 AND qg.sortOrder > 0 ORDER BY qg.sortOrder ASC")
	List<QuestionGroup> getAllQForQuestionnaireWithPositiveSortOrder(Questionnaire questionnaire);
	
	// TODO FH rename function or remove and filter in java
	@Query("SELECT qg FROM QuestionGroup qg JOIN qg.questionnaire q WHERE "
			+ "qg.deleted = false AND q.deleted = false AND "
			+ "NOT EXISTS(FROM Question q1 WHERE q1.questionGroup = qg AND q1.requirement NOT IN ?1 AND q1.deleted = false AND q1.onlyAdminVisible = false) AND "
			+ "EXISTS(FROM Question q1 WHERE q1.questionGroup = qg AND q1.requirement IN ?1 AND q1.deleted = false AND q1.onlyAdminVisible = false) "
			+ "ORDER BY qg.sortOrder")
	List<QuestionGroup> findAllQuestionGroupsForRequirement(Collection<Requirement> requirements);
}
