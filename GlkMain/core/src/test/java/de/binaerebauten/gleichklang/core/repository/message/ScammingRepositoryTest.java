package de.binaerebauten.gleichklang.core.repository.message;

import de.binaerebauten.gleichklang.core.model.message.Scamming;
import de.binaerebauten.gleichklang.core.repository.AbstractRepositoryTest;
import de.binaerebauten.gleichklang.core.utils.DefaultEntityFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Collections;

/**
 * Created by rgoerner on 03.05.16.
 */
public class ScammingRepositoryTest extends AbstractRepositoryTest<Scamming>
{
    @Autowired
    private ScammingRepository scammingRepository;

    @Autowired
    private DefaultEntityFactory entityFactory;

    public ScammingRepositoryTest()
    {
    }

    @Override
    protected Collection<Scamming> getPersistedEntities()
    {
        return Collections.singletonList(entityFactory.persistDefaultScamming());
    }

    @Override
    protected JpaRepository<Scamming, Long> getRepository()
    {
        return scammingRepository;
    }

}
