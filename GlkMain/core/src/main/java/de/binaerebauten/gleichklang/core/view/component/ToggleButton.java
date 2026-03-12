package de.binaerebauten.gleichklang.core.view.component;

import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.CustomComponent;

import java.util.*;

public class ToggleButton extends CustomComponent
{
	public enum ToggleStyle
	{
		BUTTON,
		CHECKBOX
	}
	
	public enum ToggleGoal
	{
		YES,
		NO
	}
	
	public interface ToggleClickListener
	{
		void buttonClick(ToggleGoal toggleGoal);
	}
	
	public interface SimpleToggleClickListener extends ToggleClickListener
	{
		@Override
		default void buttonClick(ToggleGoal toggleGoal)
		{
			buttonClick(toggleGoal == ToggleGoal.YES);
		}
		
		void buttonClick(boolean state);
	}
	
	public interface AutoRefresher
	{
		ToggleGoal getToggleGoal();
	}
	
	public interface SimpleAutoRefresher extends AutoRefresher
	{
		boolean getSimpleState();
		
		@Override
		default ToggleGoal getToggleGoal()
		{
			return getSimpleState() ? ToggleGoal.NO : ToggleGoal.YES;
		}
	}
	
	private final ToggleStyle toggleStyle;
	private final CheckBox checkBox;
	private final ValueChangeListener checkboxValueChangeListener;
	private final Map<ToggleGoal, List<ClickListener>> clickListeners = new HashMap<>();
	private final Map<ToggleGoal, Button> buttons = new HashMap<>();
	private AutoRefresher autoRefresher = null;
	
	public ToggleButton()
	{
		this(null, ToggleStyle.BUTTON);
	}
	
	public ToggleButton(String caption)
	{
		this(caption, ToggleStyle.BUTTON);
	}
	
	public ToggleButton(ToggleStyle toggleStyle)
	{
		this(null, toggleStyle);
	}
	
	public ToggleButton(String caption, ToggleStyle toggleStyle)
	{
		this.toggleStyle = Objects.requireNonNull(toggleStyle);
		
		for (ToggleGoal type : ToggleGoal.values())
		{
			final Button button = new Button(type.name());
			buttons.put(type, button);
			clickListeners.put(type, new ArrayList<>());
			button.addClickListener(event -> buttonClicked(event, type));
		}
		
		checkBox = ComponentFactory.getInstance().createField(CheckBox.class);
		checkboxValueChangeListener = event -> buttons.get(checkBox.getValue() ? ToggleGoal.YES : ToggleGoal.NO).click();
		
		setCaption(caption);
	}
	
	private void buttonClicked(ClickEvent event, ToggleGoal type)
	{
		clickListeners.get(type).forEach(l -> l.buttonClick(event));
		refresh();
	}
	
	public void setCaption(ToggleGoal toggleGoal, String caption)
	{
		buttons.get(toggleGoal).setCaption(caption);
	}
	
	public void setCaption(String toNoCaption, String toYesCaption)
	{
		setCaption(ToggleGoal.NO, toNoCaption);
		setCaption(ToggleGoal.YES, toYesCaption);
	}
	
	public void addClickListener(ToggleGoal toggleGoal, ClickListener clickListener)
	{
		Objects.requireNonNull(clickListener);
		
		clickListeners.get(toggleGoal).add(clickListener);
	}
	
	public void addClickListener(ClickListener toYesClickListener, ClickListener toNoClickListener)
	{
		addClickListener(ToggleGoal.YES, toYesClickListener);
		addClickListener(ToggleGoal.NO, toNoClickListener);
	}
	
	public void addToggleClickListener(SimpleToggleClickListener toggleClickListener)
	{
		addToggleClickListener((ToggleClickListener) toggleClickListener);
	}
	
	public void addToggleClickListener(ToggleClickListener toggleClickListener)
	{
		Objects.requireNonNull(toggleClickListener);
		
		for(ToggleGoal toggleGoal : ToggleGoal.values())
		{
			addClickListener(toggleGoal, event -> toggleClickListener.buttonClick(toggleGoal));
		}
	}
	
	private void activateButton(ToggleGoal toggleGoal)
	{
		switch(toggleStyle)
		{
			case BUTTON:
				setCompositionRoot(buttons.get(toggleGoal));
				break;
			case CHECKBOX:
				setCompositionRoot(checkBox);
				checkBox.removeValueChangeListener(checkboxValueChangeListener);
				checkBox.setValue(ToggleGoal.NO.equals(toggleGoal));
				checkBox.addValueChangeListener(checkboxValueChangeListener);
				break;
		}
	}
	
	public void setAutoRefresher(AutoRefresher autoRefresher)
	{
		this.autoRefresher = autoRefresher;
		refresh();
	}
	
	public void setAutoRefresher(SimpleAutoRefresher autoRefresher)
	{
		setAutoRefresher((AutoRefresher) autoRefresher);
	}
	
	private void refresh()
	{
		if (autoRefresher == null) return;
		
		activateButton(autoRefresher.getToggleGoal());
	}
}
