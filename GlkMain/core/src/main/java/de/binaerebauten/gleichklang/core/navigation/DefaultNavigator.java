package de.binaerebauten.gleichklang.core.navigation;

import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.UI;
import de.binaerebauten.gleichklang.core.view.component.MenuItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class DefaultNavigator extends AbstractNavigator
{
	private static final Logger LOG = LoggerFactory.getLogger(DefaultNavigator.class);
	
	private final Navigator navigator;
	private final List<MenuItem<String>> parents = new ArrayList<>();
	private final Map<String, MenuItem<String>> menuItems = new HashMap<>();
	
	public DefaultNavigator(ComponentContainer container)
	{
		super();
		
		navigator = new Navigator(UI.getCurrent(), container);
		navigator.addProvider(this);
	}
	
	public MenuItem<String> createParentMenuItem(String key, String caption)
	{
		return createMenuItem(new DefaultViewItem(key, caption), null);
	}
	
	public MenuItem<String> createParentMenuItem(ViewItem viewItem)
	{
		return createMenuItem(viewItem, null);
	}
	
	/**
	 * Creates a menu item for the given parameters and registers the given view
	 * under the viewKey.
	 */
	public MenuItem<String> createMenuItem(ViewItem viewItem, MenuItem<String> parent)
	{
		addViewItem(viewItem);
		
		final MenuItem<String> menuItem = new MenuItem<>(viewItem.getCaption(), viewItem.getKey());
		
		if (parent != null)
		{
			parent.addChild(menuItem);
		}
		else
		{
			parents.add(menuItem);
		}
		
		menuItems.put(viewItem.getKey(), menuItem);
		
		return menuItem;
	}
	
	public void setVisibleMenuItem(String viewKey, boolean visible)
	{
		final MenuItem<String> menuItem = menuItems.get(viewKey);
		if (menuItem != null)
		{
			menuItem.setVisible(visible);
		}
	}
	
	public List<MenuItem<String>> getParentItems()
	{
		return parents;
	}
	
	@Override
	public void navigateTo(String viewKey)
	{
		try
		{
			navigator.navigateTo(viewKey);
		}
		catch (IllegalArgumentException ex)
		{
			LOG.warn("Could not navigate to {} ", viewKey, ex);
		}
	}
	
	public MenuItem<String> getMenuByKey(String key)
	{
		return menuItems.get(key);
	}
	
	public Collection<MenuItem<String>> getMenuItems()
	{
		return menuItems.values();
	}
	
	public void addViewChangeListener(ViewChangeListener listener)
	{
		if (listener == null)
		{
			return;
		}
		
		this.navigator.addViewChangeListener(listener);
	}
}
