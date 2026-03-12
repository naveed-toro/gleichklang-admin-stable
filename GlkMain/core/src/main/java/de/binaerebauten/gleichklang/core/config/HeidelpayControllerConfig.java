package de.binaerebauten.gleichklang.core.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.Collections;
import java.util.List;

/**
 * This configuration adds everything required for the heidelpay server side integration.
 */
@Configuration
@EnableWebMvc
public class HeidelpayControllerConfig extends WebMvcConfigurerAdapter
{
	public static final String TRANSACTION_MEDIA_TYPE_VALUE = "text/xml;charset=\"utf-8\"";

	@Override
	public void extendMessageConverters(List<HttpMessageConverter<?>> converters)
	{
		Jaxb2RootElementHttpMessageConverter jaxb2RootElementHttpMessageConverter = new Jaxb2RootElementHttpMessageConverter();

		// add a message converter with the media userType required by heidelpay push
		MediaType mediaType = MediaType.parseMediaType(TRANSACTION_MEDIA_TYPE_VALUE);

		List<MediaType> supportedMediaTypes = Collections.singletonList(mediaType);
		jaxb2RootElementHttpMessageConverter.setSupportedMediaTypes(supportedMediaTypes);

		converters.add(jaxb2RootElementHttpMessageConverter);
	}
}
