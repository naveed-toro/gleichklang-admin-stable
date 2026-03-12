package de.binaerebauten.gleichklang.core.repository.user;

import de.binaerebauten.gleichklang.core.model.user.ClientInformation;
import de.binaerebauten.gleichklang.core.repository.AbstractRepositoryTest;
import de.binaerebauten.gleichklang.core.repository.user.ClientInformationRepository;
import de.binaerebauten.gleichklang.core.utils.DefaultEntityFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Collections;

public class ClientInformationRepositoryTest extends AbstractRepositoryTest<ClientInformation>
{
	@Autowired
	private ClientInformationRepository clientInformationRepository;
	
	@Autowired
	private DefaultEntityFactory entityFactory;
	
	public ClientInformationRepositoryTest()
	{
	}
	
	@Override
	protected Collection<ClientInformation> getPersistedEntities()
	{
		return Collections.singletonList(entityFactory.persistDefaultClientInformation());
	}
	
	@Override
	protected JpaRepository<ClientInformation, Long> getRepository()
	{
		return clientInformationRepository;
	}
}
