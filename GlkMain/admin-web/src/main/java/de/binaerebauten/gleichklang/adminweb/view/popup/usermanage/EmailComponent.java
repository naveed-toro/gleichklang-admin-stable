package de.binaerebauten.gleichklang.adminweb.view.popup.usermanage;

import com.google.common.base.Strings;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.Notification.Type;
import de.binaerebauten.gleichklang.adminweb.view.popup.I18N;
import de.binaerebauten.gleichklang.core.model.mail.UserMailTemplate;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.view.component.ComponentFactory;
import de.binaerebauten.gleichklang.core.view.component.MessageBox;
import de.binaerebauten.gleichklang.core.view.component.MessageBox.DialogResult;
import org.apache.commons.lang.StringEscapeUtils;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.stream.Collectors;

public class EmailComponent extends CustomComponent
{
	public interface EmailComponentListener
	{
		String getEmailContent(UserMailTemplate template, User user, String... params);
		
		void sendEmail(UserMailTemplate template, User user, String... params);
	}
	
	private final EmailComponentListener listener;
	private final User user;
	
	private final CssLayout container = new CssLayout();
	private final ComboBox templateComboBox;
	private final Label preview;
	private final Button sendButton;
	private final TextField signOffAdditionalTextField;
	private final EnumSet<RecommendationCategory> categories = EnumSet.noneOf(RecommendationCategory.class);
	private final RichTextArea optimizationTextArea;
	
	public EmailComponent(EmailComponentListener listener, User user)
	{
		this.listener = listener;
		this.user = user;
		
		templateComboBox = createTemplateComboBox();
		preview = createPreview();
		sendButton = createSendButton();
		signOffAdditionalTextField = createSignOffAdditionalTextField();
		optimizationTextArea = createOptimizationTextArea();
		
		setCompositionRoot(createLayout());
	}
	
	private Component createLayout()
	{
		final VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);
		layout.setSpacing(true);
		
		layout.addComponent(templateComboBox);
		layout.addComponents(container);
		
		return layout;
	}
	
	private Label createPreview()
	{
		final Label label = new Label();
		label.setCaption(I18N.USERMANAGE_POPUP_PREVIEW.msg());
		label.setContentMode(ContentMode.HTML);
		
		return label;
	}
	
	private RichTextArea createOptimizationTextArea()
	{
		final RichTextArea textArea = new RichTextArea();
		textArea.setValue(I18N.USERMANAGEPOPUP_VALUE_EMAILOPTIMIZATIONTEXT.msg());
		textArea.addValueChangeListener(l -> updateView());
		
		return textArea;
	}
	
	private void updateView()
	{
		final UserMailTemplate template = (UserMailTemplate) templateComboBox.getValue();
		if (template == null)
		{
			preview.setValue(null);
			return;
		}
		
		final String emailText;
		
		switch (template)
		{
			case SIGNOFF_ACK_MSG:
				emailText = listener.getEmailContent(template, user, getAdditionalText());
				sendButton.setEnabled(!signOffAdditionalTextField.isEmpty());
				break;
			case OPTIMIZATION_DONE:
				final String recommendationCategory = getCategoriesAsString();
				emailText = listener.getEmailContent(template, user, recommendationCategory, optimizationTextArea.getValue());
				sendButton.setEnabled(!Strings.isNullOrEmpty(recommendationCategory));
				break;
			default:
				emailText = null;
		}
		
		preview.setValue(emailText);
	}
	
	private ComboBox createTemplateComboBox()
	{
		final ComboBox comboBox = ComponentFactory.getInstance().createField(ComboBox.class);
		comboBox.setWidth("20em");
		comboBox.setCaption(I18N.USERMANAGE_POPUP_TEMPLATE.msg());
		comboBox.addItems(
				UserMailTemplate.SIGNOFF_ACK_MSG,
				UserMailTemplate.OPTIMIZATION_DONE);
		
		comboBox.addValueChangeListener(event -> loadView());
		
		return comboBox;
	}
	
	private void loadView()
	{
		final UserMailTemplate template = (UserMailTemplate) templateComboBox.getValue();
		
		container.removeAllComponents();
		
		if(template == null) return;
		
		switch (template)
		{
			case SIGNOFF_ACK_MSG:
				container.addComponent(createSignOffComponent());
				break;
			case OPTIMIZATION_DONE:
				container.addComponent(createOptimizationComponent());
				break;
		}
		
		updateView();
	}
	
	private Component createOptimizationComponent()
	{
		final VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);
		
		layout.addComponents(createCategoryCheckBoxes(), optimizationTextArea, preview, sendButton);
		
		return layout;
	}
	
	private Component createSignOffComponent()
	{
		final VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);
		
		layout.addComponents(signOffAdditionalTextField, preview, sendButton);
		
		return layout;
	}
	
	private TextField createSignOffAdditionalTextField()
	{
		final TextField additionalTextField = ComponentFactory.getInstance().createField(TextField.class);
		additionalTextField.setCaption(I18N.USERMANAGE_POPUP_ADDITIONAL_INFO.msg());
		additionalTextField.addValueChangeListener(l -> updateView());
		
		return additionalTextField;
	}
	
	private HorizontalLayout createCategoryCheckBoxes()
	{
		categories.clear();
		
		final HorizontalLayout layout = new HorizontalLayout();
		Arrays.stream(RecommendationCategory.values()).forEach(cat ->
		{
			final CheckBox checkBox = ComponentFactory.getInstance().createField(CheckBox.class);
			checkBox.setCaption(cat.getName());
			checkBox.addValueChangeListener(l ->
			{
				if(checkBox.getValue())
				{
					categories.add(cat);
				}
				else
				{
					categories.remove(cat);
				}
			});
			checkBox.addValueChangeListener(l -> updateView());
			layout.addComponent(checkBox);
		});
		
		return layout;
	}
	
	private Button createSendButton()
	{
		final Button sendButton = new Button(I18N.USERMANAGE_POPUP_SENDBUTTON.msg());
		sendButton.setEnabled(false);
		sendButton.addClickListener(event ->
		{
			final UserMailTemplate template = (UserMailTemplate) templateComboBox.getValue();
			
			final String msg = I18N.SEND_USER_EMAIL_CONFIRMATION.msg(template, user.getEmail());
			MessageBox.show(msg, MessageBox.MessageBoxButtons.YES_NO, r ->
			{
				if (DialogResult.YES.equals(r))
				{
					switch (template)
					{
						case SIGNOFF_ACK_MSG:
							listener.sendEmail(template, user, getAdditionalText());
							break;
						case OPTIMIZATION_DONE:
							listener.sendEmail(template, user, getCategoriesAsString(), optimizationTextArea.getValue());
							break;
					}
					
					templateComboBox.setValue(null);
					Notification.show(I18N.USERMANAGE_POPUP_SENT.msg(), Type.TRAY_NOTIFICATION);
				}
			});
		});
		
		return sendButton;
	}
	
	private String getAdditionalText()
	{
		return StringEscapeUtils.escapeHtml(signOffAdditionalTextField.getValue());
	}
	
	private String getCategoriesAsString()
	{
		return categories.stream()
				.map(RecommendationCategory::toString)
				.collect(Collectors.joining(", "));
	}
}
