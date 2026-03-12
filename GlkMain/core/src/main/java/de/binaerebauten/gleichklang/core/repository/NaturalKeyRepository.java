package de.binaerebauten.gleichklang.core.repository;

import de.binaerebauten.gleichklang.core.model.NaturalKeyEntity;
import de.binaerebauten.gleichklang.core.model.NaturalKeyEntity.NaturalKey;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;

@NoRepositoryBean
public interface NaturalKeyRepository<T extends NaturalKeyEntity<ID>, ID extends Serializable> extends CrudRepository<T, ID>
{
	T findByNaturalKey(NaturalKey naturalKey);
}
