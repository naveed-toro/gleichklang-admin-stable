package de.binaerebauten.gleichklang.core.view.component.filter;

import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CustomComponent;
import de.binaerebauten.gleichklang.core.model.filter.AbstractFilter;
import de.binaerebauten.gleichklang.core.model.filter.EnumFilter;
import de.binaerebauten.gleichklang.core.view.component.ComponentFactory;
import de.binaerebauten.gleichklang.core.view.component.FilterComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnumFilterComponent<T extends Enum<T>> extends CustomComponent implements FilterComponent
{
	private final EnumFilter<T> filter;
	private final ComboBox comboBox;

	private static final Logger LOG = LoggerFactory.getLogger(EnumFilterComponent.class);
	
	
	public EnumFilterComponent(EnumFilter<T> filter, Class<T> type)
	{
		this.filter = filter;

		comboBox = (ComboBox) ComponentFactory.getInstance().createFieldByType(type);
		comboBox.setRequired(true);

		setCompositionRoot(comboBox);
	}

	@Override
	public void commit() throws CommitException
	{
		try
		{
			comboBox.validate();
			filter.setEnumValue((T) comboBox.getValue());
		}
		catch (Exception e)
		{
			LOG.error("Commit failed", e);
			throw new CommitException("Commit failed", e);
		}
	}

	@Override
	public AbstractFilter getFilter()
	{
		return filter;
	}
}
