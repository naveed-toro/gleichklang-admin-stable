package de.binaerebauten.gleichklang.core.repository;

import de.binaerebauten.gleichklang.core.model.IdKeyPairResult;
import de.binaerebauten.gleichklang.core.model.questionnaire.ChoiceGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ChoiceGroupRepository extends JpaRepository<ChoiceGroup, Long>, DeleteRepository<ChoiceGroup, Long>, JpaSpecificationExecutor<ChoiceGroup>
{
    @Query("SELECT NEW de.binaerebauten.gleichklang.core.model.IdKeyPair(cg.id, cg.i18nKey) FROM ChoiceGroup cg")
    List<IdKeyPairResult> getAllAsIdKeyPair();
}
