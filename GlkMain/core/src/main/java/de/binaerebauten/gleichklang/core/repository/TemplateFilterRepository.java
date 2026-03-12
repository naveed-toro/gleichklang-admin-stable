package de.binaerebauten.gleichklang.core.repository;

import de.binaerebauten.gleichklang.core.model.IdKeyPairResult;
import de.binaerebauten.gleichklang.core.model.filter.TemplateFilter;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TemplateFilterRepository extends DeleteRepository<TemplateFilter, Long>, JpaSpecificationExecutor<TemplateFilter>
{

    @Query("SELECT NEW de.binaerebauten.gleichklang.core.model.IdKeyPair(tf.id, tf.name) FROM TemplateFilter tf WHERE tf.deleted = FALSE")
    List<IdKeyPairResult> getAllAsIdKeyPair();

}
