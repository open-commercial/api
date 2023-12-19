package sic.service.impl;

import org.springframework.stereotype.Component;
import sic.exception.ServiceException;
import sic.service.IEmailService;
import java.util.Map;

@Component
public class EmailServiceFactory {

  private final Map<String, IEmailService> emailServices;

  public EmailServiceFactory(Map<String, IEmailService> emailServices) {
    this.emailServices = emailServices;
  }

  public IEmailService getEmailService(EmailServiceProvider emailServiceProvider) {
    var emailService = emailServices.get(emailServiceProvider.getName());
    if (emailService == null) {
      throw new ServiceException("Proveedor de email no soportado!");
    }
    return emailService;
  }
}