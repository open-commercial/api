package sic.service;

public interface ICorreoElectronicoService {

  void enviarMailPorEmpresa(
      long idEmpresa,
      String toEmail,
      String bbc,
      String subject,
      String mensaje,
      byte[] byteArray,
      String attachmentDescription);
}
