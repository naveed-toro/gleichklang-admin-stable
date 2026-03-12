package de.binaerebauten.gleichklang.core.view.component;

import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup.BindException;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Field;
import de.binaerebauten.gleichklang.core.utils.PropertyPathBuilder;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;

import javax.persistence.metamodel.Attribute;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class ComponentGroup<T>
{
	private final BeanFieldGroup<T> beanFieldGroup;
	private final List<Field<?>> additionalFields = new ArrayList<>();
	
	public ComponentGroup(Class<T> beanType)
	{
		this(beanType, null);
	}
	
	public ComponentGroup(Class<T> beanType, T dataSource)
	{
		beanFieldGroup = new BeanFieldGroup<>(beanType);
		beanFieldGroup.setFieldFactory(ComponentFactory.getInstance());
		setItemDataSource(dataSource);
	}

	private void initStyleListener(Field<?> field)
	{
		field.addValueChangeListener(event ->
		{
			if (event.getProperty().getValue() != null)
			{
				field.removeStyleName(CssStyle.EMPTY_ANSWER.getStyleName());
				field.addStyleName(CssStyle.ANSWERED.getStyleName());
			}
			else
			{
				field.removeStyleName(CssStyle.ANSWERED.getStyleName());
				field.addStyleName(CssStyle.EMPTY_ANSWER.getStyleName());
			}

		});
	}
	
	/**
	 * Binds the field with the given attribute from the current item. If an
	 * item has not been set then the binding is postponed until the item is set
	 * using {@link #setItemDataSource(Object)}.
	 * <p>
	 * This method also adds validators when applicable.
	 * </p>
	 *
	 * @param field     The field to bind
	 * @param attribute The attribute to bind to the field
	 * @throws BindException If the field is null or the property id is already bound to
	 *                       another field by this field binder
	 */
	public Field<?> bind(Field<?> field, Attribute<?, ?>... attribute)
			throws BindException
	{
		beanFieldGroup.bind(field, PropertyPathBuilder.getFieldName(attribute));
		initStyleListener(field);
		return field;
	}
	
	public Field<?> buildAndBind(Attribute<?, ?>... attribute)
			throws BindException
	{
		return buildAndBind(false, (String) null, Field.class, attribute);
	}
	
	public <F extends Field<?>> F buildAndBind(Class<F> fieldType, Attribute<?, ?>... attribute)
			throws BindException
	{
		return buildAndBind(false, (String) null, fieldType, attribute);
	}
	
	public Field<?> buildAndBind(String caption, Attribute<?, ?>... singleAttribute)
			throws BindException
	{
		return buildAndBind(false, caption, Field.class, singleAttribute);
	}
	
	public <F extends Field<?>> F buildAndBind(String caption, Class<F> fieldType, Attribute<?, ?>... attribute)
			throws BindException
	{
		return buildAndBind(false, caption, fieldType, attribute);
	}
	
	public Field<?> buildAndBind(boolean required, Attribute<?, ?>... attribute)
			throws BindException
	{
		return buildAndBind(required, (String) null, Field.class, attribute);
	}
	
	public Field<?> buildAndBind(boolean required, String caption, Attribute<?, ?>... attribute)
			throws BindException
	{
		return buildAndBind(required, caption, Field.class, attribute);
	}
	
	public <F extends Field<?>> F buildAndBind(boolean required, Class<F> fieldType, Attribute<?, ?>... attribute)
			throws BindException
	{
		return buildAndBind(required, (String) null, fieldType, attribute);
	}
	
	public <F extends Field<?>> F buildAndBind(boolean required, String caption, Class<F> fieldType, Attribute<?, ?>... attribute)
			throws BindException
	{
		final F field = beanFieldGroup.buildAndBind(caption, PropertyPathBuilder.getFieldName(attribute), fieldType);
		field.setRequired(required);
		initStyleListener(field);

		return field;
	}
	
	public void commit() throws CommitException
	{
		beanFieldGroup.commit();
	}
	
	/**
	 * Returns true iff. this component has uncommited changes.
	 *
	 * @return true iff. there are uncommited changes
	 */
	public boolean isModified()
	{
		return beanFieldGroup.isModified();
	}
	
	public BeanItem<T> getItemDataSource()
	{
		return beanFieldGroup.getItemDataSource();
	}
	
	public void setItemDataSource(T bean)
	{
		beanFieldGroup.setItemDataSource(bean);
	}
	
	public void addAdditionalFields(Field<?>... fields)
	{
		additionalFields.addAll(Arrays.asList(fields));
	}
	
	public Collection<Field<?>> getFields()
	{
		final List<Field<?>> allFields = new ArrayList<>(additionalFields);
		allFields.addAll(beanFieldGroup.getFields());
		return allFields;
	}
	
	public void unbindAll()
	{
		new ArrayList<>(beanFieldGroup.getFields()).forEach(beanFieldGroup::unbind);
	}
}
