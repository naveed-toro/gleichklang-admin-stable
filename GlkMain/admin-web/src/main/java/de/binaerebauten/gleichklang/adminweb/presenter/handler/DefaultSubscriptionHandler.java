package de.binaerebauten.gleichklang.adminweb.presenter.handler;

import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import de.binaerebauten.gleichklang.adminweb.view.popup.CancelSubscriptionPopup;
import de.binaerebauten.gleichklang.adminweb.view.popup.EditSubscriptionPopup;
import de.binaerebauten.gleichklang.adminweb.view.popup.NewSubscriptionPopup;
import de.binaerebauten.gleichklang.core.model.mail.UserMailTemplate;
import de.binaerebauten.gleichklang.core.model.payment.*;
import de.binaerebauten.gleichklang.core.model.payment.Product.ProductType;
import de.binaerebauten.gleichklang.core.model.user.CancelReason;
import de.binaerebauten.gleichklang.core.model.user.MemberStatus;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.presenter.PopupOpener;
import de.binaerebauten.gleichklang.core.repository.I18NRepository;
import de.binaerebauten.gleichklang.core.repository.ProductRepository;
import de.binaerebauten.gleichklang.core.service.SubscriptionService;
import de.binaerebauten.gleichklang.core.service.UserService;
import de.binaerebauten.gleichklang.core.service.mail.MailSendService;
import de.binaerebauten.gleichklang.core.service.mail.UserMailTemplateService;
import de.binaerebauten.gleichklang.core.service.payment.InvoiceService;
import de.binaerebauten.gleichklang.core.service.payment.PaymentService;
import de.binaerebauten.gleichklang.core.service.validator.ValidationException;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class DefaultSubscriptionHandler
{
	public interface RefreshListener
	{
		void refreshView();
	}
	
	private final RefreshListener refreshListener;
	private final PopupOpener popupOpener;
	private final ProductRepository subscriptionOfferRepository;
	
	private final SubscriptionService subscriptionService;
	private final InvoiceService invoiceService;
	private final PaymentService paymentService;
	private final UserService userService;
	private final MailSendService mailSendService;
	private final UserMailTemplateService userMailTemplateService;
	private final TransactionTemplate transactionTemplate;
	private final I18NRepository i18NRepository;

	public DefaultSubscriptionHandler(ApplicationContext ctx, RefreshListener refreshListener, PopupOpener popupOpener)
	{
		this.refreshListener = refreshListener;
		this.popupOpener = popupOpener;
		
		subscriptionOfferRepository = ctx.getBean(ProductRepository.class);

		subscriptionService = ctx.getBean(SubscriptionService.class);
		invoiceService = ctx.getBean(InvoiceService.class);
		paymentService = ctx.getBean(PaymentService.class);
		userService = ctx.getBean(UserService.class);
		mailSendService = ctx.getBean(MailSendService.class);
		userMailTemplateService = ctx.getBean(UserMailTemplateService.class);
		transactionTemplate = ctx.getBean(TransactionTemplate.class);
		i18NRepository = ctx.getBean(I18NRepository.class);
	}
	
	public void editSubscription(Subscription subscription)
	{
		EditSubscriptionPopup popup = new EditSubscriptionPopup(subscription, subscriptionOfferRepository::findAll, subscriptionService::saveAndUpdateStatus);
		popup.setCaption(subscription.getUser().getAlias() + ", " + subscription.getUser().getEmail());
		popup.addCloseListener(e -> refreshView());
		popupOpener.tryOpenPopup(popup);
	}
	
	public void newSubscription(User user)
	{
		if (MemberStatus.DELETED.equals(user.getMemberStatus()))
		{
			Notification.show("Nicht möglich für gelöschte Nutzer!", Type.WARNING_MESSAGE);
			return;
		}
		
		if (paymentService.existsPendingPayments(user))
		{
			Notification.show("Es existieren noch ausstehende Bezahlungen für diesen Nutzer", Type.WARNING_MESSAGE);
		}
		
		final NewSubscriptionPopup popup = new NewSubscriptionPopup(user, subscriptionOfferRepository::findAll, this::save);
		popupOpener.tryOpenPopup(popup);
	}
	
	private void save(User user, SubscriptionOffer subscriptionOffer, MonetaryAmount monetaryAmount, boolean isPaid) throws ValidationException
	{
		Objects.requireNonNull(user);
		Objects.requireNonNull(subscriptionOffer);
		Objects.requireNonNull(monetaryAmount);
		
		final Optional<Subscription> currentSubscription = subscriptionService.findCurrentSubscription(user);
		if (!currentSubscription.isPresent() && ProductType.UPGRADE_OFFER.equals(subscriptionOffer.getProductType()))
		{
			throw new ValidationException("Upgrade Offer ohne vorherige Subscription nicht möglich");
		}
		
		if (paymentService.findCurrentPayment(user).map(AbstractPayment::getState).filter(PaymentState.PENDING::equals).isPresent())
		{
			throw new ValidationException("Der Nutzer hat noch offene Zahlungen für ein Initail-, Renewal- oder UpgradeOffer- bitte klären Sie diesen Sachverhalt");
		}
		
		if (MemberStatus.DELETED.equals(user.getMemberStatus()))
		{
			throw new ValidationException("Nicht möglich für gelöschte Nutzer!");
		}
		
		try
		{
			transactionTemplate.execute(transactionStatus ->
			{
				try
				{
					invoiceService.createAndSaveInvoice(user, subscriptionOffer, PaymentMethod.PREPAYMENT, monetaryAmount, isPaid);
					userService.activateUser(user);
					userService.updateCategories(user, subscriptionOffer, true);
				}
				catch (Exception ex)
				{
					throw new RuntimeException(ex);
				}
				return null;
			});
		}
		catch (Exception ex)
		{
			throw new ValidationException(ex);
		}
		refreshView();
	}
	
	public void deactivateAutoRenewal(Subscription subscription)
	{
		subscription.setAutomaticRenewal(false);
		subscriptionService.saveAndUpdateStatus(subscription);
	}
	
	public void cancelSubscription(Subscription subscription, RefreshListener refreshListener)
	{
		final CancelSubscriptionPopup popup = new CancelSubscriptionPopup(subscription, s -> cancel(s, refreshListener));
		popup.setCaption(subscription.getUser().getAlias() + ", " + subscription.getUser().getEmail());
		popup.addCloseListener(e -> refreshView());
		popupOpener.tryOpenPopup(popup);
	}

	public void cancelSubscription(Subscription subscription)
	{
		cancelSubscription(subscription, null);
	}
	
	private void cancel(Subscription subscription, RefreshListener refreshListener)
	{
		// must be send before cancelling, because cancelled user doesn't receive any mails

		String successOfMediation = i18NRepository.successOfMediation(subscription.getUser().getId());
		String satisfactionWithHarmony = i18NRepository.satisfactionWithHarmony(subscription.getUser().getId(), "DE");
		Set<CancelReason> cancelReason = subscription.getUser().getCancelReasons();

		boolean isSatisfactionWithHarmony = satisfactionWithHarmony != null && !satisfactionWithHarmony.isEmpty() &&
				(satisfactionWithHarmony.equalsIgnoreCase("zufrieden") || satisfactionWithHarmony.equalsIgnoreCase("Satisfied"));
		boolean isSuccessOfMediation = successOfMediation != null && (
				successOfMediation.equalsIgnoreCase("Ich habe partnerschaft gefunden.") || successOfMediation.equalsIgnoreCase("I have found partnership.") ||
						successOfMediation.equalsIgnoreCase("ich habe freundschaft gefunden.") || successOfMediation.equalsIgnoreCase("I have found friendship.") ||
						successOfMediation.equalsIgnoreCase("ich habe partnerschaft und freundschaft gefunden.") ||
						successOfMediation.equalsIgnoreCase("I have found partnership and friendship."));


		if ((isSatisfactionWithHarmony || cancelReason.contains(CancelReason.SUCCESS_THROW_GK) || isSuccessOfMediation)
				&& !cancelReason.contains(CancelReason.UNHAPPY_WITH_SERVICE)) {
			mailSendService.sendEmail(subscription.getUser(), userMailTemplateService.createMailTemplateInstance(UserMailTemplate.SUBSCRIPTION_CANCELLED_WITH_SATISFACTION, subscription.getUser()));
		}
		else {
			mailSendService.sendEmail(subscription.getUser(), userMailTemplateService.createMailTemplateInstance(UserMailTemplate.SUBSCRIPTION_CANCELLED, subscription.getUser()));
		}
		subscriptionService.cancelSubscription(subscription);
		 if (refreshListener != null) refreshListener.refreshView();
	}
	
	private void refreshView()
	{
		if (refreshListener != null) refreshListener.refreshView();
	}
}
