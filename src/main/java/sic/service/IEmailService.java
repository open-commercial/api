package sic.service;

public interface IEmailService {

  boolean isServicioDeshabilitado();

  void enviarEmail(String toEmail, String bcc, String subject, String mensaje,
                   byte[] byteArray, String attachmentDescription);
}
