package sic.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
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
import sic.service.IConfiguracionDelSistemaService;
import sic.service.ICorreoElectronicoService;
import java.util.Properties;

@Service
public class CorreoElectronicoServiceImpl implements ICorreoElectronicoService {

  @Value("${SIC_MAIL_ENV}")
  private String mailEnv;

  private IConfiguracionDelSistemaService configuracionDelSistemaService;
  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Autowired
  public CorreoElectronicoServiceImpl(IConfiguracionDelSistemaService configuracionDelSistemaService) {
    this.configuracionDelSistemaService = configuracionDelSistemaService;
  }

  @Override
  @Async
  public void enviarMailPorEmpresa(
          long idEmpresa,
          String toEmail,
          String subject,
          String mensaje,
          byte[] byteArray,
          String attachmentDescription) {
    if (mailEnv.equals("production")) {
      Properties props = new Properties();
      props.put("mail.smtp.host", "smtp.gmail.com"); // SMTP Host
      props.put("mail.smtp.port", "587"); // TLS Port
      props.put("mail.smtp.auth", "true"); // enable authentication
      props.put("mail.smtp.starttls.enable", "true"); // enable STARTTLS
      // create Authenticator object to pass in Session.getInstance argument
      try {
        Authenticator auth =
                new Authenticator() {
                  // override the getPasswordAuthentication method
                  protected PasswordAuthentication getPasswordAuthentication() {
                    ConfiguracionDelSistema cds = configuracionDelSistemaService
                            .getConfiguracionDelSistemaPorId(idEmpresa);
                    return new PasswordAuthentication(cds.getEmailUsername(), cds.getEmailPassword());
                  }
                };
        Session session = Session.getInstance(props, auth);
        MimeMessage message = new MimeMessage(session);
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(toEmail);
        mailMessage.setSubject(subject);
        mailMessage.setText(mensaje);
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom(configuracionDelSistemaService
                .getConfiguracionDelSistemaPorId(idEmpresa)
                .getEmailUsername());
        helper.setTo(mailMessage.getTo());
        helper.setSubject(mailMessage.getSubject());
        helper.setText(mailMessage.getText());
        if (byteArray != null) {
          ByteArrayDataSource bds = new ByteArrayDataSource(byteArray, "application/pdf");
          helper.addAttachment(attachmentDescription, bds);
        }
        Transport.send(helper.getMimeMessage());
      } catch (MessagingException | MailException  ex) {
        logger.error(ex.getMessage(), ex);
      }
    } else {
      logger.warn("Mail environment = {}, el mail NO se envió.", mailEnv);
    }
  }
}
