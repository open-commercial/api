package sic.service;

public interface ICorreoElectronicoService {

  void enviarMail(String toEmail, String subject, String message);

  void enviarMailConAdjunto(
      String toEmail,
      String subject,
      String mensaje,
      byte[] byteArray,
      String attachmentDescription);
}
