package de.binaerebauten.gleichklang.core.view.component;




import com.vaadin.server.FontAwesome;
import com.vaadin.ui.*;
import de.binaerebauten.gleichklang.core.model.mail.AdminEmail;
import de.binaerebauten.gleichklang.core.service.mail.MailReceiveService;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;

import java.util.Date;

public class AdminEmailListComponent extends CustomComponent {

   public enum Operation
    {
        READ,
        DELETE,
        SEARCH,
        CLEAR,
        RELOAD,
        REPLY;
    }

    public interface AdminEmailListener
    {

       void viewAdminEmail(AdminEmailOperationData data, Operation operation);
    }

    public class AdminEmailOperationData
    {
        AdminEmail email;
        MailReceiveService.SupportedSearchTypes searchType;
        String paramValue;
        Date date;

        public AdminEmail getEmail() {
            return email;
        }

        public void setEmail(AdminEmail email) {
            this.email = email;
        }

        public MailReceiveService.SupportedSearchTypes getSearchType() {
            return searchType;
        }

        public void setSearchType(MailReceiveService.SupportedSearchTypes searchType) {
            this.searchType = searchType;
        }

        public String getParamValue() {
            return paramValue;
        }

        public void setParamValue(String paramValue) {
            this.paramValue = paramValue;
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        AdminEmailOperationData(AdminEmail email)
        {
            this.email = email;
        }

        AdminEmailOperationData (MailReceiveService.SupportedSearchTypes searchType, String paramValue, Date date)
        {
            this.searchType = searchType;
            this.paramValue = paramValue;
            this.date = date;
        }
    }


    //final CustomLazyBeanPagingComponent<AdminEmail> pagingComponent;
   private final TableControl<AdminEmail> adminEmailTableControl;

    public AdminEmailListener getAdminEmailListener() {
        return adminEmailListener;
    }

    public void setAdminEmailListener(AdminEmailListener adminEmailListener) {
        this.adminEmailListener = adminEmailListener;
    }

    private AdminEmailListener adminEmailListener;

    public AdminEmailListComponent()
    {
        VerticalLayout baseLayout = new VerticalLayout();
        HorizontalLayout searchLayout = new HorizontalLayout();
        HorizontalLayout searchButtonLayout = new HorizontalLayout();
        searchLayout.setSpacing(true);

        final ComboBox searchDropDown = new ComboBox("Search Parameter Type");
        searchDropDown.setEnabled(true);
        searchDropDown.setTextInputAllowed(false);
        searchDropDown.setInputPrompt("--Select--");

        MailReceiveService.SupportedSearchTypes searchTypes[] = MailReceiveService.SupportedSearchTypes.values();

        for(MailReceiveService.SupportedSearchTypes type:searchTypes)
        {
            searchDropDown.addItem(type);
        }

        Button searchButton = new Button("Search");
        Button removeFilter = new Button();
        removeFilter.setIcon(FontAwesome.REMOVE);
        Button reload = new Button("");
        reload.setIcon(FontAwesome.REFRESH);
        TextField txtFld = new TextField("Search Parameter Value");
        DateField dateField = new DateField("Sent Date");
        Label searchMessageLabel = new Label("");
        searchLayout.addComponent(searchDropDown);
        searchLayout.addComponent(txtFld);
        searchLayout.addComponent(dateField);
        searchButtonLayout.addComponent(searchButton);
        searchButtonLayout.addComponent(removeFilter);
        searchButtonLayout.addComponent(reload);



        //TODO : read messages from property files
        searchButton.addClickListener(item->
        {
            if(searchDropDown.getValue() == null)
            {
                showHideError(true,searchMessageLabel," Please select a search parameter"); }
            else {
                MailReceiveService.SupportedSearchTypes searchType = (MailReceiveService.SupportedSearchTypes) searchDropDown.getValue();


                if(searchType.equals(MailReceiveService.SupportedSearchTypes.DATE))
                {
                    if(dateField.getValue() == null)
                    {
                        showHideError(true,searchMessageLabel," Please select a date");
                        return;
                    }
                }else if( !searchType.equals(MailReceiveService.SupportedSearchTypes.DATE) && (txtFld.getValue() == null || txtFld.getValue().isEmpty()))
                {
                    showHideError(true,searchMessageLabel," Please enter a parameter value");
                    return;
                }

                    System.out.print("Performing search");
                    showHideError(false,searchMessageLabel,"");
                    AdminEmailOperationData data = new AdminEmailOperationData(searchType, txtFld.getValue(), dateField.getValue());
                    adminEmailListener.viewAdminEmail(data, Operation.SEARCH);

            }
        });

        removeFilter.addClickListener(item -> {

            showHideError(false,searchMessageLabel,"");
            txtFld.setValue("");
            dateField.setValue(null);
            searchDropDown.setValue(null);
            adminEmailListener.viewAdminEmail(null, Operation.CLEAR);});

        reload.addClickListener(item -> {

            showHideError(false,searchMessageLabel,"");
            txtFld.setValue("");
            dateField.setValue(null);
            searchDropDown.setValue(null);
            adminEmailListener.viewAdminEmail(null, Operation.RELOAD);});


        searchButtonLayout.setComponentAlignment(searchButton, Alignment.BOTTOM_RIGHT);
        searchButtonLayout.setComponentAlignment(removeFilter, Alignment.BOTTOM_LEFT);
        searchLayout.addComponent(searchButtonLayout);
        searchLayout.setComponentAlignment(searchButtonLayout, Alignment.BOTTOM_RIGHT);
        baseLayout.addComponent(searchMessageLabel);
        baseLayout.addComponent(searchLayout);
        adminEmailTableControl = new TableControl<>(createAdminEmailTable());
        adminEmailTableControl.getTable().addItemClickListener((item->
                {
                    if( adminEmailListener != null)
                        adminEmailListener.viewAdminEmail(new AdminEmailOperationData(item),Operation.READ);
                }
        ),true);

        baseLayout.addComponent(adminEmailTableControl);
        baseLayout.setComponentAlignment(adminEmailTableControl, Alignment.MIDDLE_CENTER);
        baseLayout.setSpacing(true);
        baseLayout.setMargin(true);

        setCompositionRoot(baseLayout);
    }

    private void showHideError(boolean show, Label searchMessageLabel, String message)
    {
        searchMessageLabel.setValue(message);
        searchMessageLabel.setStyleName("failure");
        searchMessageLabel.addStyleName(CssStyle.FORM_PART_EMPTY.getStyleName());
        searchMessageLabel.setVisible(show);
    }

    private LazyBeanTable<AdminEmail> createAdminEmailTable()
    {
        final LazyBeanTable<AdminEmail> table = new LazyBeanTable<>();
        table.setSelectable(true);
        table.setMultiSelect(true);
        table.setSizeFull();
        table.setSortEnabled(true);
        table.getNativeTable().setColumnExpandRatio(1, 50);

        table.addGeneratedColumn("Sender", (source, itemId, columnId) -> itemId.getSender() );
        table.addGeneratedColumn("Subject", (source, itemId, columnId) -> itemId.getSubject());
        table.addGeneratedColumn("Send Date", (source, itemId, columnId) -> itemId.getSentDate());
        table.addGeneratedColumn("Status", (source, itemId, columnId) -> itemId.getStatus());
        return table;
    }


    public void setAdminEmailTableHandler(LazyBeanItemContainer.LazyBeanFilteredItemsHandler<AdminEmail> handler)
    {
        adminEmailTableControl.getTable().setHandler(handler);
       // pagingComponent.setHandler(handler);
    }


}
