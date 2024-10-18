package sic.service;

import org.springframework.stereotype.Component;
import sic.exception.ServiceException;

import java.util.Map;

@Component
public class EmailServiceFactory {

  private final Map<String, EmailService> emailServices;

  public EmailServiceFactory(Map<String, EmailService> emailServices) {
    this.emailServices = emailServices;
  }

  public EmailService getEmailService(String emailServiceProvider) {
    var emailService = emailServices.get(emailServiceProvider);
    if (emailService == null) {
      throw new ServiceException("Proveedor de email no soportado!");
    }
    return emailService;
  }
}