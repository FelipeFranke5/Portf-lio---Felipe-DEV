package dev.franke.felipe.website_backend.dto;

import java.util.UUID;

public record SkillDTO(UUID id, String name, String category, SkillLevel skillLevel) {}
