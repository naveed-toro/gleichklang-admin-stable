package de.binaerebauten.gleichklang.memberweb.presenter;

import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import de.binaerebauten.gleichklang.core.model.payment.AbstractPayment;
import de.binaerebauten.gleichklang.core.model.payment.BankAccount;
import de.binaerebauten.gleichklang.core.model.payment.PaymentState;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.navigation.DefaultNavigator;
import de.binaerebauten.gleichklang.core.presenter.SubNavigatePresenter;
//import de.binaerebauten.gleichklang.core.repository.AudioRepository;
import de.binaerebauten.gleichklang.core.repository.AudioRepository;
import de.binaerebauten.gleichklang.core.repository.BankAccountRepository;
import de.binaerebauten.gleichklang.core.repository.PaymentRepository;
import de.binaerebauten.gleichklang.core.repository.user.CompleteUserRepository;
import de.binaerebauten.gleichklang.core.security.AuthenticationService;
import de.binaerebauten.gleichklang.core.security.AuthenticationService.RedirectGoal;
import de.binaerebauten.gleichklang.core.service.UserDataService;
import de.binaerebauten.gleichklang.core.service.UserService;
import de.binaerebauten.gleichklang.core.service.mail.NewsletterService;
import de.binaerebauten.gleichklang.core.service.validator.UniqueValidationException;
import de.binaerebauten.gleichklang.core.service.validator.ValidationException;
import de.binaerebauten.gleichklang.core.view.component.MessageBox;
import de.binaerebauten.gleichklang.core.view.popup.GenericPopup;
import de.binaerebauten.gleichklang.memberweb.navigation.DefaultNavigatorFactory.MemberMenuItem;
import de.binaerebauten.gleichklang.memberweb.presenter.handler.DefaultPersonalDataHandler;
import de.binaerebauten.gleichklang.memberweb.view.UserDataView;
import de.binaerebauten.gleichklang.memberweb.view.UserDataView.UserDataTab;
import de.binaerebauten.gleichklang.memberweb.view.UserDataView.UserDataViewListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

import static de.binaerebauten.gleichklang.memberweb.view.I18N.USER_VALIDATION_INCORRECTPASSWORD;

public class UserDataPresenter extends SubNavigatePresenter<UserDataTab> implements UserDataViewListener {
	private static final Logger LOG = LoggerFactory.getLogger(UserDataPresenter.class);
	private final DefaultNavigator navigator;
	private final CompleteUserRepository userWithAddressRepository;
	private final AuthenticationService authenticationService;
	private final PaymentRepository paymentRepository;
	private final BankAccountRepository bankAccountRepository;
	private final UserDataView view;
	private final UserService userService;
	private final UserDataService userDataService;
	private final NewsletterService newsletterService;
	private final ApplicationContext ctx;

	public UserDataPresenter(ApplicationContext ctx, UserDataView view, DefaultNavigator navigator) {
		super(view, UserDataTab.class, MemberMenuItem.USER_DATA);

		this.view = view;
		this.ctx = ctx;
        this.navigator=navigator;
		authenticationService = ctx.getBean(AuthenticationService.class);
		userService = ctx.getBean(UserService.class);
		userDataService = ctx.getBean(UserDataService.class);
		newsletterService = ctx.getBean(NewsletterService.class);
		userWithAddressRepository = ctx.getBean(CompleteUserRepository.class);
		paymentRepository = ctx.getBean(PaymentRepository.class);
		bankAccountRepository = ctx.getBean(BankAccountRepository.class);

		view.setCancelSubscriptionVisible(userService.canCancelSubscription(authenticationService.getAuthenticatedUserId(), LocalDateTime.now()));
		view.setListener(this);
	}

	@Override
	public void enter(UserDataTab navigationEnum, String parameters) {
		final User user = userWithAddressRepository.findById(authenticationService.getAuthenticatedUserId());

		switch (navigationEnum) {
			case USERDATA:
				view.initPersonalDataTab(new DefaultPersonalDataHandler(ctx, user));
				break;
			case PASSWORD:
				view.initPasswordTab();
				break;
			case EMAIL:
				view.setUser(user);
				break;
			case MESSAGES:
				view.setUser(user);
				break;
			case CANCELLATION:
				view.setUser(user);
				break;
		}
	}

	@Override
	public void leave()
	{
		this.view.setUser(null);
		
		super.leave();
	}

	@Override
	public void showMessages()
	{
		navigator.navigateTo("MESSAGE_TO_GLEICHKLANG");
	}

	@Override
	public void issueExtention()
	{
		navigator.navigateTo("DEACTIVATE_PROLONGATION");
	}
	@Override
	public User saveUser(User user)
	{
		try
		{
			user = userService.save(user);
		}
		catch (UniqueValidationException e)
		{
			LOG.error("Error while saving user:", e);
			Notification.show(I18N.DATA_NOT_SAVED.msg(), Type.ERROR_MESSAGE);
		}
		return user;
	}
	
	@Override
	public void savePassword(String oldPassword, String newPassword) throws ValidationException
	{
		if (oldPassword == null)
		{
			throw new ValidationException(USER_VALIDATION_INCORRECTPASSWORD.msg());
		}
		
		userService.changePassword(oldPassword, newPassword);
		refresh();
	}
	
	@Override
	public void sendChangeMail(User user, String newMail) throws UniqueValidationException
	{
		userService.sendChangeMail(user, newMail);
		refresh();
	}
	
	@Override
	public void cancelSubscription(User user, boolean deleteUser, boolean mailsAfterCancel) {

        boolean paymentPending = paymentRepository.paymentPending(user, PaymentState.PENDING);

        if(paymentPending)

        {
            final List<AbstractPayment> previousPayments = paymentRepository.getUserPendingPaymentList(user, PaymentState.PENDING);
            String allAmmount = "";
            String referenceId = "";
            long ammount = 0;
            String checkAmmount= "";
            for(AbstractPayment abstractPayment : previousPayments){
                ammount=abstractPayment.getAmount().getAmount().longValue();
                if(previousPayments.size()==1){
					allAmmount += Long.toString(ammount) +" EUR";
					referenceId +=abstractPayment.getExternalReferenceId();
				}
				else {
					allAmmount += Long.toString(ammount) + " EUR" + ", ";
					referenceId += abstractPayment.getExternalReferenceId() + ", ";
				}
            }

            final BankAccount bankAccount = bankAccountRepository.findByBankName("GLS Bank");
           String accountNumber = bankAccount.getAccountNumber();
           String bankName = bankAccount.getBankName();
           String bankNumber = bankAccount.getBankNumber();
           String bic = bankAccount.getBic();
           String iban = bankAccount.getIban();

            if(deleteUser)
			{

                MessageBox.show(I18N.ACCOUNT_CAN_NOT_BE_DELETED.msg(allAmmount,referenceId,bankName,accountNumber,bankNumber,iban,bic)


						, MessageBox.MessageBoxButtons.OK, MessageBox.MessageBoxStyle.ATTENTION, null);

            }
            else {

                MessageBox.show(I18N.ACCOUNT_CAN_BE_CANCELED.msg(allAmmount,referenceId,bankName,accountNumber,bankNumber,iban,bic),
                        MessageBox.MessageBoxButtons.YES_NO, MessageBox.MessageBoxStyle.QUESTION, r -> doCancel(r, user, false, mailsAfterCancel));

            }
        }
        else
        {
            MessageBox.show(I18N.USERDATAPRESENTER_MESSAGE_SUBSCRIPTIONCANCEL.msg(),
                    MessageBox.MessageBoxButtons.YES_NO, MessageBox.MessageBoxStyle.QUESTION, r -> doCancel(r, user, deleteUser, mailsAfterCancel));
        }

    }
	
	private void doCancel(MessageBox.DialogResult dialogResult, User user, boolean deleteUser, boolean mailsAfterCancel)
	{

		if (dialogResult == MessageBox.DialogResult.YES)
		{
			user.getUserSettings().setDisableNewsNotifications(!mailsAfterCancel);
			
			try
			{
				userService.save(user);
			}
			catch (UniqueValidationException e)
			{
				LOG.error("could not save user for cancellation", e);
			}
			
			if (deleteUser)
			{
				if (mailsAfterCancel && newsletterService.findUser(user)!=null) newsletterService.addMail(user);
				userDataService.deleteUserData(user);
			}
			else
			{
				userService.cancelSubscription(user);
			}
			authenticationService.logout(RedirectGoal.LANDING);
		}
	}
}
