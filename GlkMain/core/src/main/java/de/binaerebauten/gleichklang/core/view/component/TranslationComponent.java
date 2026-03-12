package de.binaerebauten.gleichklang.core.view.component;

import com.google.common.base.Preconditions;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.ui.*;
import de.binaerebauten.gleichklang.core.model.I18NEntity;
import de.binaerebauten.gleichklang.core.model.I18NEntity.BaseName;
import de.binaerebauten.gleichklang.core.model.I18NEntity.Language;
import de.binaerebauten.gleichklang.core.model.I18NEntity_;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;

import java.util.Collection;
import java.util.HashSet;

public class TranslationComponent extends CustomComponent
{
	private final ComponentGroup<I18NEntity> i18NEntityComponentGroup;
	private final HashSet<I18NEntity> i18NEntities;

	public TranslationComponent(Collection<I18NEntity> i18NEntities, BaseName baseName)
	{
		this(I18N.TRANSLATION_COMPONENT_NAME.msg(), i18NEntities, baseName, TextField.class, true);
	}

	public TranslationComponent(String caption, Collection<I18NEntity> i18NEntities, BaseName baseName)
	{
		this(caption, i18NEntities, baseName, TextField.class, true);
	}

	public TranslationComponent(String caption, Collection<I18NEntity> i18NEntities, BaseName baseName, Class<? extends Field<?>> fieldType, boolean required)
	{
		this.i18NEntityComponentGroup = new ComponentGroup<>(I18NEntity.class);
		this.i18NEntities = new HashSet<>();

		if (i18NEntities != null) this.i18NEntities.addAll(i18NEntities);

		Preconditions.checkNotNull(baseName);
		Preconditions.checkArgument(this.i18NEntities.stream().allMatch(i18NEntity -> baseName.equals(i18NEntity.getBaseName())));

		final I18NEntity germanEntity = findOrCreateGermanEntity(this.i18NEntities, baseName);
		this.i18NEntities.add(germanEntity);
		i18NEntityComponentGroup.setItemDataSource(germanEntity);

		final Component component = i18NEntityComponentGroup.buildAndBind(required, caption, fieldType, I18NEntity_.value);

		if (required)
			addStyleName(CssStyle.REQUIRED_FIELD.getStyleName());

		setCaption(caption);
		setCompositionRoot(component);
	}

	private I18NEntity findOrCreateGermanEntity(Collection<I18NEntity> i18NEntities, BaseName baseName)
	{
		final Language german = Language.DE;

		for (I18NEntity entity : i18NEntities)
		{
			if (german.equals(entity.getLanguage()))
				return entity;
		}

		final I18NEntity entity = new I18NEntity();
		entity.setLanguage(german);
		entity.setBaseName(baseName);
		entity.setValue("");
		return entity;
	}

	public HashSet<I18NEntity> commit(String i18nKey) throws CommitException
	{
		i18NEntityComponentGroup.commit();
		setI18nKey(i18nKey);
		return getI18NEntities();
	}
	
	public Collection<Field<?>> getFields()
	{
		return i18NEntityComponentGroup.getFields();
	}
	
	public void setI18nKey(String i18nKey)
	{
		i18NEntities.forEach(i18NEntity -> i18NEntity.setKey(i18nKey));
	}
	
	public HashSet<I18NEntity> getI18NEntities()
	{
		return i18NEntities;
	}
	
	/**
	 * Returns a validatable field of the component.
	 *
	 * @return
	 */
	public Field<?> getValidatableComponent()
	{
		Component root = getCompositionRoot();
		if (root instanceof Field) {
			return (Field<?>) root;
		}

		return null;
	}

}
