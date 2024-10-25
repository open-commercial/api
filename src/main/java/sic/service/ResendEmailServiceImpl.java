package sic.service;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.Attachment;
import com.resend.services.emails.model.SendEmailRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import sic.exception.ServiceException;

import java.util.Base64;
import java.util.Locale;

@Service("resend")
public class ResendEmailServiceImpl implements EmailService {

  @Value("${RESEND_TOKEN}")
  private String resendToken;

  @Value("${RESEND_FROM}")
  private String resendFrom;

  private final MessageSource messageSource;

  @Autowired
  public ResendEmailServiceImpl(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  @Override
  public boolean isServicioConfigurado() {
    return resendToken != null && !resendToken.isEmpty()
            && resendFrom != null && !resendFrom.isEmpty();
  }

  @Override
  @Async
  public void enviarEmail(String toEmail, String bcc, String subject,
                          String mensaje, byte[] byteArray, String attachmentName) {
    if (!isServicioConfigurado()) {
      throw new ServiceException(messageSource.getMessage(
              "mensaje_correo_resend_no_configurado", null, Locale.getDefault()));
    }
    var resend = new Resend(resendToken);
    Attachment attachment;
    SendEmailRequest sendEmailRequest;
    if (byteArray != null) {
      attachment = Attachment.builder()
              .fileName(attachmentName)
              .content(Base64.getEncoder().encodeToString(byteArray))
              .build();
      sendEmailRequest = SendEmailRequest.builder()
              .from(resendFrom)
              .to(toEmail)
              .bcc(bcc)
              .attachments(attachment)
              .html(mensaje)
              .subject(subject)
              .build();
    } else {
      sendEmailRequest = SendEmailRequest.builder()
              .from(resendFrom)
              .to(toEmail)
              .bcc(bcc)
              .html(mensaje)
              .subject(subject)
              .build();
    }
    try {
      resend.emails().send(sendEmailRequest);
    } catch (ResendException ex) {
      throw new ServiceException(messageSource.getMessage(
              "mensaje_correo_error", null, Locale.getDefault()), ex);
    }
  }
}
