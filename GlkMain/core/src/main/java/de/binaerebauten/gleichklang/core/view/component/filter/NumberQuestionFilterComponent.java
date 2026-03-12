package de.binaerebauten.gleichklang.core.view.component.filter;

import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import de.binaerebauten.gleichklang.core.model.filter.AbstractFilter;
import de.binaerebauten.gleichklang.core.model.filter.NumberQuestionFilter;
import de.binaerebauten.gleichklang.core.model.filter.NumberQuestionFilter_;
import de.binaerebauten.gleichklang.core.model.questionnaire.NumberQuestion;
import de.binaerebauten.gleichklang.core.utils.DefaultI18N;
import de.binaerebauten.gleichklang.core.view.component.ComponentGroup;
import de.binaerebauten.gleichklang.core.view.component.FilterComponent;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;

public class NumberQuestionFilterComponent extends CustomComponent implements FilterComponent
{
	private final ComponentGroup<NumberQuestionFilter> numberQuestionFilterComponentGroup;

	public NumberQuestionFilterComponent(NumberQuestionFilter filter, FilterComponentHandler handler)
	{
		numberQuestionFilterComponentGroup = new ComponentGroup<>(NumberQuestionFilter.class, filter);

		final HorizontalLayout layout = new HorizontalLayout();
		layout.setSpacing(true);
		layout.setStyleName(CssStyle.NUMBER_FILTER_COMPONENT.getStyleName());

		final ComboBox comboBox = numberQuestionFilterComponentGroup.buildAndBind(ComboBox.class, NumberQuestionFilter_.numberQuestion);
		comboBox.setContainerDataSource(new BeanItemContainer<>(NumberQuestion.class, handler.getNumberQuestions()));
		comboBox.setItemCaptionMode(ItemCaptionMode.PROPERTY);
		comboBox.setItemCaptionPropertyId(DefaultI18N.NAME);
		
		layout.addComponent(comboBox);
		layout.addComponent(numberQuestionFilterComponentGroup.buildAndBind(true, "min", NumberQuestionFilter_.min));
		layout.addComponent(numberQuestionFilterComponentGroup.buildAndBind(true, "max", NumberQuestionFilter_.max));

		setCompositionRoot(layout);
	}

	@Override
	public void commit() throws CommitException
	{
		numberQuestionFilterComponentGroup.commit();
	}

	@Override
	public AbstractFilter getFilter()
	{
		return numberQuestionFilterComponentGroup.getItemDataSource().getBean();
	}
}
