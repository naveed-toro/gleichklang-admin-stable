package de.binaerebauten.gleichklang.core.view.component.filter;

import com.google.common.base.Strings;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TextField;
import de.binaerebauten.gleichklang.core.model.filter.AbstractFilter;
import de.binaerebauten.gleichklang.core.model.filter.NumberFilter;
import de.binaerebauten.gleichklang.core.model.filter.NumberFilter_;
import de.binaerebauten.gleichklang.core.view.component.ComponentGroup;
import de.binaerebauten.gleichklang.core.view.component.FilterComponent;

public class NumberFilterComponent extends CustomComponent implements FilterComponent
{
	private final ComponentGroup<NumberFilter> numberFilterComponentGroup;
	private final TextField textField;
	
	public NumberFilterComponent(NumberFilter filter)
	{
		numberFilterComponentGroup = new ComponentGroup<>(NumberFilter.class, filter);
		textField = numberFilterComponentGroup.buildAndBind(true, TextField.class, NumberFilter_.value);
		
		setCompositionRoot(textField);
	}

	@Override
	public void commit() throws CommitException
	{
		textField.setValue(Strings.nullToEmpty(textField.getValue()).trim());
		numberFilterComponentGroup.commit();
	}

	@Override
	public AbstractFilter getFilter()
	{
		return numberFilterComponentGroup.getItemDataSource().getBean();
	}
}
