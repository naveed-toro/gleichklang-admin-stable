package de.binaerebauten.gleichklang.core.view.component;

import com.vaadin.data.Property;
import com.vaadin.ui.Tree;

import java.util.*;

public class MenuTree extends Tree
{
	private class AutoTreeExpander implements ValueChangeListener
	{
		@Override
		public void valueChange(Property.ValueChangeEvent event)
		{
			final Object selectedItem = getValue();
			if (selectedItem == null) return;
			
			final Set<Object> todo = new HashSet<>(getItemIds());
			Object rootItem = selectedItem;
			do
			{
				todo.remove(rootItem);
				expandItem(rootItem);
				rootItem = getParent(rootItem);
			} while (rootItem != null);
			
			todo.forEach(MenuTree.this::collapseItem);
		}
	}
	
	private class AutoSelector implements ValueChangeListener
	{
		@Override
		public void valueChange(Property.ValueChangeEvent event)
		{
			final Object selectedItem = getValue();
			if (selectedItem == null) return;
			
			final Collection<?> children = getChildren(selectedItem);
			if (children != null && !children.isEmpty())
			{
				select(children.iterator().next());
			}
		}
	}
	
	private final AutoSelector autoSelector = new AutoSelector();
	private final AutoTreeExpander autoTreeExpander = new AutoTreeExpander();
	
	public MenuTree()
	{
		init();
	}
	
	private void init()
	{
		this.setImmediate(true);
		this.setNewItemsAllowed(false);
		this.setMultiSelect(false);
		this.setNullSelectionAllowed(false);
		this.setItemStyleGenerator((Tree.ItemStyleGenerator) (source, itemId) -> {
			if (itemId instanceof MenuItem)
			{
				MenuItem<?> menuItem = (MenuItem<?>) itemId;
				if (menuItem.isDisabled())
				{
					return "disabled";
				}
			}
			return "";
		});
	}
	
	public void setAutoExpandSelection(boolean enabled)
	{
		removeValueChangeListener(autoTreeExpander);
		if (enabled)
			addValueChangeListener(autoTreeExpander);
	}
	
	public void setAutoSelector(boolean enabled)
	{
		removeValueChangeListener(autoSelector);
		if (enabled)
			addValueChangeListener(autoSelector);
	}
	
	public void addMenuItem(MenuItem<?> menuItem)
	{
		addVisibleChangeListenerToMenuItem(menuItem);
		addMenu(menuItem, null);
	}
	
	private void addVisibleChangeListenerToMenuItem(MenuItem<?> menuItem)
	{
		menuItem.addVisibleChangedListener(this::refresh);
		for (MenuItem<?> childMenuItem : menuItem)
		{
			addVisibleChangeListenerToMenuItem(childMenuItem);
		}
	}
	
	private void addMenu(MenuItem<?> menuItem, MenuItem<?> parentItem)
	{
		final boolean isParentItem = menuItem.iterator().hasNext();
		
		if (!menuItem.isVisible())
			return;
		
		this.addItem(menuItem);
		this.setChildrenAllowed(menuItem, isParentItem);
		this.setItemCaption(menuItem, menuItem.toString());
		this.setParent(menuItem, parentItem);
		
		for (MenuItem<?> childMenuItem : menuItem)
		{
			addMenu(childMenuItem, menuItem);
		}
	}
	
	private void refresh(MenuItem<?> menuItem)
	{
		if(!menuItem.isVisible())
		{
			removeItem(menuItem);
		}
		else
		{
			addMenu(menuItem, menuItem.getRoot());
		}
	}
	
	public void selectFirstChild()
	{
		final Collection<?> rootItems = rootItemIds();
		if(!rootItems.isEmpty())
		{
			Object child = rootItems.iterator().next();
			while(hasChildren(child))
			{
				child = getChildren(child).iterator().next();
			}
			select(child);
		}
	}
}
