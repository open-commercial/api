package sic.controller;

import lombok.Data;
import java.util.Arrays;
import java.util.Date;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

@Data
public class ApiError {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    private String message;

    ApiError(MethodArgumentNotValidException ex) {
        message = ex.getMessage() + "\n(Transaction ID: " + new Date().getTime() + ")\n";
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            message = message.concat(error.getField() + ": " + error.getDefaultMessage() + "\n");
        }
        log(message, ex);
    }

    ApiError(ConstraintViolationException ex) {
        message = ex.getMessage() + "\n(Transaction ID: " + new Date().getTime() + ")\n";
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            message = message.concat(violation.getRootBeanClass().getSimpleName() + ":\n " +
                    violation.getPropertyPath() + ": " + violation.getMessage() + "\n");
        }
        log(message, ex);
    }

    ApiError(Exception ex) {
        message = ex.getMessage() + "\n(Transaction ID: " + new Date().getTime() + ")";
        log(message, ex);
    }

    public String getMessage() {
        return this.message;
    }

    private void log(String mensaje, Exception ex) {
        if (ex.getCause() != null) {
            LOGGER.error(mensaje + " " + ex.getCause().getMessage() + "\n" + Arrays.toString(ex.getStackTrace()));
        } else {
            LOGGER.error(mensaje + " " + ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_error_sin_causa") + "\n" + Arrays.toString(ex.getStackTrace()));
        }
    }

}
