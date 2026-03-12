package de.binaerebauten.gleichklang.core.security;

import java.io.IOException;
import java.util.Set;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class SimpleCORSFilter extends OncePerRequestFilter
{
	private static final String ORIGIN_HEADER = "Origin";
	
	@Value("${cors.allowed.origins}")
	private Set<String> allowedOrigins;
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException
	{
		String originValue = request.getHeader(ORIGIN_HEADER);
		if (originValue != null)
		{
			originValue = originValue.trim();
			if (allowedOrigins.contains(originValue))
			{
				response.setHeader("Access-Control-Allow-Origin", originValue);
				response.setHeader("Access-Control-Allow-Credentials", "true");
				response.setHeader("Access-Control-Allow-Methods", "POST, GET, PUT, OPTIONS, DELETE");
				response.setHeader("Access-Control-Max-Age", "3600");
				response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
			}
		}
		filterChain.doFilter(request, response);
	}
}