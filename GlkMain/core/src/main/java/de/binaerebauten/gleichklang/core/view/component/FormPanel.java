package de.binaerebauten.gleichklang.core.view.component;

import com.google.common.base.Strings;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Resource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.themes.ValoTheme;
import de.binaerebauten.gleichklang.core.view.component.validator.SaveHelper;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;
import de.binaerebauten.gleichklang.core.view.popup.DescriptionBox;
import org.apache.commons.lang.StringUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings("serial")
public class FormPanel extends Panel implements Comparable<FormPanel>
{
	private final int SHORT_DESCRIPTION_LENGTH = 512;
	
	private final Map<Component, Component> formComponentMap = new HashMap<>();
	private final VerticalLayout innerContent;
	private final Button saveButton;
	private final VerticalLayout content;
	private final Label captionLabel;
	private final Image formIcon;
	private final ComponentContainer descriptionContainer;
	private final HorizontalLayout captionContainer;
	
	public FormPanel()
	{
		content = new VerticalLayout();
		content.addStyleName(CssStyle.FORM_PANEL.getStyleName());
		content.setWidth(100, Unit.PERCENTAGE);

		captionContainer = new HorizontalLayout();
		captionContainer.setStyleName(CssStyle.FORM_CAPTION_CONTAINER.getStyleName());
        captionContainer.setVisible(false);
        captionContainer.setWidth(100, Unit.PERCENTAGE);

        formIcon = new Image();
        formIcon.setStyleName(CssStyle.FORM_ICON.getStyleName());
        formIcon.setVisible(false);
        captionContainer.addComponent(formIcon);

        final VerticalLayout captionLayout = new VerticalLayout();
        captionLayout.setWidth(100, Unit.PERCENTAGE);
        captionContainer.addComponent(captionLayout);


        captionContainer.setExpandRatio(formIcon, 0.0f);
        captionContainer.setExpandRatio(captionLayout, 1.0f);

        captionLabel = new Label();
        captionLabel.addStyleName(CssStyle.FORM_CAPTION.getStyleName());
        captionLabel.setVisible(false);
        captionLabel.setSizeUndefined();
        captionLayout.addComponent(captionLabel);

		descriptionContainer = new VerticalLayout();
		descriptionContainer.setWidth(100, Unit.PERCENTAGE);
		descriptionContainer.addStyleName(CssStyle.FORM_DESCRIPTION_LONG.getStyleName());
        descriptionContainer.setVisible(false);
		captionLayout.addComponent(descriptionContainer);

		innerContent = new VerticalLayout();
		innerContent.addStyleName(CssStyle.FORM_CONTENT.getStyleName());
		innerContent.setSpacing(true);
		innerContent.setWidth(100, Unit.PERCENTAGE);
		innerContent.setHeightUndefined();
		
		saveButton = new Button(I18N.FORMPANEL_ACTION_SAVE.msg());
		saveButton.setVisible(false);
		
		content.addComponent(captionContainer);
		content.addComponent(innerContent);
		content.addComponent(saveButton);
		
		content.setComponentAlignment(saveButton, Alignment.MIDDLE_RIGHT);
		
		setContent(content);
		setHeightUndefined();
	}
	
	public FormPanel(String captionLabel)
	{
		this();
		setCaption(captionLabel);
	}
	
	@Override
	public void setCaption(String caption)
	{
		super.setCaption(null);
		captionLabel.setValue(caption);
		captionLabel.setVisible(!Strings.isNullOrEmpty(caption));
		updateCaptionVisibility();
	}
	
	/**
	 * Set description of the panel.
	 *
	 * @param descriptionText text to display
	 * @param maximumLength   length of description before cut and use "more"-button, -1 for infinite (disable "more"-button)
	 */
	public void setDescription(String descriptionText, int maximumLength)
	{
		final String newDescription = maximumLength < 0 ? descriptionText : StringUtils.left(descriptionText, maximumLength);
		
		final Label descriptionLabel = new Label();
		descriptionLabel.setContentMode(ContentMode.HTML);
		descriptionLabel.setWidth(100, Unit.PERCENTAGE);
		descriptionLabel.addStyleName(CssStyle.FORM_DESCRIPTION.getStyleName());
		descriptionLabel.setValue(newDescription);
		
		final Button moreButton = new Button(I18N.FORMPANEL_LINK_MORE.msg());
		moreButton.addStyleName(BaseTheme.BUTTON_LINK);
		moreButton.addClickListener(event -> DescriptionBox.show(descriptionText));
		//noinspection StringEquality (reference compare is correct here)
		moreButton.setVisible(descriptionText != newDescription);
		
		descriptionContainer.removeAllComponents();
		descriptionContainer.addComponents(descriptionLabel, moreButton);
		descriptionContainer.setVisible(!Strings.isNullOrEmpty(descriptionText));
        updateCaptionVisibility();
	}

    /**
     * Set Icon of the the panel.
     * @param icon icon to display
     */
    @Override
    public void setIcon(Resource icon) {
        super.setIcon(null);
        formIcon.setSource(icon);
        formIcon.setVisible(icon != null);
        updateCaptionVisibility();
    }

    /**
	 * Set description of the panel. Cut and use "more"-button after {@link #SHORT_DESCRIPTION_LENGTH}.
	 *
	 * @param descriptionText text to display
	 */
	@Override
	public void setDescription(String descriptionText)
	{
		setDescription(descriptionText, SHORT_DESCRIPTION_LENGTH);
	}
	
	public void setMargin(boolean enabled)
	{
		content.setMargin(enabled);
		innerContent.setMargin(enabled);
	}
	
	public void addComponent(Component component)
	{
		if (component != null)
		{
			innerContent.addComponent(component);
		}
	}
	
	public void addComponent(Component component, int index)
	{
		innerContent.addComponent(component, index);
	}
	
	public void removeComponent(Component component)
	{
		if (component != null)
		{
			innerContent.removeComponent(component);
		}
	}
	
	/**
	 * shows {@link Component} in {@link HorizontalLayout}
	 * with caption on the left side and {@link Component} on the right side
	 * if the {@link Component} instanceof {@link AbstractTextField} or {@link ComboBox}
	 * otherwise it shows {@link Component} as one line with caption as header
	 *
	 * @param component element to put
	 */
	public Component addFormElement(Component component)
	{
		Objects.requireNonNull(component);
		
		final Component formComponent = singleColumn(component) ? createSingleColumnLayout(component) : createDoubleColumnLayout(component);
		formComponentMap.put(component, formComponent);
		return formComponent;
	}
	
	/**
	 * Wrapped in CustomComponent to suppress the caption; caption must not removed because it is used in validation
	 *
	 * @param component
	 * @return
	 */
	private Component getClearComponent(Component component)
	{
		return new CustomComponent(component);
	}
	
	public Component addFormSeparator()
	{
        HorizontalLine separator = new HorizontalLine();
		addComponent(separator);
		return separator;
	}
	
	private VerticalLayout createSingleColumnLayout(Component component)
	{
        VerticalLayout layout = new VerticalLayout();

        if (!Strings.isNullOrEmpty(component.getCaption()) || !Strings.isNullOrEmpty(component.getDescription())) {
            HorizontalLayout labelLayout = new HorizontalLayout();
            labelLayout.setMargin(false);
            labelLayout.addStyleName(CssStyle.FORM_LABEL_LAYOUT.getStyleName());
            labelLayout.setWidth(100, Unit.PERCENTAGE);

            Label label = getLabel(component.getCaption(), isRequired(component));
            labelLayout.addComponent(label);
            labelLayout.setExpandRatio(label, 1.0f);

            addDescription(labelLayout, component.getDescription(), false);
            layout.addComponent(labelLayout);
        }

		layout.addComponent(getClearComponent(component));
		innerContent.addComponent(layout);
		
		layout.addStyleName(CssStyle.FORM_SINGLE_COLUMN_LAYOUT.getStyleName());
		return layout;
	}
	
	private Label getLabel(String caption, boolean isRequired)
	{
		Label label = new Label(caption, ContentMode.HTML);
		if (!Strings.isNullOrEmpty(caption))
		{
			label.addStyleName(CssStyle.FORM_LABEL.getStyleName());
			if (isRequired)
			{
				label.addStyleName(CssStyle.FORM_REQUIRED.getStyleName());
			}
		}
		
		return label;
	}
	
	private void addDescription(HorizontalLayout labelLayout, String description, boolean left)
	{
		if (!StringUtils.isEmpty(description))
		{
			final Button descriptionBtn = new Button("?");
			descriptionBtn.setStyleName(BaseTheme.BUTTON_LINK);
			descriptionBtn.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
			descriptionBtn.addStyleName(CssStyle.FORM_BUTTON_DESCRIPTION.getStyleName());
			descriptionBtn.setIcon(FontAwesome.INFO_CIRCLE);
			descriptionBtn.setDescription(description);
			descriptionBtn.addClickListener(event -> DescriptionBox.show(description));
			labelLayout.addComponent(descriptionBtn, left ? 0 : 1);
			labelLayout.setExpandRatio(descriptionBtn, 0.0f);
		}
	}
	
	private HorizontalLayout createDoubleColumnLayout(Component component)
	{
		final HorizontalLayout layout = new HorizontalLayout();
		layout.addStyleName(CssStyle.FORM_LAYOUT.getStyleName());
		layout.setSizeFull();
		
		HorizontalLayout labelLayout = new HorizontalLayout();
		labelLayout.setStyleName("form-panel-label-wrapper");
		labelLayout.setWidth(100, Unit.PERCENTAGE);

		
		Label label = getLabel(component.getCaption(), isRequired(component));
		labelLayout.addComponent(label);
		labelLayout.setExpandRatio(label, 1.0f);
		
		addDescription(labelLayout, component.getDescription(), true);
		layout.addComponent(labelLayout);
		
		if (component.getStyleName().isEmpty())
		{
			component.addStyleName(CssStyle.FORM_ELEMENT.getStyleName());
		}
		
		layout.addComponent(getClearComponent(component));
		layout.addStyleName(CssStyle.FORM_DOUBLE_COLUMN_LAYOUT.getStyleName());
		innerContent.addComponent(layout);
		
		return layout;
	}
	
	private boolean isRequired(Component component)
	{
		return component instanceof AbstractField && ((AbstractField<?>) component).isRequired();
	}
	
	public void addFormElements(Collection<? extends Component> components)
	{
		components.forEach(this::addFormElement);
	}
	
	public void removeFormElements(Collection<? extends Component> components)
	{
		components.forEach(this::removeFormElement);
	}
	
	public void removeFormElement(Component component)
	{
		removeComponent(formComponentMap.remove(component));
	}
	
	public void setVisibleFormElement(Component component, boolean visible)
	{
		if (!formComponentMap.containsKey(component)) return;
		formComponentMap.get(component).setVisible(visible);
	}
	
	/**
	 * @param listener
	 * @deprecated create Save-Button outside with the {@link SaveHelper} instead,
	 * because not only one FormPanel should be save, but a set of fields in-
	 * and outside the panel(s) and so the save button should not part of the panel itself.
	 */
	@Deprecated
	public void setSaveListener(ClickListener listener)
	{
		saveButton.addClickListener(listener);
		saveButton.setVisible(listener != null);
	}
	
	private boolean singleColumn(Component component)
	{
		final Class<?> componentType = component.getClass();

		return !AbstractTextField.class.isAssignableFrom(componentType)
                && !CheckBox.class.isAssignableFrom(componentType)
                && (!AbstractSelect.class.isAssignableFrom(componentType) || ((AbstractSelect) component).isMultiSelect())
                && !Label.class.isAssignableFrom(componentType)
                && !BirthDateField.class.isAssignableFrom(componentType)
				&& !LabelField.class.isAssignableFrom(componentType)
				&& !ToggleButton.class.isAssignableFrom(componentType);
	}

    /**
     * Checks if caption, description and icon is not empty and sets the
     * Form Title visible or not
     */
	private void updateCaptionVisibility() {
	    captionContainer.setVisible(formIcon.isVisible() || captionLabel.isVisible() || descriptionContainer.isVisible());
    }

	/**
	 * @deprecated {@see #setSaveListener}
	 * @param saveButtonCaption
	 */
	@Deprecated
	public void setSaveButtonCaption(String saveButtonCaption)
	{
		saveButton.setCaption(saveButtonCaption);
	}
	
	@Override
	public int compareTo(FormPanel o)
	{
		return this.hashCode() - o.hashCode();
	}
	
	public void removeAllComponents()
	{
		this.innerContent.removeAllComponents();
	}
	
}
