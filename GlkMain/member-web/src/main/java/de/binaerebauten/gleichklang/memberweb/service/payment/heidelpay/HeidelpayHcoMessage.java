package de.binaerebauten.gleichklang.memberweb.service.payment.heidelpay;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Splitter;
import de.binaerebauten.gleichklang.core.model.payment.AvailableCurrency;
import de.binaerebauten.gleichklang.core.model.payment.ExternalPayment;
import de.binaerebauten.gleichklang.core.model.payment.MonetaryAmount;
import de.binaerebauten.gleichklang.core.model.payment.PaymentMethod;
import de.binaerebauten.gleichklang.core.service.payment.heidelpay.HeidelpayPaymentMethodCode;
import de.binaerebauten.gleichklang.core.service.payment.heidelpay.PaymentCode;
import de.binaerebauten.gleichklang.core.service.payment.heidelpay.ProcessingResultType;
import de.binaerebauten.gleichklang.core.service.payment.heidelpay.TransactionType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.*;

/**
 * Represents a message to the heidelpay hco api.
 * This message is used as a request and as a response.
 *
 * @author matthias.koester@binaere-bauten.de
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HeidelpayHcoMessage
{
	@JsonProperty(HeidelpayHcoParameters.SECURITY_SENDER)
	private String securitySender;

	@JsonProperty(HeidelpayHcoParameters.USER_LOGIN)
	private String userLogin;

	@JsonProperty(HeidelpayHcoParameters.USER_PWD)
	private String userPwd;

	@JsonProperty(HeidelpayHcoParameters.TRANSACTION_CHANNEL)
	private String transactionChannel;

	@JsonProperty(HeidelpayHcoParameters.TRANSACTION_MODE)
	private String transactionMode;

	@JsonProperty(HeidelpayHcoParameters.PAYMENT_CODE)
	private String paymentCode;

	@JsonProperty(HeidelpayHcoParameters.PRESENTATION_USAGE)
	private String usage;

	@JsonProperty(HeidelpayHcoParameters.PRESENTATION_AMOUNT)
	private String amount;

	@JsonProperty(HeidelpayHcoParameters.PRESENTATION_CURRENCY)
	private String currency;
	
	@JsonProperty(HeidelpayHcoParameters.FRONTEND_MODE)
	private String frontendMode;
	
	@JsonProperty(HeidelpayHcoParameters.FRONTEND_ENABLED)
	private boolean frontendEnabled;
	
	@JsonProperty(HeidelpayHcoParameters.FRONTEND_POPUP)
	private boolean frontendPopup;

	@JsonProperty(HeidelpayHcoParameters.FRONTEND_LANGUAGE)
	private String frontendLanguage;

	@JsonProperty(HeidelpayHcoParameters.FRONTEND_RESPONSEURL)
	private String frontendResponseUrl;

	@JsonProperty(HeidelpayHcoParameters.FRONTEND_REDIRECT_URL)
	private String frontendRedirectUrl;

	@JsonProperty("FRONTEND.REDIRECT_TIME")
	private int frontEndRedirectTime;

	@JsonProperty(HeidelpayHcoParameters.NAME_GIVEN)
	private String nameGiven;

	@JsonProperty(HeidelpayHcoParameters.NAME_FAMILY)
	private String nameFamily;

	@JsonProperty(HeidelpayHcoParameters.CONTACT_EMAIL)
	private String contactEmail;

	@JsonProperty(HeidelpayHcoParameters.ADDRESS_STREET)
	private String addressStreet;

	@JsonProperty(HeidelpayHcoParameters.ADDRESS_CITY)
	private String addressCity;

	@JsonProperty(HeidelpayHcoParameters.ADDRESS_ZIP)
	private String addressZip;

	@JsonProperty(HeidelpayHcoParameters.ADDRESS_COUNTRY)
	private String addressCountry;

	@JsonProperty(HeidelpayHcoParameters.ADDRESS_STATE)
	private String addressState;

	@JsonProperty(HeidelpayHcoParameters.REQUEST_VERSION)
	private String requestVersion;

	@JsonProperty(HeidelpayHcoParameters.POST_VALIDATION)
	private String postValidation;

	@JsonProperty(HeidelpayHcoParameters.FRONTEND_FORM_WIDTH)
	private String frontendFormWidth;

	@JsonProperty(HeidelpayHcoParameters.FRONTEND_HEIGHT)
	private String frontendHeight;
	
	@JsonProperty(HeidelpayHcoParameters.FRONTEND_CSS_PATH)
	private String frontendCssPath;

	@JsonProperty(HeidelpayHcoParameters.FRONTEND_JSCRIPT_PATH)
	private String frontendJscriptPath;
	
	@JsonProperty(HeidelpayHcoParameters.FRONTEND_BUTTON_1_NAME)
	private String frontendButton1Name;
	
	@JsonProperty(HeidelpayHcoParameters.FRONTEND_BUTTON_1_TYPE)
	private String frontendButton1Type;
	
	@JsonProperty(HeidelpayHcoParameters.FRONTEND_BUTTON_1_LABEL)
	private String frontendButton1Label;
	
	@JsonProperty(HeidelpayHcoParameters.FRONTEND_BUTTON_2_NAME)
	private String frontendButton2Name;
	
	@JsonProperty(HeidelpayHcoParameters.FRONTEND_BUTTON_2_TYPE)
	private String frontendButton2Type;
	
	@JsonProperty(HeidelpayHcoParameters.FRONTEND_BUTTON_2_LABEL)
	private String frontendButton2Label;

	@JsonProperty(HeidelpayHcoParameters.IDENTIFICATION_UNIQUEID)
	private String identificationUniqueId;

	@JsonProperty(HeidelpayHcoParameters.IDENTIFICATION_REFERENCEID)
	private String identificationReferenceId;

	@JsonProperty(HeidelpayHcoParameters.IDENTIFICATION_TRANSACTIONID)
	private String identificationTransactionId;

	@JsonProperty(HeidelpayHcoParameters.IDENTIFICATION_INVOICEID)
	private String identificationInvoiceId;

	@JsonProperty(HeidelpayHcoParameters.IDENTIFICATION_SHOPPERID)
	private String identificationShopperId;

	@JsonProperty(HeidelpayHcoParameters.PROCESSING_CODE)
	private String processingCode;

	@JsonProperty(HeidelpayHcoParameters.PROCESSING_REASON)
	private String processingReason;

	@JsonProperty(HeidelpayHcoParameters.PROCESSING_RETURN_CODE)
	private String processingReturnCode;

	@JsonProperty(HeidelpayHcoParameters.PROCESSING_RETURN)
	private String processingReturn;

	@JsonProperty(HeidelpayHcoParameters.ACCOUNT_REGISTRATION)
	private String accountRegistration;


	private Set<PaymentMethod> paymentMethods;

	public void setPaymentMethods(Set<PaymentMethod> paymentMethods)
	{
		this.paymentMethods = paymentMethods;
	}

	public String getFrontendFormWidth()
	{
		return frontendFormWidth;
	}

	public void setFrontendFormWidth(String frontendFormWidth)
	{
		this.frontendFormWidth = frontendFormWidth;
	}

	public String getFrontendHeight()
	{
		return frontendHeight;
	}

	public void setFrontendHeight(String frontendHeight)
	{
		this.frontendHeight = frontendHeight;
	}
	
	public String getFrontendCssPath()
	{
		return frontendCssPath;
	}
	
	public void setFrontendCssPath(String frontendCssPath)
	{
		this.frontendCssPath = frontendCssPath;
	}
	
	public String getFrontendJscriptPath()
	{
		return frontendJscriptPath;
	}
	
	public void setFrontendJscriptPath(String frontendJscriptPath)
	{
		this.frontendJscriptPath = frontendJscriptPath;
	}
	
	public String getFrontendButton1Name()
	{
		return frontendButton1Name;
	}
	
	public void setFrontendButton1Name(String frontendButton1Name)
	{
		this.frontendButton1Name = frontendButton1Name;
	}
	
	public String getFrontendButton1Type()
	{
		return frontendButton1Type;
	}
	
	public void setFrontendButton1Type(String frontendButton1Type)
	{
		this.frontendButton1Type = frontendButton1Type;
	}
	
	public String getFrontendButton1Label()
	{
		return frontendButton1Label;
	}
	
	public void setFrontendButton1Label(String frontendButton1Label)
	{
		this.frontendButton1Label = frontendButton1Label;
	}
	
	public String getFrontendButton2Name()
	{
		return frontendButton2Name;
	}
	
	public void setFrontendButton2Name(String frontendButton2Name)
	{
		this.frontendButton2Name = frontendButton2Name;
	}
	
	public String getFrontendButton2Type()
	{
		return frontendButton2Type;
	}
	
	public void setFrontendButton2Type(String frontendButton2Type)
	{
		this.frontendButton2Type = frontendButton2Type;
	}
	
	public String getFrontendButton2Label()
	{
		return frontendButton2Label;
	}
	
	public void setFrontendButton2Label(String frontendButton2Label)
	{
		this.frontendButton2Label = frontendButton2Label;
	}
	
	public String getFrontendRedirectUrl()
	{
		return frontendRedirectUrl;
	}

	public void setFrontendRedirectUrl(String frontendRedirectUrl)
	{
		this.frontendRedirectUrl = frontendRedirectUrl;
	}

	public int getFrontEndRedirectTime()
	{
		return frontEndRedirectTime;
	}

	public void setFrontEndRedirectTime(int frontEndRedirectTime)
	{
		this.frontEndRedirectTime = frontEndRedirectTime;
	}

	public String getAddressCity()
	{
		return addressCity;
	}

	public void setAddressCity(String addressCity)
	{
		this.addressCity = addressCity;
	}

	public String getNameGiven()
	{
		return nameGiven;
	}

	public void setNameGiven(String nameGiven)
	{
		this.nameGiven = nameGiven;
	}

	public String getContactEmail()
	{
		return contactEmail;
	}

	public void setContactEmail(String contactEmail)
	{
		this.contactEmail = contactEmail;
	}

	public String getNameFamily()
	{
		return nameFamily;
	}

	public void setNameFamily(String nameFamily)
	{
		this.nameFamily = nameFamily;
	}

	public String getAddressStreet()
	{
		return addressStreet;
	}

	public void setAddressStreet(String addressStreet)
	{
		this.addressStreet = addressStreet;
	}

	public String getAddressZip()
	{
		return addressZip;
	}

	public void setAddressZip(String addressZip)
	{
		this.addressZip = addressZip;
	}

	public String getAddressCountry()
	{
		return addressCountry;
	}

	public void setAddressCountry(String addressCountry)
	{
		this.addressCountry = addressCountry;
	}

	public String getAddressState()
	{
		return addressState;
	}

	public void setAddressState(String addressState)
	{
		this.addressState = addressState;
	}

	public String getUserPwd()
	{
		return userPwd;
	}

	public void setUserPwd(String userPwd)
	{
		this.userPwd = userPwd;
	}

	public String getFrontendResponseUrl()
	{
		return frontendResponseUrl;
	}

	public void setFrontendResponseUrl(String frontendResponseUrl)
	{
		this.frontendResponseUrl = frontendResponseUrl;
	}

	public String getPostValidation()
	{
		return postValidation;
	}

	public void setPostValidation(String postValidation)
	{
		this.postValidation = postValidation;
	}

	public String getSecuritySender()
	{
		return securitySender;
	}

	public void setSecuritySender(String securitySender)
	{
		this.securitySender = securitySender;
	}

	public String getUserLogin()
	{
		return userLogin;
	}

	public void setUserLogin(String userLogin)
	{
		this.userLogin = userLogin;
	}

	public String getTransactionChannel()
	{
		return transactionChannel;
	}

	public void setTransactionChannel(String transactionChannel)
	{
		this.transactionChannel = transactionChannel;
	}

	public String getTransactionMode()
	{
		return transactionMode;
	}

	public void setTransactionMode(String transactionMode)
	{
		this.transactionMode = transactionMode;
	}

	/**
	 * This is a property that can be set by the application.
	 * <p/>
	 * It stores the id of the external payment that was triggered by this message.
	 *
	 * @return the identification id set by the app
	 */
	public String getIdentificationTransactionId()
	{
		return identificationTransactionId;
	}

	/**
	 * This is a property that can be set by the application.
	 * It stores the id of the external payment that was triggered by this message.
	 *
	 * @param identificationTransactionId
	 */
	public void setIdentificationTransactionId(String identificationTransactionId)
	{
		this.identificationTransactionId = identificationTransactionId;
	}

	public String getPaymentCode()
	{
		return paymentCode;
	}

	@JsonIgnore
	public TransactionType getTransactionType()
	{
		PaymentCode paymentCode = PaymentCode.parse(this.paymentCode);

		return paymentCode.transactionType;
	}

	public void setPaymentCode(String paymentCode)
	{
		this.paymentCode = paymentCode;
	}

	public String getUsage()
	{
		return usage;
	}

	public void setUsage(String usage)
	{
		this.usage = usage;
	}

	public String getAmount()
	{
		return amount;
	}

	public void setAmount(String amount)
	{
		this.amount = amount;
	}

	public String getCurrency()
	{
		return currency;
	}

	public void setCurrency(String currency)
	{
		this.currency = currency;
	}

	public String getFrontendMode()
	{
		return frontendMode;
	}

	public void setFrontendMode(String frontendMode)
	{
		this.frontendMode = frontendMode;
	}

	public boolean isFrontendEnabled()
	{
		return frontendEnabled;
	}

	public void setFrontendEnabled(boolean frontendEnabled)
	{
		this.frontendEnabled = frontendEnabled;
	}

	public boolean isFrontendPopup()
	{
		return frontendPopup;
	}

	public void setFrontendPopup(boolean frontendPopup)
	{
		this.frontendPopup = frontendPopup;
	}

	public String getFrontendLanguage()
	{
		return frontendLanguage;
	}

	public void setFrontendLanguage(String frontendLanguage)
	{
		this.frontendLanguage = frontendLanguage;
	}

	public String getRequestVersion()
	{
		return requestVersion;
	}

	public void setRequestVersion(String requestVersion)
	{
		this.requestVersion = requestVersion;
	}

	public static HeidelpayHcoMessage fromMap(Map<String, String> map)
	{
		ObjectMapper mapper = new ObjectMapper();
		HeidelpayHcoMessage responseMsg = mapper.convertValue(map, HeidelpayHcoMessage.class);

		return responseMsg;
	}

	public MultiValueMap<String, String> toMultiValueMap()
	{
		ObjectMapper mapper = new ObjectMapper();
		Map<String, String> map = mapper.convertValue(this, new TypeReference<Map<String, String>>()
		{
		});

		if (paymentMethods != null && paymentMethods.size() > 0)
		{
			map.put("FRONTEND.PM.DEFAULT_DISABLE_ALL", "true");

			Iterator<PaymentMethod> iterator = paymentMethods.iterator();
			int i = 0;
			while (iterator.hasNext())
			{
				PaymentMethod paymentMethod = iterator.next();
				i++;

				map.put(String.format("FRONTEND.PM.%s.METHOD", i), HeidelpayPaymentMethodCode.asCode(paymentMethod));
				map.put(String.format("FRONTEND.PM.%s.ENABLED", i), "true");
			}
		}

		LinkedMultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
		multiValueMap.setAll(map);

		return multiValueMap;
	}

	/**
	 * This is set by heidelpay and must be referenced for certain operations.
	 * it's stored as {@link de.binaerebauten.gleichklang.core.model.payment.ExternalPaymentRegistration#registrationId}.
	 *
	 * @return
	 */
	public String getIdentificationUniqueId()
	{
		return identificationUniqueId;
	}

	public String getProcessingCode()
	{
		return processingCode;
	}

	public void setProcessingCode(String processingCode)
	{
		this.processingCode = processingCode;
	}

	public String getProcessingReason()
	{
		return processingReason;
	}

	public void setProcessingReason(String processingReason)
	{
		this.processingReason = processingReason;
	}

	public void setIdentificationUniqueId(String identificationUniqueId)
	{
		this.identificationUniqueId = identificationUniqueId;
	}

	public String getAccountRegistration()
	{
		return accountRegistration;
	}

	public void setAccountRegistration(String accountRegistration)
	{
		this.accountRegistration = accountRegistration;
	}

	public String getIdentificationReferenceId()
	{
		return identificationReferenceId;
	}

	public void setIdentificationReferenceId(String identificationReferenceId)
	{
		this.identificationReferenceId = identificationReferenceId;
	}

	@Deprecated //never used so also eventually not correct
	public ExternalPayment createPayment()
	{
		ExternalPayment payment = new ExternalPayment();

		payment.setMethod(getPaymentMethod());

		DecimalFormat df = (DecimalFormat) DecimalFormat.getNumberInstance(Locale.US);
		df.setParseBigDecimal(true);
		df.setMinimumFractionDigits(2);

		try
		{
			MonetaryAmount amount =
					new MonetaryAmount((BigDecimal) df.parse(getAmount()), AvailableCurrency.valueOf(getCurrency()));
			payment.setAmount(amount);
		}
		catch (ParseException e)
		{
			throw new NumberFormatException(e.getMessage());
		}

		return payment;
	}

	public String getProcessingReturnCode()
	{
		return processingReturnCode;
	}

	public void setProcessingReturnCode(String processingReturnCode)
	{
		this.processingReturnCode = processingReturnCode;
	}

	public String getProcessingReturn()
	{
		return processingReturn;
	}

	public void setProcessingReturn(String processingReturn)
	{
		this.processingReturn = processingReturn;
	}

	@JsonIgnore
	public PaymentMethod getPaymentMethod()
	{
		Objects.requireNonNull(paymentCode, "paymentCode == null");

		String paymentMethodCode = Splitter.on(".").split(paymentCode)
				.iterator().next();
		return HeidelpayPaymentMethodCode.asPaymentMethod(paymentMethodCode);
	}

	@JsonIgnore
	public ProcessingResultType getProcessingResult()
	{
		try
		{
			return ProcessingResultType.valueOf(getPostValidation());
		}
		catch (RuntimeException e)
		{
			return ProcessingResultType.UNKNOWN;
		}
	}

	public String getIdentificationInvoiceId()
	{
		return identificationInvoiceId;
	}

	public void setIdentificationInvoiceId(String identificationInvoiceId)
	{
		this.identificationInvoiceId = identificationInvoiceId;
	}

	public String getIdentificationShopperId()
	{
		return identificationShopperId;
	}

	public void setIdentificationShopperId(String identificationShopperId)
	{
		this.identificationShopperId = identificationShopperId;
	}
}
