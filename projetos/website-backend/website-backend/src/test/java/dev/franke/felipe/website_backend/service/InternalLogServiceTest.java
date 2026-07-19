package dev.franke.felipe.website_backend.service;

import dev.franke.felipe.website_backend.exception.InternalLogException;
import dev.franke.felipe.website_backend.exception.InternalLogNotFoundException;
import dev.franke.felipe.website_backend.model.InternalLog;
import dev.franke.felipe.website_backend.repository.InternalLogRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InternalLogServiceTest {

    @Mock
    private InternalLogRepository internalLogRepository;

    private InternalLogService service;

    private void initService() {
        service = new InternalLogService(internalLogRepository);
    }

    @Test
    @DisplayName("Given a valid UUID that exists, getLogById must return the InternalLog found by the repository")
    void getLogByIdReturnsLogWhenFound() {
        initService();
        UUID id = UUID.randomUUID();
        InternalLog stored = new InternalLog();
        stored.setId(id);
        when(internalLogRepository.findById(id)).thenReturn(Optional.of(stored));

        InternalLog result = service.getLogById(id.toString());

        assertSame(stored, result);
    }

    @Test
    @DisplayName("Given a syntactically invalid UUID, getLogById must throw InternalLogException and never call the repository")
    void getLogByIdThrowsOnInvalidUuid() {
        initService();
        String invalidId = "not-a-uuid";

        InternalLogException exception = assertThrows(
                InternalLogException.class,
                () -> service.getLogById(invalidId)
        );

        assertEquals(invalidId + " is not a valid UUID.", exception.getMessage());
        verifyNoInteractions(internalLogRepository);
    }

    @Test
    @DisplayName("Given a valid UUID that does not exist, getLogById must throw InternalLogNotFoundException")
    void getLogByIdThrowsWhenNotFound() {
        initService();
        UUID id = UUID.randomUUID();
        when(internalLogRepository.findById(id)).thenReturn(Optional.empty());

        InternalLogNotFoundException exception = assertThrows(
                InternalLogNotFoundException.class,
                () -> service.getLogById(id.toString())
        );

        assertEquals("InternalLog not found with ID: " + id, exception.getMessage());
    }

    @Test
    @DisplayName("getFirstLogs must delegate directly to internalLogRepository.findFirst100Results")
    void getFirstLogsDelegatesToRepository() {
        initService();
        InternalLog first = new InternalLog();
        InternalLog second = new InternalLog();
        when(internalLogRepository.findFirst100Results()).thenReturn(List.of(first, second));

        List<InternalLog> result = service.getFirstLogs();

        assertEquals(List.of(first, second), result);
        verify(internalLogRepository, times(1)).findFirst100Results();
    }

    @Test
    @DisplayName("pruneOldLogs must delegate to internalLogRepository.deleteOldLogs exactly once")
    void pruneOldLogsDelegatesToRepository() {
        initService();

        service.pruneOldLogs();

        verify(internalLogRepository, times(1)).deleteOldLogs();
        verifyNoMoreInteractions(internalLogRepository);
    }

    @Test
    @DisplayName("Given a null exception, getInternalLog must return an empty Optional and never touch the repository")
    void getInternalLogReturnsEmptyOnNullException() {
        initService();

        Optional<InternalLog> result = service.getInternalLog(null);

        assertTrue(result.isEmpty());
        verifyNoInteractions(internalLogRepository);
    }

    @Test
    @DisplayName("Given an exception with a null message, getInternalLog must not throw and must persist a fallback error message instead")
    void getInternalLogHandlesNullExceptionMessage() {
        initService();
        RuntimeException toLog = new RuntimeException((String) null);
        when(internalLogRepository.save(any(InternalLog.class))).thenAnswer(invocation -> invocation.getArgument(0));
        ArgumentCaptor<InternalLog> captor = ArgumentCaptor.forClass(InternalLog.class);

        Optional<InternalLog> result = assertDoesNotThrow(() -> service.getInternalLog(toLog));

        assertTrue(result.isPresent());
        verify(internalLogRepository, times(1)).save(captor.capture());
        assertEquals("No message provided", captor.getValue().getErrorMessage());
    }

    @Test
    @DisplayName("Given a short error message, getInternalLog must persist an InternalLog with the exception's simple class name and full message")
    void getInternalLogPersistsLogWithExpectedFields() {
        initService();
        RuntimeException toLog = new RuntimeException("simple failure");
        when(internalLogRepository.save(any(InternalLog.class))).thenAnswer(invocation -> invocation.getArgument(0));
        ArgumentCaptor<InternalLog> captor = ArgumentCaptor.forClass(InternalLog.class);

        Optional<InternalLog> result = service.getInternalLog(toLog);

        assertTrue(result.isPresent());
        verify(internalLogRepository, times(1)).save(captor.capture());
        InternalLog saved = captor.getValue();
        assertEquals("RuntimeException", saved.getSimpleClassName());
        assertEquals("simple failure", saved.getErrorMessage());
        assertNotNull(saved.getStackTrace());
        assertSame(saved, result.get());
    }

    @Test
    @DisplayName("Given an error message longer than 300 characters, getInternalLog must truncate it to 300 chars plus an ellipsis")
    void getInternalLogTruncatesLongErrorMessage() {
        initService();
        String longMessage = "a".repeat(400);
        RuntimeException toLog = new RuntimeException(longMessage);
        when(internalLogRepository.save(any(InternalLog.class))).thenAnswer(invocation -> invocation.getArgument(0));
        ArgumentCaptor<InternalLog> captor = ArgumentCaptor.forClass(InternalLog.class);

        service.getInternalLog(toLog);

        verify(internalLogRepository).save(captor.capture());
        String savedMessage = captor.getValue().getErrorMessage();
        assertEquals(303, savedMessage.length());
        assertTrue(savedMessage.endsWith("..."));
        assertEquals("a".repeat(300) + "...", savedMessage);
    }

    @Test
    @DisplayName("Given a stack trace longer than 1000 characters, getInternalLog must truncate it to 1000 chars plus an ellipsis")
    void getInternalLogTruncatesLongStackTrace() {
        initService();
        RuntimeException toLog = new RuntimeException("failure with a big stack trace");
        StackTraceElement[] bigStackTrace = new StackTraceElement[100];
        for (int i = 0; i < bigStackTrace.length; i++) {
            bigStackTrace[i] = new StackTraceElement(
                    "SomeClassWithALongName", "someMethodWithALongName", "SomeClassWithALongName.java", i
            );
        }
        toLog.setStackTrace(bigStackTrace);
        when(internalLogRepository.save(any(InternalLog.class))).thenAnswer(invocation -> invocation.getArgument(0));
        ArgumentCaptor<InternalLog> captor = ArgumentCaptor.forClass(InternalLog.class);

        service.getInternalLog(toLog);

        verify(internalLogRepository).save(captor.capture());
        String savedStackTrace = captor.getValue().getStackTrace();
        assertEquals(1003, savedStackTrace.length());
        assertTrue(savedStackTrace.endsWith("..."));
    }

    @Test
    @DisplayName("If internalLogRepository.save throws, getInternalLog must catch it and return an empty Optional")
    void getInternalLogReturnsEmptyWhenSaveFails() {
        initService();
        RuntimeException toLog = new RuntimeException("failure");
        doThrow(new RuntimeException("simulated database failure"))
                .when(internalLogRepository).save(any(InternalLog.class));

        Optional<InternalLog> result = service.getInternalLog(toLog);

        assertTrue(result.isEmpty());
    }
}
