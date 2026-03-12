package de.binaerebauten.gleichklang.adminweb.service;

import com.google.common.base.Strings;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import de.binaerebauten.gleichklang.adminweb.view.popup.usermanage.UserDataExportPopup;
import de.binaerebauten.gleichklang.core.initializer.AppUI;
import de.binaerebauten.gleichklang.core.model.I18N;
import de.binaerebauten.gleichklang.core.model.I18NEntity;
import de.binaerebauten.gleichklang.core.model.LocalizedEntity;
import de.binaerebauten.gleichklang.core.model.NaturalKeyEntity.NaturalKey;
import de.binaerebauten.gleichklang.core.model.message.Message;
import de.binaerebauten.gleichklang.core.model.payment.AbstractPayment;
import de.binaerebauten.gleichklang.core.model.payment.Subscription;
import de.binaerebauten.gleichklang.core.model.payment.SubscriptionOffer;
import de.binaerebauten.gleichklang.core.model.questionnaire.Answer;
import de.binaerebauten.gleichklang.core.model.questionnaire.Question;
import de.binaerebauten.gleichklang.core.model.questionnaire.QuestionGroup;
import de.binaerebauten.gleichklang.core.model.questionnaire.Questionnaire;
import de.binaerebauten.gleichklang.core.model.user.*;
import de.binaerebauten.gleichklang.core.repository.AnswerRepository;
import de.binaerebauten.gleichklang.core.repository.I18NRepository;
import de.binaerebauten.gleichklang.core.repository.LocatableRepository;
import de.binaerebauten.gleichklang.core.repository.QuestionnaireRepository;
import de.binaerebauten.gleichklang.core.repository.user.UserRepository;
import de.binaerebauten.gleichklang.core.service.*;
import de.binaerebauten.gleichklang.core.service.file.*;
import de.binaerebauten.gleichklang.core.service.payment.PaymentService;
import de.binaerebauten.gleichklang.core.utils.StringUtils;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanTable;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

import java.io.*;
import java.math.BigInteger;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;
import de.binaerebauten.gleichklang.core.repository.user.UserStatisticRepository;
import java.util.concurrent.Future;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static de.binaerebauten.gleichklang.core.utils.FunctionalUtils.nullSafe;

@Service
public class ExportService
{
	private static final Logger LOG = LoggerFactory.getLogger(ExportService.class);

	private final AnswerService answerService;
	private final SubscriptionService subscriptionService;
	private final PaymentService paymentService;
	private final ClientInformationService clientInformationService;
	private final MessageService messageService;
	private final AvatarService avatarService;
	private final MediaService mediaService;

	@Autowired
	public ExportService(
			AnswerService answerService,
			SubscriptionService subscriptionService,
			PaymentService paymentService,
			ClientInformationService clientInformationService,
			MessageService messageService,
			AvatarService avatarService,
			MediaService mediaService)
	{
		this.answerService = Objects.requireNonNull(answerService);
		this.subscriptionService = Objects.requireNonNull(subscriptionService);
		this.paymentService = Objects.requireNonNull(paymentService);
		this.clientInformationService = Objects.requireNonNull(clientInformationService);
		this.messageService = Objects.requireNonNull(messageService);
		this.avatarService = Objects.requireNonNull(avatarService);
		this.mediaService = Objects.requireNonNull(mediaService);
	}

	public InputStream exportUserInformation(User user)
	{
		Objects.requireNonNull(user);

		final StringBuilder builder = new StringBuilder();

		builder.append(createHeadline("Gleichklang Datenexport", 4));

		builder.append(createHeadline("Registrierungsdaten"));
		builder.append(createKeyValue("E-Mail", user.getEmail()));
		builder.append(createKeyValue("Registrierungszeit", user.getCreateDate()));
		builder.append(createKeyValue("Registrierungs-IP", user.getRegisterIp()));
		builder.append(createKeyValue("Confirmation-Date", user.getConfirmationDate()));
		builder.append(createKeyValue("Confirmation-IP", user.getConfirmationIp()));

		builder.append(createHeadline("Person"));
		builder.append(createKeyValue("Nutzerstatus", user.getMemberStatus()));
		builder.append(createKeyValue("Alias", user.getAlias()));
		builder.append(createKeyValue("Nachname", user.getLastName()));
		builder.append(createKeyValue("Vorname", user.getFirstName()));
		builder.append(createKeyValue("Geschlecht", answerService.getAnswerValue(user, NaturalKey.SEX)));
		builder.append(createKeyValue("Alter", user.getAge()));
		builder.append(createKeyValue("Geburtsdatum", user.getBirthDate()));
		builder.append(createKeyValue("E-Mail", user.getEmail()));
		builder.append(createKeyValue("Passwort", user.getPassword()));
		builder.append(createKeyValue("E-Mail Confirmation", user.isEmailConfirmed()));

		for (Address address : user.getAddresses())
		{
			builder.append(createHeadline("Anschrift", 2));
			builder.append(createKeyValue("Kontinent", address.getContinent()));
			builder.append(createKeyValue("Land", address.getCountry()));
			builder.append(createKeyValue("Region", address.getRegion()));
			builder.append(createKeyValue("PLZ", nullSafe(() -> address.getZip().getZip())));
			builder.append(createKeyValue("Stadt", address.getCity()));
			builder.append(createKeyValue("Straße/Hausnummer", address.getStreetWithNumber()));
		}

		builder.append(createHeadline("Informationen", 2));
		builder.append(createKeyValue("Aufmerksamkeit durch...", answerService.getAnswerValue(user, NaturalKey.AWARE_THROUGH)));
		builder.append(createKeyValue("Freitext", answerService.getAnswerValue(user, NaturalKey.FREE_TEXT_GK)));

		final UserSettings userSettings = user.getUserSettings();
		builder.append(createHeadline("Nutzereinstellungen"));
		builder.append(createKeyValue("cancellation_policy_accepted", userSettings.isCancellationPolicyAccepted()));
		builder.append(createSeparator());
		builder.append(createKeyValue("community_rules_accepted", userSettings.isCommunityRulesAccepted()));
		builder.append(createSeparator());
		builder.append(createKeyValue("disable_ads", userSettings.isDisableAds()));
		builder.append(createSeparator());
		builder.append(createKeyValue("general_terms_accepted", userSettings.isGeneralTermsAccepted()));
		builder.append(createSeparator());
		builder.append(createKeyValue("privacy_policy_accepted", userSettings.isPrivacyPolicyAccepted()));
		builder.append(createSeparator());
		builder.append(createKeyValue("disable_recommendation_notifications", userSettings.isDisableRecommendationNotifications()));
		builder.append(createSeparator());
		builder.append(createKeyValue("disable_cipher_message_notifications", userSettings.isDisableCipherMessageNotifications()));
		builder.append(createSeparator());
		builder.append(createKeyValue("disable_positive_ranking_notifications", userSettings.isDisablePositiveRankingNotifications()));
		builder.append(createSeparator());
		builder.append(createKeyValue("disable_news_notifications", userSettings.isDisableNewsNotifications()));
		builder.append(createSeparator());
		builder.append(createKeyValue("disable_footprint_notifications", userSettings.isDisableFootprintNotifications()));
		builder.append(createSeparator());
		builder.append(createKeyValue("enable_marketing_notifications", userSettings.isEnableMarketingNotifications()));

		final List<Subscription> subscriptions = subscriptionService.findAllSubscriptions(user);
		builder.append(createHeadline("Abonnements"));
		for (int i = 0; i < subscriptions.size(); i++)
		{
			final Subscription subscription = subscriptions.get(i);
			final SubscriptionOffer offer = subscription.getOffer();
			builder.append(createKeyValue("EMail", user.getEmail()));
			builder.append(createKeyValue("Alias", user.getAlias()));
			builder.append(createKeyValue("Startdatum", subscription.getBegin()));
			builder.append(createKeyValue("Enddatum", subscription.getEnd()));
			builder.append(createKeyValue("Zustand", subscription.getState(), true));
			builder.append(createKeyValue("Aboname", offer));
			builder.append(createKeyValue("Automatische Verlängerung", subscription.isAutomaticRenewal()));
			builder.append(createKeyValue("Erneuerungsabo", offer != null ? offer.getAutoRenewalOffer() : null));
			if (i < subscriptions.size() - 1)
				builder.append(createSeparator());
		}

		final List<AbstractPayment> payments = paymentService.findAllPayments(user);
		builder.append(createHeadline("Rechnungen"));
		for (int i = 0; i < payments.size(); i++)
		{
			final AbstractPayment payment = payments.get(i);
			builder.append(createKeyValue("Alias", user.getAlias()));
			builder.append(createKeyValue("Email", user.getEmail()));
			builder.append(createKeyValue("Datum", payment.getCreateDate()));
			builder.append(createKeyValue("Bezahlart", payment.getMethod(), true));
			builder.append(createKeyValue("Betrag", nullSafe(() -> payment.getAmount().toString())));
			builder.append(createKeyValue("Bezahlstatus", payment.getState(), true));
			builder.append(createKeyValue("Verwendungszweck", payment.getExternalReferenceId()));
			if (i < payments.size() - 1)
				builder.append(createSeparator());
		}

		final List<ClientInformation> clientInformations = clientInformationService.findAll(user);
		builder.append(createHeadline("Browserdaten"));
		for (int i = 0; i < clientInformations.size(); i++)
		{
			final ClientInformation clientInformation = clientInformations.get(i);
			builder.append(createKeyValue("create_date", clientInformation.getCreateDate()));
			builder.append(createKeyValue("change_date", clientInformation.getChangeDate()));
			builder.append(createKeyValue("browser", clientInformation.getBrowser()));
			builder.append(createKeyValue("browser_major_version", clientInformation.getBrowserMajorVersion()));
			builder.append(createKeyValue("browser_minor_version", clientInformation.getBrowserMinorVersion()));
			builder.append(createKeyValue("browser_outdated", clientInformation.isBrowserOutdated()));
			builder.append(createKeyValue("browser_height", clientInformation.getBrowserHeight()));
			builder.append(createKeyValue("browser_width", clientInformation.getBrowserWidth()));
			builder.append(createKeyValue("country", clientInformation.getCountry()));
			builder.append(createKeyValue("touch_device", clientInformation.isTouchDevice()));
			builder.append(createKeyValue("language", clientInformation.getLanguage()));
			builder.append(createKeyValue("layout", clientInformation.getLayout()));
			builder.append(createKeyValue("screen_height", clientInformation.getScreenHeight()));
			builder.append(createKeyValue("screen_width", clientInformation.getScreenWidth()));
			builder.append(createKeyValue("timezone_offset", clientInformation.getTimezoneOffset()));
			builder.append(createKeyValue("os", clientInformation.getOs()));
			if (i < clientInformations.size() - 1)
				builder.append(createSeparator());
		}

		final List<Message> sendMessages = messageService.getOutgoingMessages(user);
		builder.append(createHeadline("Gesendete Nachrichten an ihre Vorschläge"));
		for (int i = 0; i < sendMessages.size(); i++)
		{
			final Message message = sendMessages.get(i);
			builder.append(createKeyValue("Empfänger", message.getReceiverEnvelope().getUser().getAlias()));
			builder.append(createKeyValue("Betreff", message.getSubject()));
			builder.append(createKeyValue("Body", message.getBody()));
			if (i < sendMessages.size() - 1)
				builder.append(createSeparator());
		}

		final List<Message> adminMessages = messageService.getAdminMessages(user);
		builder.append(createHeadline("Gesendete und empfangende Nachrichten von Gleichklang"));
		for (int i = 0; i < adminMessages.size(); i++)
		{
			final Message message = adminMessages.get(i);
			final User receiver = message.getReceiverEnvelope().getUser();
			builder.append(createKeyValue("Empfänger", receiver == null ? "Admin" : receiver.getAlias()));
			builder.append(createKeyValue("Betreff", message.getSubject()));
			builder.append(createKeyValue("Body", message.getBody()));
			if (i < adminMessages.size() - 1)
				builder.append(createSeparator());
		}

		return new ByteArrayInputStream(builder.toString().replaceAll("\\n", "\r\n").getBytes());
	}

	private String createHeadline(String value)
	{
		return createHeadline(value, 3);
	}

	private String createHeadline(String value, int border)
	{
		final StringBuilder borderStringTop = new StringBuilder();
		final StringBuilder borderStringLeft = new StringBuilder();
		final String borderStringBottom;
		final String borderStringRight;

		for (int i = 0; i < border; i++)
		{
			borderStringLeft.append('#');
			borderStringTop.append("#");
		}

		for (int i = 0; i < value.length() + border; i++)
		{
			borderStringTop.append("#");
		}

		borderStringBottom = borderStringTop.toString();
		borderStringRight = borderStringLeft.toString();

		if (border < 3)
			return borderStringLeft + value + borderStringRight + "\n";
		else
			return borderStringTop + "\n" + borderStringLeft + value + borderStringRight + "\n" + borderStringBottom + "\n";
	}

	private <E extends Enum<E>> String createKeyValue(String key, Enum<E> value)
	{
		return createKeyValue(key, value, false);
	}

	private <E extends Enum<E>> String createKeyValue(String key, Enum<E> value, boolean tryTranslate)
	{
		return createKeyValue(key, Objects.isNull(value) ? null : tryTranslate ? value.toString() : value.name());
	}

	private String createKeyValue(String key, int value)
	{
		return createKeyValue(key, String.valueOf(value));
	}

	private String createKeyValue(String key, Boolean value)
	{
		return createKeyValue(key, Objects.isNull(value) ? null : value ? "Ja" : "Nein");
	}

	private String createKeyValue(String key, Optional<String> value)
	{
		return createKeyValue(key, Objects.isNull(value) ? null : value.orElse(null));
	}

	private String createKeyValue(String key, LocalizedEntity value)
	{
		return createKeyValue(key, Objects.isNull(value) ? null : value.msg());
	}

	private String createKeyValue(String key, LocalDateTime value)
	{
		return createKeyValue(key, StringUtils.timeToString(value));
	}

	private String createKeyValue(String key, LocalDate value)
	{
		return createKeyValue(key, Objects.isNull(value) ? null : value.toString());
	}

	private String createKeyValue(String key, String value)
	{
		final String stringValue = Strings.isNullOrEmpty(value) ? " -" : value;
		if (Objects.isNull(key)) return "";

		return "#" + key + "#" + "\n" + stringValue + "\n";
	}

	private String createSeparator()
	{
		return "##########\n";
	}

	public InputStream exportQuestionsAndAnswers(User user)
	{
		final List<Answer> answers = answerService.getAnswers(user);

		final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		final OutputStreamWriter outputStreamWriter = new OutputStreamWriter(byteArrayOutputStream);
		try
		{
			final CSVPrinter csvPrinter = CSVFormat.DEFAULT
					.withHeader("Fragebogenname", "Fragegruppenname", "Fragegruppenbeschreibung", "Frage", "Antwort")
					.print(outputStreamWriter);
			for (Answer answer : answers)
			{
				final Question question = answer.getQuestion();
				final QuestionGroup questionGroup = question.getQuestionGroup();
				final Questionnaire questionnaire = questionGroup.getQuestionnaire();

				csvPrinter.printRecord(questionnaire.getName(), questionGroup.getName(), questionGroup.getDescription(), question.getName(), answer.getValue());
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
			catch (IOException ignored)
			{
			}
		}

		return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
	}

	public InputStream exportImagesAndAvatars(User user)
	{
		final byte[] buffer = new byte[1024];
		final ByteArrayOutputStream bos = new ByteArrayOutputStream();
		final ZipOutputStream zos = new ZipOutputStream(bos);

		try
		{
			final List<AvatarUploadFile> avatars = avatarService.getAvatars(user);
			final List<MediaUploadFile> medias = mediaService.getMedias(user);

			final Set<File> files = new HashSet<>();
			avatars.stream().map(AbstractUploadFile::toFile).forEach(files::add);
			avatars.stream().map(AvatarUploadFile::toThumbnailFile).forEach(files::add);
			medias.stream().map(AbstractUploadFile::toFile).forEach(files::add);

			final Set<String> filenames = new HashSet<>();

			for (File file : files)
			{
				if (file.exists() && !filenames.contains(file.getName()))
				{
					filenames.add(file.getName());

					final FileInputStream fis = new FileInputStream(file);
					zos.putNextEntry(new ZipEntry(file.getName()));
					int length;
					while ((length = fis.read(buffer)) > 0)
					{
						zos.write(buffer, 0, length);
					}
					zos.closeEntry();
					fis.close();
				}
			}
		}
		catch (Exception ex)
		{
			LOG.error("exportImages failed", ex);
			Notification.show("exportImages failed: " + ex.getMessage());
		}
		finally
		{
			try
			{
				zos.close();
				bos.close();
			}
			catch (Exception ignored)
			{
			}
		}

		return new ByteArrayInputStream(bos.toByteArray());
	}

	@Async
	public ListenableFuture<ByteArrayOutputStream> exportUserDataAdmin(List<User> users,UserDataExportPopup userDataExportPopup) {

		UserDataExportService userDataExportService = AppUI.getApplicationContext().getBean(UserDataExportService.class);

		UserStatisticRepository userStatisticRepository = AppUI.getApplicationContext().getBean(UserStatisticRepository.class);

		UserStatistic userStatistic = new UserStatistic(LocalDateTime.now());
		userStatistic.setStatus("In Progress");
		userStatisticRepository.save(userStatistic);

		UserRepository userRepository= AppUI.getApplicationContext().getBean(UserRepository.class);

		List<Map<User,List<Object>>> answerList= userDataExportService.exportUserDataAdmin(users,userDataExportPopup);
		LOG.info("answerList returned="+answerList.size());

		List<String> finalHeaders=new ArrayList<>();
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		OutputStreamWriter outputStreamWriter = new OutputStreamWriter(byteArrayOutputStream);

		try {

			for (String header : userDataExportPopup.getHeaders()) {
				if (header != null && header.contains("#")) {
					String qtype = header.split("#")[0];

					if (qtype.contains(":")) {
						qtype = qtype.split(":")[0];

					}
					if ((qtype.equals("TextQuestion") || qtype.equals("NumberQuestion") || qtype.equals("ChoiceQuestion"))) {
						String headerLabel = header.split("#")[1];
						finalHeaders.add(headerLabel.split("::")[0]);
					}
				} else {
					finalHeaders.add(header);
				}

			}

			LOG.info("finalHeaders = " + finalHeaders.size());
		}
		catch (Exception e) {
				LOG.error("Error in Data export of users from User Controll", e);
			}
		try
		{

			CSVPrinter csvPrinter = CSVFormat.DEFAULT
					.withDelimiter(';')
					.withHeader(Arrays.copyOf(finalHeaders.toArray(), finalHeaders.toArray().length, String[].class))
					.print(outputStreamWriter);

			answerList.forEach(userListMap -> {
				userListMap.forEach((user, obJectList) -> {
					try {
						LOG.info("printing csv records");
						csvPrinter.printRecord(obJectList.toArray());
					} catch (IOException e) {
						LOG.error("Error in Data export of users from User Controll", e);
					}
				});
			});
		}
		catch (IOException e)
		{
			LOG.error("Error in Data export of users from User Controll", e);
		}
		finally
		{
			try
			{
				outputStreamWriter.close();
				byteArrayOutputStream.close();
			}
			catch (
					IOException ignored) {
				LOG.error("Error in Data export of users from User Controll", ignored);
			}
		}

		userStatistic.setEndDate(LocalDateTime.now());
		userStatistic.setStatus("Completed");
		userStatisticRepository.save(userStatistic);

		return new AsyncResult<>(byteArrayOutputStream);
	}

	public InputStream exportUserDataAdminBasic(List<User> users) {

		UserRepository userRepository= AppUI.getApplicationContext().getBean(UserRepository.class);

		LOG.info(String.format("Exporting %d users ...", users.size()));

		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		OutputStreamWriter outputStreamWriter = new OutputStreamWriter(byteArrayOutputStream);
		try {
			CSVPrinter csvPrinter = CSVFormat.DEFAULT
					.withDelimiter(';')
					.withHeader("email", "lastname", "firstname", "alias", "subscription_state")
					.print(outputStreamWriter);
			for (User user : users) {
				csvPrinter.printRecord(user.getEmail(),
						user.getLastName(), user.getFirstName(),
						user.getAlias(), user.getMemberStatus());
			}
		} catch (IOException e) {
			LOG.error(e.getMessage());
		} finally {
			try {
				outputStreamWriter.close();
				byteArrayOutputStream.close();
			} catch (IOException ignored) {
			}
		}

		return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
	}

}
