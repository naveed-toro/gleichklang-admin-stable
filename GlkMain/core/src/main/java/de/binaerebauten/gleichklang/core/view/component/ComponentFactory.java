package de.binaerebauten.gleichklang.core.view.component;

import com.vaadin.data.Item;
import com.vaadin.data.fieldgroup.FieldGroup.BindException;
import com.vaadin.data.fieldgroup.FieldGroupFieldFactory;
import com.vaadin.server.*;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import de.binaerebauten.gleichklang.core.config.SpringProfile;
import de.binaerebauten.gleichklang.core.model.I18NEntity;
import de.binaerebauten.gleichklang.core.view.component.converter.LocalDateConverter;
import de.binaerebauten.gleichklang.core.view.component.converter.LocalDateStringConverter;
import de.binaerebauten.gleichklang.core.view.component.converter.LocalDateTimeConverter;
import de.binaerebauten.gleichklang.core.view.component.converter.LocalDateTimeStringConverter;
import de.binaerebauten.gleichklang.core.view.converter.SetConverter;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@SuppressWarnings("serial")
public class ComponentFactory implements FieldGroupFieldFactory
{
	private static final Object CAPTION_PROPERTY_ID = "Caption";

	private static final ComponentFactory INSTANCE = new ComponentFactory();
	
	private ComponentFactory()
	{
	}
	
	public static ComponentFactory getInstance()
	{
		return INSTANCE;
	}
	
	/**
	 * @param fieldType the type of the field
	 * @return true if any AbstractField can be assigned to the field
	 */
	private boolean anyField(Class<?> fieldType)
	{
		return fieldType == Field.class || fieldType == AbstractField.class;
	}
	
	private RichTextArea createRichTextArea()
	{
		final RichTextArea rta = new RichTextArea();
		rta.setImmediate(true);
		rta.setSizeFull();
		rta.setNullRepresentation("");
		return rta;
	}
	
	@SuppressWarnings("unchecked")
	private <T extends AbstractTextField> T createAbstractTextField(Class<?> type, Class<T> fieldType)
	{
		final Class<T> abstractTextFieldType = fieldType == AbstractTextField.class ? (Class<T>) TextField.class : fieldType;
		
		try
		{
			final T field = abstractTextFieldType.newInstance();
			field.setImmediate(true);
			field.setMaxLength(255);
			field.setNullRepresentation("");
			field.setSizeFull();
			if (type == Integer.class || type == int.class)
			{
				field.setConverter(type);
			}

			field.addValueChangeListener((event) -> {
				if (field.getValue() == null)
					field.addStyleName(CssStyle.EMPTY_ANSWER.getStyleName());
				else
					field.addStyleName(CssStyle.ANSWERED.getStyleName());
			});
			return field;
		}
		catch (final Exception e)
		{
			throw new BindException("Could not create a field of type " + fieldType, e);
		}
	}
	
	@SuppressWarnings("unchecked")
	private <T extends AbstractSelect> T createCompatibleSelect(Class<T> fieldType)
	{
		final AbstractSelect select;
		if (ListSelect.class.isAssignableFrom(fieldType))
		{
			select = new ListSelect();
			select.setMultiSelect(false);
		}
		else if (NativeSelect.class.isAssignableFrom(fieldType))
		{
			select = new NativeSelect();
		}
		else if (OptionGroup.class.isAssignableFrom(fieldType))
		{
			select = new OptionGroup();
			select.setMultiSelect(false);
		}
		else if (Table.class.isAssignableFrom(fieldType))
		{
			final Table t = new Table();
			t.setSelectable(true);
			select = t;
		}
		else if (TwinColSelect.class.isAssignableFrom(fieldType))
		{
			select = new TwinColSelect();
			select.setMultiSelect(false);
		}
		else
		{
			select = new ComboBox();
			((ComboBox)select).setFilteringMode(FilteringMode.CONTAINS);
		}

		select.addValueChangeListener((event) -> {
			if (select.getValue() == null)
				select.addStyleName(CssStyle.EMPTY_ANSWER.getStyleName());
			else
				select.addStyleName(CssStyle.ANSWERED.getStyleName());
		});

		select.setImmediate(true);
		select.setNullSelectionAllowed(false);
		select.setSizeFull();

		return (T) select;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private <T extends Field> T createDefaultField(Class<?> type, Class<T> fieldType)
	{
		if (RichTextArea.class.isAssignableFrom(fieldType))
		{
			return (T) createRichTextArea();
		}
		
		if (LabelField.class.isAssignableFrom(fieldType))
		{
			return (T) createLabelField(type);
		}
		
		if (AbstractSelect.class.isAssignableFrom(fieldType))
		{
			return (T) createCompatibleSelect((Class<AbstractSelect>) fieldType);
		}
		
		if (AbstractTextField.class.isAssignableFrom(fieldType))
		{
			return (T) createAbstractTextField(type, (Class<AbstractTextField>) fieldType);
		}
		
		if (CheckBox.class.isAssignableFrom(fieldType))
		{
			return (T) createCheckBox();
		}
		
		if (anyField(fieldType))
		{
			return (T) createAbstractTextField(type, TextField.class);
		}
		
		return null;
	}
	
	/**
	 * Creates a new field for a given type and value type.
	 * If the value type is enum, it populates the field with enum values.
	 *
	 * @param type type of values in the field
	 * @param fieldType class of the field
	 * @return a new field
	 */
	@SuppressWarnings({ "rawtypes" })
	@Override
	public <T extends Field> T createField(Class<?> type, Class<T> fieldType)
	{
		T field = null;
		if (Enum.class.isAssignableFrom(type))
		{
			field = createEnumField(type, fieldType);
		}
		else if (LocalDate.class.isAssignableFrom(type) || LocalDateTime.class.isAssignableFrom(type))
		{
			field = createDateField(type, fieldType);
		}
		else if (Boolean.class.isAssignableFrom(type) || boolean.class.isAssignableFrom(type))
		{
			field = createBooleanField(type, fieldType);
		}
		else if (Set.class.isAssignableFrom(type))
		{
			field = createMultiSelectField(fieldType);
		}

		return Objects.nonNull(field) ? field : createDefaultField(type, fieldType);
	}
	
	@SuppressWarnings("unchecked")
	private <T extends Field> T createEnumField(Class<?> type, Class<T> fieldType)
	{
		// Determine first if we should (or can) create a select for the enum
		Class<AbstractSelect> selectClass = null;
		if (AbstractSelect.class.isAssignableFrom(fieldType))
		{
			selectClass = (Class<AbstractSelect>) fieldType;
		}
		else if (anyField(fieldType))
		{
			selectClass = AbstractSelect.class;
		}
		
		if (selectClass != null)
		{
			final AbstractSelect s = createCompatibleSelect(selectClass);
			populateWithEnumData(s, (Class<? extends Enum<?>>) type);
			return (T) s;
		}
		
		return null;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private <T extends Field> T createMultiSelectField(Class<T> fieldType)
	{
		Class<? extends AbstractSelect> selectClass = null;
		if (AbstractSelect.class.isAssignableFrom(fieldType))
		{
			selectClass = (Class<AbstractSelect>) fieldType;
		}
		else if (anyField(fieldType))
		{
			selectClass = ListSelect.class;
		}
		
		if (selectClass != null)
		{
			final AbstractSelect s = createCompatibleSelect(selectClass);
			
			if (s instanceof ComboBox)
			{
				s.setConverter(new SetConverter());
			}
			else
			{
				s.setMultiSelect(true);
			}
			return (T) s;
		}
		
		return null;
	}

	/**
	 * Populates the given select with all the enums in the given {@link Enum}
	 * class. Uses {@link Enum}.toString() for caption.
	 *
	 * @param select    The select to populate
	 * @param enumClass The Enum class to use
	 */
	public void populateWithEnumData(AbstractSelect select, Class<? extends Enum> enumClass)
	{
		populateWithEnumData(select, EnumSet.allOf(enumClass));
	}

	/**
	 * Populates the given select with the enum values given by the items parameter.
	 * Uses {@link Enum}.toString() for caption.
	 *
	 * This method should be used to initialize a select component with a restricted set
	 * of enum values.
	 *
	 * @param select    The select to populate
	 * @param items     The Enum items to use
	 */
	@SuppressWarnings({ "unchecked" })
	public void populateWithEnumData(AbstractSelect select, Set<?> items)
	{
		select.removeAllItems();
		select.addContainerProperty(CAPTION_PROPERTY_ID, String.class, "");
		select.setItemCaptionPropertyId(CAPTION_PROPERTY_ID);
		for (final Object r : items)
		{
			final Item newItem = select.addItem(r);
			newItem.getItemProperty(CAPTION_PROPERTY_ID).setValue(r.toString());
		}
	}
	
	private CheckBox createCheckBox()
	{
		final CheckBox cb = new CheckBox(null);
		cb.setImmediate(true);
		return cb;
	}
	
	@SuppressWarnings("unchecked")
	private <T extends Field> T createBooleanField(Class<?> type, Class<T> fieldType)
	{
		Class<? extends AbstractSelect> selectFieldClass = null;
		
		if (Boolean.class.isAssignableFrom(type) && anyField(fieldType))
		{
			selectFieldClass = OptionGroup.class;
		}
		else if (AbstractSelect.class.isAssignableFrom(fieldType))
		{
			selectFieldClass = (Class<AbstractSelect>) fieldType;
		}
		
		if (selectFieldClass != null)
		{
			final AbstractSelect abstractSelect = createCompatibleSelect(selectFieldClass);
			abstractSelect.addItems(Boolean.TRUE, Boolean.FALSE);
			abstractSelect.setItemCaption(Boolean.TRUE, I18N.COMPONENTFACTORY_LABEL_BOOLEANOPTIONTRUE.msg());
			abstractSelect.setItemCaption(Boolean.FALSE, I18N.COMPONENTFACTORY_LABEL_BOOLEANOPTIONFALSE.msg());
			
			return (T) abstractSelect;
		}
		
		if (fieldType.isAssignableFrom(CheckBox.class))
		{
			return (T) createCheckBox();
		}
		
		return null;
	}

	/**
	 * Creates {@link Link} for the given label, locale and path
	 * Resources are located under webapp/ {locale.language} /resources
	 * @param linkLabel
	 * @param locale
	 * @param pathToResource
	 * @param newTab open in new tab?
	 * @return
	 */
	public Link createLink(String linkLabel, Locale locale, String pathToResource, boolean newTab)
	{
		final I18NEntity.Language language = I18NEntity.Language.valueOf(locale);
		final String languageString = language != null ? language.toString() : I18NEntity.Language.DE.toString();

		String basepath = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath();
		final String path = basepath + "/" + languageString + pathToResource;
		FileResource resource = new FileResource(new File(path));
		final Link link = new Link(linkLabel, resource);
		if(newTab){
			link.setTargetName("_blank");
		}
		return link;
	}

	public Link createExternalLink(String linkLabel, String url, boolean newTab){
		Link link = new Link(linkLabel,  new ExternalResource(url));
		if(newTab){
			link.setTargetName("_blank");
		}
		return link;
	}


	@SuppressWarnings("unchecked")
	private <T extends Field> T createDateField(Class<?> type, Class<T> fieldType)
	{
		final DateField field;
		
		if (InlineDateField.class.isAssignableFrom(fieldType))
		{
			field = new InlineDateField();
		}
		else if (anyField(fieldType) || DateField.class.isAssignableFrom(fieldType))
		{
			field = new PopupDateField();
		}
		else
		{
			return null;
		}
		
		field.setImmediate(true);
		field.setSizeFull();

		if(type != null)
		{
			if (LocalDate.class.isAssignableFrom(type))
			{
				field.setConverter(new LocalDateConverter());
			}
			else if (LocalDateTime.class.isAssignableFrom(type))
			{
				field.setConverter(new LocalDateTimeConverter());
			}
		}

		return (T) field;
	}
	
	/**
	 * Creates a field based on the data type that we want to edit
	 *
	 * @param type The type that we want to edit using the field
	 * @return A field that is capable of editing the given type of data
	 */
	public Field<?> createFieldByType(Class<?> type)
	{
		return createField(type, Field.class);
	}
	
	/**
	 * Creates a field based on the data type that we want to edit
	 *
	 * @param type    The type that we want to edit using the field
	 * @param caption The caption of the field
	 * @return A field that is capable of editing the given type of data
	 */
	public Field<?> createFieldByType(Class<?> type, String caption)
	{
		final Field<?> field = createField(type, Field.class);
		field.setCaption(caption);
		return field;
	}
	
	/**
	 * Creates a field
	 *
	 * @param fieldType The type of field we want to create.
	 * @return
	 */
	public <T extends Field<?>> T createField(Class<T> fieldType)
	{
		return createField(fieldType, (String) null);
	}
	
	/**
	 * Creates a field
	 *
	 * @param fieldType The type of field we want to create.
	 * @return A field that can be assigned to the given fieldType
	 */
	@SuppressWarnings("unchecked")
	public <T extends Field<?>> T createField(Class<T> fieldType, String caption)
	{
		final T field;
		if (DateField.class.isAssignableFrom(fieldType))
		{
			field = (T) createDateField(null, (Class < ?extends DateField>) fieldType);
		}
		else if (CheckBox.class.isAssignableFrom(fieldType))
		{
			field = createBooleanField(Boolean.class, fieldType);
		}
		else
		{
			field = createField(Object.class, fieldType);
		}
		
		if (Objects.nonNull(field))
		{
			field.setCaption(caption);
		}
		
		return field;
	}
	
	private LabelField createLabelField(Class<?> type)
	{
		final LabelField labelField = new LabelField();
		labelField.setValidationVisible(false);
		if (LocalDate.class.isAssignableFrom(type))
		{
			labelField.setConverter(new LocalDateStringConverter());
		}
		if(LocalDateTime.class.isAssignableFrom(type))
		{
			labelField.setConverter(new LocalDateTimeStringConverter());
		}
		
		return labelField;
	}

	public Component getLogo()
	{
		VerticalLayout logo = new VerticalLayout();

		final Image image = new Image();
		image.setSource(new ThemeResource("img/logo.svg"));
		image.setStyleName(CssStyle.LOGO.getStyleName());

		logo.addComponent(image);
		logo.setComponentAlignment(image, Alignment.MIDDLE_CENTER);

		return logo;
	}

	public Component getBuildNumberComponent() {
	    HorizontalLayout layout = new HorizontalLayout();
	    layout.setWidth(100, Sizeable.Unit.PERCENTAGE);
	    layout.setVisible(false);
        if (!SpringProfile.PROD.isActive())
        {
            Link buildInfo = new Link("Build Info", new ExternalResource("api/build"));
            buildInfo.addStyleName(CssStyle.BUILD_LABEL.getStyleName());
            layout.addComponent(buildInfo);
            layout.setComponentAlignment(buildInfo, Alignment.TOP_RIGHT);
            layout.setVisible(true);
        }

        return layout;
    }

	public Component getSmallLogo() {

		final Image image = new Image();
        image.setSource(new ThemeResource("img/logo_small.svg"));
        image.setStyleName(CssStyle.LOGO.getStyleName());
		image.setHeight(40, Sizeable.Unit.PIXELS);
		image.setWidth(40, Sizeable.Unit.PIXELS);

        return image;
	}

	public Label createBoldLabel(String text)
	{
		final Label label = new Label("<B>" + text + "</B>");
		label.setContentMode(ContentMode.HTML);
		return label;
	}
	
}
