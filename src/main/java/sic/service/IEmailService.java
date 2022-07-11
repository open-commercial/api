package sic.service;

import sic.modelo.EnvioDeCorreoGrupal;
import sic.modelo.dto.EnvioDeCorreoGrupalDTO;

public interface IEmailService {

  void enviarEmail(
      String[] toEmail,
      String bcc,
      String subject,
      String mensaje,
      byte[] byteArray,
      String attachmentDescription);

  void enviarEmail(
          String toEmail,
          String bcc,
          String subject,
          String mensaje,
          byte[] byteArray,
          String attachmentDescription);

  EnvioDeCorreoGrupal enviarCorreoGrupal(EnvioDeCorreoGrupalDTO envioDeCorreoGrupal);
}
