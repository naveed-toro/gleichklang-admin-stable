package de.binaerebauten.gleichklang.core.service.payment.unzer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import de.binaerebauten.gleichklang.core.model.heidelpay.query.ProcessingType;
import de.binaerebauten.gleichklang.core.model.heidelpay.query.ReturnType;
import de.binaerebauten.gleichklang.core.model.heidelpay.query.TransactionResponseType;
import de.binaerebauten.gleichklang.core.model.payment.*;
import de.binaerebauten.gleichklang.core.model.user.Address;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.repository.*;
import de.binaerebauten.gleichklang.core.repository.user.UserRepository;
import de.binaerebauten.gleichklang.core.service.mail.MailSendService;
import de.binaerebauten.gleichklang.core.service.mail.UserMailTemplateService;
import de.binaerebauten.gleichklang.core.service.payment.InvoiceService;
import de.binaerebauten.gleichklang.core.service.payment.PaymentException;
import de.binaerebauten.gleichklang.core.service.payment.heidelpay.PaymentCode;
import de.binaerebauten.gleichklang.core.service.payment.heidelpay.ProcessingResultType;
import de.binaerebauten.gleichklang.core.service.payment.heidelpay.TransactionID;
import de.binaerebauten.gleichklang.core.service.payment.unzer.dto.*;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import javax.transaction.Transactional;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

import static de.binaerebauten.gleichklang.core.model.mail.UserMailTemplate.RENEWAL_FAILED_ACTUAL;
import static de.binaerebauten.gleichklang.core.utils.FunctionalUtils.nullSafe;

/**
 * Provides operation for handling the mapping of heidelpay transaction ids to
 * the internal payment model.
 */
@Service
public class UnzerTransactionService {
    private final HeidelpayTransactionRepository heidelpayTransactionRepository;

    private enum PaymentResponseState {
        PENDING,
        COMPLETED,
        CANCELED,
        PARTLY,
        PAYMENT_REVIEW,
        CHARGE_BACK,
        PAY_PAGE_INITIATED
    }
    @Value("${unzer.api.private.key}")
    private String privateKey;

    @Value("${server.base_url}")
    private String serverBaseUrl;

    private final ExternalPaymentRepository externalPaymentRepository;

    private final ExternalPaymentRegistrationRepository externalPaymentRegistrationRepository;
    private static final Logger LOG = LoggerFactory.getLogger(UnzerTransactionService.class);

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MailSendService mailSendService;

    @Autowired
    private UserMailTemplateService userMailTemplateService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private InvoiceRepository invoiceRepository;

    /**
     * This service uses constructor based dependency injection to ease testing.
     *
     * @param heidelpayTransactionRepository
     * @param externalPaymentRepository
     * @param externalPaymentRegistrationRepository
     */
    @Autowired
    public UnzerTransactionService(HeidelpayTransactionRepository heidelpayTransactionRepository,
                                   ExternalPaymentRepository externalPaymentRepository,
                                   ExternalPaymentRegistrationRepository externalPaymentRegistrationRepository) {
        this.heidelpayTransactionRepository = heidelpayTransactionRepository;
        this.externalPaymentRepository = externalPaymentRepository;
        this.externalPaymentRegistrationRepository = externalPaymentRegistrationRepository;
    }

    @org.springframework.transaction.annotation.Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 120000)
    public Map<String, String> processProlongationOrRenewPayment(Map<String, Object> successMap, ExternalPayment payment, User user) throws PaymentException {
        Map<String, String> result = new HashMap<>();
        try {
            ChargeAuthorizeCard chargeAuthorizeCard = new ChargeAuthorizeCard();
            chargeAuthorizeCard.setAmount(payment.getAmount().getAmount() + "");
            chargeAuthorizeCard.setCurrency(AvailableCurrency.EUR.name());
            chargeAuthorizeCard.setCardHolder(user.getFirstName() + " " + user.getLastName());
            chargeAuthorizeCard.setReturnUrl(serverBaseUrl + "rest/process3dResponse/" + user.getId() + "?prolongationAndRenew=true");
            Resources resources = new Resources();
            resources.setCustomerId("");
			/*if(PaymentMethod.CREDIT_CARD==payment.getMethod()) {
				resources.setCustomerId(user.getCustId());
				chargeAuthorizeCard.setCard3ds("true");
			}*/
            resources.setTypeId(user.getCardId());
            chargeAuthorizeCard.setCard3ds("true");
            chargeAuthorizeCard.setResources(resources);

            ChargeAuthorizeCardResponse chargeAuthorizeCardResponse = chargeDirectlyToCardToUnzer(successMap, user, chargeAuthorizeCard, true);
            if (chargeAuthorizeCardResponse != null && chargeAuthorizeCardResponse.isSuccess()) {
                successMap.put("chargeAuthorizeCardResponse", chargeAuthorizeCardResponse);
                String chargeId = chargeAuthorizeCardResponse.getId();
                String paymentId = chargeAuthorizeCardResponse.getResources().getPaymentId();

                LOG.info("Result cardId======" + chargeAuthorizeCardResponse.getResources().getTypeId());
                LOG.info("Result chargeId======" + chargeAuthorizeCardResponse.getId());
                LOG.info("Result paymentId======" + chargeAuthorizeCardResponse.getResources().getPaymentId());
                //process3dResponse(successMap, user.getId(), paymentId);

                invoiceRepository.saveAndFlush(payment.getInvoice());
                payment.setChargeId(chargeAuthorizeCardResponse.getId());
                payment.setPaymentId(chargeAuthorizeCardResponse.getResources().getPaymentId());
                paymentRepository.saveAndFlush(payment);
            }


            ///////////////////////

        } catch (Exception e) {
            LOG.error("Error while contacting external payment system", e.getMessage());
            throw new PaymentException(e);

        }
        //}

        return result;
    }
    public HttpHeaders createAuthHeaders(String username, String password) {
        return new HttpHeaders() {{
            String auth = username + ":" + password;
            byte[] encodedAuth = org.apache.commons.codec.binary.Base64.encodeBase64(
                    auth.getBytes(Charset.forName("US-ASCII")));
            String authHeader = "Basic " + new String(encodedAuth);
            set("Authorization", authHeader);
        }};
    }
    public ChargeAuthorizeCardResponse chargeDirectlyToCardToUnzer(Map<String, Object> successMap, User user, ChargeAuthorizeCard chargeAuthorizeCard, boolean prolongationRenewProcess) {
        HttpHeaders httpHeaders = createAuthHeaders(privateKey, "");
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        ObjectMapper mapper = new ObjectMapper();
        ChargeAuthorizeCardResponse chargeAuthorizeCardResponse = null;

        String chargeAuthorizeCardJson = "";
        try {
            chargeAuthorizeCard.getResources().setCustomerId("");
            chargeAuthorizeCardJson = mapper.writeValueAsString(chargeAuthorizeCard);
            LOG.info("charge authorize card request =========" + chargeAuthorizeCardJson);

        } catch (JsonProcessingException e) {
            LOG.error("Json Error", e.getMessage());
            successMap.put("Error", "Json Error");
        }
        HttpEntity<String> request = new HttpEntity<String>(chargeAuthorizeCardJson, httpHeaders);

        try {
            String result = "";
            try {
                result = restTemplate.postForObject("https://api.unzer.com/v1/payments/charges", request, String.class);
            } catch (HttpClientErrorException | HttpServerErrorException e) {
                LOG.error("chargeDirectlyToCardToUnzer error=========" + e.getResponseBodyAsString());
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    Map<String, Object> map = objectMapper.readValue(e.getResponseBodyAsString(), Map.class);
                    String jsonResult = new JSONObject(map).toString();
                    UnzerErrorsDTO unzerErrorsDTO = objectMapper.readValue(jsonResult, UnzerErrorsDTO.class);
                    successMap.put("Error", unzerErrorsDTO.getErrors().size() > 0 ? unzerErrorsDTO.getErrors().get(0).getCustomerMessage() : "Error contacting unzer payment gateway");

                } catch (Exception ex) {
                    successMap.put("Error", "Error contacting unzer payment gateway");

                }
            }
            LOG.info("charge authorize card response =========" + result);

            ObjectMapper objectMapper = new ObjectMapper();
            JSONObject json = new JSONObject(result);


            AbstractPayment payment = null;
            Optional<AbstractPayment> currAbstractPayment = paymentRepository.findCurrentPayment(user);
            if (currAbstractPayment.isPresent()) {
                payment = currAbstractPayment.get();
            }

            chargeAuthorizeCardResponse = objectMapper.readValue(result, ChargeAuthorizeCardResponse.class);
            Boolean isSuccess = (Boolean) json.get("isSuccess");
            Boolean isPending = (Boolean) json.get("isPending");
            chargeAuthorizeCardResponse.setSuccess(isSuccess);

            if (!prolongationRenewProcess) {
                getOrCreateExternalPaymentRegistration(user, chargeAuthorizeCardResponse.getProcessing().getUniqueId());
            }
            if (isSuccess.booleanValue() && payment instanceof ExternalPayment) {
                //getOrCreateExternalPaymentRegistration(user, chargeAuthorizeCardResponse.getProcessing().getUniqueId());
                ((ExternalPayment) payment).setExternalId(chargeAuthorizeCardResponse.getProcessing().getUniqueId());
                invoiceService.paymentReceived(payment, Boolean.valueOf(isSuccess));
            }
            chargeAuthorizeCardResponse.setPending(isPending);
            if (json.has("isError")) {
                Boolean isError = (Boolean) json.get("isError");
                chargeAuthorizeCardResponse.setError(isError);

                if (isError.booleanValue()) {
                    invoiceService.paymentFailed(payment);

                    final Product product = payment.getBaseProduct();
                    final Invoice invoice = invoiceService.createAndSaveInvoice(user, product, PaymentMethod.PREPAYMENT);
                    final Prepayment prepayment = (Prepayment) invoice.getPayments().stream()
                            .filter(p -> PaymentState.PENDING.equals(p.getState()))
                            .findFirst()
                            .orElseThrow(() -> new PaymentException("Received unknown payment for user", user.getEmail()));

                    mailSendService.sendEmail(user, userMailTemplateService.createMailTemplateInstance(RENEWAL_FAILED_ACTUAL, prepayment));

                    LOG.warn("External-Payment {} failed, Prepayment is created!", payment.getId());
                    successMap.put("Error", "Charge directly to card failed");
                }
            }

        } catch (Exception e) {
            successMap.put("Error", "Charge directly to card failed");
        }
        return chargeAuthorizeCardResponse;

    }

    @Transactional
    public ExternalPaymentRegistration createExternalPaymentRegistration(User user, String registrationId) {
        ExternalPaymentRegistration externalPaymentRegistration = new ExternalPaymentRegistration();

        externalPaymentRegistration.setLastUsedDate(LocalDateTime.now());
        externalPaymentRegistration.setRegistrationId(registrationId);
        externalPaymentRegistration.setUser(user);
        String externalReferenceId = UUID.randomUUID().toString();
        externalPaymentRegistration.setExternalReferenceId(externalReferenceId);

        externalPaymentRegistrationRepository.save(externalPaymentRegistration);
        return externalPaymentRegistration;
    }

    public ExternalPaymentRegistration getOrCreateExternalPaymentRegistration(User user, String registrationId) {
        Objects.requireNonNull(user, "user == null");
        Objects.requireNonNull(registrationId, "registrationId == null");

        ExternalPaymentRegistration externalPaymentRegistration =
                externalPaymentRegistrationRepository.findByUser(user);

        if (externalPaymentRegistration != null) {
            externalPaymentRegistrationRepository.delete(externalPaymentRegistration.getId());
        }

        externalPaymentRegistration = createExternalPaymentRegistration(user, registrationId);

        return externalPaymentRegistration;
    }
}
