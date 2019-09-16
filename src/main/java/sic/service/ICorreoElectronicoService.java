package sic.service;

public interface ICorreoElectronicoService {

  void enviarEmail(
      String toEmail,
      String subject,
      String mensaje,
      byte[] byteArray,
      String attachmentDescription);
}
