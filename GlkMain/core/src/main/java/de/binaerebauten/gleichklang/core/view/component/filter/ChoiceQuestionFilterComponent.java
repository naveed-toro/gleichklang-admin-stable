package de.binaerebauten.gleichklang.core.view.component.filter;

import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import de.binaerebauten.gleichklang.core.model.filter.AbstractFilter;
import de.binaerebauten.gleichklang.core.model.filter.ChoiceQuestionFilter;
import de.binaerebauten.gleichklang.core.model.filter.ChoiceQuestionFilter_;
import de.binaerebauten.gleichklang.core.model.questionnaire.Choice;
import de.binaerebauten.gleichklang.core.model.questionnaire.ChoiceQuestion;
import de.binaerebauten.gleichklang.core.utils.DefaultI18N;
import de.binaerebauten.gleichklang.core.view.component.ComponentGroup;
import de.binaerebauten.gleichklang.core.view.component.FilterComponent;

public class ChoiceQuestionFilterComponent extends CustomComponent implements FilterComponent
{
	private final ComponentGroup<ChoiceQuestionFilter> choiceQuestionFilterComponentGroup;

	public ChoiceQuestionFilterComponent(ChoiceQuestionFilter filter, FilterComponentHandler handler)
	{
		choiceQuestionFilterComponentGroup = new ComponentGroup<>(ChoiceQuestionFilter.class, filter);

		final HorizontalLayout layout = new HorizontalLayout();
		layout.setSpacing(true);

		final ComboBox comboBox = choiceQuestionFilterComponentGroup.buildAndBind(ComboBox.class, ChoiceQuestionFilter_.choiceQuestion);
		comboBox.setContainerDataSource(new BeanItemContainer<>(ChoiceQuestion.class, handler.getChoiceQuestions()));
		comboBox.setItemCaptionMode(ItemCaptionMode.PROPERTY);
		comboBox.setItemCaptionPropertyId(DefaultI18N.NAME);

		final ComboBox choiceComboBox = choiceQuestionFilterComponentGroup.buildAndBind(ComboBox.class, ChoiceQuestionFilter_.choice);
		choiceComboBox.setItemCaptionMode(ItemCaptionMode.PROPERTY);
		choiceComboBox.setItemCaptionPropertyId(DefaultI18N.NAME);

		comboBox.addValueChangeListener(event ->
		{
			choiceComboBox.setValue(null);
			choiceComboBox.setContainerDataSource(null);

			final ChoiceQuestion choiceQuestion = (ChoiceQuestion) comboBox.getValue();
			if (choiceQuestion != null)
			{
				choiceComboBox.setContainerDataSource(new BeanItemContainer<>(Choice.class, choiceQuestion.getChoiceGroup().getChoices()));
			}
		});

		layout.addComponent(comboBox);
		layout.addComponent(choiceComboBox);

		setCompositionRoot(layout);
	}

	@Override
	public void commit() throws CommitException
	{
		choiceQuestionFilterComponentGroup.commit();
	}

	@Override
	public AbstractFilter getFilter()
	{
		return choiceQuestionFilterComponentGroup.getItemDataSource().getBean();
	}
}
