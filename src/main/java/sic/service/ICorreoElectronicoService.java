package sic.service;

import org.springframework.mail.MailException;

public interface ICorreoElectronicoService {

    void sendMail(String toEmail, String subject, String message) throws MailException;

    void sendMailWhitAttachment(String toEmail, String subject, String mensaje, byte[] byteArray, String attachmentDescription) throws MailException;
}
