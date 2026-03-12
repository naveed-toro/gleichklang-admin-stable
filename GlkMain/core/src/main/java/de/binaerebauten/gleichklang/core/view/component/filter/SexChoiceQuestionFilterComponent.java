package de.binaerebauten.gleichklang.core.view.component.filter;

import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import de.binaerebauten.gleichklang.core.model.filter.AbstractFilter;
import de.binaerebauten.gleichklang.core.model.filter.SexChoiceQuestionFilter;
import de.binaerebauten.gleichklang.core.model.filter.SexChoiceQuestionFilter_;
import de.binaerebauten.gleichklang.core.model.questionnaire.Choice;
import de.binaerebauten.gleichklang.core.utils.DefaultI18N;
import de.binaerebauten.gleichklang.core.view.component.ComponentGroup;
import de.binaerebauten.gleichklang.core.view.component.FilterComponent;

/**
 * Created by rgoerner on 27.04.17.
 */
public class SexChoiceQuestionFilterComponent extends CustomComponent implements FilterComponent
{
    private final ComponentGroup<SexChoiceQuestionFilter> sexChoiceQuestionFilterComponentGroup;

    public SexChoiceQuestionFilterComponent(SexChoiceQuestionFilter filter, FilterComponentHandler handler)
    {
        sexChoiceQuestionFilterComponentGroup = new ComponentGroup<>(SexChoiceQuestionFilter.class, filter);

        final HorizontalLayout layout = new HorizontalLayout();
        layout.setSpacing(true);
	
		final ComboBox choiceComboBox = sexChoiceQuestionFilterComponentGroup.buildAndBind(ComboBox.class, SexChoiceQuestionFilter_.choice);
        choiceComboBox.setItemCaptionMode(AbstractSelect.ItemCaptionMode.PROPERTY);
        choiceComboBox.setItemCaptionPropertyId(DefaultI18N.NAME);
        choiceComboBox.setContainerDataSource(new BeanItemContainer<>(Choice.class, handler.getSexChoiceQuestionChoices()));

        layout.addComponent(choiceComboBox);

        setCompositionRoot(layout);
    }

    @Override
    public void commit() throws FieldGroup.CommitException
    {
        sexChoiceQuestionFilterComponentGroup.commit();
    }

    @Override
    public AbstractFilter getFilter()
    {
        return sexChoiceQuestionFilterComponentGroup.getItemDataSource().getBean();
    }
}
