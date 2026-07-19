package dev.franke.felipe.website_backend.repository;

import dev.franke.felipe.website_backend.model.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ContactRepository extends JpaRepository<Contact, UUID> {
    @Query(
            value = "SELECT * FROM contact_messages WHERE retry_count <= 3 AND sent = FALSE ORDER BY created_at DESC",
            nativeQuery = true
    )
    List<Contact> findPendingMessages();

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
            value = "DELETE FROM contact_messages WHERE sent = TRUE",
            nativeQuery = true
    )
    void deleteAlreadySentMessages();

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
            value = "DELETE FROM contact_messages WHERE created_at < NOW() - INTERVAL '14 days'",
            nativeQuery = true
    )
    void deleteOldMessages();
}
