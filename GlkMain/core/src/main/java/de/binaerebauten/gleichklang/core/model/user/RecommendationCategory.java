package de.binaerebauten.gleichklang.core.model.user;

import com.vaadin.server.ThemeResource;
import de.binaerebauten.gleichklang.core.utils.DefaultEnumI18N;

/**
 * The recommendation available recommendation categories.
 */
public enum RecommendationCategory implements DefaultEnumI18N
{
	PARTNERSHIP,
	FRIENDSHIP;
	
	public ThemeResource getIcon()
	{
		ThemeResource icon = null;
		
		switch (this)
		{
			case PARTNERSHIP:
				icon = new ThemeResource("img/icon_category_partnership.png");
				break;
			case FRIENDSHIP:
				icon = new ThemeResource("img/icon_category_friendship.png");
				break;
		}
		
		return icon;
	}
	
	public ThemeResource getOutlinedIcon()
	{
		ThemeResource icon = null;
		
		switch (this)
		{
			case PARTNERSHIP:
				icon = new ThemeResource("img/icon_category_partnership_outline.svg");
				break;
			case FRIENDSHIP:
				icon = new ThemeResource("img/icon_category_friendship_outline.svg");
				break;
		}
		
		return icon;
	}
	
	@Override
	public String toString()
	{
		return msg();
	}
}
