package de.binaerebauten.gleichklang.core.config;

import com.google.common.collect.Lists;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;

/**
 * This configuration adds everything required for the heidelpay client integration.
 */
@Configuration
public class HeidelpayClientConfig
{
	@Bean
	public RestTemplate heidelpayRestTemplate()
	{
		RestTemplate restTemplate = new RestTemplate();

		ArrayList<HttpMessageConverter<?>> httpMessageConverters = Lists.newArrayList(restTemplate.getMessageConverters());

		FormHttpMessageConverter heidelpayResponseMessageConverter = new FormHttpMessageConverter();

		// add a specific message converter since heidelpay sends content type text/plain
		heidelpayResponseMessageConverter.setSupportedMediaTypes(Collections.singletonList(MediaType.TEXT_PLAIN));
		httpMessageConverters.add(0, heidelpayResponseMessageConverter);

		restTemplate.setMessageConverters(httpMessageConverters);

		return restTemplate;
	}
}
