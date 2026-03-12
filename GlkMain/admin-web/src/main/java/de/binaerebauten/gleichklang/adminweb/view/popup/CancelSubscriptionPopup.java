package de.binaerebauten.gleichklang.adminweb.view.popup;

import com.vaadin.ui.*;
import de.binaerebauten.gleichklang.core.model.payment.Subscription;
import de.binaerebauten.gleichklang.core.service.payment.PaymentException;
import de.binaerebauten.gleichklang.core.view.component.Popup;

import java.util.Objects;

/**
 * This popup just shows a confirmation to cancel the subscription of an user.
 */
public class CancelSubscriptionPopup extends Popup
{
	public interface CancelSubscriptionCallback
	{
		/**
		 * This method is called when an admin presses the "cancel subscription" button.
		 *
		 * @param subscription
		 * @throws PaymentException
		 */
		void cancel(Subscription subscription) throws PaymentException;
	}

	private final CancelSubscriptionCallback cancelSubscriptionCallback;

	public CancelSubscriptionPopup(Subscription subscription, CancelSubscriptionCallback cancelSubscriptionCallback)
	{
		super(I18N.CANCELSUBSCRIPTIONPOPUP_CAPTION_TITLE.msg());

		Objects.requireNonNull(subscription, "subscription");
		this.cancelSubscriptionCallback = Objects.requireNonNull(cancelSubscriptionCallback, "cancelSubscriptionCallback == null");

		final VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);
		layout.setMargin(true);

		final Label label = new Label(I18N.CANCELSUBSCRIPTIONPOPUP_CAPTION_AREYOUSURE.msg(subscription.getUser().getAlias()));
		layout.addComponent(label);

		final HorizontalLayout buttonPanel = new HorizontalLayout();
		buttonPanel.setSpacing(true);
		buttonPanel.setMargin(false);

		layout.addComponent(buttonPanel);

		final Button cancelSubscriptionButton = new Button(I18N.CANCELSUBSCRIPTIONPOPUP_ACTION_YES.msg());
		cancelSubscriptionButton.addClickListener(event -> cancelSubscription(subscription));

		buttonPanel.addComponent(cancelSubscriptionButton);

		setContent(layout);
	}

	private void cancelSubscription(Subscription subscription)
	{
		try
		{
			cancelSubscriptionCallback.cancel(subscription);
			close();
		}
		catch (PaymentException e)
		{
			Notification.show("Error in subscription update", e.getMessage(), Notification.Type.ERROR_MESSAGE);
		}
	}
}
