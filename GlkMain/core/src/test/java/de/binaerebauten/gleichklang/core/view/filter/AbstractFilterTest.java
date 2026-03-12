package de.binaerebauten.gleichklang.core.view.filter;

import de.binaerebauten.gleichklang.core.model.BaseEntity;
import de.binaerebauten.gleichklang.core.repository.BasePersistenceTest;
import de.binaerebauten.gleichklang.core.utils.DefaultEntityFactory;
import org.junit.After;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.List;

/**
 * Abstract class for filter tests.
 *
 * @param <T> the entity type
 * @param <F> the filter type
 */
public abstract class AbstractFilterTest<T extends BaseEntity, F extends Specification<T>>
		extends BasePersistenceTest
{
	@Autowired
	private DefaultEntityFactory entityFactory;
	private Collection<T> entities;

	protected abstract Collection<T> getPersistedEntities();

	protected abstract JpaSpecificationExecutor<T> getSpecificationExecutor();

	protected List<T> findAll(F filter)
	{
		return getSpecificationExecutor().findAll(filter);
	}

	@Before
	public void setup()
	{
		entities = getPersistedEntities();
	}

	@After
	public void teardown()
	{
		entityFactory.reset();
	}

}
