package dev.franke.felipe.website_backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ContactRequest(
        @NotBlank(message = "Field is required")
        @Size(max = 100, message = "Cannot exceed 100 characters")
        String name,
        @NotBlank(message = "Field is required")
        @Email(message = "Must be a valid Email")
        @Size(max = 254, message = "Cannot exceed 254 characters")
        String email,
        @NotBlank(message = "Field is required")
        @Size(max = 3000, message = "Cannot exceed 3000 characters")
        String message
) {}
