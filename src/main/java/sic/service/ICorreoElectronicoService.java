package sic.service;

public interface ICorreoElectronicoService {

  void enviarMailPorEmpresa(
      long idEmpresa,
      String toEmail,
      String subject,
      String mensaje,
      byte[] byteArray,
      String attachmentDescription);
}
