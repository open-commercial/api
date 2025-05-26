package org.opencommercial.controller;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.opencommercial.exception.ForbiddenException;
import org.opencommercial.exception.ServiceException;
import org.opencommercial.exception.UnauthorizedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Locale;

@RestControllerAdvice
@Slf4j
public class ExceptionControllerAdvice {

  private final MessageSource messageSource;

  @Autowired
  public ExceptionControllerAdvice(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  private String logError(Exception ex) {
    String mensaje =
        ex.getMessage()
            + "\n(Transaction ID: "
            + LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            + ")";
    log.error(mensaje, ex);
    return ex.getMessage();
  }

  private String logWarn(Exception ex) {
    String mensaje =
            ex.getMessage()
                    + "\n(Transaction ID: "
                    + LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    + ")";
    log.warn(mensaje, ex);
    return ex.getMessage();
  }

  @ExceptionHandler(ConstraintViolationException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public String handleConstraintViolationException(ConstraintViolationException ex) {
    String mensaje = "";
    for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
      mensaje = mensaje.concat(violation.getMessage() + "\n");
    }
    String transactionID =
        mensaje.concat(
            "(Transaction ID: "
                + LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                + ")");
    log.error(transactionID, ex);
    return mensaje;
  }

  @ExceptionHandler(ServiceException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public String handleServiceException(ServiceException ex) {
    return this.logError(ex);
  }

  @ExceptionHandler(UnauthorizedException.class)
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  public String handleUnauthorizedException(UnauthorizedException ex) {
    return this.logWarn(ex);
  }

  @ExceptionHandler(ForbiddenException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  public String handleForbiddenException(ForbiddenException ex) {
    return this.logWarn(ex);
  }

  @ExceptionHandler(EntityNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public String handleEntityNotFoundException(EntityNotFoundException ex) {
    return this.logError(ex);
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public String handleException(Exception ex) {
    return this.logError(
            new Exception(messageSource.getMessage("mensaje_error_request", null, Locale.getDefault()), ex));
  }
}
