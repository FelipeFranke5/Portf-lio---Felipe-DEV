package dev.franke.felipe.website_backend.service;

import dev.franke.felipe.website_backend.dto.ContactRequest;
import dev.franke.felipe.website_backend.dto.ContactResponse;
import dev.franke.felipe.website_backend.model.Contact;
import dev.franke.felipe.website_backend.repository.ContactRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContactServiceTest {

    @Mock
    private ContactRepository contactRepository;

    @Mock
    private MailSender mailSender;

    @Mock
    private InternalLogService internalLogService;

    private final SimpleMailMessage templateMessage = buildTemplateMessage();

    private ContactService service;

    private SimpleMailMessage buildTemplateMessage() {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo("felipe@example.com");
        message.setSubject("Felipe, you have received a new message from your Site!");
        return message;
    }

    private void initService() {
        service = new ContactService(contactRepository, mailSender, templateMessage, internalLogService);
    }

    private Contact buildContact(String name, String email, String message, boolean sent, int retryCount) {
        Contact contact = new Contact();
        contact.setName(name);
        contact.setEmail(email);
        contact.setMessage(message);
        contact.setSent(sent);
        contact.setRetryCount((short) retryCount);
        return contact;
    }

    @Test
    @DisplayName("Given a valid ContactRequest, saveContact must persist a NEW Contact with the request's data")
    void saveContactPersistsNewContactWithRequestData() {
        initService();
        ContactRequest request = new ContactRequest("John", "john@email.com", "Hello, I would like a quote");
        UUID generatedId = UUID.randomUUID();
        LocalDateTime generatedCreatedAt = LocalDateTime.now();
        when(contactRepository.save(any(Contact.class))).thenAnswer(invocation -> {
            Contact toSave = invocation.getArgument(0);
            toSave.setId(generatedId);
            toSave.setCreatedAt(generatedCreatedAt);
            return toSave;
        });
        ArgumentCaptor<Contact> captor = ArgumentCaptor.forClass(Contact.class);

        ContactResponse response = service.saveContact(request);

        verify(contactRepository, times(1)).save(captor.capture());
        Contact saved = captor.getValue();
        assertEquals(request.name(), saved.getName());
        assertEquals(request.email(), saved.getEmail());
        assertEquals(request.message(), saved.getMessage());
        assertFalse(saved.isSent(), "A brand new Contact must start as not sent");
        assertEquals(0, saved.getRetryCount(), "A brand new Contact must start with retryCount 0");
        assertEquals(new ContactResponse(generatedId, generatedCreatedAt), response);
    }

    @Test
    @DisplayName("saveContact must not swallow exceptions raised by the repository - it must propagate them")
    void saveContactPropagatesRepositoryExceptions() {
        initService();
        ContactRequest request = new ContactRequest("John", "john@email.com", "Message");
        doThrow(new RuntimeException("simulated database constraint violation"))
                .when(contactRepository).save(any(Contact.class));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> service.saveContact(request));

        assertEquals("simulated database constraint violation", exception.getMessage());
    }

    @Test
    @DisplayName("pruneOldMessages must delegate to contactRepository.deleteOldMessages exactly once")
    void pruneOldMessagesDelegatesToRepository() {
        initService();
        service.pruneOldMessages();
        verify(contactRepository, times(1)).deleteOldMessages();
        verifyNoMoreInteractions(contactRepository, mailSender);
    }

    @Test
    @DisplayName("pruneSentMessages must delegate to contactRepository.deleteAlreadySentMessages exactly once")
    void pruneSentMessagesDelegatesToRepository() {
        initService();
        service.pruneSentMessages();
        verify(contactRepository, times(1)).deleteAlreadySentMessages();
        verifyNoMoreInteractions(contactRepository, mailSender);
    }

    @Test
    @DisplayName("Given no pending messages, sendPendingMessages must not touch the mail sender or save anything")
    void sendPendingMessagesWithEmptyListDoesNothing() {
        initService();
        when(contactRepository.findPendingMessages()).thenReturn(List.of());

        service.sendPendingMessages();

        verifyNoInteractions(mailSender);
        verify(contactRepository, never()).save(any());
    }

    @Test
    @DisplayName("Given one pending message and a successful send, sendPendingMessages must mark it as sent")
    void sendPendingMessagesMarksAsSentOnSuccess() {
        initService();
        Contact pending = buildContact("John", "john@email.com", "Test message", false, 0);
        when(contactRepository.findPendingMessages()).thenReturn(List.of(pending));

        service.sendPendingMessages();

        ArgumentCaptor<SimpleMailMessage> mailCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(1)).send(mailCaptor.capture());
        String body = mailCaptor.getValue().getText();
        assertNotNull(body);
        assertTrue(body.contains("John"));
        assertTrue(body.contains("john@email.com"));
        assertTrue(body.contains("Test message"));

        ArgumentCaptor<Contact> savedCaptor = ArgumentCaptor.forClass(Contact.class);
        verify(contactRepository, times(1)).save(savedCaptor.capture());
        assertTrue(savedCaptor.getValue().isSent(), "Contact must be marked as sent after a successful send");
        assertSame(pending, savedCaptor.getValue());
    }

    @Test
    @DisplayName("Given one pending message and a failed send, sendPendingMessages must increment its retry count instead of marking it as sent")
    void sendPendingMessagesIncrementsRetryCountOnFailure() {
        initService();
        Contact pending = buildContact("John", "john@email.com", "Test message", false, 1);
        when(contactRepository.findPendingMessages()).thenReturn(List.of(pending));
        doThrow(new MailSendException("simulated SMTP failure")).when(mailSender).send(any(SimpleMailMessage.class));

        service.sendPendingMessages();

        ArgumentCaptor<Contact> savedCaptor = ArgumentCaptor.forClass(Contact.class);
        verify(contactRepository, times(1)).save(savedCaptor.capture());
        assertFalse(savedCaptor.getValue().isSent(), "Contact must NOT be marked as sent when the send fails");
        assertEquals(2, savedCaptor.getValue().getRetryCount(), "retryCount must be incremented by exactly 1");
    }

    @Test
    @DisplayName("Given several pending messages, sendPendingMessages must attempt to send every one of them")
    void sendPendingMessagesProcessesEveryPendingMessage() {
        initService();
        Contact first = buildContact("John", "john@email.com", "First message", false, 0);
        Contact second = buildContact("Jane", "jane@email.com", "Second message", false, 0);
        when(contactRepository.findPendingMessages()).thenReturn(List.of(first, second));

        service.sendPendingMessages();

        verify(mailSender, times(2)).send(any(SimpleMailMessage.class));
        verify(contactRepository, times(2)).save(any(Contact.class));
    }

    @Test
    @DisplayName(
            "Safe guard: if a Contact already marked as sent somehow reaches sendPendingMessages, " +
            "it must be skipped - no mail sent, no save performed"
    )
    void sendPendingMessagesSkipsAlreadySentContact() {
        initService();
        Contact alreadySent = buildContact("John", "john@email.com", "Message", true, 0);
        when(contactRepository.findPendingMessages()).thenReturn(List.of(alreadySent));

        service.sendPendingMessages();

        verifyNoInteractions(mailSender);
        verify(contactRepository, never()).save(any());
    }

    @Test
    @DisplayName(
            "Safe guard: if a Contact with retryCount above 3 somehow reaches sendPendingMessages, " +
            "it must be skipped - no mail sent, no save performed"
    )
    void sendPendingMessagesSkipsContactWithExceededRetryCount() {
        initService();
        Contact exceeded = buildContact("John", "john@email.com", "Message", false, 4);
        when(contactRepository.findPendingMessages()).thenReturn(List.of(exceeded));

        service.sendPendingMessages();

        verifyNoInteractions(mailSender);
        verify(contactRepository, never()).save(any());
    }
}
