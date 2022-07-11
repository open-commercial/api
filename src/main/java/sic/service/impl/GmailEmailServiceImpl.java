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
import sic.exception.BusinessServiceException;
import sic.modelo.EnvioDeCorreoGrupal;
import sic.modelo.Rol;
import sic.modelo.Usuario;
import sic.modelo.dto.EnvioDeCorreoGrupalDTO;
import sic.repository.EnvioDeCorreoGrupalRepository;
import sic.service.IUsuarioService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import sic.service.IEmailService;
import java.util.Locale;
import java.util.Properties;

@Service
public class GmailEmailServiceImpl implements IEmailService {

  @Value("${SIC_MAIL_ENV}")
  private String mailEnv;

  @Value("${SIC_MAIL_USERNAME}")
  private String emailUsername;

  @Value("${SIC_MAIL_PASSWORD}")
  private String emailPassword;

  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private final MessageSource messageSource;
  private final EnvioDeCorreoGrupalRepository envioDeCorreoGrupalRepository;
  private final IUsuarioService usuarioService;

  @Autowired
  public GmailEmailServiceImpl(MessageSource messageSource, EnvioDeCorreoGrupalRepository envioDeCorreoGrupalRepository,
            IUsuarioService usuarioService) {
    this.messageSource = messageSource;
    this.envioDeCorreoGrupalRepository = envioDeCorreoGrupalRepository;
    this.usuarioService = usuarioService;
  }

  @Override
  @Async
  public void enviarEmail(
      String[] toEmails,
      String bcc,
      String subject,
      String mensaje,
      byte[] byteArray,
      String attachmentDescription) {
    if (mailEnv.equals("production")
        && !emailUsername.isEmpty()
        && !emailPassword.isEmpty()) {
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
                return new PasswordAuthentication(emailUsername, emailPassword);
              }
            };
        MimeMessage message = new MimeMessage(Session.getInstance(props, auth));
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom(emailUsername);
        helper.setTo(toEmails);
        if (bcc != null && !bcc.isEmpty()) helper.setBcc(bcc);
        helper.setSubject(subject);
        helper.setText(mensaje);
        if (byteArray != null) {
          ByteArrayDataSource bds = new ByteArrayDataSource(byteArray, "application/pdf");
          helper.addAttachment(attachmentDescription, bds);
        }
        Transport.send(helper.getMimeMessage());
      } catch (MessagingException | MailException ex) {
        throw new BusinessServiceException(
            messageSource.getMessage("mensaje_correo_error", null, Locale.getDefault()), ex);
      }
    } else {
      logger.error("Mail environment = {}, el mail NO se envió.", mailEnv);
    }
  }

  @Override
  @Async
  public void enviarEmail(
          String toEmail,
          String bcc,
          String subject,
          String mensaje,
          byte[] byteArray,
          String attachmentDescription) {
    List<String> correosDeUsuarios = new ArrayList<>();
    correosDeUsuarios.add(toEmail);
    this.enviarEmail(correosDeUsuarios.toArray(String[]::new), bcc, subject, mensaje, byteArray, attachmentDescription);
  }

  @Override
  @Async
  public EnvioDeCorreoGrupal enviarCorreoGrupal(EnvioDeCorreoGrupalDTO envioDeCorreoGrupalDTO) {
    EnvioDeCorreoGrupal envioDeCorreoGrupal = this.crearEnvioDeCorreoGrupal(envioDeCorreoGrupalDTO);
    List<Usuario> usuariosParaEnviarReporte = this.getUsuariosParaEnviaEmail(envioDeCorreoGrupalDTO.getRoles());
    List<String> correosDeUsuarios = new ArrayList<>();
    usuariosParaEnviarReporte.stream().filter(usuario -> usuario.getEmail() != null)
                    .forEach(usuario -> correosDeUsuarios.add(usuario.getEmail()));
    this.enviarEmail(
            correosDeUsuarios.toArray(String[]::new),
            emailUsername,
            envioDeCorreoGrupalDTO.getAsunto(),
            envioDeCorreoGrupalDTO.getMensaje(),
            envioDeCorreoGrupal.getReporteAdjunto(),
            envioDeCorreoGrupal.getDescripcionAdjunto());
    return envioDeCorreoGrupalRepository.save(this.crearEnvioDeCorreoGrupal(envioDeCorreoGrupalDTO));
  }

  private EnvioDeCorreoGrupal crearEnvioDeCorreoGrupal(EnvioDeCorreoGrupalDTO envioDeCorreoGrupalDTO) {
     return EnvioDeCorreoGrupal.builder()
             .fecha(LocalDateTime.now())
             .asunto(envioDeCorreoGrupalDTO.getAsunto())
             .roles(envioDeCorreoGrupalDTO.getRoles())
             .mensaje(envioDeCorreoGrupalDTO.getMensaje())
             .reporteAdjunto(envioDeCorreoGrupalDTO.getReporteAdjunto())
             .build();
  }

  private List<Usuario> getUsuariosParaEnviaEmail(List<Rol> roles) {
    List<Usuario> usuarios = new ArrayList<>();
    roles.forEach(rol -> {
      usuarios.addAll(usuarioService.getUsuariosPorRol(rol).getContent());
    });
    return usuarios;
  }
}
