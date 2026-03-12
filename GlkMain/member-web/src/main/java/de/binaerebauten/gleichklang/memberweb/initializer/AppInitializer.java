package de.binaerebauten.gleichklang.memberweb.initializer;

import de.binaerebauten.gleichklang.core.initializer.BaseAppInitializer;
import de.binaerebauten.gleichklang.memberweb.config.MemberRootConfig;

public class AppInitializer extends BaseAppInitializer
{

	@Override
	protected Class<?> getRootConfigClass()
	{
		return MemberRootConfig.class;
	}

	@Override
	protected Class<MemberUI> getAppUiClass()
	{
		return MemberUI.class;
	}
}
