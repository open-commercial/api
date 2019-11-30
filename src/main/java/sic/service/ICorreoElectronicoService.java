package sic.service;

public interface ICorreoElectronicoService {

  void enviarEmail(
      String toEmail,
      String bcc,
      String subject,
      String mensaje,
      byte[] byteArray,
      String attachmentDescription);
}
