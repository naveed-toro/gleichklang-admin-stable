package de.binaerebauten.gleichklang.core.utils;

import de.binaerebauten.gleichklang.core.service.mail.EmailLinkService;
import de.binaerebauten.gleichklang.core.utils.ParametersHolder.ParameterKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Objects;

/**
 * Small helper component to build app specific uris.
 */
@Component
public class AppUrlBuilder
{

	private static final Logger LOG = LoggerFactory.getLogger(AppUrlBuilder.class);

	private final URI serverBaseUrl;

	@Value("${server.ui.host}")
	private String serverUiHost;
	
	@Autowired
	public AppUrlBuilder(@Value("${server.base_url}") URI serverBaseUrl)
	{
		this.serverBaseUrl = serverBaseUrl;
	}
	
	private UriComponentsBuilder fromBaseUrl()
	{
		return UriComponentsBuilder.fromUri(serverBaseUrl);
	}
	
	/*public URI toApi(String token)
	{
		Objects.requireNonNull(token, "token == null");
		
		return fromBaseUrl().pathSegment("api", EmailLinkService.API_PATH).queryParam(EmailLinkService.TOKEN_PARAMETER, token).build().toUri();
	}*/

	public URI toApi(String token)
	{
		Objects.requireNonNull(token, "token == null");

		return UriComponentsBuilder.newInstance()
				.scheme("http").host(serverUiHost)
				.path("forgetpassword").queryParam(EmailLinkService.TOKEN_PARAMETER, token).build().toUri();
	}

	public URI toApi(String token,String tempPassword)
	{
		Objects.requireNonNull(token, "token == null");
		Objects.requireNonNull(tempPassword, "tempPassword == null");

//		return fromBaseUrl().pathSegment("rest").pathSegment("setResetPasswordFromEmailLink").queryParam(EmailLinkService.TOKEN_PARAMETER, token).queryParam(EmailLinkService.TEMP_PASSWORD, tempPassword).build().toUri();
		return fromBaseUrl().pathSegment("rest").pathSegment("setResetPasswordFromEmailLink")
				.pathSegment(token, tempPassword).build().toUri();
	}
	
	public URI toAutoLogin(String email, String password)
	{
		// must be a fragment, because only fragments can be removed without reloading the page
		final String fragment = EmailLinkService.QUICK_LOGIN_FRAGMENT + "?" + ParameterKey.EMAIL.getValue() + "=" + email + "&" + ParameterKey.PASSWORD.getValue() + "=" + password;
		
		return toVaadinFragment(fragment);
	}
	
	/**
	 * Builds an url to the given vaddin fragment/view.
	 *
	 * @param fragment the non-null fragment
	 * @return the absolute url to the given fragment
	 */
	public URI toVaadinFragment(String fragment)
	{
		Objects.requireNonNull(fragment, "fragment == null");
		
		return fromBaseUrl().fragment(fragment).build().toUri();
	}

	public URI toAutoLoginReact(String email, String password)
	{
		Objects.requireNonNull(email, "email== null");
		Objects.requireNonNull(password, "password== null");
		return UriComponentsBuilder.newInstance()
				.scheme("https").host(serverUiHost)
				.path("autoLogin").queryParam(EmailLinkService.EMAIL_PARAMETER, email).queryParam(EmailLinkService.TEMP_PASSWORD, password).build().toUri();
	}

    public URI toApiMailConfirmation(String token)
    {
        Objects.requireNonNull(token, "token == null");
		LOG.info("in createEmailLinkMailConfirmation");
        return UriComponentsBuilder.newInstance()
                .scheme("http").host(serverUiHost)
                .path("link").queryParam(EmailLinkService.TOKEN_PARAMETER, token).build().toUri();
    }
	
	public URI toAppPath()
	{
		return fromBaseUrl().build().toUri();
	}
	
	public URI toAppPath(String path)
	{
		Objects.requireNonNull(path, "path == null");
		
		return fromBaseUrl().path(path).build().toUri();
	}
	
	public URI toAppPath(ParametersHolder parametersHolder)
	{
		Objects.requireNonNull(parametersHolder, "parametersHolder == null");
		
		return fromBaseUrl().queryParams(parametersHolder.getMultiMap()).build().toUri();
	}
	
	public URI toAppPath(String path, ParametersHolder parametersHolder)
	{
		Objects.requireNonNull(path, "path == null");
		Objects.requireNonNull(parametersHolder, "parametersHolder == null");
		
		return fromBaseUrl().path(path).queryParams(parametersHolder.getMultiMap()).build().toUri();
	}
}
