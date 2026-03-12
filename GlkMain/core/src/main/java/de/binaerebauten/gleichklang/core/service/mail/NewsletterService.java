package de.binaerebauten.gleichklang.core.service.mail;

import com.google.common.collect.Lists;
import de.binaerebauten.gleichklang.core.model.mail.Newsletter;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.repository.mail.NewsletterRepository;
import de.binaerebauten.gleichklang.core.utils.CheckedTransactional;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanItemContainer.LazyBeanFilteredItemsHandler;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class NewsletterService
{
	private static final Logger LOG = LoggerFactory.getLogger(NewsletterService.class);
	
	private final NewsletterRepository newsletterRepository;
	
	@Autowired
	public NewsletterService(NewsletterRepository newsletterRepository)
	{
		this.newsletterRepository = Objects.requireNonNull(newsletterRepository);
	}
	
	@CheckedTransactional
	public void addMail(User user)
	{
		if (user == null || user.getEmail() == null || !user.isEmailConfirmed()) return;
		
		final Newsletter newsletter = new Newsletter();
		newsletter.setEmail(user.getEmail());
		newsletter.setConfirmationIp(user.getConfirmationIp());
		newsletter.setConfirmationDate(user.getConfirmationDate());
		
		try
		{
			newsletterRepository.saveAndFlush(newsletter);
		}
		catch (DataIntegrityViolationException e)
		{
			LOG.error("User {} could not add to newsletter", user.getEmail(), e);
		}
	}
	
	@Transactional
	public void removeMail(User user)
	{
		if (user == null || user.getEmail() == null || !user.isEmailConfirmed()) return;
		
		newsletterRepository.deleteByEmail(user.getEmail());
	}
	
	public LazyBeanFilteredItemsHandler<Newsletter> createNewsletterHandler()
	{
		return newsletterRepository::findAll;
	}
	
	@Transactional
	public void delete(Newsletter newsletter)
	{
		newsletterRepository.delete(newsletter);
	}
	
	@Transactional
	public long removeFromNewsletter(Collection<String> mails)
	{
		if(mails.isEmpty()) return 0;
		
		final AtomicInteger removedItems = new AtomicInteger(0);
		
		Lists.partition(new ArrayList<>(mails), 1000).forEach(mailsPartition ->
		{
			final Set<Newsletter> newsletters = newsletterRepository.findByEmailIn(mailsPartition);
			newsletterRepository.delete(newsletters);
			removedItems.addAndGet(newsletters.size());
		});
		
		return removedItems.get();
	}
	
	public InputStream export()
	{
		final List<Newsletter> newsletters = newsletterRepository.findAll();
		LOG.info("Exporting {} newsletters ...", newsletters.size());
		
		final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		final OutputStreamWriter outputStreamWriter = new OutputStreamWriter(byteArrayOutputStream);
		try
		{
			final CSVPrinter csvPrinter = CSVFormat.DEFAULT
					.withDelimiter(';')
					.withHeader("email", "confirmationIp", "confirmationDate")
					.print(outputStreamWriter);
			for (Newsletter newsletter : newsletters)
			{
				csvPrinter.printRecord(newsletter.getEmail(), newsletter.getConfirmationIp(), newsletter.getConfirmationDate());
			}
		}
		catch (IOException e)
		{
			LOG.error(e.getMessage());
		}
		finally
		{
			try
			{
				outputStreamWriter.close();
				byteArrayOutputStream.close();
			}
			catch (IOException ignored) {}
		}
		
		return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
	}

	@Transactional
	public Newsletter findUser(User user)
	{
	  return newsletterRepository.findByEmail(user.getEmail());
	}

}
