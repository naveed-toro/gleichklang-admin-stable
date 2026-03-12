package de.binaerebauten.gleichklang.adminweb.service.payment;

import de.binaerebauten.gleichklang.core.model.mail.UserMailTemplate;
import de.binaerebauten.gleichklang.core.model.payment.InitialSubscriptionOffer;
import de.binaerebauten.gleichklang.core.model.payment.PaymentState;
import de.binaerebauten.gleichklang.core.model.payment.Prepayment;
import de.binaerebauten.gleichklang.core.model.payment.RenewalOffer;
import de.binaerebauten.gleichklang.core.model.user.MemberStatus;
import de.binaerebauten.gleichklang.core.repository.PrepaymentRepository;
import de.binaerebauten.gleichklang.core.service.mail.MailSendService;
import de.binaerebauten.gleichklang.core.service.mail.MailTemplateInstance;
import de.binaerebauten.gleichklang.core.service.mail.UserMailTemplateService;
import de.binaerebauten.gleichklang.core.utils.MailReminder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.MailException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;

/**
 * This service is responsible for reminding users to pay their prepayment.
 */
@Service
public class PrepaymentReminderService
{
	private final static Logger LOG = LoggerFactory.getLogger(PrepaymentReminderService.class);
	
	private static final int PAGE_SIZE = 10;
	
	@Autowired
	private UserMailTemplateService userMailTemplateService;
	
	@Autowired
	private MailSendService mailSendService;
	
	@Autowired
	private PrepaymentRepository prepaymentRepository;
	
	@Autowired
	private TransactionTemplate transactionTemplate;
	
	@Autowired
	private MailReminder<Prepayment> mailReminder;
	
	/**
	 * This method regularly retrieves all pending prepayments and directly
	 * sends reminder emails.
	 */
	@Scheduled(cron = "0 35 2 * * *", zone = "Europe/Berlin")//every day at 2:35 am
	public void processPendingPrepayments()
	{
		Pageable pageable = new PageRequest(0, PAGE_SIZE);
		LocalDateTime today = LocalDateTime.now();
		boolean hasContent;
		try
		{
			do
			{
				hasContent = transactionTemplate.execute(status ->
				{
					final Page<Prepayment> page = prepaymentRepository.findPendingPrepaymentsToRemind(today, pageable);
					page.forEach(this::processPendingPayment);
					
					return page.hasContent();
				});
			} while (hasContent);
		}
		catch (Throwable e)
		{
			LOG.error("processPendingPrepayments: ", e);
		}
	}
	
	private void processPendingPayment(Prepayment prepayment)
	{
		transactionTemplate.execute(status ->
		{
			int reminderCount = prepayment.getReminderCount();
			
			if (mailReminder.isReminderActive(prepayment, reminderCount))
			{
				UserMailTemplate mailTemplate = mailReminder.getMailTemplate(prepayment, reminderCount).get();
				MailTemplateInstance mailTemplateInstance = userMailTemplateService.createMailTemplateInstance(mailTemplate, prepayment);
				
				try
				{
					mailSendService.sendEmail(prepayment.getUser(), mailTemplateInstance);
				}
				catch (MailException e)
				{
					LOG.error("Error sending prepayment reminder for user {} ", prepayment.getUser().getEmail(), e);
				}
				
				LocalDateTime previousReminderDate = prepayment.getNextReminderDate() != null ? prepayment.getNextReminderDate() : LocalDateTime.now();
				
				LocalDateTime nextReminderDate = mailReminder.getNextReminderDate(previousReminderDate, prepayment, reminderCount).get();
				
				prepayment.setNextReminderDate(nextReminderDate);
				prepayment.setReminderCount(reminderCount + 1);
			}
			else
			{
				prepayment.setState(PaymentState.FAILED);
				if (prepayment.getInvoice().getItems().stream().anyMatch(i -> i.getProduct() instanceof InitialSubscriptionOffer || i.getProduct() instanceof RenewalOffer))
				{
					prepayment.getUser().setMemberStatus(MemberStatus.CANCELED);
				}
			}
			prepaymentRepository.save(prepayment);
			
			return null;
		});
	}
	
}
