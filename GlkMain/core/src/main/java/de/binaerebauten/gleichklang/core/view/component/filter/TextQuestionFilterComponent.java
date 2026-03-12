package de.binaerebauten.gleichklang.core.view.component.filter;

import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import de.binaerebauten.gleichklang.core.model.filter.AbstractFilter;
import de.binaerebauten.gleichklang.core.model.filter.TextQuestionFilter;
import de.binaerebauten.gleichklang.core.model.filter.TextQuestionFilter_;
import de.binaerebauten.gleichklang.core.model.questionnaire.TextQuestion;
import de.binaerebauten.gleichklang.core.utils.DefaultI18N;
import de.binaerebauten.gleichklang.core.view.component.ComponentGroup;
import de.binaerebauten.gleichklang.core.view.component.FilterComponent;

public class TextQuestionFilterComponent extends CustomComponent implements FilterComponent
{
	private final ComponentGroup<TextQuestionFilter> textQuestionFilterComponentGroup;

	public TextQuestionFilterComponent(TextQuestionFilter filter, FilterComponentHandler handler)
	{
		textQuestionFilterComponentGroup = new ComponentGroup<>(TextQuestionFilter.class, filter);

		final HorizontalLayout layout = new HorizontalLayout();
		layout.setSpacing(true);

		final ComboBox comboBox = textQuestionFilterComponentGroup.buildAndBind(ComboBox.class, TextQuestionFilter_.textQuestion);
		comboBox.setContainerDataSource(new BeanItemContainer<>(TextQuestion.class, handler.getTextQuestions()));
		comboBox.setItemCaptionMode(ItemCaptionMode.PROPERTY);
		comboBox.setItemCaptionPropertyId(DefaultI18N.NAME);

		layout.addComponent(comboBox);
		layout.addComponent(textQuestionFilterComponentGroup.buildAndBind(TextQuestionFilter_.text));

		setCompositionRoot(layout);
	}

	@Override
	public void commit() throws CommitException
	{
		textQuestionFilterComponentGroup.commit();
	}

	@Override
	public AbstractFilter getFilter()
	{
		return textQuestionFilterComponentGroup.getItemDataSource().getBean();
	}
}
