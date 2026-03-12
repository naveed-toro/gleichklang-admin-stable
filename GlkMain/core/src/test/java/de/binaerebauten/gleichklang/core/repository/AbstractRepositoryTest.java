package de.binaerebauten.gleichklang.core.repository;

import de.binaerebauten.gleichklang.core.model.BaseEntity;
import de.binaerebauten.gleichklang.core.utils.DefaultEntityFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public abstract class AbstractRepositoryTest<T extends BaseEntity> extends BasePersistenceTest
{
	protected static final Pageable singlePage = new PageRequest(0, Integer.MAX_VALUE);

	@Autowired
	private DefaultEntityFactory entityFactory;
	private Collection<T> entities;

	protected abstract Collection<T> getPersistedEntities();

	protected abstract JpaRepository<T, Long> getRepository();

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

	@Test
	public void testFindAll() throws Exception
	{
		final Collection<T> entities = getRepository().findAll();
		assertThat(entities.size(), equalTo(this.entities.size()));
		for (T entity : entities)
		{
			assertThat(this.entities, hasItem(entity));
		}
	}

	@Test
	public void testFindOne()
	{
		final T entity = getRepository().findOne(entities.stream().findFirst().get().getId());
		assertThat(entity, notNullValue());
	}

	@Test
	@Transactional
	public void testDelete() throws Exception
	{
		T entity;
		final Long id = entities.stream().findFirst().get().getId();

		entity = getRepository().findOne(id);
		assertThat(entity, notNullValue());

		getRepository().delete(id);
		entity = getRepository().findOne(id);

		assertThat(entity, nullValue());
	}
}
