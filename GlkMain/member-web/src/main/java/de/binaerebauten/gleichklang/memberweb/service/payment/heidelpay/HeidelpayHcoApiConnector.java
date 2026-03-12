package de.binaerebauten.gleichklang.memberweb.service.payment.heidelpay;

import com.google.common.annotations.VisibleForTesting;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import de.binaerebauten.gleichklang.core.model.heidelpay.query.AddressType;
import de.binaerebauten.gleichklang.core.model.heidelpay.query.ContactType;
import de.binaerebauten.gleichklang.core.model.heidelpay.query.CustomerType;
import de.binaerebauten.gleichklang.core.model.heidelpay.query.NameType;
import de.binaerebauten.gleichklang.core.model.locatable.Country;
import de.binaerebauten.gleichklang.core.model.locatable.Region;
import de.binaerebauten.gleichklang.core.model.locatable.Zip;
import de.binaerebauten.gleichklang.core.model.payment.ExternalPayment;
import de.binaerebauten.gleichklang.core.model.payment.ExternalPaymentRegistration;
import de.binaerebauten.gleichklang.core.model.payment.MonetaryAmount;
import de.binaerebauten.gleichklang.core.model.payment.PaymentMethod;
import de.binaerebauten.gleichklang.core.model.user.Address;
import de.binaerebauten.gleichklang.core.model.user.AddressNotFoundException;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.service.payment.PaymentException;
import de.binaerebauten.gleichklang.core.service.payment.heidelpay.PaymentCode;
import de.binaerebauten.gleichklang.core.service.payment.heidelpay.ProcessingResultType;
import de.binaerebauten.gleichklang.core.service.payment.heidelpay.TransactionID;
import de.binaerebauten.gleichklang.core.service.payment.heidelpay.TransactionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * This class implements the exchange with the heidelpay payment system.
 */
@Component
public class HeidelpayHcoApiConnector
{
	/**
	 * Representation mode of the the hCO page.
	 */
	private enum Mode
	{
		INITIAL_PAYMENT,
		CHANGE_DATA,
		PAYMENT
	}
	
	private final static Logger LOG = LoggerFactory.getLogger(HeidelpayHcoApiConnector.class);

	public static final String REQUEST_VERSION = "1.0";
	public static final boolean FRONTEND_POPUP = false;
	public static final String BUTTON_1_NAME = "PAY";
	public static final String BUTTON_1_TYPE = "Button";

	@Autowired
	private RestTemplate restTemplate;

	@Value("${heidelpay.hco.url}")
	private URI hcoUrl;

	@Value("${heidelpay.security.sender}")
	private String securitySender;

	@Value("${heidelpay.user.login}")
	private String userLogin;

	@Value("${heidelpay.user.pwd}")
	private String userPwd;

	@Value("${heidelpay.transaction.channel}")
	private String transactionChannel;

	@Value("${heidelpay.transaction.mode}")
	private String transactionMode;

	@Value("${heidelpay.frontend.response_url}")
	private String frontendResponseUrl;
	
	@Value("${heidelpay.frontend.css_path}")
	private String frontendCssPath;
	
	@Value("${heidelpay.frontend.jscript_path}")
	private String frontendJscriptPath;

	/**
	 * Retrieves the url to the initial registration form for the given payment from heidelpay.
	 *
	 * @param externalPayment the payment
	 * @return the payment form url for the given payment
	 * @throws PaymentException
	 */
	public URI getInitialRegistrationFormUrl(ExternalPayment externalPayment)
			throws PaymentException
	{
		Objects.requireNonNull(externalPayment, "externalPayment == null");

		HeidelpayHcoMessage message = createInitialRegistrationFormUrlRequest(externalPayment);

		return getFrontEndRedirectUrl(message);
	}

	/**
	 * Retrieves the url to the payment registration form from heidelpay.
	 *
	 * @param paymentMethod               the non-null payment method
	 * @param externalPaymentRegistration the non-null external payment registration
	 * @return the url to the payment registration form
	 * @throws PaymentException
	 */
	public URI getPaymentRegistrationFormUrl(ExternalPaymentRegistration externalPaymentRegistration,
			PaymentMethod paymentMethod) throws PaymentException
	{
		Objects.requireNonNull(externalPaymentRegistration, "externalPaymentRegistration == null");
		Objects.requireNonNull(paymentMethod, "paymentMethod == null");

		User user = externalPaymentRegistration.getUser();

		HeidelpayHcoMessage message = createPaymentUrlRequest(user, paymentMethod, TransactionType.REGISTRATION, Mode.CHANGE_DATA);
		String identificationTransactionId = TransactionID.toTransactionID(externalPaymentRegistration).toString();
		message.setIdentificationTransactionId(identificationTransactionId);

		return getFrontEndRedirectUrl(message);
	}

	/**
	 * Retrieves the url to the payment data update form from heidelpay.
	 *
	 * @param customer                    the non-null customer
	 * @param paymentMethod               the non-null payment method
	 * @param externalPaymentRegistration the non-null external payment registration
	 * @return the url to the payment registration form
	 * @throws PaymentException
	 */
	public URI getPaymentDataUpdateFormUrl(CustomerType customer, PaymentMethod paymentMethod,
			ExternalPaymentRegistration externalPaymentRegistration)
			throws PaymentException
	{
		Objects.requireNonNull(customer, "customer == null");
		Objects.requireNonNull(paymentMethod, "paymentMethod == null");
		Objects.requireNonNull(externalPaymentRegistration, "externalPaymentRegistration == null");

		HeidelpayHcoMessage message = createReregistrationUrlRequest(customer, paymentMethod, TransactionType.REREGISTRATION);

		String registrationId = externalPaymentRegistration.getRegistrationId();

		message.setAccountRegistration(registrationId);
		String identificationTransactionId = TransactionID.toTransactionID(externalPaymentRegistration).toString();
		message.setIdentificationTransactionId(identificationTransactionId);

		return getFrontEndRedirectUrl(message);
	}

	@VisibleForTesting
	HeidelpayHcoMessage sendMessage(HeidelpayHcoMessage message) throws PaymentException
	{
		Objects.requireNonNull(message, "message == null");

		MultiValueMap<String, String> request = message.toMultiValueMap();
		LOG.info("HCO request message:\n{}", request);

		try
		{
			ResponseEntity<MultiValueMap> response = restTemplate.postForEntity(hcoUrl, request, MultiValueMap.class);
			HttpStatus statusCode = response.getStatusCode();
			MultiValueMap body = response.getBody();
			if (statusCode.equals(HttpStatus.OK) && body != null)
			{
				Map valueMap = body.toSingleValueMap();
				return HeidelpayHcoMessage.fromMap(valueMap);
			}
			else
			{
				throw new PaymentException(statusCode.getReasonPhrase(),
						statusCode.value());
			}
		}
		catch (ResourceAccessException e)
		{
			throw new PaymentException(e);
		}
	}

	private URI getFrontEndRedirectUrl(HeidelpayHcoMessage message)
			throws PaymentException
	{
		HeidelpayHcoMessage response = sendMessage(message);

		if (response.getProcessingResult() != ProcessingResultType.ACK)
		{
			throw new PaymentException(response.getProcessingReason(), response.getProcessingCode());
		}

		String frontendRedirectUrl = response.getFrontendRedirectUrl();
		Objects.requireNonNull(frontendRedirectUrl, "frontendRedirectUrl == null");

		return URI.create(frontendRedirectUrl);
	}

	private HeidelpayHcoMessage createReregistrationUrlRequest(CustomerType customer,
			PaymentMethod paymentMethod, TransactionType transactionType)
	{
		Set<PaymentMethod> paymentMethods = Collections.singleton(paymentMethod);
		HeidelpayHcoMessage message = create(customer, paymentMethods, Mode.CHANGE_DATA);

		message.setPaymentCode(PaymentCode.of(paymentMethod, transactionType));

		return message;
	}

	private HeidelpayHcoMessage createPaymentUrlRequest(User user, PaymentMethod paymentMethod,
			TransactionType transactionType, Mode mode)
	{
		Set<PaymentMethod> paymentMethods = Collections.singleton(paymentMethod);
		HeidelpayHcoMessage message = create(user, paymentMethods, transactionType, mode);

		message.setPaymentCode(PaymentCode.of(paymentMethod, transactionType));

		return message;
	}

	private HeidelpayHcoMessage createInitialRegistrationFormUrlRequest(ExternalPayment externalPayment)
	{
		HeidelpayHcoMessage message = createPaymentUrlRequest(externalPayment.getUser(), externalPayment.getMethod(),
				TransactionType.REGISTRATION, Mode.INITIAL_PAYMENT);
		TransactionID transactionID = TransactionID.toTransactionID(externalPayment, TransactionID.Type.REGISTRATION);
		message.setIdentificationTransactionId(transactionID.toString());

		MonetaryAmount amount = externalPayment.getAmount();

		message.setAmount(amount.getAmount().toString());
		message.setCurrency(amount.getCurrency().name());

		return message;
	}

	private HeidelpayHcoMessage create(CustomerType customer, Set<PaymentMethod> paymentMethods, Mode mode)
	{
		HeidelpayHcoMessage message = create(paymentMethods, mode);

		NameType name = customer.getName();

		message.setNameGiven(name.getGiven());
		message.setNameFamily(name.getFamily());

		ContactType contact = customer.getContact();

		message.setContactEmail(contact.getEmail());

		AddressType address = customer.getAddress();

		if (address != null)
		{
			message.setAddressStreet(address.getStreet());
			message.setAddressCity(address.getCity());
			message.setAddressZip(address.getZip());
			message.setAddressState(address.getState());
			message.setAddressCountry(address.getCountry());
			message.setFrontendLanguage(address.getCountry());
		}

		return message;
	}

	private HeidelpayHcoMessage create(User user, Set<PaymentMethod> paymentMethods, TransactionType transactionType, Mode mode)
	{
		HeidelpayHcoMessage message = create(paymentMethods, mode);

		if (TransactionType.REGISTRATION.equals(transactionType) || TransactionType.DEBIT.equals(transactionType))
		{
			message.setNameGiven(user.getFirstName());
			message.setNameFamily(user.getLastName());
			message.setContactEmail(user.getEmail());

			Address primaryAddress;
			try
			{
				primaryAddress = user.getPaymentAddress();
				message.setAddressStreet(primaryAddress.getStreetWithNumber());
				message.setAddressCity(primaryAddress.getCity());
				message.setAddressZip(getZip(primaryAddress));
				message.setAddressState(getRegion(primaryAddress));
				message.setAddressCountry(getCountryCode(primaryAddress));
				message.setFrontendLanguage(getCountryCode(primaryAddress));
			}
			catch (AddressNotFoundException e)
			{
				Notification.show(e.getMessage(), Type.WARNING_MESSAGE);
			}
		}

		return message;
	}

	private HeidelpayHcoMessage create(Set<PaymentMethod> paymentMethods, Mode mode)
	{
		HeidelpayHcoMessage message = new HeidelpayHcoMessage();

		message.setRequestVersion(REQUEST_VERSION);
		message.setFrontendPopup(FRONTEND_POPUP);
		message.setFrontendEnabled(true);
		message.setFrontendCssPath(frontendCssPath);
		
		// Comment out due to changes in the PCI DSS 3.0 regulations.
		// We are not allowed to customize the hCO with own JavaScript files, unless you are compliant with PCI SAQ A-EP level.
		message.setFrontendJscriptPath(frontendJscriptPath);
		
		message.setFrontendButton1Name(BUTTON_1_NAME);
		message.setFrontendButton1Type(BUTTON_1_TYPE);
		switch (mode)
		{
			case INITIAL_PAYMENT:
			case PAYMENT:
				message.setFrontendButton1Label(I18N.FRONTEND_BUTTON_1_LABEL_PAYMENT.msg());
				break;
			case CHANGE_DATA:
				message.setFrontendButton1Label(I18N.FRONTEND_BUTTON_1_LABEL_CHANGE_DATA.msg());
				break;
		}

		message.setFrontendResponseUrl(frontendResponseUrl);

		message.setSecuritySender(securitySender);
		message.setUserLogin(userLogin);
		message.setUserPwd(userPwd);
		message.setTransactionChannel(transactionChannel);
		message.setTransactionMode(transactionMode);

		message.setPaymentMethods(paymentMethods);

		message.setFrontendFormWidth("100%");

		return message;
	}

	private String getRegion(Address primaryAddress)
	{
		Region region = primaryAddress.getRegion();
		return region != null ? region.getName() : null;
	}

	private String getZip(Address primaryAddress)
	{
		Zip zip = primaryAddress.getZip();
		return zip != null ? zip.getZip() : null;
	}

	private String getCountryCode(Address primaryAddress)
	{
		Country country = primaryAddress != null ? primaryAddress.getCountry() : null;
		return country != null ? country.getCountryCode() : null;
	}
}
