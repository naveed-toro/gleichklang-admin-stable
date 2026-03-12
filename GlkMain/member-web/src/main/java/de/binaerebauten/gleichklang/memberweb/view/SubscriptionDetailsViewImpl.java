package de.binaerebauten.gleichklang.memberweb.view;

import com.google.common.base.Strings;
import com.vaadin.data.Property;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import de.binaerebauten.gleichklang.core.model.payment.PaymentMethod;
import de.binaerebauten.gleichklang.core.model.payment.Subscription;
import de.binaerebauten.gleichklang.core.model.payment.Subscription.SubscriptionState;
import de.binaerebauten.gleichklang.core.model.payment.SubscriptionOffer_;
import de.binaerebauten.gleichklang.core.model.payment.Subscription_;
import de.binaerebauten.gleichklang.core.model.user.UserPaymentSettings;
import de.binaerebauten.gleichklang.core.view.AbstractNavigateView;
import de.binaerebauten.gleichklang.core.view.component.*;
import de.binaerebauten.gleichklang.core.view.component.ComponentReplacer.SimpleReplacer;
import de.binaerebauten.gleichklang.core.view.component.MessageBox.DialogResult;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;

import java.net.URI;
import java.util.Set;

import static de.binaerebauten.gleichklang.core.model.payment.I18N.*;

public class SubscriptionDetailsViewImpl extends AbstractNavigateView<SubscriptionDetailsView.SubscriptionViewListener> implements SubscriptionDetailsView
{
	private final ComponentGroup<Subscription> subscriptionComponentGroup;
	private final ComponentGroup<UserPaymentSettings> userPaymentSettingsComponentGroup;
	
	private final OptionGroup paymentGroup;
	private final SimpleReplacer autoRenewalReplacer = new SimpleReplacer();
	private Label stateLabel;
	private Label stateAdditionalLabel;
	private Label paymentInfoLabel;
	private Button changePaymentDataButton;
	private Button saveButton;
	private boolean userIsRegistered;
	
	public SubscriptionDetailsViewImpl()
	{
		subscriptionComponentGroup = new ComponentGroup<>(Subscription.class);
		userPaymentSettingsComponentGroup = new ComponentGroup<>(UserPaymentSettings.class);
		
		paymentGroup = new OptionGroup();
		
		setCompositionRoot(createSubscriptionPanel());
	}
	
	private Component createSubscriptionPanel()
	{
		final VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);
		layout.setSizeFull();
		
		final FormPanel wrapper = new FormPanel(I18N.SUBSCRIPTION_DETAILS_VIEW_ABO.msg());
		wrapper.addStyleName(CssStyle.GK_PANEL.getStyleName());
		
		VerticalLayout statePanel = new VerticalLayout();
		statePanel.setSizeFull();
		statePanel.addStyleName(CssStyle.ABONNEMENT_STATE.getStyleName());
		
		Label stateCaptionLabel = new Label(SUBSCRIPTIONSTATE_CAPTION.msg());
		stateCaptionLabel.addStyleName(CssStyle.BOLD.getStyleName());
		
		stateLabel = new Label();
		stateAdditionalLabel = new Label();
		statePanel.addComponent(stateCaptionLabel);
		statePanel.addComponent(stateLabel);
		statePanel.addComponent(stateAdditionalLabel);
		
		wrapper.addComponent(statePanel);
		
		Field<?> offerNameField = subscriptionComponentGroup.buildAndBind(SUBSCRIPTION_CAPTION_NAME.msg(),
				LabelField.class, Subscription_.offer, SubscriptionOffer_.name);
		offerNameField.addStyleName(CssStyle.ABONNEMENT.getStyleName());
		wrapper.addComponent(offerNameField);
		
		Field<?> beginField = subscriptionComponentGroup.buildAndBind(SUBSCRIPTION_CAPTION_BEGIN.msg(),
				LabelField.class, Subscription_.begin);
		beginField.addStyleName(CssStyle.ABONNEMENT.getStyleName());
		wrapper.addComponent(beginField);
		
		Field<?> endField = subscriptionComponentGroup.buildAndBind(SUBSCRIPTION_CAPTION_END.msg(),
				LabelField.class, Subscription_.end);
		endField.addStyleName(CssStyle.ABONNEMENT.getStyleName());
		wrapper.addComponent(endField);
		
		wrapper.addComponent(autoRenewalReplacer);
		
		paymentGroup.setCaption(I18N.SUBSCRIPTIONDETAILSVIEW_PAYMENT_OPTIONGROUP_CAPTION.msg());
		paymentGroup.addStyleName("option-group-abonnement");
		paymentGroup.addValueChangeListener(this::updatePaymentButtons);
		wrapper.addComponent(paymentGroup);
		
		paymentInfoLabel = new Label();
		paymentInfoLabel.setContentMode(ContentMode.HTML);
		wrapper.addComponent(paymentInfoLabel);
		
		final FooterCommandBar commandBar = new FooterCommandBar();
		
		changePaymentDataButton = new Button(I18N.SUBSCRIPTIONDETAILSVIEW_ACTION_CHANGEPAYMENTDATA.msg(), FontAwesome.PENCIL);
		changePaymentDataButton.setEnabled(false);
		changePaymentDataButton.setSizeUndefined();
		changePaymentDataButton.addClickListener(e -> fireEvent(l -> l.changePaymentData(getPersistedPaymentMethod())));
		commandBar.addButton(changePaymentDataButton, FooterCommandBar.Position.LEFT);
		
		saveButton = new Button(I18N.SUBSCRIPTIONDETAILSVIEW_ACTION_SAVE.msg(), FontAwesome.SAVE);
		saveButton.setEnabled(false);
		saveButton.setSizeUndefined();
		saveButton.addClickListener(this::savePaymentMethod);
		commandBar.addButton(saveButton, FooterCommandBar.Position.RIGHT);
		
		layout.addComponents(wrapper, commandBar);
		
		return layout;
	}
	
	/**
	 * @param clickEvent this unused parameter allows us to use this method as
	 *                   button click listener
	 */
	private void savePaymentMethod(Button.ClickEvent clickEvent)
	{
		fireEvent(l ->
				MessageBox.show(I18N.SUBSCRIPTIONVIEW_TAB_SUBSCRIPTION_ACTION_SAVE.msg(),
						MessageBox.MessageBoxButtons.YES_NO,
						MessageBox.MessageBoxStyle.QUESTION,
						r ->
						{
							if (DialogResult.YES.equals(r))
							{
								final PaymentMethod paymentMethod = getSelectedPaymentMethod(paymentGroup);
								l.changePaymentMethod(paymentMethod);
							}
						}));
	}
	
	private void updatePaymentButtons(Property.ValueChangeEvent valueChangeEvent)
	{
		PaymentMethod selectedPaymentMethod = (PaymentMethod) valueChangeEvent.getProperty().getValue();
		updatePaymentButtons(selectedPaymentMethod);
	}
	
	private void updatePaymentButtons(PaymentMethod selectedPaymentMethod)
	{
		final boolean isSelected = selectedPaymentMethod != null;
		final boolean isPaymentMethodChanged =  selectedPaymentMethod != getPersistedPaymentMethod();
		
		boolean enableChangePaymentData = isSelected && userIsRegistered && !isPaymentMethodChanged &&
				PaymentMethod.PREPAYMENT != selectedPaymentMethod;
		changePaymentDataButton.setEnabled(enableChangePaymentData);
		
		boolean saveButtonEnabled = (isSelected && isPaymentMethodChanged && (userIsRegistered
				|| PaymentMethod.PREPAYMENT == getPersistedPaymentMethod()
				|| PaymentMethod.PREPAYMENT == selectedPaymentMethod));
		saveButton.setEnabled(saveButtonEnabled);
	}
	
	@Override
	public void setUserIsRegistered(boolean userIsRegistered)
	{
		this.userIsRegistered = userIsRegistered;
	}
	
	@Override
	public void setSubscription(Subscription subscription, String additionalInfo)
	{
		final SubscriptionState subscriptionState = subscription != null ? subscription.getState() : SubscriptionState.PENDING;
		
		subscriptionComponentGroup.setItemDataSource(subscription);
		stateLabel.setCaption(subscriptionState.msg());
		stateAdditionalLabel.setCaption(additionalInfo);
		stateAdditionalLabel.setVisible(!Strings.isNullOrEmpty(additionalInfo));
		
		updateAutoRenewalReplacer(subscription);
	}
	
	private void updateAutoRenewalReplacer(Subscription subscription)
	{
		if (subscription == null)
		{
			autoRenewalReplacer.setComponent(null);
		}
		else if (subscription.isAutomaticRenewal())
		{
			autoRenewalReplacer.setComponent(new Label(I18N.SUBSCRIPTIONDETAILSVIEW_CAPTION_AUTORENEWALACTIVATED.msg()));
		}
		else
		{
			if(subscription.getState()==SubscriptionState.ACTIVE) {
				final Button button = new Button(I18N.SUBSCRIPTIONDETAILSVIEW_ACTION_ACTIVATEAUTORENEWAL.msg());
				button.addClickListener(event -> getListener().activateAutoRenewal(subscription));
				autoRenewalReplacer.setComponent(button);
			}
		}
	}
	
	@Override
	public void setUserPaymentSettings(UserPaymentSettings userPaymentSettings)
	{
		userPaymentSettingsComponentGroup.setItemDataSource(userPaymentSettings);
		updatePaymentButtons(getPersistedPaymentMethod());
		paymentGroup.select(userPaymentSettings.getPaymentMethod());
	}
	
	@Override
	public void setAvailablePaymentMethods(Set<PaymentMethod> paymentMethods)
	{
		ComponentFactory.getInstance().populateWithEnumData(paymentGroup, paymentMethods);
	}
	
	private PaymentMethod getSelectedPaymentMethod(OptionGroup paymentMethodOptionGroup)
	{
		return (PaymentMethod) paymentMethodOptionGroup.getValue();
	}
	
	private PaymentMethod getPersistedPaymentMethod()
	{
		return userPaymentSettingsComponentGroup.getItemDataSource().getBean().getPaymentMethod();
	}
	
	@Override
	public void showPaymentFormView(URI paymentFormUrl)
	{
		ExternalPaymentForm externalPaymentForm = new ExternalPaymentForm();
		externalPaymentForm.setPaymentFormUrl(paymentFormUrl);
		setCompositionRoot(externalPaymentForm);
	}
	
	@Override
	public void setPaymentInfo(String paymentInfo)
	{
		paymentInfoLabel.setValue(paymentInfo);
		paymentInfoLabel.setVisible(true);
	}
	
}
