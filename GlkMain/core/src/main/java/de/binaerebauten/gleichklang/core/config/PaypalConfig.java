package de.binaerebauten.gleichklang.core.config;

import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.OAuthTokenCredential;
import com.paypal.base.rest.PayPalRESTException;
import de.binaerebauten.gleichklang.core.model.paypaltoken.PaypalToken;
import de.binaerebauten.gleichklang.core.repository.user.PaypalTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

//import org.springframework.security.oauth2.client.DefaultOAuth2ClientContext;
//import org.springframework.security.oauth2.client.OAuth2RestTemplate;
//import org.springframework.security.oauth2.client.resource.OAuth2AccessDeniedException;
//import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
//import org.springframework.security.oauth2.common.OAuth2AccessToken;

@Configuration
public class PaypalConfig {

    private static final Logger LOG = LoggerFactory.getLogger(PaypalConfig.class);

    @Value("${paypal.client.id}")
    private String clientId;

    @Value("${paypal.client.secret}")
    private String clientSecret;

    @Value("${paypal.mode}")
    private String mode;

    @Autowired
    private PaypalTokenRepository paypalTokenRepository;

    @Bean
    public Map<String, String> paypalSdkConfig() {
        Map<String, String> configMap = new HashMap<>();
        configMap.put("mode", mode);
        return configMap;
    }

    @Bean
    public OAuthTokenCredential oAuthTokenCredential() {
        return new OAuthTokenCredential(clientId, clientSecret, paypalSdkConfig());
    }

    @Bean
    public APIContext apiContext() throws PayPalRESTException {
        APIContext context = new APIContext(oAuthTokenCredential().getAccessToken());
        context.setConfigurationMap(paypalSdkConfig());
        return context;
    }

    @Scheduled(fixedRate = 1800000)
    @Transactional
    public void refreshPaypalToken()
    {
        LOG.info("refreshPaypalToken started");
        try
        {

            paypalTokenRepository.deletePaypalToken();
            PaypalToken paypalToken=new PaypalToken();
            paypalToken.setToken(apiContext().getAccessToken());
            paypalTokenRepository.save(paypalToken);

         }
        catch (Throwable e)
        {
            LOG.error("refreshPaypalToken", e);
        }
        LOG.info("refreshPaypalToken ended");
    }

//    @Bean
//    public OAuth2AccessToken oAuth2AccessToken() throws PayPalRESTException {
//        ClientCredentialsResourceDetails resource = new ClientCredentialsResourceDetails();
//        resource.setAccessTokenUri("https://api-m.paypal.com/v1/oauth2/token");
//        resource.setClientId(clientId);
//        resource.setClientSecret(clientSecret);
//        resource.setGrantType("client_credentials");
//        resource.setScope(Arrays.asList("read","write","identity"));
//
//        DefaultOAuth2ClientContext defaultOAuth2ClientContext=new DefaultOAuth2ClientContext();
//        try
//        {
//            OAuth2RestTemplate oAuth2RestTemplate = new OAuth2RestTemplate(resource, defaultOAuth2ClientContext);
//            OAuth2AccessToken oAuth2AccessToken = oAuth2RestTemplate.getAccessToken();
//            return oAuth2AccessToken;
//        }
//        catch (OAuth2AccessDeniedException e)
//        {
//            return null;
//        }
//    }

}
