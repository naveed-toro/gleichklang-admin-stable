package de.binaerebauten.gleichklang.core.repository;

import de.binaerebauten.gleichklang.core.model.IdKeyPairResult;
import de.binaerebauten.gleichklang.core.model.matching.Activator;
import de.binaerebauten.gleichklang.core.model.questionnaire.Question;
import de.binaerebauten.gleichklang.core.model.questionnaire.QuestionGroup;
import de.binaerebauten.gleichklang.core.model.questionnaire.Questionnaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivatorRepository extends JpaRepository<Activator, Long> {

    @Query("SELECT NEW de.binaerebauten.gleichklang.core.model.IdKeyPair(a.id, a.naturalKey, TYPE(a)) FROM Activator a")
    List<IdKeyPairResult> getAllAsIdKeyPair();
	
	@Query("SELECT COUNT(a) > 0 FROM QuestionActivator a WHERE a.enablesQuestion = ?1")
	boolean existsEnablesQuestion(Question question);
	
	@Query("SELECT COUNT(a) > 0 FROM QuestionGroupActivator a WHERE a.enablesQuestionGroup = ?1")
	boolean existsEnablesQuestionGroup(QuestionGroup questionGroup);
	
	@Query("SELECT COUNT(a) > 0 FROM QuestionnaireActivator a WHERE a.enablesQuestionnaire = ?1")
	boolean existsEnablesQuestionnaire(Questionnaire questionnaire);
}
