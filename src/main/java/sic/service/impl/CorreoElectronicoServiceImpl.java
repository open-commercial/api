package sic.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.MailParseException;
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

  @Value("${MAIL_USERNAME}")
  private String userName;
  private JavaMailSender javaMailSender;

  @Autowired
  public CorreoElectronicoServiceImpl(JavaMailSender javaMailSender) {
    this.javaMailSender = javaMailSender;
  }

  @Override
  @Async
  public void sendMail(String toEmail, String subject, String message) throws MailException {
    SimpleMailMessage mailMessage = new SimpleMailMessage();
    mailMessage.setTo(toEmail);
    mailMessage.setSubject(subject);
    mailMessage.setText(message);
    javaMailSender.send(mailMessage);
  }

  @Override
  @Async
  public void sendMailWhitAttachment(
      String toEmail, String subject, String mensaje, byte[] byteArray, String attachmentDescription) throws MailException {
    MimeMessage message = javaMailSender.createMimeMessage();
    SimpleMailMessage mailMessage = new SimpleMailMessage();
    mailMessage.setTo(toEmail);
    mailMessage.setSubject(subject);
    mailMessage.setText(mensaje);
    try {
      MimeMessageHelper helper = new MimeMessageHelper(message, true);
      helper.setFrom(userName);
      helper.setTo(mailMessage.getTo());
      helper.setSubject(mailMessage.getSubject());
      helper.setText(mailMessage.getText());
      ByteArrayDataSource bds = new ByteArrayDataSource(byteArray, "application/pdf");
      helper.addAttachment(attachmentDescription, bds);
    } catch (MessagingException ex) {
      throw new MailParseException(ex);
    }
    javaMailSender.send(message);
  }
}
