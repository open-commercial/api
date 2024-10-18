package sic.util;

import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.stereotype.Component;
import org.springframework.validation.beanvalidation.MessageSourceResourceBundleLocator;
import jakarta.validation.Validator;
import jakarta.validation.Validation;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.Set;

@Component
public class CustomValidator {

  private final Validator validator;

  public CustomValidator() {
    validator =
            Validation.byDefaultProvider()
                      .configure()
                      .messageInterpolator(
                            new ResourceBundleMessageInterpolator(
                                    new MessageSourceResourceBundleLocator(getMessageSource())))
                      .buildValidatorFactory()
                      .getValidator();
  }

  public void validar(Object o) {
    Set<ConstraintViolation<Object>> violations = validator.validate(o);
    if (!violations.isEmpty()) throw new ConstraintViolationException(violations);
  }

  // Por default busca ValidationMessages.properties, pero no lo carga como UTF-8,
  // por eso este metodo extra
  private MessageSource getMessageSource() {
    var messageSource = new ReloadableResourceBundleMessageSource();
    messageSource.setBasename("classpath:ValidationMessages");
    messageSource.setDefaultEncoding("UTF-8");
    return messageSource;
  }
}
