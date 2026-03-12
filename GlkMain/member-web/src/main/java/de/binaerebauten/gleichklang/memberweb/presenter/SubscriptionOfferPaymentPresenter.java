package de.binaerebauten.gleichklang.memberweb.presenter;

import com.google.common.base.Preconditions;
import com.vaadin.server.Page;
import com.vaadin.ui.Notification;
import de.binaerebauten.gleichklang.core.model.LocalizedEntity;
import de.binaerebauten.gleichklang.core.model.NaturalKeyEntity.NaturalKey;
import de.binaerebauten.gleichklang.core.model.payment.*;
import de.binaerebauten.gleichklang.core.model.user.Address;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.model.user.UserPaymentSettings;
import de.binaerebauten.gleichklang.core.presenter.NavigatePresenter;
import de.binaerebauten.gleichklang.core.repository.InitialSubscriptionOfferRepository;
import de.binaerebauten.gleichklang.core.repository.UserPaymentSettingsRepository;
import de.binaerebauten.gleichklang.core.repository.user.CompleteUserRepository;
import de.binaerebauten.gleichklang.core.security.AuthenticationService;
import de.binaerebauten.gleichklang.core.service.SubscriptionService;
import de.binaerebauten.gleichklang.core.service.payment.PaymentException;
import de.binaerebauten.gleichklang.core.service.payment.PaymentService;
import de.binaerebauten.gleichklang.core.view.filter.InitialSubscriptionOfferFilter;
import de.binaerebauten.gleichklang.memberweb.service.SubscriptionOfferService;
import de.binaerebauten.gleichklang.memberweb.view.PaymentResultView;
import de.binaerebauten.gleichklang.memberweb.view.SubscriptionOfferPaymentView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Presenter for the {@link SubscriptionOfferPaymentView} and {@link PaymentResultView}.
 */
public class SubscriptionOfferPaymentPresenter extends NavigatePresenter
		implements SubscriptionOfferPaymentView.SubscriptionOfferPaymentViewListener
{
	private static final Logger LOG = LoggerFactory.getLogger(SubscriptionOfferPaymentPresenter.class);

	private final SubscriptionOfferPaymentView subscriptionOfferPaymentView;
	private final PaymentResultView paymentResultView;

	private final CompleteUserRepository userRepository;
	private final AuthenticationService authenticationService;

	private final UserPaymentSettingsRepository userPaymentSettingsRepository;
	private final InitialSubscriptionOfferRepository initialSubscriptionOfferRepository;
	private final SubscriptionOfferService subscriptionOfferService;
	private final SubscriptionService subscriptionService;
	private final PaymentService paymentService;
	
	// affiliate tracking debuggung
	private final boolean showAffiliateTrackingAlert;

	private InitialSubscriptionOfferFilter offerFilter;

	public SubscriptionOfferPaymentPresenter(ApplicationContext ctx, SubscriptionOfferPaymentView subscriptionOfferPaymentView,
			PaymentResultView paymentResultView)
	{
		super(subscriptionOfferPaymentView);
		
		this.subscriptionOfferPaymentView = subscriptionOfferPaymentView;
		this.subscriptionOfferPaymentView.setListener(this);

		this.paymentResultView = paymentResultView;
		
		this.userRepository = ctx.getBean(CompleteUserRepository.class);
		this.authenticationService = ctx.getBean(AuthenticationService.class);
		
		this.userPaymentSettingsRepository = ctx.getBean(UserPaymentSettingsRepository.class);
		this.initialSubscriptionOfferRepository = ctx.getBean(InitialSubscriptionOfferRepository.class);
		this.subscriptionOfferService = ctx.getBean(SubscriptionOfferService.class);
		this.subscriptionService = ctx.getBean(SubscriptionService.class);
		this.paymentService = ctx.getBean(PaymentService.class);
		
		final Environment environment = ctx.getEnvironment();
		this.showAffiliateTrackingAlert = environment.getProperty("affiliate_tracking.alert", boolean.class, false);
	}

	@Override
	public void select(Product product, PaymentMethod paymentMethod)
	{
		// nothing needs to be done here
	}

	public void showPaymentResult(AbstractPayment payment)
	{
		if (payment instanceof Prepayment)
		{
			Prepayment prepayment = (Prepayment) payment;
			String prepaymentInfo = prepayment.getTranslatedInfo();

			paymentResultView.showPaymentResult(prepaymentInfo);
		}
		else
		{
			String message = payment.getState() != PaymentState.FAILED ?
					I18N.SUBSCRIPTIONPRESENTER_PAYMENT_PURCHASED.msg() :
					I18N.SUBSCRIPTIONPRESENTER_PAYMENT_PURCHASED_ERROR.msg();
			paymentResultView.showPaymentResult(message);
		}
		
		executeJsForAffiliatePartners(payment);
	}
	
	private void executeJsForAffiliatePartners(AbstractPayment payment)
	{
		//add trackingpixels
		if (payment != null)
		{
			final double superclixRefund = payment.getAmount().getAmount().doubleValue() * 0.375;
			final double addcellNet = payment.getAmount().getAmount().doubleValue() / 119 * 100;
			
			//superclix trackingpixel
			final String superClixAlertBoxInject = this.showAffiliateTrackingAlert ? "alert('injected superclix trackingpixel. refund is " + superclixRefund + "'); \n" : "";
			final String superClixAlertBoxRemove = this.showAffiliateTrackingAlert ? "alert('removed superclix trackingpixel');" : "";
			Page.getCurrent().getJavaScript().execute("var img = new Image(1,1); \n" +
					"img.id = \"superclix\";" +
					"img.src = 'https://clix.superclix.de/cgi-bin/code.cgi?pp=5652&cashflow=" + superclixRefund + "&tax=1.00&goods=" + payment.getId() + "'; \n" +
					"document.head.appendChild(img); \n" +
					superClixAlertBoxInject +
					"setTimeout(function(){ \n" +
					"var el = document.getElementById('superclix'); \n" +
					" if (el != null) { \n" +
					" el.parentNode.removeChild(el); \n " + superClixAlertBoxRemove +
					" } \n" +
					"}, 6000);");
			//addcell js
			
			final String cellJSAlertBoxInject = this.showAffiliateTrackingAlert ? "script.onload = function() { alert(\"addcell Script loaded and ready\");};\n" : "";
			final String cellJSAlertBoxRemove = this.showAffiliateTrackingAlert ? "alert('removed addcell trackinscript');" : "";
			Page.getCurrent().getJavaScript().execute("var script = document.createElement('script');\n" +
					"script.id = \"addcelljs\";" +
					"script.src = \"https://www.adcell.de/js/track.js?pid=4042&eventid=5115&referenz=" + payment.getId() + "&betrag=" + addcellNet + "\";\n" +
					"document.head.appendChild(script);\n" +
					cellJSAlertBoxInject +
					"setTimeout(function(){ \n" +
					"var el = document.getElementById('addcelljs');\n" +
					"if (el != null) {\n" +
					"el.parentNode.removeChild(el);\n " + cellJSAlertBoxRemove + "}\n" +
					"}, 8000);");
			//addcell trackingpixel
			final String addCellImageAlertBoxInject = this.showAffiliateTrackingAlert ? "alert('injected addcell trackingpixel. net is " + addcellNet + "'); \n" : "";
			final String addCellImageAlertBoxRemove = this.showAffiliateTrackingAlert ? "alert('removed addcell trackingpixel');" : "";
			Page.getCurrent().getJavaScript().execute("var img = new Image(1,1); \n" +
					"img.id = \"addcellimage\"; " +
					"img.src = 'https://www.adcell.de/event.php?pid=4042&eventid=5115&referenz=" + payment.getId() + "&betrag=" + addcellNet + "'\n" +
					"document.head.appendChild(img); \n" +
					addCellImageAlertBoxInject +
					"setTimeout(function(){ \n" +
					"var el = document.getElementById('addcellimage'); \n" +
					" if (el != null) { \n" +
					" el.parentNode.removeChild(el); \n " + addCellImageAlertBoxRemove +
					" } \n" +
					"}, 10000);");
			
			// Google Converstion
			Page.getCurrent().getJavaScript().execute("var scriptGoogleVar = document.createElement('script');\n" +
					"scriptGoogleVar.id = 'adword';\n" +
					"var codeGoogle = document.createTextNode('/* <![CDATA[ */\\n' +\n" +
					"    'var google_conversion_id = 1067411411;\\n' +\n" +
					"    'var google_conversion_label = \"R7-kCM2KSRDTz_38Aw\";\\n' +\n" +
					"    'var google_conversion_value = 1.00;\\n' +\n" +
					"    'var google_conversion_currency = \"EUR\";\\n' +\n" +
					"    'var google_remarketing_only = false;\\n' +\n" +
					"    '/* ]]> */');\n" +
					"scriptGoogleVar.appendChild(codeGoogle);\n" +
					"document.head.appendChild(scriptGoogleVar);\n" +
					"var scriptGoogleSrc = document.createElement('script');\n" +
					"scriptGoogleSrc.src = '//www.googleadservices.com/pagead/conversion.js';\n" +
					"scriptGoogleSrc.type = 'text/javascript';\n" +
					"document.head.appendChild(scriptGoogleSrc);\n" +
					"var googleNoscript = document.createElement('noscript');\n" +
					"var googleLayer = document.createElement('div');\n" +
					"var googleImage = document.createElement('img');\n" +
					"googleImage.setAttribute('height', 1);\n" +
					"googleImage.setAttribute('width', 1);\n" +
					"googleImage.setAttribute('style', 'border-style:none;');\n" +
					"googleImage.src = '//www.googleadservices.com/pagead/conversion/1067411411/?value=1.00&amp;currency_code=EUR&amp;label=R7-kCM2KSRDTz_38Aw&amp;guid=ON&amp;script=0';\n" +
					"googleLayer.setAttribute('style', 'display:inline;');\n" +
					"googleLayer.appendChild(googleImage);\n" +
					"googleNoscript.appendChild(googleImage);\n" +
					"document.head.appendChild(googleNoscript);");
			
			// Bing Conversion
			Page.getCurrent().getJavaScript().execute("var scriptBingSrc = document.createElement('script');\n" +
					"var bingCode = document.createTextNode('(function(w,d,t,r,u){var f,n,i;w[u]=w[u]||[],f=function(){var o={ti:\"5189955\"};o.q=w[u],w[u]=new UET(o),w[u].push(\"pageLoad\")},n=d.createElement(t),n.src=r,n.async=1,n.onload=n.onreadystatechange=function(){var s=this.readyState;s&&s!==\"loaded\"&&s!==\"complete\"||(f(),n.onload=n.onreadystatechange=null)},i=d.getElementsByTagName(t)[0],i.parentNode.insertBefore(n,i)})(window,document,\"script\",\"//bat.bing.com/bat.js\",\"uetq\");');\n" +
					"scriptBingSrc.appendChild(bingCode);\n" +
					"document.head.appendChild(scriptBingSrc);\n" +
					"var bingNoScript = document.createElement('noscript');\n" +
					"var bingImg = document.createElement('img');\n" +
					"bingImg.src = '//bat.bing.com/action/0?ti=5189955&Ver=2';\n" +
					"bingImg.setAttribute('height', 0);\n" +
					"bingImg.setAttribute('width', 0);\n" +
					"bingImg.setAttribute('style', 'display:none; visibility: hidden;');\n" +
					"bingNoScript.appendChild(bingImg);\n" +
					"document.head.appendChild(bingNoScript);");
		}
	}
	
	@Override
	public void actionCodeChanged(String actionCode)
	{
		boolean isValid = subscriptionOfferService.isValidActionCode(actionCode, offerFilter.getCategories());

		offerFilter = offerFilter.withActionCode(actionCode, isValid);
		updateOffers();

		// Commented out in order to prevent the brute-force getting right actions codes
		// subscriptionOfferPaymentView.setActionCodeIsValid(isValid);

		if (isValid)
		{
			User user = getUser();
			Optional<UserPaymentSettings> paymentSettings = userPaymentSettingsRepository.findByUser(user);
			if (paymentSettings.isPresent())
			{
				UserPaymentSettings userPaymentSettings = paymentSettings.get();
				userPaymentSettings.setActionCode(actionCode);
				userPaymentSettingsRepository.save(userPaymentSettings);
			}
		}
	}

	@Override
	public void paymentMethodChanged(PaymentMethod paymentMethod)
	{
		try
		{
			paymentService.updateUserPaymentSettings(getUser(), paymentMethod);
		}
		catch (PaymentException e)
		{
			LOG.error("Error updating user payment settings: ", e);
			Notification.show(I18N.SUBSCRIPTIONPRESENTER_PAYMENTSETTINGS_UPDATEERRORTITLE.msg(),
					I18N.SUBSCRIPTIONPRESENTER_PAYMENTSETTINGS_UPDATEERROR.msg(), Notification.Type.ERROR_MESSAGE);
		}
	}

	@Override
	public void enter(String parameters)
	{
		final User user = getUser();
		
		final boolean directDebitPossible = user.getOptionalPaymentAddress()
				.map(Address::getCountry)
				.map(LocalizedEntity::getI18nKey)
				.filter(k -> NaturalKey.GERMANY.naturalKey.equals(k) || NaturalKey.AUSTRIA.naturalKey.equals(k))
				.isPresent();
		
		final Set<PaymentMethod> availablePaymentMethods = Arrays.stream(PaymentMethod.values())
				.filter(paymentMethod -> !PaymentMethod.DIRECT_DEBIT.equals(paymentMethod) || directDebitPossible)
				.collect(Collectors.toCollection(LinkedHashSet::new));
		subscriptionOfferPaymentView.setAvailablePaymentMethods(availablePaymentMethods);
		
		Set<RecommendationCategory> categories = subscriptionService.getLastSubscriptionOfferCategories(user.getId());
		if (categories.isEmpty())
		{
			categories = user.getCategories();
		}
		
		UserPaymentSettings userPaymentSettings = paymentService.createOrGetUserPaymentSettings(user);
		if(!availablePaymentMethods.contains(userPaymentSettings.getPaymentMethod()))
		{
			paymentMethodChanged(PaymentMethod.PREPAYMENT);
			userPaymentSettings = paymentService.createOrGetUserPaymentSettings(user);
		}
		
		Preconditions.checkState(availablePaymentMethods.contains(userPaymentSettings.getPaymentMethod()));
		
		String actionCode = userPaymentSettings.getActionCode();
		subscriptionOfferPaymentView.setUserPaymentSettings(userPaymentSettings);

		boolean isValid = subscriptionOfferService.isValidActionCode(actionCode, categories);
		offerFilter = new InitialSubscriptionOfferFilter(LocalDate.now(), categories, actionCode, isValid);

		updateOffers();
	}

	private void updateOffers()
	{
		List<InitialSubscriptionOffer> offers = initialSubscriptionOfferRepository.findAll(offerFilter,
				InitialSubscriptionOfferRepository.DEFAULT_SORT);

		// For some action codes, we have to show also the standard tariffs
		boolean showStandardTariffs = offers.stream().anyMatch(Product::isAdditional);
		if (showStandardTariffs)
		{
			InitialSubscriptionOfferFilter standardOfferFilter = offerFilter.withActionCode("", true);
			offers.addAll(initialSubscriptionOfferRepository.findAll(standardOfferFilter,
					InitialSubscriptionOfferRepository.DEFAULT_SORT));
		}

		subscriptionOfferPaymentView.setOffers(offers);
	}

	private User getUser()
	{
		Long authenticatedUserId = authenticationService.getAuthenticatedUserId();
		return userRepository.findById(authenticatedUserId);
	}

}
