package de.binaerebauten.gleichklang.core.repository;

import de.binaerebauten.gleichklang.core.model.DeletableEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;

import javax.transaction.Transactional;
import java.io.Serializable;
import java.util.List;

@NoRepositoryBean
public interface DeleteRepository<T extends DeletableEntity<ID>, ID extends Serializable> extends CrudRepository<T, ID>
{
	@Modifying
	@Transactional
	@Query("UPDATE #{#entityName} e SET " + DeletableEntity.DELETED + " = true WHERE e = ?1")
	void markAsDeleted(T entity);

	@Modifying
	@Transactional
	@Query("UPDATE #{#entityName} e SET " + DeletableEntity.DELETED + " = false WHERE e = ?1")
	void restoreDeleted(T entity);

	@Query("FROM #{#entityName} e WHERE " + DeletableEntity.DELETED + " = false")
	List<T> findAllNotDeleted();

	@Query("FROM #{#entityName} e WHERE " + DeletableEntity.DELETED + " = false")
	Page<T> findAllNotDeleted(Pageable pageable);
}
