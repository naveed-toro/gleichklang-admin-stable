package de.binaerebauten.gleichklang.adminweb.view;

import com.vaadin.data.Property;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.*;
import de.binaerebauten.gleichklang.adminweb.view.NewsView.NewsViewListener;
import de.binaerebauten.gleichklang.adminweb.view.filter.NewsActiveFilter;
import de.binaerebauten.gleichklang.adminweb.view.filter.NewsTextFilter;
import de.binaerebauten.gleichklang.adminweb.view.filter.NewsValidFilter;
import de.binaerebauten.gleichklang.core.model.I18NEntity;
import de.binaerebauten.gleichklang.core.model.news.News;
import de.binaerebauten.gleichklang.core.model.news.News_;
import de.binaerebauten.gleichklang.core.model.user.Admin_;
import de.binaerebauten.gleichklang.core.view.AbstractNavigateView;
import de.binaerebauten.gleichklang.core.view.component.ComponentFactory;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanItemContainer;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanTable;
import de.binaerebauten.gleichklang.core.view.component.TableControl;
import de.binaerebauten.gleichklang.core.view.filter.SimpleAttributeFilter;

import java.time.LocalDate;
import java.time.LocalDateTime;


/**
 * Created by rgoerner on 10.12.15.
 */
@SuppressWarnings("serial")
public class NewsViewImpl extends AbstractNavigateView<NewsViewListener> implements NewsView
{
	private final LazyBeanTable<News> newsTable;
    private final Button sendViaEmailButton;

    public NewsViewImpl()
    {
        newsTable = createNewsTable();
	
		final HorizontalLayout wrapper = new HorizontalLayout();

        sendViaEmailButton = createSendViaEmailButton();
        wrapper.addComponents(createNewsAdministration(), sendViaEmailButton);

        final VerticalLayout layout = new VerticalLayout();
        layout.addComponent(createFilterControl());
        layout.addComponent(wrapper);
        layout.addComponent(newsTable);
        layout.setSizeFull();
        layout.setMargin(true);
        layout.setSpacing(true);
        setCompositionRoot(layout);
    }
    
    private Button createSendViaEmailButton()
    {
        final Button sendEmail = new Button(I18N.NEWSVIEW_EMAIL_NOTIFICATION.msg());
        sendEmail.setEnabled(false);
        sendEmail.addClickListener(event -> fireEvent(action -> action.sendNewsViaEmail(newsTable.getValue())));

        return sendEmail;
    }

    private Component createFilterControl()
    {
        final NewsTextFilter newsTextFilter = new NewsTextFilter();
        final NewsValidFilter newsValidFilter = new NewsValidFilter();
        final NewsActiveFilter newsActiveFilter = new NewsActiveFilter();
        final SimpleAttributeFilter<News, String> newsLanguageFilter = new SimpleAttributeFilter<>(News_.language);
        
        final HorizontalLayout controlLayout = new HorizontalLayout();
        controlLayout.setSpacing(true);
        controlLayout.setMargin(new MarginInfo(false, false, true, false));
        controlLayout.setWidth("90%");

        controlLayout.setResponsive(true);
        controlLayout.setDefaultComponentAlignment(Alignment.BOTTOM_LEFT);

        final TextField titleTextField = ComponentFactory.getInstance().createField(TextField.class, I18N.NEWSVIEW_TEXT_TITLE.msg());
        titleTextField.addTextChangeListener(textChangeEvent -> newsTextFilter.setValue(textChangeEvent.getText()));
        controlLayout.addComponent(titleTextField);

        final DateField validFromDateField = (DateField) ComponentFactory.getInstance().createFieldByType(LocalDate.class, I18N.NEWSVIEW_VALIDFROM.msg());
        validFromDateField.setResolution(Resolution.DAY);
		validFromDateField.setConvertedValue(LocalDate.now().minusDays(365));
        validFromDateField.addValueChangeListener(valueChangedEvent -> newsValidFilter.setValue(getStartOfDay(validFromDateField)));
        controlLayout.addComponent(validFromDateField);

        final VerticalLayout platformVisibleWrapper = new VerticalLayout();
        final HorizontalLayout chechboxWrapper = new HorizontalLayout();

        final Label platformVisibleLabel = new Label(I18N.NEWSVIEW_TABLE_STATUS.msg());

        final CheckBox activeCheckBox = ComponentFactory.getInstance().createField(CheckBox.class,
                I18N.NEWSVIEW_CHECKBOX_ACTIVE.msg());
        final CheckBox inactiveCheckBox = ComponentFactory.getInstance().createField(CheckBox.class,
                I18N.NEWSVIEW_CHECKBOX_INACTIVE.msg());

        activeCheckBox.setValue(true);
        inactiveCheckBox.setValue(true);

        activeCheckBox.addValueChangeListener(boxChangeEvent -> newsActiveFilter.setValue(activeCheckBox.getValue(), inactiveCheckBox.getValue()));
        inactiveCheckBox.addValueChangeListener(boxChangeEvent -> newsActiveFilter.setValue(activeCheckBox.getValue(), inactiveCheckBox.getValue()));

        chechboxWrapper.addComponents(activeCheckBox, inactiveCheckBox);
        platformVisibleWrapper.addComponents(platformVisibleLabel, chechboxWrapper);
        controlLayout.addComponent(platformVisibleWrapper);

        final ComboBox languageFiler = new ComboBox(I18N.NEWSVIEW_LANGUAGE.msg());

        for (I18NEntity.Language l : I18NEntity.Language.values())
        {
            languageFiler.addItem(l.name());
        }
        languageFiler.setNullSelectionAllowed(true);

        languageFiler.addValueChangeListener(textChangedEvent -> newsLanguageFilter.setValue(textChangedEvent.getProperty().toString()));

        controlLayout.addComponent(languageFiler);

        controlLayout.setExpandRatio(titleTextField, 0.4f);
        controlLayout.setExpandRatio(validFromDateField, 0.2f);
        controlLayout.setExpandRatio(platformVisibleWrapper, 0.2f);
        controlLayout.setExpandRatio(languageFiler, 0.15f);
	
		newsTextFilter.setItemComponent(newsTable);
		newsValidFilter.setItemComponent(newsTable);
		newsActiveFilter.setItemComponent(newsTable);
		newsLanguageFilter.setItemComponent(newsTable);

        return controlLayout;
    }
    
    private LocalDateTime getStartOfDay(DateField dateField)
    {
        final LocalDate localDate = (LocalDate) dateField.getConvertedValue();
        return localDate != null ? localDate.atStartOfDay() : null;
    }
    
    private LazyBeanTable<News> createNewsTable()
    {
        final LazyBeanTable<News> table = new LazyBeanTable<>();

        table.setSelectable(true);
        table.setSizeFull();

        table.addContainerProperty(I18N.NEWSVIEW_TABLE_TITLE.msg(), News_.title);
        table.addContainerProperty(I18N.NEWSVIEW_TABLE_VALIDFROM.msg(), News_.validFrom);
        table.addContainerProperty(I18N.NEWSVIEW_TABLE_VALIDTO.msg(), News_.validTo);

        table.getNativeTable().addGeneratedColumn(I18N.NEWSVIEW_TABLE_STATUS.msg(), (source, itemId, columnId) ->
        {
            String textToDisplay = "";
            Property prop = source.getItem(itemId).getItemProperty("active");

            if (prop.getValue().equals(true))
                textToDisplay = I18N.NEWSVIEW_CHECKBOX_ACTIVE.msg();
            else
                textToDisplay = I18N.NEWSVIEW_CHECKBOX_INACTIVE.msg();

            return textToDisplay;
        });

        table.addContainerProperty(I18N.NEWSVIEW_LANGUAGE.msg(), News_.language);
        table.addContainerProperty(I18N.NEWSVIEW_SUMVISITS.msg(), News_.sumNewsVisits);
        table.getNativeTable().addGeneratedColumn(I18N.NEWSVIEW_TABLE_EMAIL_NOTIFICATION.msg(), (source, itemId, columnId) ->
        {
            String textToDisplay = "";
            Property prop = source.getItem(itemId).getItemProperty("emailNotification");

            if (prop.getValue().equals(true))
                textToDisplay = I18N.NEWSVIEW_CHECKBOX_ACTIVE.msg();
            else
                textToDisplay = I18N.NEWSVIEW_CHECKBOX_INACTIVE.msg();

            return textToDisplay;
        });

        table.addContainerProperty(I18N.NEWSVIEW_TABLE_EMAIL_SENDDATE.msg(), News_.emailSendDate);
        table.addContainerProperty(I18N.NEWSVIEW_TABLE_EMAIL_SENDER.msg(), News_.admin, Admin_.alias);

        table.addValueChangeListener(event -> {
            if (!event.isEmpty())
                sendViaEmailButton.setEnabled((!event.iterator().next().isEmailNotification() && event.iterator().next().getValidTo().isAfter(LocalDateTime.now())));
        });

        return table;
    }


    private Component createNewsAdministration()
    {
        final TableControl<News> tableControl = new TableControl<>(newsTable);
        tableControl.setButtonCaptions(I18N.NEWSVIEW_CONTROL_NEW.msg(), I18N.NEWSVIEW_CONTROL_EDIT.msg(), I18N.NEWSVIEW_CONTROL_DELETE.msg());

        tableControl.setNewCallback(() -> fireEvent(NewsViewListener::createNews));
        tableControl.setEditCallback(item -> fireEvent(action -> action.editNews(item)));
        tableControl.setDeleteCallback(item -> fireEvent(eventAction -> eventAction.deleteNews(item)));

        tableControl.setSizeUndefined();

        return tableControl;
    }

    @Override
    public void disableSendEmailButton()
    {
        sendViaEmailButton.setEnabled(false);
    }


    @Override
    public void setNewsHandler(LazyBeanItemContainer.LazyBeanFilteredItemsHandler<News> handler)
    {
        newsTable.clearValue();
        newsTable.setHandler(handler);
    }
}
