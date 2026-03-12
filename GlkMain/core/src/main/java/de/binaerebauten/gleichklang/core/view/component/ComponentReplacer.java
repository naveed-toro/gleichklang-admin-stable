package de.binaerebauten.gleichklang.core.view.component;

import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;

/**
 * A placeholder for a component in a layout. So a component can easily replaced.
 *
 * @param <T>
 */
public class ComponentReplacer<T extends Component> extends CustomComponent
{
	public static class SimpleReplacer extends ComponentReplacer<Component>
	{
		public SimpleReplacer()
		{
		}

		public SimpleReplacer(Component component)
		{
			super(component);
		}
	}

	private T component;

	/**
	 * Initialized an empty component (label)
	 */
	public ComponentReplacer()
	{
		this(null);
	}

	/**
	 * Initialized with a concrete component
	 *
	 * @param component
	 */
	public ComponentReplacer(T component)
	{
		setComponent(component);
	}

	public T getComponent()
	{
		return component;
	}

	public void setComponent(T component)
	{
		this.component = component;
		if (component == null)
		{
			setCompositionRoot(new Label());
		}
		else
		{
			setCompositionRoot(component);
		}
	}
}
