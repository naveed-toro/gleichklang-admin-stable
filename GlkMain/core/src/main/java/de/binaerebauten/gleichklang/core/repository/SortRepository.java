package de.binaerebauten.gleichklang.core.repository;

import de.binaerebauten.gleichklang.core.model.SortableEntity;
import de.binaerebauten.gleichklang.core.view.component.TableControl.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;
import java.util.Map;

@NoRepositoryBean
public interface SortRepository<T extends SortableEntity<ID>, ID extends Serializable> extends CrudRepository<T, ID>
{
	int findMinSortOrder(Specification<T> specification);

	int findMaxSortOrder(Specification<T> specification);

	Map<Integer, T> moveItems(Map<Integer, T> items, Direction direction, Specification<T> specification);
}
