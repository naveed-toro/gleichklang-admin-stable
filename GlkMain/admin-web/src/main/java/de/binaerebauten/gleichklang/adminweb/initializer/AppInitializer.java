package de.binaerebauten.gleichklang.adminweb.initializer;

import de.binaerebauten.gleichklang.adminweb.config.AdminRootConfig;
import de.binaerebauten.gleichklang.core.initializer.BaseAppInitializer;

public class AppInitializer extends BaseAppInitializer
{
	@Override
	protected Class<?> getRootConfigClass()
	{
		return AdminRootConfig.class;
	}

	@Override
	protected Class<AdminUI> getAppUiClass()
	{
		return AdminUI.class;
	}
}
