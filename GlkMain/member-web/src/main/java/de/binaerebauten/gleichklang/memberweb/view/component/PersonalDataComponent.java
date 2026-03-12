package de.binaerebauten.gleichklang.memberweb.view.component;

import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import de.binaerebauten.gleichklang.core.model.questionnaire.Answer;
import de.binaerebauten.gleichklang.core.model.user.Address;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.model.user.User_;
import de.binaerebauten.gleichklang.core.service.validator.UniqueValidationException;
import de.binaerebauten.gleichklang.core.view.component.question.QuestionComponent;
import de.binaerebauten.gleichklang.core.view.component.validator.SaveHelper;
import de.binaerebauten.gleichklang.core.view.commit_strategy.ValidationStrategy;
import de.binaerebauten.gleichklang.core.view.component.*;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;
import de.binaerebauten.gleichklang.core.service.LocatableHandler;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class PersonalDataComponent extends CustomComponent implements Savable
{
	public interface PersonalDataHandler extends LocatableHandler
	{
		User getUser();

		Answer getSexAnswer();

		Address getNewAddress(Collection<Address> addresses);

		void save(User user, Answer sexAnswer) throws UniqueValidationException;
	}

	private final PersonalDataHandler personalDataHandler;

	private final List<AddressPanel> addressPanels = new ArrayList<>();
	private final QuestionComponent sexQuestionComponent;

	private final VerticalLayout addressContainer;
	private final SaveHelper saveHelper;

	private final ComponentGroup<User> personalDataFieldGroup;

	public PersonalDataComponent(ValidationStrategy validationStrategy, PersonalDataHandler personalDataHandler)
	{
		Objects.requireNonNull(personalDataHandler);

		this.personalDataHandler = personalDataHandler;

		sexQuestionComponent = new QuestionComponent(personalDataHandler.getSexAnswer(), personalDataHandler);

		personalDataFieldGroup = new ComponentGroup<>(User.class, personalDataHandler.getUser());

		addressContainer = new VerticalLayout();
		addressContainer.addStyleName(CssStyle.GK_PANEL.getStyleName());

		saveHelper = new SaveHelper(this::commit, this::save);
		saveHelper.getSaveButton().setVisible(false);
		saveHelper.getValidationComponent().setValidationStrategy(validationStrategy);

		setCompositionRoot(createLayout());

		initAddresses();
		saveHelper.addFields(personalDataFieldGroup);
		saveHelper.addFields(sexQuestionComponent.getFields());
	}

	private Component createLayout()
	{
		final VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);
		layout.setStyleName(CssStyle.QUESTIONNAIRE.getStyleName());

		layout.addComponent(saveHelper.getValidationComponent());
		layout.addComponent(createPersonPanel());
		layout.addComponent(addressContainer);
		layout.addComponent(createControls());

		return layout;
	}

	private Component createControls()
	{
		final Button newAddressButton = new Button(de.binaerebauten.gleichklang.memberweb.view.I18N.ADDRESS_NEW_BUTTON.msg(), FontAwesome.PLUS_CIRCLE);
		newAddressButton.addClickListener(event -> addAddress());

		final FooterCommandBar commandBar = new FooterCommandBar();
		commandBar.addButton(newAddressButton, FooterCommandBar.Position.LEFT);
		commandBar.addButton(saveHelper.getSaveButton(), FooterCommandBar.Position.RIGHT);

		return commandBar;
	}

	private void initAddresses()
	{
		final User user = personalDataHandler.getUser();

		user.getOptionalPaymentAddress().ifPresent(this::addAddress);
		user.setAddresses(removeDuplicateAddress(user.getAddresses()));
		user.getAddresses().stream()
				.filter(address -> !address.isPayment())
				.forEachOrdered(this::addAddress);

		if(addressPanels.isEmpty()) addAddress();
	}

	private FormPanel createPersonPanel()
	{
		final FormPanel personPanel = new FormPanel(de.binaerebauten.gleichklang.memberweb.view.I18N.USERDATAPANEL_CAPTION_PERSON.msg());
		personPanel.addStyleName(CssStyle.GK_PANEL.getStyleName());

		final Component userName = personalDataFieldGroup.buildAndBind(de.binaerebauten.gleichklang.memberweb.view.I18N.USERDATAPANEL_CAPTION_ALIAS.msg(), LabelField.class, User_.alias);
		userName.addStyleName("user-name-label");
		((LabelField)userName).setContentMode(ContentMode.HTML);

		personPanel.addFormElement(userName);
		personPanel.addFormElement(personalDataFieldGroup.buildAndBind(true, de.binaerebauten.gleichklang.memberweb.view.I18N.USER_CAPTION_LASTNAME.msg(), TextField.class, User_.lastName));
		personPanel.addFormElement(personalDataFieldGroup.buildAndBind(true, de.binaerebauten.gleichklang.memberweb.view.I18N.USER_CAPTION_FIRSTNAME.msg(), TextField.class, User_.firstName));
		personPanel.addFormElement(personalDataFieldGroup.bind(new BirthDateField(true), User_.birthDate));
		personPanel.addFormElement(sexQuestionComponent);

		return personPanel;
	}

	private void addAddress()
	{
		final Address newAddress = personalDataHandler.getNewAddress(getAddresses());
		if (newAddress == null) return;

		addAddress(newAddress);
	}

	private List<Address> getAddresses()
	{
		return addressPanels.stream().map(AddressPanel::getAddress).collect(Collectors.toList());
	}

	private void addAddress(Address address)
	{
		final AddressPanel addressPanel = new AddressPanel(address, personalDataHandler, addressPanels.size() + 1);
		final HorizontalLine horizontalLine = new HorizontalLine();

		if (!address.isPayment())
		{
			addressPanel.setAddressRemoveListener(removedAddress ->
			{
				addressPanels.remove(addressPanel);
				addressContainer.removeComponent(addressPanel);
				addressContainer.removeComponent(horizontalLine);
				saveHelper.removeFields(addressPanel.getFields());
				refreshAddressPanelCaptions();
			});
		}

		addressPanels.add(addressPanel);
		addressContainer.addComponent(addressPanel);
		addressContainer.addComponent(horizontalLine);
		saveHelper.addFields(addressPanel.getFields());
	}

	private void refreshAddressPanelCaptions()
	{
		for (int i = 0; i < addressPanels.size(); i++)
		{
			addressPanels.get(i).setAddressNumber(i + 1);
		}
	}

	private void save() throws UniqueValidationException
	{
		personalDataHandler.save(personalDataFieldGroup.getItemDataSource().getBean(), sexQuestionComponent.getAnswer());
	}

	@Override
	public void saveComplete(SaveResultListener saveResultListener)
	{
		saveHelper.saveComplete(saveResultListener);
	}

	public void setVisibleSaveButton(boolean visible)
	{
		saveHelper.getSaveButton().setVisible(visible);
	}

	private void commit() throws CommitException
	{
		//noinspection Convert2streamapi
		for (Field<?> field : sexQuestionComponent.getFields())
		{
			field.commit();
		}

		for (AddressPanel addressPanel : addressPanels)
		{
			addressPanel.commit();
		}
		personalDataFieldGroup.commit();



		final User user = personalDataFieldGroup.getItemDataSource().getBean();
		user.setAddresses(getAddresses());
	}

	public Collection<Field<?>> getFields()
	{
		return saveHelper.getValidationComponent().getFields();
	}

	public List<Address> removeDuplicateAddress(List<Address> addresses)
	{
		Map<String,Address> addressStringMap = new HashMap<>();
		List<Address> addressList = new CopyOnWriteArrayList<>(addresses);
		List<Address> addressListSaved = new CopyOnWriteArrayList<>();

		int i=0;
		for (Address address : addresses)
		{
			if(addressStringMap.containsKey(getAddressString(address)) && i<addressList.size())
			{
				addressList.remove(i);
			}
			else
			{
				addressStringMap.put(getAddressString(address),address);
			}
			i++;
		}
		List keySetList=new ArrayList<String>(addressStringMap.keySet());

		for (int j=keySetList.size()-1;j>=0;j--)
		{
			String key= (String) keySetList.get(j);
			addressListSaved.add(addressStringMap.get(key));
		}
		return addressListSaved;
	}

	public String getAddressString(Address address) {

		StringBuilder addressBuilder = new StringBuilder();

		addressBuilder.append(address.getContinent()!=null? address.getContinent().getName() :"").append("-");
		addressBuilder.append(address.getCountry()!=null?address.getCountry().getName() :"").append("-");
		addressBuilder.append(address.getRegion()!=null? address.getRegion().getName() :"").append("-");
		addressBuilder.append(address.getZip()!=null?address.getZip().getName() :"").append("-");
		addressBuilder.append(address.getCity()!=null? address.getCity() :"").append("-");
		addressBuilder.append(address.getStreetWithNumber()!=null?address.getStreetWithNumber() :"");

		return addressBuilder.toString().toLowerCase();
	}

}
