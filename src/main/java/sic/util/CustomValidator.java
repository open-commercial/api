package sic.util;

import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.stereotype.Component;
import org.springframework.validation.beanvalidation.MessageSourceResourceBundleLocator;

import javax.validation.*;
import java.util.Set;

@Component
public class CustomValidator {

  private Validator validator;

  public CustomValidator() {
    this.build();
  }

  public void validar(Object o) {
    Set<ConstraintViolation<Object>> violations = validator.validate(o);
    if (!violations.isEmpty()) throw new ConstraintViolationException(violations);
  }

  private void build() {
    validator =
        Validation.byDefaultProvider()
            .configure()
            .messageInterpolator(
                new ResourceBundleMessageInterpolator(
                    new MessageSourceResourceBundleLocator(getMessageSource())))
            .buildValidatorFactory()
            .getValidator();
  }

  // Por default busca ValidationMessages.properties, pero no lo carga como UTF-8,
  // por eso este metodo extra
  private MessageSource getMessageSource() {
    ReloadableResourceBundleMessageSource messageSource =
        new ReloadableResourceBundleMessageSource();
    messageSource.setBasename("classpath:ValidationMessages");
    messageSource.setDefaultEncoding("UTF-8");
    return messageSource;
  }
}
