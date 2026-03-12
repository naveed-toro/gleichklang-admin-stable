package de.binaerebauten.gleichklang.core.repository;

import de.binaerebauten.gleichklang.core.config.PersistenceTestConfig;
import de.binaerebauten.gleichklang.core.config.RootTestConfig;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { RootTestConfig.class, PersistenceTestConfig.class })
public abstract class BasePersistenceTest
{

}
