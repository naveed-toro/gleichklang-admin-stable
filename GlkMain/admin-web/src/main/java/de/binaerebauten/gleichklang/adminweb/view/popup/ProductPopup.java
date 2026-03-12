package de.binaerebauten.gleichklang.adminweb.view.popup;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.vaadin.data.Container;
import com.vaadin.data.Container.Filterable;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.data.util.filter.Compare.Equal;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.ui.*;
import de.binaerebauten.gleichklang.core.model.I18NEntity;
import de.binaerebauten.gleichklang.core.model.payment.*;
import de.binaerebauten.gleichklang.core.model.payment.I18N;
import de.binaerebauten.gleichklang.core.model.payment.Product.ProductType;
import de.binaerebauten.gleichklang.core.service.validator.ValidationException;
import de.binaerebauten.gleichklang.core.view.component.ComponentFactory;
import de.binaerebauten.gleichklang.core.view.component.ComponentGroup;
import de.binaerebauten.gleichklang.core.view.component.Popup;
import de.binaerebauten.gleichklang.core.view.component.TranslationComponent;
import de.binaerebauten.gleichklang.core.view.component.validator.ValidationComponent;
import de.binaerebauten.gleichklang.core.view.component.validator.ValidationResult;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;
import org.apache.commons.lang.StringUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.binaerebauten.gleichklang.adminweb.view.popup.I18N.QUESTIONNAIREPOPUP_NOTIFICATION_INVALIDENTRIES;
import static de.binaerebauten.gleichklang.adminweb.view.popup.I18N.SUBSCRIPTIONOFFERPOPUP_TITLE_CAPTION;

/**
 * This popup is responsible for viewing and editing of product entities
 * {@link Product}.
 */
public class ProductPopup extends Popup
{
	public interface SaveCallback
	{
		/**
		 * Saves the given subscription offers together with the given translations.
		 *
		 * @param product          the non-null product
		 * @param i18nDescriptions the non-null i18n entities for the description {@link SubscriptionOffer#getDescription()}
		 * @throws ValidationException
		 */
		void save(Product product, Collection<I18NEntity> i18nDescriptions)
				throws ValidationException;
	}
	
	private final Optional<ComboBox> optionalProductTypeComboBox;

	private final Map<ProductType, Component> productTypeEditorComponent = new EnumMap<>(ProductType.class);
	private final Map<ProductType, ComponentGroup<? extends Product>> productTypeComponentGroup = new EnumMap<>(ProductType.class);
	private final ValidationComponent validationComponent;

	private Map<ProductType, TranslationComponent> productTypeTranslationComponent = new EnumMap<>(ProductType.class);

	private Component currentEditor;
	private ProductType activeProductType;

	/**
	 * Creates a new popup with the given parameters for editing the given product.
	 * @param product                        the product to view
	 * @param renewalOffers                  the renewal offers that can be used for setting the auto renewal offer
	 *                                       {@link InitialSubscriptionOffer#autoRenewalOffer} of the shown subscription offer
	 * @param subscriptionOfferCategories    all available subscription offer categories
	 * @param serviceOfferRequiredCategories all available service offer required categories
	 * @param i18nDescriptions               the i18n descriptions
	 * @param saveCallback                   the save callback
	 */
	public ProductPopup(Product product,
			List<SubscriptionOffer> allOffers, List<RenewalOffer> renewalOffers,
			List<SubscriptionOfferCategory> subscriptionOfferCategories,
			List<ServiceOfferRequiredCategory> serviceOfferRequiredCategories,
			List<I18NEntity> i18nDescriptions, SaveCallback saveCallback)
	{
		this(Collections.singletonList(product), allOffers, renewalOffers,
				subscriptionOfferCategories, serviceOfferRequiredCategories,
				i18nDescriptions, saveCallback);
	}

	/**
	 * Creates a new popup with the given parameters.
	 * @param products                       the non empty list of products to view
	 * @param allOffers
	 * @param renewalOffers                  the renewal offers that can be used for setting the auto renewal offer
	 *                                       {@link InitialSubscriptionOffer#autoRenewalOffer} of the shown subscription offer
	 * @param subscriptionOfferCategories    all available subscription offer categories
	 * @param serviceOfferRequiredCategories all available service offer required categories
	 * @param i18nDescriptions               the i18n descriptions
	 * @param saveCallback                   the save callback
	 */
	public ProductPopup(Collection<Product> products,
			List<SubscriptionOffer> allOffers, List<RenewalOffer> renewalOffers,
			List<SubscriptionOfferCategory> subscriptionOfferCategories,
			List<ServiceOfferRequiredCategory> serviceOfferRequiredCategories,
			List<I18NEntity> i18nDescriptions, SaveCallback saveCallback)
	{
		super(SUBSCRIPTIONOFFERPOPUP_TITLE_CAPTION.msg());

		Preconditions.checkArgument(products.size() > 0);
		Objects.requireNonNull(products, "products == null");
		Objects.requireNonNull(renewalOffers, "renewalOffers == null");
		
		for (ProductType productType : ProductType.values())
		{
			productTypeComponentGroup.put(productType, new ComponentGroup<>((Class<SubscriptionOffer>) productType.getProductClass()));
		}

		final VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);
		layout.setMargin(true);

		validationComponent = new ValidationComponent();
		layout.addComponent(validationComponent, 0);

		EditorBuilder editorBuilder = new EditorBuilder(allOffers, renewalOffers,
				i18nDescriptions, subscriptionOfferCategories, serviceOfferRequiredCategories);
		for (Product product : products)
		{
			Component offerEditor = product.accept(editorBuilder);
			offerEditor.setVisible(false);

			ProductType productType = ProductType.of(product);

			productTypeEditorComponent.put(productType, offerEditor);
		}

		optionalProductTypeComboBox = createProductTypeComboBox();
		optionalProductTypeComboBox.ifPresent(layout::addComponent);

		layout.addComponents(Iterables.toArray(productTypeEditorComponent.values(), Component.class));

		Component controlButtons = createControlButtons(saveCallback);
		layout.addComponent(controlButtons);

		setContent(layout);
	}

	private Optional<ComboBox> createProductTypeComboBox()
	{
		boolean createProductTypeComboBox = productTypeEditorComponent.size() > 1;

		ComboBox productTypeComboBox;
		if (createProductTypeComboBox)
		{
			productTypeComboBox = ComponentFactory.getInstance().createField(ProductType.class, ComboBox.class);
			productTypeComboBox.setRequired(true);
			productTypeComboBox.setTextInputAllowed(false);
			productTypeComboBox.addValueChangeListener(this::changeEditor);
		}
		else
		{
			productTypeComboBox = null;
			currentEditor = productTypeEditorComponent.values().iterator().next();
			currentEditor.setVisible(true);

			activeProductType = productTypeEditorComponent.keySet().iterator().next();
			
			validationComponent.addFields(productTypeComponentGroup.get(activeProductType));
			validationComponent.addFields(productTypeTranslationComponent.get(activeProductType).getValidatableComponent());
		}
		
		return Optional.ofNullable(productTypeComboBox);
	}

	/**
	 * Sets the active editor to the editor for the given product type.
	 *
	 * @param activeProductType the product type for which the editor should be shown
	 */
	public void setActiveEditor(ProductType activeProductType)
	{
		Preconditions.checkState(optionalProductTypeComboBox.isPresent());

		optionalProductTypeComboBox.get().setValue(activeProductType);
	}

	private Component createControlButtons(SaveCallback saveCallback)
	{
		final HorizontalLayout layout = new HorizontalLayout();
		layout.setSpacing(true);
		
		final Button saveButton = new Button(de.binaerebauten.gleichklang.adminweb.view.popup.I18N.SUBSCRIPTIONOFFERPOPUP_ACTION_SAVE.msg());
		saveButton.addClickListener(event ->
		{
			ComponentGroup<? extends Product> componentGroup = productTypeComponentGroup.get(activeProductType);
			TranslationComponent translationComponent = productTypeTranslationComponent.get(activeProductType);
			
			ValidationResult result = validationComponent.validate();
			if (result.isSuccess())
			{
				try
				{
					componentGroup.commit();

					Product product = componentGroup.getItemDataSource().getBean();
					HashSet<I18NEntity> i18NEntities = translationComponent.commit(product.getI18nKey());
					
					saveCallback.save(product, i18NEntities);
					close();
				}
				catch (FieldGroup.CommitException e)
				{
					String fieldCaption = e.getInvalidFields().entrySet().stream().findFirst().get().getKey().getCaption();
					Notification.show(QUESTIONNAIREPOPUP_NOTIFICATION_INVALIDENTRIES.msg(fieldCaption), Notification.Type.ERROR_MESSAGE);
				}
				catch (ValidationException e)
				{
					Notification.show(e.getLocalizedMessage(), Notification.Type.ERROR_MESSAGE);
				}
			}
			
			result.showValidationNotification();
		});

		layout.addComponents(saveButton);

		return layout;
	}

	private void changeEditor(Property.ValueChangeEvent valueChangeEvent)
	{
		ProductType productType = (ProductType) valueChangeEvent.getProperty().getValue();
		changeEditor(productType);
	}

	private void changeEditor(ProductType productType)
	{
		Component editor = productTypeEditorComponent.get(productType);

		if (currentEditor != null && currentEditor != editor)
		{
			currentEditor.setVisible(false);
		}
		editor.setVisible(true);
		currentEditor = editor;
		
		activeProductType = productType;
		
		validationComponent.addFields(productTypeComponentGroup.get(activeProductType));
		validationComponent.addFields(productTypeTranslationComponent.get(activeProductType).getValidatableComponent());
	}

	/**
	 * This visitor builds an editor component for a visited subscription offer.
	 */
	private class EditorBuilder implements ProductVisitor<Component>
	{
		private final List<SubscriptionOffer> allOffers;
		private final List<RenewalOffer> renewalOffers;
		private final List<I18NEntity> i18nDescriptions;
		private final List<SubscriptionOfferCategory> subscriptionOfferCategories;
		private final List<ServiceOfferRequiredCategory> serviceOfferRequiredCategories;

		private TextField durationField;
		private ComboBox durationUnitField;

		public EditorBuilder(List<SubscriptionOffer> allOffers,
				List<RenewalOffer> renewalOffers, List<I18NEntity> i18nDescriptions,
				List<SubscriptionOfferCategory> subscriptionOfferCategories,
				List<ServiceOfferRequiredCategory> serviceOfferRequiredCategories)
		{
			this.allOffers = allOffers;
			this.renewalOffers = renewalOffers;
			this.i18nDescriptions = i18nDescriptions;
			this.subscriptionOfferCategories = ImmutableList.copyOf(subscriptionOfferCategories);
			this.serviceOfferRequiredCategories = ImmutableList.copyOf(serviceOfferRequiredCategories);
		}

		@Override
		public Component visit(InitialSubscriptionOffer initialSubscriptionOffer)
		{
			ComponentGroup<InitialSubscriptionOffer> componentGroup =
					(ComponentGroup<InitialSubscriptionOffer>) productTypeComponentGroup.get(ProductType.of(initialSubscriptionOffer));

			componentGroup.setItemDataSource(initialSubscriptionOffer);

			final VerticalLayout container = createContainer();

			final Component namePaymentContainer = createSubscriptionOfferNamePaymentFields(componentGroup);
			final Component timeContainer = createSubscriptionOfferTimeContainer(componentGroup);
			final Component recommendationRenewalContainer =
					createSubscriptionOfferRecommendationCategoriesContainer(componentGroup);
			final Component translationContainer = createTranslationContainer(initialSubscriptionOffer, componentGroup);
			final Component actionCodeContainer = createActionCodeContainer(componentGroup);

			container.addComponents(namePaymentContainer, actionCodeContainer,
					timeContainer, recommendationRenewalContainer, translationContainer);

			return container;
		}

		@Override
		public Component visit(RenewalOffer renewalOffer)
		{
			ComponentGroup<RenewalOffer> componentGroup =
					(ComponentGroup<RenewalOffer>) productTypeComponentGroup.get(ProductType.of(renewalOffer));
			componentGroup.setItemDataSource(renewalOffer);

			final VerticalLayout container = createContainer();

			final Component namePaymentContainer = createSubscriptionOfferNamePaymentFields(componentGroup);
			final Component timeContainer = createSubscriptionOfferTimeContainer(componentGroup);
			final Component recommendationRenewalContainer =
					createSubscriptionOfferRecommendationCategoriesContainer(componentGroup);
			final Component translationContainer = createTranslationContainer(renewalOffer, componentGroup);

			container.addComponents(namePaymentContainer, timeContainer, recommendationRenewalContainer, translationContainer);

			return container;
		}

		@Override
		public Component visit(UpgradeOffer upgradeOffer)
		{
			ComponentGroup<UpgradeOffer> componentGroup =
					(ComponentGroup<UpgradeOffer>) productTypeComponentGroup.get(ProductType.of(upgradeOffer));

			componentGroup.setItemDataSource(upgradeOffer);

			final VerticalLayout container = createContainer();

			final Component namePaymentContainer = createSubscriptionOfferNamePaymentFields(componentGroup);
			final Component timeContainer = createSubscriptionOfferTimeContainer(componentGroup);
			final Component recommendationRenewalContainer =
					createSubscriptionOfferRecommendationCategoriesContainer(componentGroup);
			final Component translationContainer = createTranslationContainer(upgradeOffer, componentGroup);
			final Component upgradeTypeContainer = createUpgradeOfferContainer(componentGroup);

			container.addComponents(namePaymentContainer, upgradeTypeContainer,
					timeContainer, recommendationRenewalContainer, translationContainer);

			return container;
		}

		@Override
		public Component visit(Chargeback chargeback)
		{
			ComponentGroup<Chargeback> componentGroup =
					(ComponentGroup<Chargeback>) productTypeComponentGroup.get(ProductType.of(chargeback));

			componentGroup.setItemDataSource(chargeback);

			final VerticalLayout container = createContainer();
			final ComponentContainer namePaymentContainer = createNamePaymentFields(componentGroup);
			final ComboBox forMethodField = componentGroup.buildAndBind(true, I18N.CHARGEBACK_CAPTION_FORMETHOD.msg(),
					ComboBox.class, Chargeback_.forMethod);
			namePaymentContainer.addComponent(forMethodField);

			final Component timeContainer = createTimeContainer(componentGroup);
			final Component translationContainer = createTranslationContainer(chargeback, componentGroup);

			container.addComponents(namePaymentContainer, timeContainer, translationContainer);

			return container;
		}

		@Override
		public Component visit(ServiceOffer serviceOffer)
		{
			ComponentGroup<ServiceOffer> componentGroup =
					(ComponentGroup<ServiceOffer>) productTypeComponentGroup.get(ProductType.of(serviceOffer));

			componentGroup.setItemDataSource(serviceOffer);

			final VerticalLayout container = createContainer();

			final Component namePaymentContainer = createNamePaymentFields(componentGroup);
			final Component timeContainer = createTimeContainer(componentGroup);
			final Component serviceOfferRequiredCategoriesContainer =
					createServiceOfferRequiredCategoriesContainer(componentGroup);
			final Component translationContainer = createTranslationContainer(serviceOffer, componentGroup);

			container.addComponents(namePaymentContainer, timeContainer,
					serviceOfferRequiredCategoriesContainer, translationContainer);

			return container;
		}

		private Component createActionCodeContainer(ComponentGroup<InitialSubscriptionOffer> componentGroup)
		{
			HorizontalLayout actionCodeContainer = createHorizontalLayout();

			TextField actionCodeField = componentGroup
					.buildAndBind(I18N.SUBSCRIPTIONOFFER_CAPTION_ACTIONCODE.msg(),
							TextField.class, SubscriptionOffer_.actionCode);
			actionCodeContainer.addComponent(new VerticalLayout(actionCodeField));

			CheckBox additionalCheckBox = componentGroup
					.buildAndBind(I18N.SUBSCRIPTIONOFFER_CAPTION_ADDITIONAL.msg(),
							CheckBox.class, SubscriptionOffer_.additional);
			actionCodeContainer.addComponent(additionalCheckBox);
			actionCodeContainer.setComponentAlignment(additionalCheckBox, Alignment.BOTTOM_LEFT);

			return actionCodeContainer;
		}

		private Component createUpgradeOfferContainer(ComponentGroup<UpgradeOffer> componentGroup)
		{
			HorizontalLayout upgradeOfferContainer = createHorizontalLayout();
			upgradeOfferContainer.setHeight(20, Unit.EM);

			ComboBox upgradeTypeField = componentGroup
					.buildAndBind(true, I18N.UPGRADEOFFER_CAPTION_UPGRADETYPE.msg(), ComboBox.class, UpgradeOffer_.upgradeType);
			upgradeTypeField.setTextInputAllowed(false);
			upgradeTypeField.addValueChangeListener(event -> setDurationFields(componentGroup, (UpgradeType) event.getProperty().getValue()));

			TwinColSelect subscriptionOfferField = componentGroup.buildAndBind(true,
					I18N.UPGRADEOFFER_CAPTION_SUBSCRIPTIONOFFERS.msg(), TwinColSelect.class, UpgradeOffer_.subscriptionOffers);
			subscriptionOfferField.addStyleName(CssStyle.DOUBLE_WIDTH_TWINCOLSELECT.getStyleName());
			
			Container subscriptionOffersDataSource = new BeanItemContainer<>(SubscriptionOffer.class, allOffers);
			subscriptionOfferField.setContainerDataSource(subscriptionOffersDataSource);
			subscriptionOfferField.setItemCaptionMode(AbstractSelect.ItemCaptionMode.PROPERTY);
			subscriptionOfferField.setItemCaptionPropertyId(Product_.name.getName());
			subscriptionOfferField.setLeftColumnCaption(I18N.UPGRADEOFFER_CAPTION_SUBSCRIPTIONOFFERS_LEFT_COLUMN.msg());
			subscriptionOfferField.setRightColumnCaption(I18N.UPGRADEOFFER_CAPTION_SUBSCRIPTIONOFFERS_RIGHT_COLUMN.msg());
			UpgradeOffer upgradeOffer = componentGroup.getItemDataSource().getBean();
			subscriptionOfferField.setValue(upgradeOffer.getSubscriptionOffers());
			
			ComboBox typeFilterComboBox = createTypeFilter((Filterable) subscriptionOffersDataSource);
			TextField nameFilterField = createNameFilter((Filterable) subscriptionOffersDataSource);
			TextField actionCodeFilterField = createActionCodeFilter((Filterable) subscriptionOffersDataSource);

			VerticalLayout verticalLayout = new VerticalLayout(typeFilterComboBox, nameFilterField, actionCodeFilterField);
			verticalLayout.setSpacing(true);
			upgradeOfferContainer.addComponents(new VerticalLayout(upgradeTypeField),
					verticalLayout, subscriptionOfferField);

			return upgradeOfferContainer;
		}
		
		private ComboBox createTypeFilter(final Filterable subscriptionOffersDataSource)
		{
			ComboBox typeFilterComboBox = ComponentFactory.getInstance()
					.createField(ComboBox.class, I18N.UPGRADEOFFER_CAPTION_SUBSCRIPTIONOFFERS_TYPE_FILTER.msg());
			Set<ProductType> typeFilterComboBoxData = Stream.of(
					ProductType.INITIAL_SUBSCRIPTION_OFFER,
					ProductType.RENEWAL_OFFER,
					ProductType.UPGRADE_OFFER)
					.collect(Collectors.toCollection(HashSet::new));
			ComponentFactory.getInstance().populateWithEnumData(typeFilterComboBox, typeFilterComboBoxData);
			typeFilterComboBox.setNullSelectionAllowed(true);
			typeFilterComboBox.setTextInputAllowed(false);
			typeFilterComboBox.addValueChangeListener(new ValueChangeListener()
			{
				private Compare filter;

				@Override
				public void valueChange(ValueChangeEvent event)
				{
					if (filter != null)
					{
						subscriptionOffersDataSource.removeContainerFilter(filter);
					}

					ProductType productType = (ProductType) event.getProperty().getValue();
					if (Objects.nonNull(productType))
					{
						filter = new Equal(Product.TYPE, productType.getProductClass());
						subscriptionOffersDataSource.addContainerFilter(filter);
					}
				}
			});
			return typeFilterComboBox;
		}
		
		private TextField createNameFilter(final Filterable subscriptionOffersDataSource)
		{
			TextField nameFilterField = ComponentFactory.getInstance().createField(
					TextField.class, I18N.UPGRADEOFFER_CAPTION_SUBSCRIPTIONOFFERS_NAME_FILTER.msg());
			nameFilterField.addTextChangeListener(new TextChangeListener()
			{
				private SimpleStringFilter filter;
				
				@Override
				public void textChange(TextChangeEvent event)
				{
					if (filter != null)
					{
						subscriptionOffersDataSource.removeContainerFilter(filter);
					}
					String name = event.getText();
					if (StringUtils.isNotEmpty(name))
					{
						filter = new SimpleStringFilter(Product_.name.getName(), name, true, false);
						subscriptionOffersDataSource.addContainerFilter(filter);
					}
				}
			});
			return nameFilterField;
		}

		private TextField createActionCodeFilter(final Filterable subscriptionOffersDataSource)
		{
			TextField actionCodeFilterField = ComponentFactory.getInstance().createField(
					TextField.class, I18N.UPGRADEOFFER_CAPTION_SUBSCRIPTIONOFFERS_ACTIONCODE_FILTER.msg());
			actionCodeFilterField.addTextChangeListener(new TextChangeListener()
			{
				private SimpleStringFilter filter;
				
				@Override
				public void textChange(TextChangeEvent event)
				{
					if (filter != null)
					{
						subscriptionOffersDataSource.removeContainerFilter(filter);
					}
					String actionCode = event.getText();
					if (StringUtils.isNotEmpty(actionCode))
					{
						filter = new SimpleStringFilter(Product_.actionCode.getName(), actionCode, true, false);
						subscriptionOffersDataSource.addContainerFilter(filter);
					}
				}
			});
			return actionCodeFilterField;
		}
		
		private void setDurationFields(ComponentGroup<? extends SubscriptionOffer> componentGroup, UpgradeType upgradeType)
		{
			SubscriptionOffer offer = componentGroup.getItemDataSource().getBean();
			if (offer instanceof UpgradeOffer)
			{
				// necessary to get the right default duration
				((UpgradeOffer) offer).setUpgradeType(upgradeType);
			}
			durationField.setValue(String.valueOf(offer.getDefaultDuration()));
			durationUnitField.setValue(DurationUnit.MONTHS);
			
			boolean enableDurationFields = upgradeType != UpgradeType.DONATION;
			durationField.setEnabled(enableDurationFields);
			durationUnitField.setEnabled(enableDurationFields);
		}

		private VerticalLayout createContainer()
		{
			final VerticalLayout layout = new VerticalLayout();
			layout.setSpacing(true);
			layout.setMargin(false);
			return layout;
		}

		private Component createTranslationContainer(Product product, ComponentGroup<? extends Product> componentGroup)
		{
			VerticalLayout translationContainer = createContainer();

			Field<?> i18nKeyField = componentGroup
					.buildAndBind(true, I18N.PRODUCT_CAPTION_I18NKEY.msg(), TextField.class, Product_.i18nKey);

			ProductType productType = ProductType.of(product);

			TranslationComponent translationComponent =
					new TranslationComponent(I18N.PRODUCT_CAPTION_TRANSLATEDDESCRIPTION.msg(), i18nDescriptions, I18NEntity.BaseName.PRODUCT_DESCRIPTION, RichTextArea.class,
							true);
			productTypeTranslationComponent.put(productType, translationComponent);

			translationContainer.addComponents(i18nKeyField, translationComponent);

			return translationContainer;
		}

		private Component createSubscriptionOfferRecommendationCategoriesContainer(ComponentGroup<? extends SubscriptionOffer> componentGroup)
		{
			final HorizontalLayout recommendationRenewalContainer = createHorizontalLayout();

			TwinColSelect recommendationCategoriesField = componentGroup
					.buildAndBind(true, I18N.SUBSCRIPTIONOFFER_CAPTION_CATEGORIES.msg(), TwinColSelect.class, SubscriptionOffer_.categories);
			recommendationCategoriesField.addStyleName(CssStyle.DOUBLE_WIDTH_TWINCOLSELECT.getStyleName());
			recommendationCategoriesField.setLeftColumnCaption(I18N.SUBSCRIPTIONOFFER_CAPTION_CATEGORIES_LEFT_COLUMN.msg());
			recommendationCategoriesField.setRightColumnCaption(I18N.SUBSCRIPTIONOFFER_CAPTION_CATEGORIES_RIGHT_COLUMN.msg());
			Container recommendationCategoriesDataSource = new BeanItemContainer<>(SubscriptionOfferCategory.class);
			subscriptionOfferCategories.forEach(recommendationCategoriesDataSource::addItem);

			recommendationCategoriesField.setContainerDataSource(recommendationCategoriesDataSource);
			recommendationCategoriesField.setItemCaptionMode(AbstractSelect.ItemCaptionMode.PROPERTY);
			recommendationCategoriesField.setItemCaptionPropertyId(SubscriptionOfferCategory.LOCALIZED_LABEL_PROPERTY);
			SubscriptionOffer subscriptionOffer = componentGroup.getItemDataSource().getBean();
			recommendationCategoriesField.setValue(subscriptionOffer.getCategories());

			ComboBox renewalOfferComboBox = componentGroup
					.buildAndBind(I18N.SUBSCRIPTIONOFFER_CAPTION_AUTORENEWALOFFER.msg(), ComboBox.class,
							InitialSubscriptionOffer_.autoRenewalOffer); // small hack, I use InitialSubscriptionOffer here ;-)
			Container renewalOfferDataSource = new BeanItemContainer<>(RenewalOffer.class, renewalOffers);
			renewalOfferComboBox.setContainerDataSource(renewalOfferDataSource);
			renewalOfferComboBox.setItemCaptionMode(AbstractSelect.ItemCaptionMode.PROPERTY);
			renewalOfferComboBox.setItemCaptionPropertyId(Product_.name.getName());
			RenewalOffer autoRenewalOffer = subscriptionOffer.getAutoRenewalOffer();
			renewalOfferComboBox.setValue(autoRenewalOffer);

			recommendationRenewalContainer.addComponents(recommendationCategoriesField, renewalOfferComboBox);

			return recommendationRenewalContainer;
		}

		private Component createServiceOfferRequiredCategoriesContainer(ComponentGroup<? extends ServiceOffer> componentGroup)
		{
			final HorizontalLayout container = createHorizontalLayout();

			TwinColSelect requiredCategoriesField = componentGroup
					.buildAndBind(I18N.SERVICEOFFER_CAPTION_REQUIREDCATEGORIES.msg(), TwinColSelect.class, ServiceOffer_.requiredCategories);
			Container recommendationCategoriesDataSource = new BeanItemContainer<>(ServiceOfferRequiredCategory.class);
			serviceOfferRequiredCategories.forEach(recommendationCategoriesDataSource::addItem);

			requiredCategoriesField.setContainerDataSource(recommendationCategoriesDataSource);
			requiredCategoriesField.setItemCaptionMode(AbstractSelect.ItemCaptionMode.PROPERTY);
			requiredCategoriesField.setItemCaptionPropertyId(ServiceOfferRequiredCategory.LOCALIZED_LABEL_PROPERTY);
			ServiceOffer serviceOffer = componentGroup.getItemDataSource().getBean();
			requiredCategoriesField.setValue(serviceOffer.getRequiredCategories());

			container.addComponent(requiredCategoriesField);

			return container;
		}

		private ComponentContainer createTimeContainer(ComponentGroup<? extends Product> componentGroup)
		{
			final HorizontalLayout timeContainer = createHorizontalLayout();

			DateField beginField = componentGroup
					.buildAndBind(I18N.SUBSCRIPTIONOFFER_CAPTION_BEGIN.msg(), DateField.class, Product_.begin);
			DateField endField = componentGroup
					.buildAndBind(I18N.SUBSCRIPTIONOFFER_CAPTION_END.msg(), DateField.class, Product_.end);

			timeContainer.addComponents(beginField, endField);

			return timeContainer;
		}

		private Component createSubscriptionOfferTimeContainer(ComponentGroup<? extends SubscriptionOffer> componentGroup)
		{
			final ComponentContainer timeContainer = createTimeContainer(componentGroup);

			durationField = componentGroup
					.buildAndBind(true, I18N.SUBSCRIPTIONOFFER_CAPTION_DURATION.msg(), TextField.class, SubscriptionOffer_.duration);
			durationUnitField = componentGroup
					.buildAndBind(true, I18N.SUBSCRIPTIONOFFER_CAPTION_DURATIONUNIT.msg(), ComboBox.class, SubscriptionOffer_.durationUnit);
			durationUnitField.setTextInputAllowed(false);

			timeContainer.addComponents(durationField, durationUnitField);

			return timeContainer;
		}

		private ComponentContainer createNamePaymentFields(ComponentGroup<? extends Product> componentGroup)
		{
			final HorizontalLayout namePaymentContainer = createHorizontalLayout();

			TextField nameField = componentGroup
					.buildAndBind(true, I18N.SUBSCRIPTIONOFFER_CAPTION_NAME.msg(), TextField.class, Product_.name);
			TextField amountField = componentGroup
					.buildAndBind(true, I18N.MONATARYAMOUNT_CAPTION_AMOUNT.msg(), TextField.class, Product_.amount, MonetaryAmount_.amount);

			ComboBox currencyField = ComponentFactory.getInstance().createField(AvailableCurrency.class, ComboBox.class);
			currencyField.setCaption(I18N.MONATARYAMOUNT_CAPTION_CURRENCY.msg());
			currencyField.setTextInputAllowed(false);
			currencyField.setRequired(true);
			componentGroup.bind(currencyField, Product_.amount, MonetaryAmount_.currency);

			namePaymentContainer.addComponents(nameField, amountField, currencyField);

			return namePaymentContainer;
		}

		private ComponentContainer createSubscriptionOfferNamePaymentFields(ComponentGroup<? extends SubscriptionOffer> componentGroup)
		{
			final ComponentContainer namePaymentContainer = createNamePaymentFields(componentGroup);

			ComboBox tariffField = componentGroup
					.buildAndBind(true, I18N.SUBSCRIPTIONOFFER_CAPTION_TARIFF.msg(), ComboBox.class, SubscriptionOffer_.tariff);
			tariffField.setTextInputAllowed(false);

			namePaymentContainer.addComponent(tariffField);

			return namePaymentContainer;
		}

		private HorizontalLayout createHorizontalLayout()
		{
			final HorizontalLayout horizontalLayout = new HorizontalLayout();

			horizontalLayout.setSpacing(true);
			horizontalLayout.setMargin(false);

			return horizontalLayout;
		}
	}


}
