package sic.controller;

import java.util.*;
import javax.persistence.EntityNotFoundException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import sic.service.ServiceException;

@ControllerAdvice
public class ControllersExceptionHandler {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    private String log(Exception ex) {
        String mensaje = ex.getMessage() + "\n(Transaction ID: " + new Date().getTime() + ")";
        if (ex.getCause() != null) {
            LOGGER.error(mensaje + " " + ex.getCause().getMessage() + "\n" + Arrays.toString(ex.getStackTrace()));
        } else {
            LOGGER.error(mensaje + " " + ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_error_sin_causa") + "\n" + Arrays.toString(ex.getStackTrace()));
        }
        return mensaje;
    }
    
    @ExceptionHandler(ServiceException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public String handleServiceException(ServiceException ex) {
        return log(ex);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public String handleConstraintViolationException(ConstraintViolationException ex) {
        log(ex);
        String message = "";
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            message = message.concat(violation.getMessage() + "\n");
        }
        return message;
    }

    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ResponseBody
    public String handleUnauthorizedException(UnauthorizedException ex) {
        return log(ex);
    }       
    
    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public String handleEntityNotFoundException(EntityNotFoundException ex) {        
        return log(ex);
    }
    
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public String handleException(Exception ex) {
        return log(ex);
    }
}
