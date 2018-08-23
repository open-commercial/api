package sic.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.util.ByteArrayDataSource;
import sic.service.ICorreoElectronicoService;

@Service
public class CorreoElectronicoServiceImpl implements ICorreoElectronicoService {

  @Value("${SIC_MAIL_ENV}")
  private String mailEnv;

  @Value("${SIC_MAIL_USERNAME}")
  private String mailUsername;

  private JavaMailSender javaMailSender;
  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Autowired
  public CorreoElectronicoServiceImpl(JavaMailSender javaMailSender) {
    this.javaMailSender = javaMailSender;
  }

  @Override
  @Async
  public void enviarMail(String toEmail, String subject, String message) {
    if (mailEnv.equals("production")) {
      SimpleMailMessage mailMessage = new SimpleMailMessage();
      mailMessage.setTo(toEmail);
      mailMessage.setSubject(subject);
      mailMessage.setText(message);
      try {
        javaMailSender.send(mailMessage);
      } catch (MailException ex) {
        logger.error(ex.getMessage(), ex);
      }
    } else {
      logger.warn("Mail environment = {}, el mail NO se envió.", mailEnv);
    }
  }

  @Override
  @Async
  public void enviarMailConAdjunto(
      String toEmail,
      String subject,
      String mensaje,
      byte[] byteArray,
      String attachmentDescription) {
    if (mailEnv.equals("production")) {
      MimeMessage message = javaMailSender.createMimeMessage();
      SimpleMailMessage mailMessage = new SimpleMailMessage();
      mailMessage.setTo(toEmail);
      mailMessage.setSubject(subject);
      mailMessage.setText(mensaje);
      try {
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom(mailUsername);
        helper.setTo(mailMessage.getTo());
        helper.setSubject(mailMessage.getSubject());
        helper.setText(mailMessage.getText());
        ByteArrayDataSource bds = new ByteArrayDataSource(byteArray, "application/pdf");
        helper.addAttachment(attachmentDescription, bds);
        javaMailSender.send(message);
      } catch (MessagingException | MailException ex) {
        logger.error(ex.getMessage(), ex);
      }
    } else {
      logger.warn("Mail environment = {}, el mail NO se envió.", mailEnv);
    }
  }
}
