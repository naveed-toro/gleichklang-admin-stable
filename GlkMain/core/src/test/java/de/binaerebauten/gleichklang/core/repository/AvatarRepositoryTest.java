package de.binaerebauten.gleichklang.core.repository;

import de.binaerebauten.gleichklang.core.model.media.Avatar;
import de.binaerebauten.gleichklang.core.utils.DefaultEntityFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Collections;

public class AvatarRepositoryTest extends AbstractRepositoryTest<Avatar>
{
	@Autowired
	private AvatarRepository avatarRepository;

	@Autowired
	private DefaultEntityFactory entityFactory;

	public AvatarRepositoryTest()
	{
	}

	@Override
	protected Collection<Avatar> getPersistedEntities()
	{
		return Collections.singletonList(entityFactory.persistDefaultAvatar());
	}

	@Override
	protected JpaRepository<Avatar, Long> getRepository()
	{
		return avatarRepository;
	}
}
