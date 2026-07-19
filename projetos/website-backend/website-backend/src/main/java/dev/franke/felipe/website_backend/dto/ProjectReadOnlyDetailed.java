package dev.franke.felipe.website_backend.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ProjectReadOnlyDetailed(
        UUID id,
        String name,
        String description,
        List<String> stack,
        String githubURL,
        String demoURL,
        boolean featured,
        LocalDateTime createdAt
) {}
