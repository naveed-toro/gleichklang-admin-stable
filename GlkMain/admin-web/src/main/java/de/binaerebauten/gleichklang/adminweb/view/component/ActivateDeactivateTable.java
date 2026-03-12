package de.binaerebauten.gleichklang.adminweb.view.component;

import com.vaadin.ui.CustomComponent;
import de.binaerebauten.gleichklang.core.model.ActiveDeactiveSubscription;
import de.binaerebauten.gleichklang.core.model.ActiveDeactiveSubscription_;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanItemContainer;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanTable;
import de.binaerebauten.gleichklang.core.view.component.TableControl;

public class ActivateDeactivateTable extends CustomComponent {

    public interface ActivateDeactivateHandler
    {

        void refreshActivateDeactivateTable();

    }

    private final LazyBeanTable<ActiveDeactiveSubscription> activeDeactivateTable;
    private final TableControl<ActiveDeactiveSubscription> tableControl;

    public ActivateDeactivateTable()
    {
        activeDeactivateTable = createActivateDeactivateTable();

        tableControl = new TableControl<>(activeDeactivateTable);

        setCompositionRoot(tableControl);
    }

    private LazyBeanTable<ActiveDeactiveSubscription> createActivateDeactivateTable() {

        final LazyBeanTable<ActiveDeactiveSubscription> table = new LazyBeanTable<>();
        table.setSelectable(true);
        table.setSizeFull();

        table.addContainerProperty("Responsible", ActiveDeactiveSubscription_.userType);
        table.addContainerProperty("Alias", ActiveDeactiveSubscription_.alias);
        table.addContainerProperty("Type", ActiveDeactiveSubscription_.type);
        table.addContainerProperty("Create Date", ActiveDeactiveSubscription_.createDate);

        return table;

    }

    public void setTableHandler(LazyBeanItemContainer.LazyBeanFilteredItemsHandler<ActiveDeactiveSubscription> handler)
    {
        activeDeactivateTable.setHandler(handler);
    }
}
