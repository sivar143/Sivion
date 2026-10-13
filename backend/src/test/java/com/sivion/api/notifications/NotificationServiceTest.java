package com.sivion.api.notifications;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {
    @Mock
    private NotificationRepository repository;

    @InjectMocks
    private NotificationService service;

    @Test
    void inboxIsScopedToTenantAndAuthenticatedRecipient() {
        when(repository.findTop100ByTenantIdAndRecipientUsernameOrderByCreatedAtDesc(7L, "siva"))
            .thenReturn(List.of());

        assertEquals(List.of(), service.inbox(7L, "siva"));

        verify(repository).findTop100ByTenantIdAndRecipientUsernameOrderByCreatedAtDesc(7L, "siva");
    }

    @Test
    void markReadRejectsNotificationsOwnedByAnotherUserOrTenant() {
        when(repository.findByIdAndTenantIdAndRecipientUsername(12L, 7L, "siva"))
            .thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> service.markRead(7L, "siva", 12L));
        verify(repository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void sendTrimsAndPersistsValidNotification() {
        when(repository.save(org.mockito.ArgumentMatchers.any(Notification.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        Notification result = service.send(7L, " siva ", "  Welcome  ", "  Your account is ready.  ");

        assertEquals("siva", result.getRecipientUsername());
        assertEquals("Welcome", result.getTitle());
        assertEquals("Your account is ready.", result.getMessage());
        assertNotNull(result.getCreatedAt());
    }
}
