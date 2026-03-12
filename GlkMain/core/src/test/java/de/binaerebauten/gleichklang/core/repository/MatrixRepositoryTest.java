package de.binaerebauten.gleichklang.core.repository;

import de.binaerebauten.gleichklang.core.model.matching.MatchingMatrix;
import de.binaerebauten.gleichklang.core.utils.DefaultEntityFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Collections;

public class MatrixRepositoryTest extends AbstractRepositoryTest<MatchingMatrix>
{
	@Autowired
	private MatrixRepository matrixRepository;

	@Autowired
	private DefaultEntityFactory entityFactory;

	public MatrixRepositoryTest()
	{
	}

	@Override
	protected Collection<MatchingMatrix> getPersistedEntities()
	{
		return Collections.singletonList(entityFactory.persistDefaultMatchingMatrix());
	}

	@Override
	protected JpaRepository<MatchingMatrix, Long> getRepository()
	{
		return matrixRepository;
	}
}
