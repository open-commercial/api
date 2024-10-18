package sic.service;

public interface EmailService {

  boolean isServicioConfigurado();

  void enviarEmail(String toEmail, String bcc, String subject, String mensaje,
                   byte[] byteArray, String attachmentName);
}
