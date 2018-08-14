package sic.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import sic.service.ICorreoElectronicoService;

@Service
public class CorreoElectronicoServiceImpl implements ICorreoElectronicoService {

    private JavaMailSender javaMailSender;

    @Autowired
    public CorreoElectronicoServiceImpl(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

   // @Async
    public void sendMail(String toEmail, String subject, String message) throws MailException {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(toEmail);
        mailMessage.setSubject(subject);
        mailMessage.setText(message);
        //mailMessage.setFrom("ventas@globodistribuciones.com"); recuperar del propperties
        mailMessage.setFrom("mayol.jose.f@gmail.com");
        javaMailSender.send(mailMessage);
    }
}
