package de.binaerebauten.gleichklang.core.repository;

import de.binaerebauten.gleichklang.core.model.IdEntity;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;
import java.util.List;

/**
 * Created by rgoerner on 04.10.16.
 */
@NoRepositoryBean
public interface SpareEntityRepository<T extends IdEntity<ID>, ID extends Serializable> extends CrudRepository<T, ID>
{
	List<ID> findAllWithIdsOnly(Specification<T> spec);
	
	/**
	 * @Query("SELECT e." + IdEntity.ID + " FROM #{#entityName} e")
	 * not possible because of the specification
	 *
	 */
	List<ID> findAllWithIdsOnly();
}
