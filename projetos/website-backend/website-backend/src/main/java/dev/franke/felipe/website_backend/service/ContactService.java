package dev.franke.felipe.website_backend.service;

import dev.franke.felipe.website_backend.dto.ContactRequest;
import dev.franke.felipe.website_backend.dto.ContactResponse;
import dev.franke.felipe.website_backend.exception.ContactException;
import dev.franke.felipe.website_backend.model.Contact;
import dev.franke.felipe.website_backend.model.InternalLog;
import dev.franke.felipe.website_backend.repository.ContactRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContactService {

    private final ContactRepository contactRepository;
    private final MailSender mailSender;
    private final SimpleMailMessage templateMessage;
    private final InternalLogService internalLogService;

    private static final long PRUNE_SENT_RATE_MS = 1800000; // A cada meia hora
    private static final long SEND_PENDING_MESSAGES_RATE_MS = 3600000; // A cada uma hora
    private static final long PRUNE_OLD_MESSAGES_RATE_MS = 86400000; // A cada 24 horas
    private static final short EMAIL_MAX_ATTEMPTS = 3;

    // ------------ MÉTODOS QUE NÃO SERÃO VISÍVEIS INICIO ------------

    @Scheduled(fixedRate = PRUNE_OLD_MESSAGES_RATE_MS)
    @Transactional
    public void pruneOldMessages() {
        log.info("Start prune old messages");
        contactRepository.deleteOldMessages();
        log.info("End prune old messages");
    }

    @Scheduled(fixedRate = PRUNE_SENT_RATE_MS)
    @Transactional
    public void pruneSentMessages() {
        log.info("Start prune sent messages");
        contactRepository.deleteAlreadySentMessages();
        log.info("End prune sent messages");
    }

    @Scheduled(fixedRate = SEND_PENDING_MESSAGES_RATE_MS)
    public void sendPendingMessages() {
        log.info("Sending pending contact messages");
        List<Contact> pendingMessages = contactRepository.findPendingMessages();
        if (pendingMessages.isEmpty()) {
            log.debug("No pending contact messages found");
            return;
        }
        pendingMessages.forEach(this::sendMessage);
        log.trace("Messages sent");
    }

    // ------------ MÉTODOS QUE NÃO SERÃO VISÍVEIS FIM ------------

    public ContactResponse saveContact(ContactRequest contactRequest) {
        log.info("Called saveContact() from ContactService");
        log.trace("Request: {}", contactRequest);
        Contact contact = new Contact();
        contact.setName(contactRequest.name());
        contact.setEmail(contactRequest.email());
        contact.setMessage(contactRequest.message());
        contact = contactRepository.save(contact);
        return new ContactResponse(contact.getId(), contact.getCreatedAt());
    }

    private void incrementRetryCount(Contact contact) {
        log.info("Called incrementRetryCount() from ContactService");
        short addTo = 1;
        short newValue = (short) (contact.getRetryCount() + addTo);
        contact.setRetryCount(newValue);
        contactRepository.save(contact);
        log.trace("Saved successfully");

        /*
            Conforme anotado no arquivo ARCHITECTURE.md, quando existem mais de
            três tentativas, o sistema falha sileciosamente e após um tempo, este
            registro é excluído do sistema. Vale salvar essa informação (sobre
            máximo de tentativas alcançado) em algum lugar.
        */
        if (newValue > EMAIL_MAX_ATTEMPTS) {
            log.error("Email Failed to be delivered. EMAIL_MAX_ATTEMPTS reached!");
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder
                    .append("Failed to send Email. Max retries reached! ")
                    .append("ID: ")
                    .append(contact.getId())
                    .append(". Email: ")
                    .append(contact.getEmail())
                    .append(". Retries: ")
                    .append(contact.getRetryCount())
                    .append(". Name: ")
                    .append(contact.getName());

            Exception customException = new ContactException(stringBuilder.toString());
            Optional<InternalLog> savedLog = internalLogService.getInternalLog(customException);
            log.error("Saved Log: {}", savedLog);
        }
    }

    private void markMessageAsSent(Contact contact) {
        log.info("Called markMessageAsSent() from ContactService");
        contact.setSent(true);
        contactRepository.save(contact);
        log.trace("Marked successfully");
    }

    private void sendMessage(Contact contact) {
        log.info("Called isMessageSent() from ContactService");

        /*
            Safe guards. Esses cenários nunca devem ocorrer devido ao filtro utilizado
            nas queries de SQL.
        */

        if (contact == null) {
            log.warn("Null contact object");
            return;
        }
        if (contact.isSent()) {
            log.warn("Contact is already sent");
            return;
        }
        if (contact.getRetryCount() > EMAIL_MAX_ATTEMPTS) {
            log.warn("Retry count exceeded");
            return;
        }

        StringBuilder messageBody = new StringBuilder();
        messageBody.append("Felipe, você recebeu uma nova mensagem. Segue abaixo as informações:\n\n")
                .append("Nome da pessoa que enviou: ").append(contact.getName())
                .append("\nEndereço de E-mail: ").append(contact.getEmail())
                .append("\nQuando a mensagem foi cadastrada no sistema: ").append(contact.getCreatedAt())
                .append("\nMensagem: ").append(contact.getMessage());
        SimpleMailMessage message = new SimpleMailMessage(templateMessage);
        message.setText(messageBody.toString());
        try {
            mailSender.send(message);
            log.debug("Sent successfully");
            markMessageAsSent(contact);
        } catch (MailException mailException) {
            log.error("Error sending email notification!", mailException);
            incrementRetryCount(contact);
        }
    }
}
