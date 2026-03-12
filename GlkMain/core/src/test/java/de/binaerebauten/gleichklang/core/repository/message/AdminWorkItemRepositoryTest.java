package de.binaerebauten.gleichklang.core.repository.message;

import de.binaerebauten.gleichklang.core.model.message.AdminWorkItem;
import de.binaerebauten.gleichklang.core.repository.AbstractRepositoryTest;
import de.binaerebauten.gleichklang.core.utils.DefaultEntityFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Collections;

/**
 * Created by rgoerner on 03.05.16.
 */
public class AdminWorkItemRepositoryTest extends AbstractRepositoryTest<AdminWorkItem>
{
    @Autowired
    private AdminWorkItemRepository workItemRepository;

    @Autowired
    private DefaultEntityFactory entityFactory;

    public AdminWorkItemRepositoryTest()
    {
    }

    @Override
    protected Collection<AdminWorkItem> getPersistedEntities()
    {
        return Collections.singletonList(entityFactory.persistDefaultAdminWorkItem());
    }

    @Override
    protected JpaRepository<AdminWorkItem, Long> getRepository()
    {
        return workItemRepository;
    }

}
