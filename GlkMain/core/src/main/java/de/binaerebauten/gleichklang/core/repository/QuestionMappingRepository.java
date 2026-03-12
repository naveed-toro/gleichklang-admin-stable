package de.binaerebauten.gleichklang.core.repository;

import de.binaerebauten.gleichklang.core.model.IdKeyPairResult;
import de.binaerebauten.gleichklang.core.model.matching.AbstractQuestionsMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionMappingRepository extends JpaRepository<AbstractQuestionsMapping, Long> {

    @Query("SELECT new de.binaerebauten.gleichklang.core.model.IdKeyPair(qm.id, qm.naturalKey, TYPE(qm)) FROM AbstractQuestionsMapping qm")
    List<IdKeyPairResult> getAllAsIdKeyPair();

}
