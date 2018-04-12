package sic.controller;

import java.util.*;
import javax.persistence.EntityNotFoundException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import sic.service.ServiceException;

@ControllerAdvice
public class ControllersExceptionHandler {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    private String message(Exception ex) {
        String mensaje = ex.getMessage() + "\n(Transaction ID: " + new Date().getTime() + ")";
        log(ex, mensaje);
        return mensaje;
    }

    private void log(Exception ex, String mensaje) {
        if (ex.getCause() != null) {
            LOGGER.error(mensaje + " " + ex.getCause().getMessage() + "\n" + Arrays.toString(ex.getStackTrace()));
        } else {
            LOGGER.error(mensaje + " " + ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_error_sin_causa") + "\n" + Arrays.toString(ex.getStackTrace()));
        }
    }
    
    @ExceptionHandler(ServiceException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public String handleServiceException(ServiceException ex) {
        return message(ex);
    }
    
    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ResponseBody
    public String handleUnauthorizedException(UnauthorizedException ex) {
        return message(ex);
    }       
    
    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public String handleEntityNotFoundException(EntityNotFoundException ex) {        
        return message(ex);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Map<String, String> handleConstraintViolationException(ConstraintViolationException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        errors.put(ex.getMessage(), "(Transaction ID: " + new Date().getTime() + ")");
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            errors.put(violation.getRootBeanClass().getSimpleName() + ": " + violation.getPropertyPath(), violation.getMessage());
        }
        return errors;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    protected Map<String, String> handleMethodArgumentNotValid(final MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        // errors.put(ex.getMessage(), "(Transaction ID: " + new Date().getTime() + ")");
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        return errors;
    }
    
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public String handleException(Exception ex) {
        return message(ex);
    }
}
