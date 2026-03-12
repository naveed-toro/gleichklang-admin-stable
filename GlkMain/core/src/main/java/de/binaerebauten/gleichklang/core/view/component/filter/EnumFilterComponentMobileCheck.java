package de.binaerebauten.gleichklang.core.view.component.filter;

import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import de.binaerebauten.gleichklang.core.model.filter.AbstractFilter;
import de.binaerebauten.gleichklang.core.model.filter.EnumFilter;
import de.binaerebauten.gleichklang.core.model.filter.EnumFilterMobile;
import de.binaerebauten.gleichklang.core.view.component.ComponentFactory;
import de.binaerebauten.gleichklang.core.view.component.FilterComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnumFilterComponentMobileCheck <T extends Enum<T>> extends CustomComponent implements FilterComponent
{
    private final EnumFilterMobile<T> filter;
    private final ComboBox comboBox;
    private final CheckBox checkBox;

    private static final Logger LOG = LoggerFactory.getLogger(EnumFilterComponentMobileCheck.class);


    public EnumFilterComponentMobileCheck(EnumFilterMobile<T> filter, Class<T> type)
    {
        HorizontalLayout h = new HorizontalLayout();
        this.filter = filter;

        comboBox = (ComboBox) ComponentFactory.getInstance().createFieldByType(type);
        comboBox.setRequired(true);
        checkBox = new CheckBox("Only Mobile");
        checkBox.setRequired(true);
        h.addComponent(comboBox);
        h.addComponent(checkBox);
        setCompositionRoot(h);
    }

    @Override
    public void commit() throws FieldGroup.CommitException
    {
        try
        {
            comboBox.validate();
            filter.setEnumValue((T) comboBox.getValue());
            filter.setCheckBoxVlue(checkBox.getValue());
        }
        catch (Exception e)
        {
            LOG.error("Commit failed", e);
            throw new FieldGroup.CommitException("Commit failed", e);
        }
    }

    @Override
    public AbstractFilter getFilter()
    {
        return filter;
    }
}
