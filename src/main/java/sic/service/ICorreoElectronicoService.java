package sic.service;

import org.springframework.mail.MailException;

public interface ICorreoElectronicoService {

    void sendMail(String toEmail, String subject, String message) throws MailException;
}
