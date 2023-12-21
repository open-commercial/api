package sic.controller;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import javax.persistence.EntityNotFoundException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import sic.exception.ForbiddenException;
import sic.exception.UnauthorizedException;
import sic.exception.ServiceException;

@RestControllerAdvice
@Slf4j
public class ExceptionControllerAdvice {

  private final MessageSource messageSource;

  @Autowired
  public ExceptionControllerAdvice(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  private String log(Exception ex) {
    String mensaje =
        ex.getMessage()
            + "\n(Transaction ID: "
            + LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            + ")";
    log.error(mensaje, ex);
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
    return this.log(ex);
  }

  @ExceptionHandler(UnauthorizedException.class)
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  public String handleUnauthorizedException(UnauthorizedException ex) {
    return this.log(ex);
  }

  @ExceptionHandler(ForbiddenException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  public String handleForbiddenException(ForbiddenException ex) {
    return this.log(ex);
  }

  @ExceptionHandler(EntityNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public String handleEntityNotFoundException(EntityNotFoundException ex) {
    return this.log(ex);
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public String handleException(Exception ex) {
    return log(
        new Exception(
            messageSource.getMessage("mensaje_error_request", null, Locale.getDefault()), ex));
  }
}
