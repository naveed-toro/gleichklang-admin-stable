package de.binaerebauten.gleichklang.core.view.component;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.ComboBox;
import de.binaerebauten.gleichklang.core.model.locatable.LocatableEntity;
import de.binaerebauten.gleichklang.core.utils.DefaultI18N;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class LocatableSelection<T extends LocatableEntity> extends ComboBox
{
	private final Class<T> type;
	private final boolean nullSelectionAllowed;
	private static final Logger LOG = LoggerFactory.getLogger(LocatableSelection.class);
	
	public LocatableSelection(Class<T> type, boolean required)
	{
		this.type = type;
		this.nullSelectionAllowed = !required;
		this.setItemCaptionMode(ItemCaptionMode.ITEM);
		this.setItemCaptionPropertyId(DefaultI18N.NAME);
		this.setSizeFull();
		this.setRequired(required);
		this.setNullSelectionAllowed(nullSelectionAllowed);
		this.setCaption(getCaptionByType(type));
		this.setInputPrompt(getSelectAllCaption(type, required));
	}
	
	private static String getSelectAllCaption(Class<?> type, boolean required)
	{
		if(required) return I18N.INPUT_PROMPT.msg();
		
		final String i18nKey = getSelectAllI18NKey(type);
		return I18N.valueOf(i18nKey).msg();
	}
	
	private static String getSelectAllI18NKey(Class<?> type)
	{
		return getI18NCaption(type) + "_ALL";
	}
	
	private static String getCaptionByType(Class<?> type)
	{
		final String i18nKey = getI18NCaption(type);
		return I18N.valueOf(i18nKey).msg();
	}
	
	private static String getI18NCaption(Class<?> type)
	{
		return "LOCATABLE_" + type.getSimpleName().toUpperCase();
	}
	
	private void updateElements(BeanItemContainer<T> componentContainer)
	{
		if (nullSelectionAllowed)
		{
			try
			{
				final T locatableObject = type.newInstance();
				locatableObject.setI18nKey("ALL");
				componentContainer.addItemAt(0, locatableObject);
				this.setNullSelectionItemId(locatableObject);
			}
			catch (InstantiationException | IllegalAccessException e)
			{
				LOG.error("Locatable elements couldn't be updated ", e);
			}
		}
		this.setContainerDataSource(componentContainer);
		this.select(0);
	}
	
	public void updateElements(List<T> values)
	{
		final BeanItemContainer<T> componentContainer = new BeanItemContainer<>(type, values);
		updateElements(componentContainer);
	}
	
	@Override
	public Class<T> getType()
	{
		return type;
	}
}
