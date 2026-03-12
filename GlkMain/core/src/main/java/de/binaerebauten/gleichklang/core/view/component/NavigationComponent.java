package de.binaerebauten.gleichklang.core.view.component;

import com.google.common.collect.BiMap;
import com.google.common.collect.EnumHashBiMap;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.*;
import com.vaadin.ui.TabSheet.Tab;
import de.binaerebauten.gleichklang.core.utils.DefaultEnumI18N;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NavigationComponent<T extends Enum<T> & DefaultEnumI18N & NavigationEnum> extends CustomComponent
{
	public interface SubNavigationListener
	{
		void navigateToSubView(NavigationEnum navigationEnum);
	}
	
	public interface NavigationChangedListener<T extends Enum<T> & DefaultEnumI18N>
	{
		void onNavigationChanged(T navigationElement);
	}
	
	private static final Logger LOG = LoggerFactory.getLogger(NavigationComponent.class);
	
	private final TabSheet tabSheet;
	private final ComboBox dropdown;
	private final HorizontalLayout dropdownLayout;
	
	private final BiMap<T, Tab> tabs;
	private final List<T> sortedList = new ArrayList<>();
	private final BeanItemContainer<T> dropdownContainer;
	private NavigationChangedListener<T> navigationChangedListener = null;
	private SubNavigationListener subNavigationListener;
	
	public NavigationComponent(Class<T> navigationEnumClass)
	{
		tabs = EnumHashBiMap.create(navigationEnumClass);
		dropdownContainer = new BeanItemContainer<>(navigationEnumClass);
		
		final VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		
		tabSheet = createTabSheet();
		dropdown = createTabDropdown();
		
		dropdownLayout = new HorizontalLayout();
		dropdownLayout.setSizeFull();
		dropdownLayout.setStyleName(CssStyle.TABSHEET_DROPDOWN_GREEN.getStyleName());
		dropdownLayout.addComponent(dropdown);
		
		layout.addComponents(dropdownLayout, tabSheet);
		
		setCompositionRoot(layout);
	}
	
	private ComboBox createTabDropdown()
	{
		final ComboBox dropdown = ComponentFactory.getInstance().createField(ComboBox.class);
		dropdown.setContainerDataSource(dropdownContainer);
		dropdown.setTextInputAllowed(false);
		dropdown.addValueChangeListener(event -> tabSheet.setSelectedTab(tabs.get(dropdown.getValue())));
		
		return dropdown;
	}
	
	private TabSheet createTabSheet()
	{
		final TabSheet tabSheet = new TabSheet();
		tabSheet.addSelectedTabChangeListener(event -> onNavigationChanged());
		
		return tabSheet;
	}
	
	public void addNavigation(T navigationElement, Component content)
	{
		if (sortedList.contains(navigationElement))
			throw new IllegalArgumentException("element already exists");
		
		sortedList.add(navigationElement);
		tabs.put(navigationElement, tabSheet.addTab(content, navigationElement.toString()));
		dropdownContainer.addItem(navigationElement);
		if (dropdown.getValue() == null) dropdown.setValue(navigationElement);
	}
	
	public void setNavigationVisible(T navigationElement, boolean visible)
	{
		tabs.get(navigationElement).setVisible(visible);
		
		if (!visible && dropdownContainer.containsId(navigationElement))
		{
			dropdownContainer.removeItem(navigationElement);
		}
		else if (visible && !dropdownContainer.containsId(navigationElement))
		{
			dropdownContainer.addItemAt(sortedList.indexOf(navigationElement), navigationElement);
		}
	}
	
	private void onNavigationChanged()
	{
		if (navigationChangedListener != null)
		{
			navigationChangedListener.onNavigationChanged(getSelectedNavigation());
		}
		
		if (subNavigationListener != null)
		{
			subNavigationListener.navigateToSubView(getSelectedNavigation());
		}
	}
	
	public T getSelectedNavigation()
	{
		return tabs.inverse().get(tabSheet.getTab(tabSheet.getSelectedTab()));
	}
	
	public void setSelectedNavigation(NavigationEnum navigationElement)
	{
		if (!sortedList.contains(navigationElement))
		{
			LOG.error("element {} not exists", navigationElement);
			return;
		}
		
		final SubNavigationListener tmpListener = this.subNavigationListener;
		this.subNavigationListener = null;
		tabSheet.setSelectedTab(tabs.get(navigationElement));
		this.subNavigationListener = tmpListener;
	}
	
	public List<T> getAvailableTabs()
	{
		return Collections.unmodifiableList(sortedList);
	}
	
	public void setNavigationChangedListener(NavigationChangedListener<T> navigationChangedListener)
	{
		this.navigationChangedListener = navigationChangedListener;
	}
	
	public void setSubNavigationListener(SubNavigationListener subNavigationListener)
	{
		this.subNavigationListener = subNavigationListener;
	}
	
	//TODO RG: ob es ein add oder set sein soll, musst du wissen und ob der initiale Style im Konstruktor überhaupt noch gesetzt werden soll
	public void setDropdownLayoutStyle(CssStyle cssStyle)
	{
		dropdownLayout.setStyleName(cssStyle.getStyleName());
	}
}
