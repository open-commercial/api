package sic.controller;

import java.util.*;
import javax.persistence.EntityNotFoundException;
import javax.validation.ConstraintViolationException;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import sic.service.ServiceException;

@ControllerAdvice
public class ControllersExceptionHandler {
    
    @ExceptionHandler(ServiceException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public String handleServiceException(ServiceException ex) {
        return (new ApiError(ex)).getMessage();
    }
    
    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ResponseBody
    public String handleUnauthorizedException(UnauthorizedException ex) {
        return (new ApiError(ex)).getMessage();
    }       
    
    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public String handleEntityNotFoundException(EntityNotFoundException ex) {        
        return (new ApiError(ex)).getMessage();
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public String handleConstraintViolationException(ConstraintViolationException ex) {
        return (new ApiError(ex)).getMessage();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    protected String handleMethodArgumentNotValid(final MethodArgumentNotValidException ex) {
        return (new ApiError(ex)).getMessage();
    }
    
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public String handleException(Exception ex) {
        return (new ApiError(new Exception(ResourceBundle.getBundle("Mensajes").getString("mensaje_error_request"), ex)).getMessage());
    }
}
