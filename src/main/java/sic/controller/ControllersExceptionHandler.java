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
        LOGGER.error(mensaje, ex);
        return mensaje;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public String handleConstraintViolationException(ConstraintViolationException ex) {
        String mensaje = "";
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            mensaje = mensaje.concat(violation.getMessage() + "\n");
        }
        mensaje = mensaje.concat("(Transaction ID: " + new Date().getTime() + ")");
        LOGGER.error(mensaje, ex);
        return mensaje;
    }

    @ExceptionHandler(ServiceException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public String handleServiceException(ServiceException ex) {
        return this.log(ex);
    }

    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ResponseBody
    public String handleUnauthorizedException(UnauthorizedException ex) {
        return this.log(ex);
    }       
    
    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public String handleEntityNotFoundException(EntityNotFoundException ex) {
        return this.log(ex);
    }
    
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public String handleException(Exception ex) {
        return this.log(ex);
    }
}
