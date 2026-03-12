package de.binaerebauten.gleichklang.core.repository;

import de.binaerebauten.gleichklang.core.model.IdKeyPairResult;
import de.binaerebauten.gleichklang.core.model.questionnaire.Question.Requirement;
import de.binaerebauten.gleichklang.core.model.questionnaire.Questionnaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface QuestionnaireRepository extends JpaRepository<Questionnaire, Long>, JpaSpecificationExecutor<Questionnaire>, SortRepository<Questionnaire, Long>, DeleteRepository<Questionnaire, Long>, NaturalKeyRepository<Questionnaire, Long> {

    @Query("SELECT NEW de.binaerebauten.gleichklang.core.model.IdKeyPair(q.id, q.i18nKey) FROM Questionnaire q")
    List<IdKeyPairResult> getAllAsIdKeyPair();

    @Query("SELECT COALESCE(MAX(q.sortOrder), 0) FROM Questionnaire q")
    Integer getMaxSortOrder();

    @Query("SELECT q FROM Questionnaire q WHERE q.sortOrder > 0 ORDER BY q.sortOrder ASC")
    List<Questionnaire> getAllWithPositivSortOrder();
	
	// TODO FH rename function or remove and filter in java
	@Query("SELECT q FROM Questionnaire q WHERE "
			+ "q.deleted = false AND "
			+ "NOT EXISTS(FROM Question q1 JOIN q1.questionGroup qg WHERE qg.questionnaire = q AND qg.deleted = false AND q1.requirement NOT IN ?1 AND q1.deleted = false AND q1.onlyAdminVisible = false) AND "
			+ "EXISTS(FROM Question q1 JOIN q1.questionGroup qg WHERE qg.questionnaire = q AND qg.deleted = false AND q1.requirement IN ?1 AND q1.deleted = false AND q1.onlyAdminVisible = false) "
			+ "ORDER BY q.sortOrder")
	List<Questionnaire> findAllQuestionnaireForRequirement(Collection<Requirement> requirements);

	@Query(value="SELECT Distinct CASE WHEN q.DTYPE='TextQuestion' THEN CONCAT('TextQuestion','#',qn.i18n_key,':',qg.i18n_key,':',q.i18n_key,'::',q.id) WHEN q.DTYPE='ChoiceQuestion' THEN CONCAT('ChoiceQuestion',':',q.choice_group_id,'#',qn.i18n_key,':',qg.i18n_key,':',q.i18n_key,':',c.i18n_key,'::',q.id,',',c.id) WHEN q.DTYPE='NumberQuestion' THEN CONCAT('NumberQuestion','#',qn.i18n_key,':',qg.i18n_key,':',q.i18n_key,'::',q.id) END  " +
			" FROM questionnaire qn " +
			" inner join question_group qg on  qn.id=qg.questionnaire_id " +
			" inner join question q on  q.question_group_id=qg.id " +
			" left outer join choice_group cg on cg.id=q.choice_group_id " +
			" left outer join choice c on c.choice_group_id=cg.id where qn.id IN (?1) ",
			nativeQuery = true)
	List<Object> getHeadersForUserDataExportFromAdmin(List<Long> qids);
}
