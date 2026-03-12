package de.binaerebauten.gleichklang.memberweb.view;

import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.VerticalLayout;
import de.binaerebauten.gleichklang.core.service.validator.ValidationException;
import de.binaerebauten.gleichklang.core.view.AbstractNavigateView;
import de.binaerebauten.gleichklang.core.view.component.ComponentFactory;
import de.binaerebauten.gleichklang.core.view.component.FormPanel;
import de.binaerebauten.gleichklang.core.view.component.validator.SaveHelper;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;
import de.binaerebauten.gleichklang.memberweb.view.PreConfigNotificationsView.PreConfigNotificationsViewListener;

/**
 * TODO @dwinkler Styling anpassen... kann stark reduziert werden! Habe es nur aus den entsprechenden Fragebögen-Views kopiert. Dort ist es genauso seltsam!
 */
public class PreConfigNotificationsViewImpl extends AbstractNavigateView<PreConfigNotificationsViewListener> implements PreConfigNotificationsView
{
	private final SaveHelper saveHelper;
	
	private final OptionGroup systemNotificationOptionGroup;
	private final OptionGroup marketingNotificationOptionGroup;
	
	public PreConfigNotificationsViewImpl()
	{
		saveHelper = new SaveHelper(this::save);
		
		systemNotificationOptionGroup = createOptionGroup(I18N.PRECONFIGNOTIFICATIONSVIEW_CAPTION_SYSTEMNOTIFICATION.msg());
		marketingNotificationOptionGroup = createOptionGroup(I18N.PRECONFIGNOTIFICATIONSVIEW_CAPTION_MARKETINGNOTIFICATION.msg());
		
		saveHelper.addFields(systemNotificationOptionGroup, marketingNotificationOptionGroup);
		
		setCompositionRoot(createLayout());
	}
	
	private void save() throws ValidationException
	{
		getListener().setNotifications((boolean) systemNotificationOptionGroup.getConvertedValue(), (boolean) marketingNotificationOptionGroup.getConvertedValue());
	}
	
	private Component createLayout()
	{
		final VerticalLayout layout = new VerticalLayout();
		layout.setStyleName(CssStyle.QUESTIONNAIRE_VIEW.getStyleName());
		layout.setWidth(100, Unit.PERCENTAGE);
		
		layout.addComponents(createHeader(), saveHelper.getValidationComponent(), createBody());
		
		return layout;
	}
	
	private Component createHeader()
	{
		final VerticalLayout panel = new VerticalLayout();
		panel.setSpacing(true);
		panel.setStyleName(CssStyle.QUESTIONNAIRE.getStyleName());
		
		final VerticalLayout rootLayout = new VerticalLayout();
		rootLayout.setStyleName(CssStyle.QUESTIONNAIRE_MAIN_HEADER.getStyleName());
		
		final VerticalLayout headerLayout = new VerticalLayout();
		headerLayout.addStyleName("root-wrapper");
		
		final Label titleLabel = new Label();
		titleLabel.setStyleName(CssStyle.QUESTIONNAIRE_LABEL.getStyleName());
		titleLabel.setValue(I18N.PRECONFIGNOTIFICATIONSVIEW_CAPTION_TITLE.msg());
		
		final Label descriptionLabel = new Label();
		descriptionLabel.setStyleName(CssStyle.QUESTIONNAIRE_DESCRIPTION.getStyleName());
		descriptionLabel.setContentMode(ContentMode.HTML);
		descriptionLabel.setValue(I18N.PRECONFIGNOTIFICATIONSVIEW_CAPTION_DESCRIPTION.msg());
		
		headerLayout.addComponents(titleLabel, descriptionLabel);
		rootLayout.addComponent(headerLayout);
		panel.addComponent(rootLayout);
		
		return panel;
	}
	
	private Component createBody()
	{
		final FormPanel subPanel = new FormPanel(I18N.PRECONFIGNOTIFICATIONSVIEW_CAPTION_SUBTITLE.msg());
		subPanel.setDescription(I18N.PRECONFIGNOTIFICATIONSVIEW_CAPTION_SUBDESCRIPTION.msg());
		subPanel.setStyleName(CssStyle.GK_PANEL.getStyleName());
		subPanel.setIcon(new ThemeResource("img/question-mark.svg"));
		
		subPanel.addFormElement(createQuestionComponent(systemNotificationOptionGroup));
		subPanel.addFormElement(createQuestionComponent(marketingNotificationOptionGroup));
		
		return subPanel;
	}
	
	private Component createQuestionComponent(OptionGroup optionGroup)
	{
		final FormPanel layout = new FormPanel();
		layout.addFormElement(optionGroup);
		
		return layout;
	}
	
	private OptionGroup createOptionGroup(String caption)
	{
		final OptionGroup optionGroup = ComponentFactory.getInstance().createField(Boolean.class, OptionGroup.class);
		optionGroup.setRequired(true);
		optionGroup.setCaption(caption);
		optionGroup.setStyleName("horizontal");
		
		return optionGroup;
	}
	
	@Override
	public void saveComplete(SaveResultListener saveResultListener)
	{
		saveHelper.saveComplete(saveResultListener);
	}
}
