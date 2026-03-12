package de.binaerebauten.gleichklang.core.repository;

import de.binaerebauten.gleichklang.core.model.IdKeyPairResult;
import de.binaerebauten.gleichklang.core.model.questionnaire.Choice;
import de.binaerebauten.gleichklang.core.model.questionnaire.ChoiceGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

/**
 * Project: serialization
 * Created by Domi on 27.05.2016.
 */
@Repository
public interface ChoiceRepository extends JpaRepository<Choice, Long>, SortRepository<Choice, Long>, NaturalKeyRepository<Choice, Long> {

    @Query("SELECT NEW de.binaerebauten.gleichklang.core.model.IdKeyPair(c.id, c.i18nKey) FROM Choice c")
    List<IdKeyPairResult> getAllAsIdKeyPair();

    @Query("SELECT c FROM Choice c WHERE c.choiceGroup.i18nKey = ?1")
    List<Choice> getChoicesForChoiceGroupKey(String i18nKey);

    @Query("SELECT c.i18nKey FROM Choice c")
    Set<String> getAllKeys();

    @Query("SELECT COALESCE(MAX(c.sortOrder), 0) from Choice c WHERE c.choiceGroup = ?1")
    Integer getMaxSortOrderForChoiceGroup(ChoiceGroup choiceGroup);


    @Query("SELECT c FROM Choice c WHERE c.choiceGroup = ?1 AND c.sortOrder > 0")
    List<Choice> getAllForChoiceGroupWithPositiveSortOrder(ChoiceGroup choiceGroup);
}
