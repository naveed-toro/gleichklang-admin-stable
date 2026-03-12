package de.binaerebauten.gleichklang.core.utils;

import com.google.common.collect.Range;
import de.binaerebauten.gleichklang.core.model.mail.UserMailTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Predicate;

/**
 * This class represents a mail reminder, that triggers sending of emails in intervals
 * depending on the number of already send emails.
 */
public class MailReminder<T>
{
	private static class RemindInterval<T>
	{
		private final Range<Integer> range;

		private final Duration duration;

		private final UserMailTemplate mailTemplate;

		private final Predicate<T> predicate;

		public RemindInterval(int minCount, int maxCount, Duration duration,
				UserMailTemplate mailTemplate, Predicate<T> predicate)
		{
			this.range = Range.closed(minCount, maxCount);
			this.duration = duration;
			this.mailTemplate = mailTemplate;
			this.predicate = predicate;
		}

		public RemindInterval(int minCount, int maxCount, Duration duration, UserMailTemplate mailTemplate)
		{
			this(minCount, maxCount, duration, mailTemplate, o -> true);
		}
	}
	
	public static class TemplateConfiguration<T>
	{
		private final Predicate<T> predicate;
		
		private final LocalTime time;
		
		private final Duration delay;
		
		private final Duration duration;
		
		private final int maxReminderCount;
		
		public TemplateConfiguration(Predicate<T> predicate, LocalTime time, Duration delay, Duration duration, int maxReminderCount)
		{
			this.predicate = predicate;
			this.time = time;
			this.delay = delay;
			this.duration = duration;
			this.maxReminderCount = maxReminderCount;
		}
		
		/**
		 * Tests the predicate with the given parameter.
		 *
		 * @param parameter	the nullable parameter passed to the predicate for further testing
		 * @return
		 */
		public boolean isScheduled(T parameter)
		{
			return predicate.test(parameter);
		}
		
		public int getMaxReminderCount()
		{
			return maxReminderCount;
		}
		
		public Duration getDelay()
		{
			return delay;
		}
		
		public Duration getDuration()
		{
			return duration;
		}
		
		public LocalTime getTime()
		{
			return Objects.nonNull(time) ? time : LocalTime.now();
		}
	}

	/**
	 * Class for building a mail reminder.
	 */
	public static class Builder<T>
	{
		private List<RemindInterval<T>> remindIntervals = new ArrayList<>();
		
		private Map<UserMailTemplate, TemplateConfiguration> templatesConfiguration = new HashMap<>();

		public ChronoUnit unit;

		private Builder()
		{
		}

		/**
		 * Adds a new reminder mail with the given parameters.
		 *
		 * @param mailTemplate the non-null mail template
		 * @param predicate    the non-null predicate which must match
		 * @param minCount     the minimum count for which this reminder should be used
		 * @param maxCount     the maximum count for which this reminder should be used
		 * @param duration     the duration after which the next email should be sent
		 * @return a builder which stores the added reminder
		 */
		public Builder addReminder(UserMailTemplate mailTemplate, Predicate<T> predicate, int minCount, int maxCount, Duration duration)
		{
			remindIntervals.add(new RemindInterval(minCount, maxCount, duration, mailTemplate, predicate));

			return this;
		}

		/**
		 * Adds a new reminder mail with the given parameters.
		 *
		 * @param mailTemplate the non-null mail template
		 * @param minCount     the minimum count for which this reminder should be used
		 * @param maxCount     the maximum count for which this reminder should be used
		 * @param duration     the duration after which the next email should be sent
		 * @return a builder which stores the added reminder
		 */
		public Builder addReminder(UserMailTemplate mailTemplate, int minCount, int maxCount, Duration duration)
		{
			remindIntervals.add(new RemindInterval(minCount, maxCount, duration, mailTemplate));

			return this;
		}
		
		/**
		 * Configures a template with given parameters.
		 *
		 * @param mailTemplate	the non-null mail template
		 * @param predicate		the non-null predicate which must match
		 * @param time			the time for sending E-Mail
		 * @param delay			the delay after that the first email should be sent
		 * @param duration		the duration after which the next email should be sent
		 * @param maxCount		the maximum count for which this reminder should be used, -1 for unlimited
		 * @return a builder which stores the added configuration
		 */
		public Builder configureTemplate(UserMailTemplate mailTemplate, Predicate<T> predicate, LocalTime time, Duration delay, Duration duration, int maxCount)
		{
			templatesConfiguration.put(mailTemplate, new TemplateConfiguration(predicate, time, delay, duration, maxCount));
			
			return this;
		}
		
		/**
		 * Configures a template with given parameters.
		 *
		 * @param mailTemplate	the non-null mail template
		 * @param time			the time for sending E-Mail
		 * @param delay			the delay after that the first email should be sent
		 * @param duration		the duration after which the next email should be sent
		 * @param maxCount		the maximum count for which this reminder should be used, -1 for unlimited
		 * @return a builder which stores the added configuration
		 */
		public Builder configureTemplate(UserMailTemplate mailTemplate, LocalTime time, Duration delay, Duration duration, int maxCount)
		{
			return configureTemplate(mailTemplate, o -> true, time, delay, duration, maxCount);
		}
		
		/**
		 * Helps to separate builder blocks for the better code comprehension.
		 *
		 * @return self
		 */
		public Builder and()
		{
			return this;
		}

		/**
		 * Builds the reminder configured by this builder.
		 *
		 * @return the reminder
		 */
		public MailReminder build()
		{
			return new MailReminder(unit, remindIntervals, templatesConfiguration);
		}
	}

	private final ChronoUnit unit;
	private final List<RemindInterval<T>> remindIntervals;
	private Map<UserMailTemplate, TemplateConfiguration<T>> templatesConfiguration;

	private MailReminder(ChronoUnit unit, List<RemindInterval<T>> remindIntervals,
			Map<UserMailTemplate, TemplateConfiguration<T>> templatesConfiguration)
	{
		this.unit = unit;
		this.remindIntervals = remindIntervals;
		this.templatesConfiguration = templatesConfiguration;
	}

	public static Builder builder(ChronoUnit unit)
	{
		Builder builder = new Builder();
		builder.unit = unit;
		return builder;
	}

	/**
	 * Returns true iff. this reminder is active for the given reminder count.
	 *
	 * @param reminderCount the reminder count
	 * @return true iff. this reminder is active for the given reminder count
	 */
	public boolean isReminderActive(int reminderCount)
	{
		return isReminderActive(null, reminderCount);
	}

	/**
	 * Returns true iff. this reminder is active for the given reminder count.
	 *
	 * @param parameter     the nullable parameter passed to the predicate for further testing
	 * @param reminderCount the reminder count
	 * @return true iff. this reminder is active for the given reminder count
	 */
	public boolean isReminderActive(T parameter, int reminderCount)
	{
		return remindIntervals.stream().anyMatch(i -> i.range.contains(reminderCount) && i.predicate.test(parameter));
	}

	/**
	 * Returns the mail template associated with the given reminder count.
	 *
	 * @param reminderCount the reminder count
	 * @return the optional mail tempalte that should be used for the given reminder count
	 */
	public Optional<UserMailTemplate> getMailTemplate(int reminderCount)
	{
		return getMailTemplate(null, reminderCount);
	}

	/**
	 * Returns the mail template associated with the given reminder count.
	 *
	 * @param parameter     the nullable parameter passed to the predicate for further testing
	 * @param reminderCount the reminder count
	 * @return the optional mail tempalte that should be used for the given reminder count
	 */
	public Optional<UserMailTemplate> getMailTemplate(T parameter, int reminderCount)
	{
		Optional<UserMailTemplate> mailTemplate = remindIntervals.stream()
				.filter(i -> i.range.contains(reminderCount) && i.predicate.test(parameter)).findFirst()
				.map(i -> i.mailTemplate);

		return mailTemplate;
	}
	
	/**
	 * Returns the template configuration.
	 *
	 * @param template
	 * @return
	 */
	public TemplateConfiguration getTemplateConfiguration(UserMailTemplate template)
	{
		return templatesConfiguration.get(template);
	}
	
	/**
	 * Returns the next reminder date for the given parameters.
	 *
	 * @param previousReminderDate the non-null previous reminder date
	 * @param reminderCount        the reminder count
	 * @return the next reminder date calculated by the duration assocaited with the given reminder count
	 */
	public Optional<LocalDateTime> getNextReminderDate(LocalDateTime previousReminderDate, int reminderCount)
	{
		return getNextReminderDate(previousReminderDate, null, reminderCount);
	}
	
	/**
	 * Returns the next reminder date for the given parameters.
	 *
	 * @param previousReminderDate the non-null previous reminder date
	 * @param parameter     the nullable parameter passed to the predicate for further testing
	 * @param reminderCount        the reminder count
	 * @return the next reminder date calculated by the duration assocaited with the given reminder count
	 */
	public Optional<LocalDateTime> getNextReminderDate(LocalDateTime previousReminderDate, T parameter, int reminderCount)
	{
		Optional<LocalDateTime> nextReminderDate = remindIntervals.stream()
				.filter(i -> i.range.contains(reminderCount) && i.predicate.test(parameter))
				.findFirst()
				.map(i -> previousReminderDate.plus(i.duration));
		
		return nextReminderDate;
	}
	
}
