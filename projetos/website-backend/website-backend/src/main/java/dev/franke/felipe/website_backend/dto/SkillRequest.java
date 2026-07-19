package dev.franke.felipe.website_backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record SkillRequest(
        @NotBlank(message = "Field is required")
        @Size(max = 50, message = "Cannot exceed 50 characters")
        String name,
        @NotBlank(message = "Field is required")
        @Size(max = 50, message = "Cannot exceed 50 characters")
        String category,
        @Positive(message = "Should be positive")
        @Max(value = 5, message = "Range allowed: 1 to 5")
        int level
) {}
