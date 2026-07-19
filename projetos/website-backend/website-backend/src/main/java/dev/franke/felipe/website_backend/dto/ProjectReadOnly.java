package dev.franke.felipe.website_backend.dto;

import java.util.List;
import java.util.UUID;

public record ProjectReadOnly(
        UUID id,
        String name,
        String description,
        List<String> stack
) {}
