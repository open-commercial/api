package sic.service;

import jakarta.mail.*;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.ByteArrayDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import sic.exception.ServiceException;

import java.util.Locale;
import java.util.Properties;

@Service("gmail")
public class GmailEmailServiceImpl implements EmailService {

  @Value("${GMAIL_USERNAME}")
  private String gmailUsername;

  @Value("${GMAIL_PASSWORD}")
  private String gmailPassword;

  private final MessageSource messageSource;

  @Autowired
  public GmailEmailServiceImpl(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  @Override
  public boolean isServicioConfigurado() {
    return gmailUsername != null && !gmailUsername.isEmpty()
            && gmailPassword != null && !gmailPassword.isEmpty();
  }

  @Override
  @Async
  public void enviarEmail(String toEmail, String bcc, String subject, String mensaje,
                          byte[] byteArray, String attachmentName) {
    if (!isServicioConfigurado()) {
      throw new ServiceException(messageSource.getMessage(
              "mensaje_correo_gmail_no_configurado", null, Locale.getDefault()));
    }
    var props = new Properties();
    props.put("mail.smtp.host", "smtp.gmail.com");
    props.put("mail.smtp.port", "587");
    props.put("mail.smtp.auth", "true");
    props.put("mail.smtp.starttls.enable", "true");
    try {
      var auth = new Authenticator() {
        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
          return new PasswordAuthentication(gmailUsername, gmailPassword);
        }
      };
      var message = new MimeMessage(Session.getInstance(props, auth));
      var helper = new MimeMessageHelper(message, true);
      helper.setFrom(gmailUsername);
      helper.setTo(toEmail);
      if (bcc != null && !bcc.isEmpty()) helper.setBcc(bcc);
      helper.setSubject(subject);
      helper.setText(mensaje);
      if (byteArray != null) {
        var byteArrayDataSource = new ByteArrayDataSource(byteArray, "application/pdf");
        helper.addAttachment(attachmentName, byteArrayDataSource);
      }
      Transport.send(helper.getMimeMessage());
    } catch (MessagingException | MailException ex) {
      throw new ServiceException(messageSource.getMessage(
              "mensaje_correo_error", null, Locale.getDefault()), ex);
    }
  }
}
