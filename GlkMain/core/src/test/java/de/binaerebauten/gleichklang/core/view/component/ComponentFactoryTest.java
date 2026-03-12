package de.binaerebauten.gleichklang.core.view.component;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.vaadin.ui.*;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertThat;

public class ComponentFactoryTest
{
	public enum TestEnum
	{
		ONE,
		TWO
	}
	
	private final ComponentFactory componentFactory = ComponentFactory.getInstance();
	private final Multimap<Class<?>, Class<? extends Field<?>>> autoFields = ArrayListMultimap.create();
	private final Multimap<Class<?>, Class<? extends Field<?>>> manualFields = ArrayListMultimap.create();
	
	public ComponentFactoryTest()
	{
		autoFields.put(String.class, TextField.class);
		autoFields.put(boolean.class, CheckBox.class);
		autoFields.put(Boolean.class, OptionGroup.class);
		autoFields.put(TestEnum.class, ComboBox.class);
		autoFields.put(LocalDate.class, DateField.class);
		autoFields.put(LocalDateTime.class, DateField.class);
		autoFields.put(Integer.class, TextField.class);
		autoFields.put(int.class, TextField.class);
		autoFields.put(Set.class, ListSelect.class);
		
		manualFields.putAll(autoFields);
		manualFields.put(String.class, PasswordField.class);
		manualFields.put(String.class, LabelField.class);
		manualFields.put(String.class, RichTextArea.class);
		manualFields.put(String.class, TextArea.class);
		manualFields.put(LocalDate.class, LabelField.class);
		manualFields.put(LocalDate.class, InlineDateField.class);
		manualFields.put(LocalDateTime.class, LabelField.class);
		manualFields.put(LocalDateTime.class, InlineDateField.class);
		manualFields.put(TestEnum.class, ListSelect.class);
		manualFields.put(TestEnum.class, NativeSelect.class);
		manualFields.put(TestEnum.class, OptionGroup.class);
		manualFields.put(TestEnum.class, Table.class);
		manualFields.put(Boolean.class, CheckBox.class);
		manualFields.put(boolean.class, OptionGroup.class);
		manualFields.put(Set.class, ComboBox.class);
		manualFields.put(Set.class, OptionGroup.class);
		manualFields.put(Set.class, TwinColSelect.class);
	}
	
	@Test
	public void testCreateFieldByType()
	{
		for (final Map.Entry<Class<?>, Class<? extends Field<?>>> entry : autoFields.entries())
		{
			final Field<?> field = componentFactory.createFieldByType(entry.getKey());
			assertThat("Key: " + entry.getKey(), field, CoreMatchers.instanceOf(entry.getValue()));
		}
	}
	
	@Test
	public void testCreateFieldByTypeWithCaption()
	{
		final String testCaption = "testCaption";
		for (final Map.Entry<Class<?>, Class<? extends Field<?>>> entry : autoFields.entries())
		{
			final Field<?> field = componentFactory.createFieldByType(entry.getKey(), testCaption);
			assertThat("Key: " + entry.getKey(), field, CoreMatchers.instanceOf(entry.getValue()));
			assertThat(field.getCaption(), CoreMatchers.is(testCaption));
		}
	}
	
	@Test
	public void testCreateField()
	{
		for (final Class<? extends Field<?>> value : manualFields.values())
		{
			final Field<?> field = componentFactory.createField(value);
			assertThat(field, CoreMatchers.instanceOf(value));
		}
	}
	
	@Test
	public void testCreateFieldWithCaption()
	{
		final String testCaption = "testCaption";
		for (final Class<? extends Field<?>> value : manualFields.values())
		{
			final Field<?> field = componentFactory.createField(value, testCaption);
			assertThat(field, CoreMatchers.instanceOf(value));
			assertThat(field.getCaption(), CoreMatchers.is(testCaption));
		}
	}
	
	@Test
	public void testCreateFieldSuper()
	{
		for (final Map.Entry<Class<?>, Class<? extends Field<?>>> entry : manualFields.entries())
		{
			final Field<?> field = componentFactory.createField(entry.getKey(), entry.getValue());
			assertThat("Key: " + entry.getKey(), field, CoreMatchers.instanceOf(entry.getValue()));
		}
	}
}
