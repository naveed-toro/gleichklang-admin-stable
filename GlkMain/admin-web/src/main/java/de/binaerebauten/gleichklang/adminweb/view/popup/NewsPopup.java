package de.binaerebauten.gleichklang.adminweb.view.popup;

import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.*;
import de.binaerebauten.gleichklang.adminweb.view.I18N;
import de.binaerebauten.gleichklang.core.model.I18NEntity;
import de.binaerebauten.gleichklang.core.model.news.News;
import de.binaerebauten.gleichklang.core.model.news.News_;
import de.binaerebauten.gleichklang.core.utils.filter.FilterSpecificationBuilder;
import de.binaerebauten.gleichklang.core.view.component.ComponentGroup;
import de.binaerebauten.gleichklang.core.view.component.FilterControlComponent;
import de.binaerebauten.gleichklang.core.view.component.FilterControlComponent.FilterControlHandler;
import de.binaerebauten.gleichklang.core.view.component.FormPanel;
import de.binaerebauten.gleichklang.core.view.component.Popup;

/**
 * Created by rgoerner on 06.01.16.
 * Popup for creating or editing a news.
 */
public class NewsPopup extends Popup
{
	public interface SaveCallback
	{
		void saveNewNews(News news);
	}

	private final FilterControlComponent filterControlComponent;
	private final ComponentGroup<News> newsFieldGroup;
	private SaveCallback saveCallback = null;

	public NewsPopup(News news, SaveCallback saveCallback, FilterControlHandler filterControlHandler,
			FilterSpecificationBuilder filterSpecificationBuilder)
	{
		this.saveCallback = saveCallback;

		newsFieldGroup = new ComponentGroup<>(News.class, news);
		filterControlComponent = createFilterControl(news, filterControlHandler, filterSpecificationBuilder);

		final VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);
		layout.setSpacing(true);

		final Component controlButtons = createControlButtons();

		layout.addComponent(createDialogPanel());
		layout.addComponent(filterControlComponent);
		layout.addComponent(controlButtons);
		layout.setComponentAlignment(controlButtons, Alignment.TOP_RIGHT);

		setContent(layout);
		this.setResponsive(true);
		this.setHeight("95%");
	}

	private FilterControlComponent createFilterControl(News news, FilterControlHandler filterControlHandler,
			FilterSpecificationBuilder filterSpecificationBuilder)
	{
		return new FilterControlComponent(news.getFilter(), filterControlHandler, filterSpecificationBuilder);
	}

	private FormPanel createDialogPanel()
	{
		final FormPanel panel = new FormPanel();

		final ComboBox languageFiler = newsFieldGroup.buildAndBind(true, I18N.NEWS_POPUP_PANEL_LANGUAGE.msg() , ComboBox.class, News_.language);

		for (I18NEntity.Language l : I18NEntity.Language.values())
		{
			languageFiler.addItem(l.name());
		}
		languageFiler.select(I18NEntity.Language.DE);

		DateField validFromDateField = newsFieldGroup.buildAndBind(true, I18N.NEWS_POPUP_VALIDFROM.msg(), DateField.class, News_.validFrom);
		DateField validToDateField = newsFieldGroup.buildAndBind(true, I18N.NEWS_POPUP_VALIDTO.msg(), DateField.class, News_.validTo);
		validFromDateField.setResolution(Resolution.MINUTE);
		validToDateField.setResolution(Resolution.MINUTE);

		panel.addComponent(languageFiler);
		panel.addComponent(newsFieldGroup.buildAndBind(false, I18N.NEWS_POPUP_ACTIVE.msg(), CheckBox.class, News_.active));
		panel.addComponent(validFromDateField);
		panel.addComponent(validToDateField);
		panel.addComponent(newsFieldGroup.buildAndBind(true, I18N.NEWS_POPUP_TITLE.msg(), TextField.class, News_.title));
		panel.addComponent(newsFieldGroup.buildAndBind(true, I18N.NEWS_POPUP_TEASER.msg(), TextField.class, News_.teaserText));

		RichTextArea body = newsFieldGroup.buildAndBind(true, I18N.NEWS_POPUP_TEXT.msg(), RichTextArea.class, News_.text);
		body.setHeight("400px");

		panel.addComponent(body);

		return panel;
	}

	private Component createControlButtons()
	{
		final HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setSpacing(true);

		final Button saveButton = new Button(I18N.NEWS_POPUP_SAVE.msg());
		saveButton.setVisible(true);

		buttonLayout.addComponent(saveButton);
		saveButton.addClickListener(event ->
		{
			try
			{
				newsFieldGroup.commit();
				News news = newsFieldGroup.getItemDataSource().getBean();
			    String text = addTargetBlank(news.getText());
				news.setText(text);
				news.setFilter(filterControlComponent.getFilter());

				saveCallback.saveNewNews(news);
				close();
			}
			catch (FieldGroup.CommitException e)
			{
				Notification.show(de.binaerebauten.gleichklang.adminweb.view.popup.I18N.TRANSLATIONPOPUP_NOTIFICATION_INVALIDENTRIES.msg(), Notification.Type.ERROR_MESSAGE);
			}
		});

		buttonLayout.addComponents(saveButton);

		return buttonLayout;
	}

	private String addTargetBlank(String newsText){
        String text = "";
		String[] arrOfStr = newsText.split("href");

		for(int i = 0;i<arrOfStr.length;i++){
			if(i+1!=arrOfStr.length){
				text =   text + arrOfStr[i] +"target=\"_blank\"" + " href";
			}
			else {
				text = text + arrOfStr[i];
			}
		}

		return  text;
	}
}

