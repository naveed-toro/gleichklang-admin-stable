package de.binaerebauten.gleichklang.adminweb.view;

import com.vaadin.data.util.converter.Converter.ConversionException;
import com.vaadin.ui.*;
import de.binaerebauten.gleichklang.adminweb.view.ScammingView.ScammingViewListener;
import de.binaerebauten.gleichklang.core.model.message.Scamming;
import de.binaerebauten.gleichklang.core.model.message.Scamming_;
import de.binaerebauten.gleichklang.core.model.user.User_;
import de.binaerebauten.gleichklang.core.view.AbstractNavigateView;
import de.binaerebauten.gleichklang.core.view.component.ComponentFactory;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanItemContainer.LazyBeanFilteredItemsHandler;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanTable;
import de.binaerebauten.gleichklang.core.view.component.MessageBox;
import de.binaerebauten.gleichklang.core.view.component.TableControl;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class ScammingViewImpl extends AbstractNavigateView<ScammingViewListener> implements ScammingView
{
	private final LazyBeanTable<Scamming> resultTable;
	
	private Consumer<Long> messageCountChanger = null;
	private Consumer<Long> durationChanger = null;
	private Consumer<String> keywordsChanger = null;
	private Consumer<Boolean> containsEmailChanger = null;
	
	public ScammingViewImpl()
	{
		resultTable = createResultTable();
		setCompositionRoot(createLayout());
	}
	
	private LazyBeanTable<Scamming> createResultTable()
	{
		final LazyBeanTable<Scamming> resultTable = new LazyBeanTable<>();
		
		resultTable.addContainerProperty(I18N.USERMANAGE_HEADER_EMAIL.msg(), Scamming_.user, User_.email);
		resultTable.addContainerProperty(I18N.USERMANAGE_HEADER_ALIAS.msg(), Scamming_.user, User_.alias);
		resultTable.addContainerProperty(I18N.USERMANAGE_HEADER_LASTNAME.msg(), Scamming_.user, User_.lastName);
		resultTable.addContainerProperty(I18N.USERMANAGE_HEADER_FIRSTNAME.msg(), Scamming_.user, User_.firstName);
		resultTable.addContainerProperty(I18N.USERMANAGE_HEADER_STATUS.msg(), Scamming_.user, User_.memberStatus);
		
		return resultTable;
	}
	
	private Component createLayout()
	{
		final VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);
		layout.setSpacing(true);
		
		layout.addComponent(createFilterComponent());
		layout.addComponent(createResultComponent());
		
		return layout;
	}
	
	private Component createResultComponent()
	{
		final TableControl<Scamming> tableControl = new TableControl<>(resultTable);
		tableControl.addButton("Anzeigen", scamming -> getListener().openUser(scamming.getUser()));
		
		return tableControl;
	}
	
	private Component createFilterComponent()
	{
		final VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);
		
		final TextField messageCountTextField = ComponentFactory.getInstance().createField(TextField.class, "Anzahl an Nachrichten");
		final TextField durationTextField = ComponentFactory.getInstance().createField(TextField.class, "Zeitspanne der Nachrichten in Sekunden");
		final CheckBox containsEmailCheckBox = ComponentFactory.getInstance().createField(CheckBox.class, "Soll E-Mail beinhalten");
		final TextField keywordsTextField = ComponentFactory.getInstance().createField(TextField.class, "Weitere Schlüsselwörter (Kommasepariert)");
		
		messageCountTextField.setConverter(Long.class);
		durationTextField.setConverter(Long.class);
		
		messageCountChanger = messageCountTextField::setConvertedValue;
		durationChanger = durationTextField::setConvertedValue;
		containsEmailChanger = containsEmailCheckBox::setValue;
		keywordsChanger = keywordsTextField::setValue;
		
		final Button searchButton = new Button("Suche");
		searchButton.addClickListener(event ->
		{
			try
			{
				final long messageCount = (long) messageCountTextField.getConvertedValue();
				final long durationSeconds = (long) durationTextField.getConvertedValue();
				final boolean containsEmail = containsEmailCheckBox.getValue();
				final List<String> keywords = Arrays.asList(keywordsTextField.getValue().split("\\s*,\\s*"));
				
				if(messageCount <= 0 || durationSeconds <= 0) throw new ConversionException("zero values not allowed");
				
				getListener().search(messageCount, durationSeconds, containsEmail, keywords);
			}
			catch (ConversionException ex)
			{
				MessageBox.show(ex.getLocalizedMessage());
			}
		});
		
		final Button refreshButton = new Button("Aktualisieren");
		refreshButton.addClickListener(event -> getListener().refresh());
		
		layout.addComponent(messageCountTextField);
		layout.addComponent(durationTextField);
		layout.addComponent(containsEmailCheckBox);
		layout.addComponent(keywordsTextField);
		layout.addComponent(searchButton);
		layout.addComponent(refreshButton);
		
		return layout;
	}
	
	@Override
	public void initValues(long messageCount, long durationSeconds, boolean containsEmail, String keywords)
	{
		messageCountChanger.accept(messageCount);
		durationChanger.accept(durationSeconds);
		containsEmailChanger.accept(containsEmail);
		keywordsChanger.accept(keywords);
	}
	
	@Override
	public void addResult(LazyBeanFilteredItemsHandler<Scamming> scammingHandler)
	{
		resultTable.setHandler(scammingHandler);
	}
}
