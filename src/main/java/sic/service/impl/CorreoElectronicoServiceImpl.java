package sic.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.mail.util.ByteArrayDataSource;
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Transport;
import sic.modelo.ConfiguracionDelSistema;
import sic.modelo.Empresa;
import sic.service.BusinessServiceException;
import sic.service.IConfiguracionDelSistemaService;
import sic.service.ICorreoElectronicoService;
import sic.service.IEmpresaService;
import java.util.Properties;
import java.util.ResourceBundle;

@Service
public class CorreoElectronicoServiceImpl implements ICorreoElectronicoService {

  @Value("${SIC_MAIL_ENV}")
  private String mailEnv;

  private final IConfiguracionDelSistemaService configuracionDelSistemaService;
  private final IEmpresaService empresaService;
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("Mensajes");

  @Autowired
  public CorreoElectronicoServiceImpl(
      IConfiguracionDelSistemaService configuracionDelSistemaService,
      IEmpresaService empresaService) {
    this.configuracionDelSistemaService = configuracionDelSistemaService;
    this.empresaService = empresaService;
  }

  @Override
  @Async
  public void enviarMailPorEmpresa(
      long idEmpresa,
      String toEmail,
      String bbc,
      String subject,
      String mensaje,
      byte[] byteArray,
      String attachmentDescription) {
    Empresa empresa = empresaService.getEmpresaPorId(idEmpresa);
    ConfiguracionDelSistema cds =
        configuracionDelSistemaService.getConfiguracionDelSistemaPorEmpresa(empresa);
    if (mailEnv.equals("production") && cds.isEmailSenderHabilitado()) {
      Properties props = new Properties();
      props.put("mail.smtp.host", "smtp.gmail.com");
      props.put("mail.smtp.port", "587");
      props.put("mail.smtp.auth", "true");
      props.put("mail.smtp.starttls.enable", "true");
      try {
        Authenticator auth =
            new Authenticator() {
              @Override
              protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(cds.getEmailUsername(), cds.getEmailPassword());
              }
            };
        Session session = Session.getInstance(props, auth);
        MimeMessage message = new MimeMessage(session);
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom(
            configuracionDelSistemaService
                .getConfiguracionDelSistemaPorId(idEmpresa)
                .getEmailUsername());
        helper.setTo(toEmail);
        if (bbc != null && !bbc.isEmpty()) helper.setBcc(bbc);
        helper.setSubject(subject);
        helper.setText(mensaje);
        if (byteArray != null) {
          ByteArrayDataSource bds = new ByteArrayDataSource(byteArray, "application/pdf");
          helper.addAttachment(attachmentDescription, bds);
        }
        Transport.send(helper.getMimeMessage());
      } catch (MessagingException | MailException ex) {
        throw new BusinessServiceException(
          RESOURCE_BUNDLE.getString("mensaje_correo_error"), ex);
      }
    } else {
      logger.error("Mail environment = {}, el mail NO se envió.", mailEnv);
    }
  }
}
