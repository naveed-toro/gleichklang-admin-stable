package de.binaerebauten.gleichklang.core.view.component.filter;

import com.google.common.base.Strings;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TextField;
import de.binaerebauten.gleichklang.core.model.filter.AbstractFilter;
import de.binaerebauten.gleichklang.core.model.filter.StringFilter;
import de.binaerebauten.gleichklang.core.model.filter.StringFilter_;
import de.binaerebauten.gleichklang.core.view.component.ComponentGroup;
import de.binaerebauten.gleichklang.core.view.component.FilterComponent;

public class StringFilterComponent extends CustomComponent implements FilterComponent
{
	private final ComponentGroup<StringFilter> stringFilterComponentGroup;
	private final TextField textField;
	
	public StringFilterComponent(StringFilter filter)
	{
		stringFilterComponentGroup = new ComponentGroup<>(StringFilter.class, filter);
		textField = stringFilterComponentGroup.buildAndBind(true, TextField.class, StringFilter_.value);
		
		setCompositionRoot(textField);
	}

	@Override
	public void commit() throws CommitException
	{
		textField.setValue(Strings.nullToEmpty(textField.getValue()).trim());
		stringFilterComponentGroup.commit();
	}

	@Override
	public AbstractFilter getFilter()
	{
		return stringFilterComponentGroup.getItemDataSource().getBean();
	}
}
