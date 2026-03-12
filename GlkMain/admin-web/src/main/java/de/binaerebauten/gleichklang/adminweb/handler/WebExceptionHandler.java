package de.binaerebauten.gleichklang.adminweb.handler;

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
public class WebExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(WebExceptionHandler.class);

    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Bad request.")
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public void badRequest(MethodArgumentNotValidException e) throws MethodArgumentNotValidException {
        LOG.error("badRequest "+e.getMessage(), e);
        throw e;
    }

    @ResponseStatus(value = HttpStatus.UNAUTHORIZED, reason = "No authorization.")
    @ExceptionHandler(SecurityException.class)
    public void authorizationFailed(SecurityException e) {
        LOG.error("No authorization "+ e.getMessage(), e);
        throw e;
    }

    @ResponseStatus(value = HttpStatus.CONFLICT, reason = "Data integrity violation.")
    @ExceptionHandler(DataIntegrityViolationException.class)
    public void conflict(DataIntegrityViolationException e) {
        LOG.error("Data integrity violation "+e.getMessage(), e);
        throw e;
    }

    @ResponseStatus(value = HttpStatus.CONFLICT, reason = "Item already exists.")
    @ExceptionHandler(EntityExistsException.class)
    public void conflict(EntityExistsException e) {
        LOG.error("Item already exists " +e.getMessage());
        throw e;
    }

    @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Item not found.")
    @ExceptionHandler(NoSuchElementException.class)
    public void notExist(NoSuchElementException e) {
        LOG.error("Item not found "+e.getMessage());
        throw e;
    }

    @ExceptionHandler(Exception.class)
    public void defaultErrorHandler(Exception ex) throws Exception {
        LOG.error("defaultErrorHandler", ex);
        throw ex;
    }
}
