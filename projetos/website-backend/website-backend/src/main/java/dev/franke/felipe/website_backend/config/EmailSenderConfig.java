package dev.franke.felipe.website_backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class EmailSenderConfig {

    @Value("${MAIL_HOST:smtp.gmail.com}")
    private String mailHost;

    @Value("${MAIL_PORT:587}")
    private String mailPort;

    @Value("${MAIL_USER:user}")
    private String mailUser;

    @Value("${MAIL_PASS:pass}")
    private String mailPass;

    private static final String SUBJECT = "Felipe, você recebeu uma nova mensagem do seu Site!";

    @Bean
    JavaMailSender mailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(mailHost);
        mailSender.setPort(Integer.parseInt(mailPort));
        mailSender.setUsername(mailUser);
        mailSender.setPassword(mailPass);
        return mailSender;
    }

    @Bean
    SimpleMailMessage templateMessage() {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(mailUser);
        message.setSubject(SUBJECT);
        return message;
    }
}
