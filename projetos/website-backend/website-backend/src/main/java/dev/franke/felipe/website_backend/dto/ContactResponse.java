package dev.franke.felipe.website_backend.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ContactResponse(UUID messageId, LocalDateTime createdAt) {
}
