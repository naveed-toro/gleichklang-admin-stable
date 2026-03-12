package de.binaerebauten.gleichklang.adminweb.view.component;

import com.vaadin.ui.CustomComponent;
import de.binaerebauten.gleichklang.core.model.user.ClientInformation;
import de.binaerebauten.gleichklang.core.model.user.ClientInformation_;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanItemContainer;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanTable;
import de.binaerebauten.gleichklang.core.view.component.TableControl;

public class ClientInformationTable extends CustomComponent {

    public interface ClientInformationHandler
    {

        void refreshClientInformationTable();

    }

    private final LazyBeanTable<ClientInformation> clientInformationTable;
    private final TableControl<ClientInformation> tableControl;

    public ClientInformationTable()
    {
        clientInformationTable = createClientInformationTable();

        tableControl = new TableControl<>(clientInformationTable);

        setCompositionRoot(tableControl);
    }

    private LazyBeanTable<ClientInformation> createClientInformationTable() {

        final LazyBeanTable<ClientInformation> table = new LazyBeanTable<>();
        table.setSelectable(true);
        table.setSizeFull();

        table.addContainerProperty("Change_Date", ClientInformation_.changeDate);
        table.addContainerProperty("Create_Date", ClientInformation_.createDate);
        table.addContainerProperty("Browser", ClientInformation_.browser);
        table.addContainerProperty("Browser_Major_Version", ClientInformation_.browserMajorVersion);
        table.addContainerProperty("Browser_Min_Version", ClientInformation_.browserMinorVersion);
        table.addContainerProperty("Browser_Outdated", ClientInformation_.browserOutdated);
        table.addContainerProperty("Browser_Height", ClientInformation_.browserHeight);
        table.addContainerProperty("Browser_Width", ClientInformation_.browserWidth);
        table.addContainerProperty("Country", ClientInformation_.country);
        table.addContainerProperty("Touch_device", ClientInformation_.touchDevice);
        table.addContainerProperty("Language", ClientInformation_.language);
        table.addContainerProperty("Layout", ClientInformation_.layout);
        table.addContainerProperty("Screen_Height", ClientInformation_.screenHeight);
        table.addContainerProperty("Screen_Width", ClientInformation_.screenWidth);
        table.addContainerProperty("Timezone_Offset", ClientInformation_.timezoneOffset);
        table.addContainerProperty("Os", ClientInformation_.os);
        table.addContainerProperty("Last_Seen", ClientInformation_.lastSeen);
        table.setSortPropertyId(false, ClientInformation_.createDate);
        return table;
    }

    public void setTableHandler(LazyBeanItemContainer.LazyBeanFilteredItemsHandler<ClientInformation> handler)
    {
        clientInformationTable.setHandler(handler);
    }
}
