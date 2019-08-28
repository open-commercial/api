package sic.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
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
import sic.modelo.Sucursal;
import sic.exception.BusinessServiceException;
import sic.service.IConfiguracionDelSistemaService;
import sic.service.ICorreoElectronicoService;
import sic.service.ISucursalService;

import java.util.Locale;
import java.util.Properties;

@Service
public class CorreoElectronicoServiceImpl implements ICorreoElectronicoService {

  @Value("${SIC_MAIL_ENV}")
  private String mailEnv;

  private final IConfiguracionDelSistemaService configuracionDelSistemaService;
  private final ISucursalService sucursalService;
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private final MessageSource messageSource;

  @Autowired
  public CorreoElectronicoServiceImpl(
      IConfiguracionDelSistemaService configuracionDelSistemaService,
      ISucursalService sucursalService,
      MessageSource messageSource) {
    this.configuracionDelSistemaService = configuracionDelSistemaService;
    this.sucursalService = sucursalService;
    this.messageSource = messageSource;
  }

  @Override
  @Async
  public void enviarMailPorSucursal(
      long idSucursal,
      String toEmail,
      String bcc,
      String subject,
      String mensaje,
      byte[] byteArray,
      String attachmentDescription) {
    Sucursal sucursal = sucursalService.getSucursalPorId(idSucursal);
    ConfiguracionDelSistema cds =
        configuracionDelSistemaService.getConfiguracionDelSistemaPorSucursal(sucursal);
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
                .getConfiguracionDelSistemaPorId(idSucursal)
                .getEmailUsername());
        helper.setTo(toEmail);
        if (bcc != null && !bcc.isEmpty()) helper.setBcc(bcc);
        helper.setSubject(subject);
        helper.setText(mensaje);
        if (byteArray != null) {
          ByteArrayDataSource bds = new ByteArrayDataSource(byteArray, "application/pdf");
          helper.addAttachment(attachmentDescription, bds);
        }
        Transport.send(helper.getMimeMessage());
      } catch (MessagingException | MailException ex) {
        throw new BusinessServiceException(messageSource.getMessage(
          "mensaje_correo_error", null, Locale.getDefault()), ex);
      }
    } else {
      logger.error("Mail environment = {}, el mail NO se envió.", mailEnv);
    }
  }
}
