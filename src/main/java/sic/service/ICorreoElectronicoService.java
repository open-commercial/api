package sic.service;

public interface ICorreoElectronicoService {

  void enviarMailPorSucursal(
      long idSucursal,
      String toEmail,
      String bbc,
      String subject,
      String mensaje,
      byte[] byteArray,
      String attachmentDescription);
}
