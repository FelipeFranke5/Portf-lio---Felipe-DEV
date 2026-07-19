package dev.franke.felipe.website_backend.repository;

import dev.franke.felipe.website_backend.model.InternalLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InternalLogRepository extends JpaRepository<InternalLog, UUID> {
    @Query(
            value = "SELECT * FROM internal_log ORDER BY created_at DESC LIMIT 100",
            nativeQuery = true
    )
    List<InternalLog> findFirst100Results();

    @Modifying(clearAutomatically = true)
    @Query(
            value = "DELETE FROM internal_log WHERE created_at < NOW() - INTERVAL '90 days'",
            nativeQuery = true
    )
    void deleteOldLogs();
}
