package de.binaerebauten.gleichklang.adminweb.view;

import com.vaadin.ui.*;
import de.binaerebauten.gleichklang.adminweb.view.TranslationView.TranslationViewListener;
import de.binaerebauten.gleichklang.core.model.I18NEntity;
import de.binaerebauten.gleichklang.core.model.I18NEntity.BaseName;
import de.binaerebauten.gleichklang.core.model.I18NEntity.Language;
import de.binaerebauten.gleichklang.core.model.I18NEntity_;
import de.binaerebauten.gleichklang.core.view.AbstractNavigateView;
import de.binaerebauten.gleichklang.core.view.component.ComponentFactory;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanItemContainer.LazyBeanFilteredItemsHandler;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanTable;
import de.binaerebauten.gleichklang.core.view.component.TableControl;
import de.binaerebauten.gleichklang.core.view.filter.SimpleAttributeFilter;
import de.binaerebauten.gleichklang.core.view.filter.SimpleStringFilter;

public class TranslationViewImpl extends AbstractNavigateView<TranslationViewListener> implements TranslationView
{
	private final LazyBeanTable<I18NEntity> translationTable;

	public TranslationViewImpl()
	{
		translationTable = createTranslationTable();

		final VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);
		layout.setMargin(true);
		layout.setSizeFull();

		layout.addComponent(createFilterComponent());
		layout.addComponent(createTranslationAdministration());

		setCompositionRoot(layout);
	}

	private Component createFilterComponent()
	{
		final SimpleAttributeFilter<I18NEntity, BaseName> baseNameFilter = new SimpleAttributeFilter<>(I18NEntity_.baseName);
		final SimpleStringFilter<I18NEntity> keyFilter = new SimpleStringFilter<>(I18NEntity_.key);
		final SimpleStringFilter<I18NEntity> valueFilter = new SimpleStringFilter<>(I18NEntity_.value);
		final SimpleAttributeFilter<I18NEntity, Language> languageFilter = new SimpleAttributeFilter<>(I18NEntity_.language);
		
		baseNameFilter.setItemComponent(translationTable);
		keyFilter.setItemComponent(translationTable);
		valueFilter.setItemComponent(translationTable);
		languageFilter.setItemComponent(translationTable);

		final HorizontalLayout layout = new HorizontalLayout();
		layout.setSpacing(true);

		final ComboBox prefixComboBox = ComponentFactory.getInstance().createField(BaseName.class, ComboBox.class);
		prefixComboBox.setCaption("Basename");
		prefixComboBox.setNullSelectionAllowed(true);
		prefixComboBox.addValueChangeListener(event -> baseNameFilter.setValue((BaseName) prefixComboBox.getValue()));
		
		final TextField keyTextField = ComponentFactory.getInstance().createField(TextField.class, "Key");
		keyTextField.addTextChangeListener(event -> keyFilter.setValue(event.getText()));
		
		final TextField valueTextField = ComponentFactory.getInstance().createField(TextField.class, "Value");
		valueTextField.addTextChangeListener(event -> valueFilter.setValue(event.getText()));
		
		final ComboBox languageComboBox = ComponentFactory.getInstance().createField(Language.class, ComboBox.class);
		languageComboBox.setCaption("Language");
		languageComboBox.setNullSelectionAllowed(true);
		languageComboBox.addValueChangeListener(event -> languageFilter.setValue((Language) languageComboBox.getValue()));

		layout.addComponents(prefixComboBox, keyTextField, valueTextField, languageComboBox);

		return layout;
	}

	private Component createTranslationAdministration()
	{
		final TableControl<I18NEntity> tableControl = new TableControl<>(translationTable);

		tableControl.setEditCallback(item -> fireEvent(action -> action.editI18NEntity(item)));

		return tableControl;
	}

	private LazyBeanTable<I18NEntity> createTranslationTable()
	{
		final LazyBeanTable<I18NEntity> table = new LazyBeanTable<>();
		table.setSelectable(true);
		table.setSizeFull();

		table.addContainerProperty(I18N.TRANSLATION_HEADER_KEY.msg(), I18NEntity_.key);
		table.addContainerProperty(I18N.TRANSLATION_HEADER_VALUE.msg(), I18NEntity_.value);

		table.setSortPropertyId(I18NEntity_.key);

		return table;
	}

	@Override
	public void setTranslationHandler(LazyBeanFilteredItemsHandler<I18NEntity> handler)
	{
		translationTable.setHandler(handler);
	}
	
}
