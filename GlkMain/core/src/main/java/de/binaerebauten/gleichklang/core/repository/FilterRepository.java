package de.binaerebauten.gleichklang.core.repository;

import de.binaerebauten.gleichklang.core.model.filter.AbstractFilter;
import de.binaerebauten.gleichklang.core.model.filter.TemplateContext;
import de.binaerebauten.gleichklang.core.model.filter.TemplateFilter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface FilterRepository extends JpaRepository<AbstractFilter, Long>
{
	@Query("SELECT t FROM TemplateFilter t WHERE ?1 MEMBER OF t.templateContexts AND t.deleted = false")
	Set<TemplateFilter> findTemplateByContext(TemplateContext templateContext);
}
