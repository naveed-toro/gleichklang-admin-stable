package de.binaerebauten.gleichklang.memberweb.controller.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.view.RedirectView;

import javax.persistence.EntityExistsException;
import java.util.NoSuchElementException;

@ControllerAdvice
public class WebExceptionHandling
{
	@Autowired
	private Environment environment;
	
	private static final Logger LOG = LoggerFactory.getLogger(WebExceptionHandling.class);

	@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Bad request.")
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public void badRequest(MethodArgumentNotValidException e) {
		LOG.error(e.getMessage());
	}

	@ResponseStatus(value = HttpStatus.UNAUTHORIZED, reason = "No authorization.")
	@ExceptionHandler(SecurityException.class)
	public void authorizationFailed(SecurityException e) {
		LOG.error(e.getMessage());
	}

	@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Data integrity violation.")
	@ExceptionHandler(DataIntegrityViolationException.class)
	public void conflict(DataIntegrityViolationException e) {
		LOG.error(e.getMessage());
	}

	@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Item already exists.")
	@ExceptionHandler(EntityExistsException.class)
	public void conflict(EntityExistsException e) { LOG.error(e.getMessage()); }

	@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Item not found.")
	@ExceptionHandler(NoSuchElementException.class)
	public void notExist(NoSuchElementException e) {
		LOG.error(e.getMessage());
	}
	
	@ExceptionHandler(Exception.class)
	public RedirectView defaultErrorHandler(Exception ex)
	{
		LOG.error("defaultErrorHandler", ex);
		
		final String landingRoot = environment.getProperty("landing.root", "https://www.gleichklang.de");
		return new RedirectView(landingRoot + "/fehler-meldung");
	}
}
