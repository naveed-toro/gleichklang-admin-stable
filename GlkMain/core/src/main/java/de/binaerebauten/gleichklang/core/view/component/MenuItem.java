package de.binaerebauten.gleichklang.core.view.component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MenuItem<T> implements Iterable<MenuItem<T>>
{
	private boolean disabled = false;

	public interface VisibleChangedListener
	{
		void visibleChanged(MenuItem<?> sender);
	}

	private final T content;
	private final String caption;
	
	private final List<MenuItem<T>> childList = new ArrayList<>();
	private MenuItem<T> root = null;
	
	private boolean visible = true;
	private final List<VisibleChangedListener> visibleChangedListeners = new ArrayList<>();
	
	public MenuItem(String caption, T content)
	{
		this.content = content;
		this.caption = caption;
	}
	
	@Override
	public String toString()
	{
		return caption;
	}

	public T getContent()
	{
		return content;
	}

	@Override
	public Iterator<MenuItem<T>> iterator()
	{
		return childList.iterator();
	}

	public void addChild(MenuItem<T> menuItem)
	{
		menuItem.root = this;
		childList.add(menuItem);
	}

	public void setVisible(boolean visible)
	{
		this.visible = visible;
		visibleChangedListeners.forEach(listener -> listener.visibleChanged(this));
	}

	public boolean isVisible()
	{
		return visible;
	}

	public void addVisibleChangedListener(VisibleChangedListener listener)
	{
		if(listener == null) return;
		visibleChangedListeners.add(listener);
	}

	public boolean isDisabled()
	{
		return disabled;
	}

	public void setDisabled(boolean disabled)
	{
		this.disabled = disabled;
	}

	public List<MenuItem<T>> getChildList() {
		return childList;
	}
	
	public MenuItem<T> getRoot()
	{
		return root;
	}
}
